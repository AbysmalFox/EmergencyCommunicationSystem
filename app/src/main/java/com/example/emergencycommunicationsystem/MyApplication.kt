package com.example.emergencycommunicationsystem

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration
import org.webrtc.PeerConnectionFactory

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

        // --- WEBRTC INITIALIZATION ---
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this)
                .createInitializationOptions()
        )
        // --- END WEBRTC INITIALIZATION ---

        // The ApiClient is now initialized in MainActivity to prevent race conditions.
        AuthManager.initialize(this)
    }
}
