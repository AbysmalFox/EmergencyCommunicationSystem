package com.example.emergencycommunicationsystem.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@Composable
fun LocationUpdater(
    onLocationUpdate: (latitude: Double, longitude: Double, accuracy: Float) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, the LaunchedEffect will re-trigger and get the location.
                Log.d("LocationUpdater", "Location permission granted by user.")
            } else {
                // Handle permission denial.
                Log.w("LocationUpdater", "Location permission denied by user.")
            }
        }
    )

    // This effect runs when the composable enters the composition and if the permission status changes.
    LaunchedEffect(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, get the location
            Log.d("LocationUpdater", "Permission already granted. Fetching location...")
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d("LocationUpdater", "Location retrieved: Lat ${location.latitude}, Lon ${location.longitude}")
                        onLocationUpdate(location.latitude, location.longitude, location.accuracy)
                    } else {
                        Log.w("LocationUpdater", "Failed to get current location (location is null). May be disabled in device settings.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationUpdater", "Failed to get current location.", e)
                }
        } else {
            // Permission not granted, request it
            Log.d("LocationUpdater", "Location permission not granted. Requesting...")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}