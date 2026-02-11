package com.example.emergencycommunicationsystem.services

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.util.NotificationChannels
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title ?: "Emergency Alert", it.body ?: "")
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Emergency Alert"
        val body = data["body"] ?: data["message"] ?: ""
        val category = data["category"] ?: "general"
        
        showNotification(title, body, category)
    }

    private fun showNotification(title: String, body: String, category: String = "general") {
        val channelId = NotificationChannels.getChannelIdForCategory(category)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_tabler_bell_ringing)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to backend if user is logged in
        val userId = com.example.emergencycommunicationsystem.AuthManager.getUserId()
        if (userId != -1) {
            sendTokenToServer(userId, token)
        }
    }

    private fun sendTokenToServer(userId: Int, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = com.example.emergencycommunicationsystem.data.network.ApiClient.settingsApiService()
                val request = com.example.emergencycommunicationsystem.network.FcmTokenRequest(userId, token)
                val response = apiService.updateFcmToken(request)
                if (response.isSuccessful) {
                    Log.d(TAG, "Token successfully sent to server")
                } else {
                    Log.e(TAG, "Failed to send token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending token to server", e)
            }
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}
