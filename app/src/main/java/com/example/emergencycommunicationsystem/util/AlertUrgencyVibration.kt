package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.Locale

enum class AlertUrgency {
    HIGH,
    MEDIUM,
    LOW
}

fun resolveAlertUrgency(
    severity: String?,
    category: String?,
    title: String?,
    content: String?
): AlertUrgency {
    val sev = severity?.trim()?.lowercase(Locale.getDefault()).orEmpty()
    if (sev.contains("high") || sev.contains("critical")) return AlertUrgency.HIGH
    if (sev.contains("medium")) return AlertUrgency.MEDIUM
    if (sev.contains("low")) return AlertUrgency.LOW

    val text = listOf(category, title, content)
        .joinToString(" ")
        .lowercase(Locale.getDefault())

    if (highKeywords.any { it in text }) return AlertUrgency.HIGH
    if (
        "weather" in text ||
        "storm" in text ||
        "rain" in text ||
        "flood" in text
    ) return AlertUrgency.MEDIUM

    return AlertUrgency.LOW
}

fun shouldVibrateForUrgency(urgency: AlertUrgency): Boolean {
    return urgency == AlertUrgency.HIGH || urgency == AlertUrgency.MEDIUM
}

fun vibrateForUrgency(context: Context, urgency: AlertUrgency) {
    if (!shouldVibrateForUrgency(urgency)) return

    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator?.hasVibrator() != true) return

    val pattern = when (urgency) {
        AlertUrgency.HIGH -> longArrayOf(0, 500, 150, 500, 150, 500)
        AlertUrgency.MEDIUM -> longArrayOf(0, 300, 180, 300)
        AlertUrgency.LOW -> longArrayOf(0, 150)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}
