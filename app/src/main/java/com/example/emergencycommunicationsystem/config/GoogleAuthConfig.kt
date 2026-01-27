package com.example.emergencycommunicationsystem.config

import com.example.emergencycommunicationsystem.BuildConfig

/**
 * Google OAuth Configuration
 * 
 * Reads Google Sign-In credentials from BuildConfig, which are injected from:
 * 1. local.properties (for local development) - recommended
 * 2. Default fallback values (if not set in local.properties)
 */
object GoogleAuthConfig {
    /**
     * Web Client ID (Used for requestIdToken and Backend verification)
     */
    val WEB_CLIENT_ID: String
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID
    
    /**
     * Web Client Secret
     */
    val WEB_CLIENT_SECRET: String
        get() = BuildConfig.GOOGLE_WEB_CLIENT_SECRET

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