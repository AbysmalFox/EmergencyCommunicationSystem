package com.example.emergencycommunicationsystem

import android.app.Application
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import org.osmdroid.config.Configuration
import org.webrtc.PeerConnectionFactory

import com.example.emergencycommunicationsystem.util.NotificationChannels

class MyApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Notification Channels
        NotificationChannels.createNotificationChannels(this)

        // --- OSMDROID CONFIGURATION ---
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        
        // Disable OSMDroid verbose logging
        Configuration.getInstance().isDebugMode = false
        Configuration.getInstance().isDebugMapView = false
        Configuration.getInstance().isDebugTileProviders = false
        
        // --- WEBRTC INITIALIZATION ---
        try {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                    .createInitializationOptions()
            )
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize WebRTC", e)
        }
        // --- END WEBRTC INITIALIZATION ---

        AuthManager.initialize(this)
    }
}
