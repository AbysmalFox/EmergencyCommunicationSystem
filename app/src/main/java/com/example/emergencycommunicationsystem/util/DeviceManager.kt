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
     * Retrieves a unique identifier for the current session or device for real-time communication.
     *
     * @return A unique identifier string.
     */
    fun getPushToken(): String {
        return "socket_comm_id_${(1000..9999).random()}_${System.currentTimeMillis()}"
    }
}