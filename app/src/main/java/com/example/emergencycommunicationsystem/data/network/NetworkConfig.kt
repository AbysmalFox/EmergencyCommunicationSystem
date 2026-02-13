package com.example.emergencycommunicationsystem.data.network

import android.os.Build
import com.example.emergencycommunicationsystem.BuildConfig

object NetworkConfig {
    /**
     * PRODUCTION BASE URL
     * ENSURE HTTPS is used strictly.
     */
    const val PRODUCTION_HOST = "https://emergency-comm.alertaraqc.com"
    const val PRODUCTION_API_URL = "$PRODUCTION_HOST/PHP/api/"

    /**
     * LOCAL DEVELOPMENT SETTINGS
     */
    private const val EMULATOR_HOST = "10.0.2.2"
    
    private val DEVICE_HOST: String 
        get() = BuildConfig.LOCAL_DEVICE_HOST

    val LOCAL_API_URL: String
        get() {
            val host = if (isEmulator()) EMULATOR_HOST else DEVICE_HOST
            return "http://$host/PHP/api/"
        }

    /**
     * URL DISCOVERY LOGIC
     */
    fun getBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            LOCAL_API_URL
        } else {
            PRODUCTION_API_URL
        }
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
