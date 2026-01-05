package com.example.emergencycommunicationsystem.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * A helper object to retrieve device-specific information.
 */
object DeviceManager {

    /*** Retrieves a unique and stable identifier for the Android device.
     * This is the best available ID for tracking a specific device installation.
     *
     * @param context The application context.
     * @return A unique string representing the device ID.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * Retrieves the user-friendly name of the device.
     * e.g., "Pixel 8 Pro"
     *
     * @return A string containing the manufacturer and model of the device.
     */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    /**
     * For now, this returns a placeholder push token.
     * TODO: Replace this with the actual token retrieval logic after implementing Firebase Cloud Messaging (FCM).
     *
     * @return A placeholder FCM token.
     */
    fun getPushToken(): String {
        // In a real app, you would get this from the Firebase SDK:
        // FirebaseMessaging.getInstance().token.addOnCompleteListener { task -> ... }
        return "placeholder_fcm_token_for_device_${(1000..9999).random()}"
    }
}