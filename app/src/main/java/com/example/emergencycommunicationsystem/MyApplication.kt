package com.example.emergencycommunicationsystem

import android.app.Application
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import org.osmdroid.config.Configuration

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

        // Suppress noisy system logs (always, not just in debug)
        suppressNoisySystemLogs()

        // --- OSMDROID CONFIGURATION ---
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        
        // Disable OSMDroid verbose logging to reduce system log spam
        Configuration.getInstance().isDebugMode = false
        Configuration.getInstance().isDebugMapView = false
        Configuration.getInstance().isDebugTileProviders = false
        
        // AGGRESSIVE tile caching optimization to minimize HWUI logs
        try {
            // Maximize cache size to minimize re-downloads (fewer downloads = fewer HWUI logs)
            Configuration.getInstance().tileFileSystemCacheMaxBytes = 500 * 1024 * 1024L // 500 MB cache (increased from 200 MB)
            Configuration.getInstance().tileFileSystemCacheTrimBytes = 400 * 1024 * 1024L // Trim at 400 MB (increased from 50 MB)
            
            // Minimize concurrent downloads to reduce tile loading frequency
            Configuration.getInstance().tileDownloadMaxQueueSize = 1 // Only 1 concurrent download (reduced from 2)
            Configuration.getInstance().tileDownloadThreads = 1 // Only 1 download thread (reduced from 2)
            
            // Set longer cache expiration to reuse cached tiles more aggressively
            // This prevents frequent tile re-downloads which trigger HWUI logs
            try {
                val cacheExpirationField = Configuration.getInstance().javaClass.getDeclaredField("tileFileSystemCacheExpirationTime")
                cacheExpirationField.isAccessible = true
                cacheExpirationField.setLong(Configuration.getInstance(), 30L * 24 * 60 * 60 * 1000) // 30 days expiration
            } catch (_: Exception) {
                // Field might not exist in this OSMDroid version
            }
        } catch (_: Exception) {
            // Some properties might not be available in all OSMDroid versions
        }
        // --- END OSMDROID CONFIGURATION ---

        // The ApiClient is now initialized in MainActivity to prevent race conditions.
        AuthManager.initialize(this)
    }
    
    /**
     * Attempts to suppress noisy system logs using all available methods.
     */
    private fun suppressNoisySystemLogs() {
        val tagsToSuppress = listOf(
            "HWUI", "OpenGLRenderer", "Gralloc", "libEGL", "Adreno",
            "RenderThread", "Choreographer", "ImageDecoder", "BitmapFactory",
            "Skia", "libpng", "libjpeg", "HeifDecoder", "ExifInterface",
            "InputMethodManager", "ViewRootImpl", "SurfaceControl"
        )
        
        // Method 1: System properties - Set to SUPPRESS/ASSERT level (highest, suppresses everything)
        try {
            tagsToSuppress.forEach { tag ->
                // Try multiple property formats
                System.setProperty("log.tag.$tag", "ASSERT")  // ASSERT is highest level
                System.setProperty("log.tag.$tag", "SILENT")  // Some devices support SILENT
                System.setProperty("android.util.Log.$tag", "ASSERT")
            }
        } catch (_: Exception) {
            // Ignore - properties might not be settable
        }
        
        // Method 2: Use reflection to set log level to ASSERT (completely suppress)
        try {
            val logClass = Log::class.java
            
            // Try setLoggable method (Android 8.0+)
            try {
                val setLoggableMethod = logClass.getMethod(
                    "setLoggable",
                    String::class.java,
                    Int::class.javaPrimitiveType
                )
                
                tagsToSuppress.forEach { tag ->
                    try {
                        // Use ASSERT level (7) - highest level, suppresses everything including warnings
                        // This should completely suppress WARNING level logs like "Image decoding logging dropped!"
                        setLoggableMethod.invoke(null, tag, Log.ASSERT)
                    } catch (_: Exception) {
                        // Try ERROR level as fallback
                        try {
                            setLoggableMethod.invoke(null, tag, Log.ERROR)
                        } catch (_: Exception) {
                            // Ignore - method might not work
                        }
                    }
                }
            } catch (_: NoSuchMethodException) {
                // setLoggable not available, try alternative methods
            }
            
            // Method 3: Try to access and modify internal log state via reflection
            try {
                // Some Android versions store log levels in a static map
                val logLevelsField = logClass.getDeclaredField("sLogLevels")
                logLevelsField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val logLevels = logLevelsField.get(null) as? MutableMap<String, Int>
                
                logLevels?.let { levels ->
                    tagsToSuppress.forEach { tag ->
                        levels[tag] = Log.ASSERT  // Set to highest level
                    }
                }
            } catch (_: Exception) {
                // Field might not exist or be accessible
            }
        } catch (_: Exception) {
            // Reflection might fail - that's okay, we have other methods
        }
        
        // Method 4: Set via environment/system properties (multiple formats)
        try {
            tagsToSuppress.forEach { tag ->
                System.setProperty("log.tag.$tag", "ASSERT")
                System.setProperty("log.tag.$tag", "SILENT")
                System.setProperty("android.util.Log.$tag", "ASSERT")
            }
        } catch (_: Exception) {
            // Ignore
        }
        
        // Method 5: Configure OSMDroid to reduce verbose logging
        try {
            Configuration.getInstance().isDebugMode = false
            Configuration.getInstance().isDebugMapView = false
            Configuration.getInstance().isDebugTileProviders = false
        } catch (_: Exception) {
            // Ignore
        }
    }
}
