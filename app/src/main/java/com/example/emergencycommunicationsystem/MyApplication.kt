package com.example.emergencycommunicationsystem

import android.app.Application
import android.content.Context
import com.example.emergencycommunicationsystem.data.network.ApiClient
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

        ApiClient.initializeAndCheckConnection()
        AuthManager.initialize(this)
    }
}
