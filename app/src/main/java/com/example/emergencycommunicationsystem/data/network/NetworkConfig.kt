package com.example.emergencycommunicationsystem.data.network

import com.example.emergencycommunicationsystem.BuildConfig

object NetworkConfig {
    // 1. Production (Your Hostinger Domain)
    const val PRODUCTION_HOST = "https://www.your-live-domain.com" // <-- REPLACE WITH YOUR REAL DOMAIN

    // 2. Local Development
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val DEVICE_HOST = "192.168.1.6"  // <-- REPLACE IF YOUR PC's IP CHANGES

    val PRODUCTION_API_URL: String = "$PRODUCTION_HOST/PHP/api/"

    val LOCAL_API_URL: String by lazy {
        val host = if (isEmulator()) EMULATOR_HOST else DEVICE_HOST
        "http://$host/PHP/api/"
    }

    private fun isEmulator(): Boolean {
        // This is a robust check for the emulator environment
        return (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk", ignoreCase = true)
                || android.os.Build.MODEL.contains("Emulator", ignoreCase = true)
                || android.os.Build.MODEL.contains("Android SDK built for x86", ignoreCase = true)
                || android.os.Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk" == android.os.Build.PRODUCT)
    }
}