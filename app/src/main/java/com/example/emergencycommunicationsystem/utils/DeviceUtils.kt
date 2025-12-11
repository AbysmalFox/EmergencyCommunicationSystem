package com.example.emergencycommunicationsystem.utils
import android.os.Build
object DeviceUtils {
    /**
     * Checks if the app is running on a known Android emulator.
     * This is a common but not foolproof method. It checks for various emulator-specific
     * properties in the Android build system.
     * @return true if it's likely an emulator, false otherwise.
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}