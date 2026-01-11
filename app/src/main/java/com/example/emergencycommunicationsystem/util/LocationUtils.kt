package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import java.io.IOException
import java.util.Locale
import kotlin.math.*

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

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1 -> "${(distanceKm * 1000).toInt()}m"
            distanceKm < 10 -> String.format("%.1f km", distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }
}
