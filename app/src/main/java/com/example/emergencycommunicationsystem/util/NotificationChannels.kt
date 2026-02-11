package com.example.emergencycommunicationsystem.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {

    const val CHANNEL_ID_FIRE = "alert_fire"
    const val CHANNEL_ID_EARTHQUAKE = "alert_earthquake"
    const val CHANNEL_ID_WEATHER = "alert_weather"
    const val CHANNEL_ID_GENERAL = "alert_general"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Define custom sounds (assuming raw resources exist, otherwise using system defaults)
            // Ideally, you would have files like res/raw/siren.mp3, res/raw/rumble.mp3, etc.
            // For this implementation, we'll try to point to them, but fallback gracefully if not found or use valid URIs.
            // Since we don't have the raw files yet, we will use Settings.System.DEFAULT_NOTIFICATION_URI for now
            // or specific system sounds to simulate distinct profiles if possible.
            // To properly implement "Distinct Audio Profiles", the user would need to add these sound files.
            // Here we setup the structure.

            // Fire Alert Channel - High Importance, Siren sound
            val fireChannel = NotificationChannel(
                CHANNEL_ID_FIRE,
                "Fire Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency alerts for fire incidents"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                // setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.siren}"), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build())
            }

            // Earthquake Alert Channel - High Importance, Rumble sound
            val earthquakeChannel = NotificationChannel(
                CHANNEL_ID_EARTHQUAKE,
                "Earthquake Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency alerts for earthquakes"
                enableLights(true)
                lightColor = android.graphics.Color.YELLOW
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                // setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.rumble}"), ...)
            }

            // Weather Alert Channel - High Importance, Ding/Rain sound
            val weatherChannel = NotificationChannel(
                CHANNEL_ID_WEATHER,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Severe weather warnings"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }

            // General Alert Channel - Default Importance
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications and updates"
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
            }

            notificationManager.createNotificationChannels(
                listOf(fireChannel, earthquakeChannel, weatherChannel, generalChannel)
            )
        }
    }
    
    fun getChannelIdForCategory(category: String?): String {
        return when (category?.lowercase()) {
            "fire" -> CHANNEL_ID_FIRE
            "earthquake", "tremor" -> CHANNEL_ID_EARTHQUAKE
            "weather", "storm", "flood", "typhoon", "rain" -> CHANNEL_ID_WEATHER
            else -> CHANNEL_ID_GENERAL
        }
    }
}
