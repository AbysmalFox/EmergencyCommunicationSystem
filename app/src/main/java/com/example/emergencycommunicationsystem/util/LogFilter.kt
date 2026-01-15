package com.example.emergencycommunicationsystem.util

import android.util.Log

/**
 * Custom Log utility that filters out noisy system logs
 * Use this instead of android.util.Log for better logcat visibility
 */
object LogFilter {
    private const val TAG_PREFIX = "EmergencyComm"
    
    // Tags to filter out (noisy system logs)
    private val FILTERED_TAGS = setOf(
        "HWUI", // Hardware UI rendering warnings
        "OpenGLRenderer", // OpenGL rendering warnings
        "Gralloc", // Graphics allocator
        "libEGL", // EGL library
        "Adreno", // GPU driver
        "RenderThread", // Rendering thread
        "Choreographer", // Frame timing
        "InputMethodManager", // Keyboard input
        "ViewRootImpl", // View rendering
        "SurfaceControl", // Surface management
        "ImageDecoder", // Image decoding
        "BitmapFactory", // Bitmap loading
        "Skia", // Skia graphics library
        "libpng", // PNG decoder
        "libjpeg", // JPEG decoder
        "HeifDecoder", // HEIF decoder
        "ExifInterface", // EXIF data
        "MediaMetadataRetriever", // Media metadata
    )
    
    // Message patterns to filter out
    private val FILTERED_MESSAGES = setOf(
        "Image decoding logging dropped!",
        "decoding",
        "decode",
        "HWUI",
        "OpenGLRenderer",
        "Gralloc",
        "EGL",
        "Adreno",
        "ImageDecoder",
        "BitmapFactory",
        "Skia",
        "libpng",
        "libjpeg",
    )
    
    /**
     * Check if a log should be filtered out
     */
    private fun shouldFilter(tag: String, message: String?): Boolean {
        // Filter by tag
        if (FILTERED_TAGS.any { tag.contains(it, ignoreCase = true) }) {
            return true
        }
        
        // Filter by message content
        if (message != null && FILTERED_MESSAGES.any { message.contains(it, ignoreCase = true) }) {
            return true
        }
        
        return false
    }
    
    /**
     * Log debug message (filtered)
     */
    @JvmStatic
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldFilter(tag, message)) {
            if (throwable != null) {
                Log.d("$TAG_PREFIX:$tag", message, throwable)
            } else {
                Log.d("$TAG_PREFIX:$tag", message)
            }
        }
    }
    
    /**
     * Log info message (filtered)
     */
    @JvmStatic
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldFilter(tag, message)) {
            if (throwable != null) {
                Log.i("$TAG_PREFIX:$tag", message, throwable)
            } else {
                Log.i("$TAG_PREFIX:$tag", message)
            }
        }
    }
    
    /**
     * Log warning message (filtered)
     */
    @JvmStatic
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldFilter(tag, message)) {
            if (throwable != null) {
                Log.w("$TAG_PREFIX:$tag", message, throwable)
            } else {
                Log.w("$TAG_PREFIX:$tag", message)
            }
        }
    }
    
    /**
     * Log error message (never filtered - errors are always important)
     */
    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // Errors are never filtered - they're always important
        if (throwable != null) {
            Log.e("$TAG_PREFIX:$tag", message, throwable)
        } else {
            Log.e("$TAG_PREFIX:$tag", message)
        }
    }
    
    /**
     * Log verbose message (filtered)
     */
    @JvmStatic
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldFilter(tag, message)) {
            if (throwable != null) {
                Log.v("$TAG_PREFIX:$tag", message, throwable)
            } else {
                Log.v("$TAG_PREFIX:$tag", message)
            }
        }
    }
}
