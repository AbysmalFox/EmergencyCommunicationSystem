package com.example.emergencycommunicationsystem.data.network

import android.os.Build
import com.example.emergencycommunicationsystem.BuildConfig

object NetworkConfig {
    // 1. Production (Your Hostinger Domain)
    const val PRODUCTION_HOST = "https://emergency-comm.alertaraqc.com"

    // 2. Local Development
    private const val EMULATOR_HOST = "10.0.2.2" // Gateway to your PC's localhost
    private val DEVICE_HOST: String
        get() = BuildConfig.LOCAL_DEVICE_HOST

    // Publicly accessible URLs for both environments
    val PRODUCTION_API_URL: String = "$PRODUCTION_HOST/PHP/api/"
    val LOCAL_API_URL: String by lazy {
        val host = if (isEmulator()) EMULATOR_HOST else DEVICE_HOST
        "http://$host/PHP/api/"
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}
