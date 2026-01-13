package com.example.emergencycommunicationsystem.config

import com.example.emergencycommunicationsystem.BuildConfig

/**
 * Google OAuth Configuration
 * 
 * Reads Google Sign-In credentials from BuildConfig, which are injected from:
 * 1. local.properties (for local development) - recommended
 * 2. Default fallback values (if not set in local.properties)
 * 
 * To set in local.properties (root of project), add:
 * GOOGLE_CLIENT_ID=1054819730704-dp3pmtvb6kmv3qs0nb17o7bun0qo3n6a.apps.googleusercontent.com
 * GOOGLE_CLIENT_SECRET=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
 * 
 * Note: local.properties is already in .gitignore and won't be committed
 * The credentials are compiled into BuildConfig at build time
 */
object GoogleAuthConfig {
    /**
     * Get Google Client ID from BuildConfig
     * This value is set in build.gradle.kts from local.properties
     */
    val CLIENT_ID: String
        get() = BuildConfig.GOOGLE_CLIENT_ID
    
    /**
     * Get Google Client Secret from BuildConfig
     * This value is set in build.gradle.kts from local.properties
     */
    val CLIENT_SECRET: String
        get() = BuildConfig.GOOGLE_CLIENT_SECRET
}
