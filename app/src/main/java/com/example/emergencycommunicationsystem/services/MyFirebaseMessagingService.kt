package com.example.emergencycommunicationsystem.services

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.util.AlertUrgency
import com.example.emergencycommunicationsystem.util.NotificationChannels
import com.example.emergencycommunicationsystem.util.resolveAlertUrgency
import com.example.emergencycommunicationsystem.util.shouldVibrateForUrgency
import com.example.emergencycommunicationsystem.util.vibrateForUrgency
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")

        var urgency = AlertUrgency.LOW
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            urgency = handleDataMessage(remoteMessage.data)
        } else {
            remoteMessage.notification?.let {
                Log.d(TAG, "Message Notification Body: ${it.body}")
                showNotification(it.title ?: "Emergency Alert", it.body ?: "")
                urgency = resolveAlertUrgency(
                    severity = null,
                    category = null,
                    title = it.title,
                    content = it.body
                )
            }
        }

        if (shouldVibrateForUrgency(urgency)) {
            vibrateForUrgency(this, urgency)
        }
    }

    private fun handleDataMessage(data: Map<String, String>): AlertUrgency {
        val title = data["title"] ?: "Emergency Alert"
        val body = data["body"] ?: data["message"] ?: ""
        val category = data["category"] ?: "general"
        val severity = data["severity"] ?: data["urgency"]
        
        showNotification(title, body, category)
        return resolveAlertUrgency(
            severity = severity,
            category = category,
            title = title,
            content = body
        )
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
        val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = com.example.emergencycommunicationsystem.data.network.ApiClient.settingsApiService()
                val request = com.example.emergencycommunicationsystem.network.FcmTokenRequest(userId, deviceId, token)
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
