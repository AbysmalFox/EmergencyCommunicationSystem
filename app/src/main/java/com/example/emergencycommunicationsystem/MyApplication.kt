package com.example.emergencycommunicationsystem

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // --- OSMDROID CONFIGURATION ---
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        // --- END OSMDROID CONFIGURATION ---

        // The ApiClient is now initialized in MainActivity to prevent race conditions.
        AuthManager.initialize(this)
    }
}
