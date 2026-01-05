package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import java.io.IOException
import java.util.Locale

object LocationUtils {

    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) {
            return null
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var address: String? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    address = addresses.firstOrNull()?.thoroughfare
                }
                address
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.thoroughfare
            }
        } catch (e: IOException) {
            // Network or I/O error
            null
        }
    }
}
