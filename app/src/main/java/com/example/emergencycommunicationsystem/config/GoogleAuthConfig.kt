package com.example.emergencycommunicationsystem.config

import com.example.emergencycommunicationsystem.BuildConfig

/**
 * Google OAuth Configuration
 * 
 * Reads Google Sign-In credentials from BuildConfig, which are injected from:
 * 1. local.properties (for local development) - recommended
 * 2. build.gradle.kts (for default fallback values)
 * 
 * SECURITY NOTE: WEB_CLIENT_SECRET was removed from the mobile app as it should
 * only be stored on the backend server to prevent APK decompilation leaks.
 */
object GoogleAuthConfig {
    /**
     * Web Client ID (Used for requestIdToken and Backend verification)
     */
    val WEB_CLIENT_ID: String
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID
    
    /**
     * Android Client ID
     */
    val ANDROID_CLIENT_ID: String
        get() = BuildConfig.GOOGLE_ANDROID_CLIENT_ID

    // Legacy support for existing code that uses CLIENT_ID
    // For requestIdToken, we MUST use the WEB_CLIENT_ID
    val CLIENT_ID: String
        get() = WEB_CLIENT_ID
}
