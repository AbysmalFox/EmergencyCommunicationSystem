package com.example.emergencycommunicationsystem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.SafeZone
import com.example.emergencycommunicationsystem.util.NavigationManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    
    // Navigation Manager
    val navigationManager = NavigationManager(viewModelScope)
    val navigationState = navigationManager.navigationState

    // User Location
    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    // Selected Destination (for Details/Preview)
    private val _selectedEvac = MutableStateFlow<SafeZone?>(null)
    val selectedEvac: StateFlow<SafeZone?> = _selectedEvac.asStateFlow()

    // Active Route Destination (for Navigation)
    private val _routeDestination = MutableStateFlow<SafeZone?>(null)
    val routeDestination: StateFlow<SafeZone?> = _routeDestination.asStateFlow()

    // Map UI State
    // Default to NOT locked so the map remains QC-first on initial load
    private val _isCameraLocked = MutableStateFlow(false)
    val isCameraLocked: StateFlow<Boolean> = _isCameraLocked.asStateFlow()

    private val _isCalculatingRoute = MutableStateFlow(false)
    val isCalculatingRoute: StateFlow<Boolean> = _isCalculatingRoute.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    @SuppressLint("MissingPermission") // Permission must be checked in UI before calling this
    fun startLocationUpdates() {
        if (isTracking) return
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateDistanceMeters(5f) // Update every 5 meters
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocation(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
        isTracking = true
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        isTracking = false
    }

    private fun updateLocation(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        _userLocation.value = geoPoint
        
        // Update Navigation Manager if navigating
        if (navigationState.value.isNavigating) {
            navigationManager.updateLocation(location.latitude, location.longitude)
        }
    }
    
    fun setSelectedEvac(evac: SafeZone?) {
        _selectedEvac.value = evac
    }

    fun setRouteDestination(destination: SafeZone?) {
        _routeDestination.value = destination
    }

    fun setCameraLocked(locked: Boolean) {
        _isCameraLocked.value = locked
    }

    fun setIsCalculatingRoute(calculating: Boolean) {
        _isCalculatingRoute.value = calculating
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
