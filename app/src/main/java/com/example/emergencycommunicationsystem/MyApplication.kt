package com.example.emergencycommunicationsystem

import android.app.Application
import android.content.Context
// import com.example.emergencycommunicationsystem.AuthManager // Assuming this is the correct path
import com.example.emergencycommunicationsystem.data.network.ApiClient
import org.osmdroid.config.Configuration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // --- OSMDROID CONFIGURATION ---
        // This is essential for OSMDroid to download map tiles.
        // It should be done once, before any map is displayed.
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        // --- END OSMDROID CONFIGURATION ---

        ApiClient.initializeAndCheckConnection()
        // Assuming AuthManager is a class you have defined in your project
        AuthManager.initialize(this)
    }
}