package com.example.myfirstapp.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.myfirstapp.data.models.WeatherState
import com.example.myfirstapp.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState
    private val apiKey = "de9f8eb51584955d6d6fe607c9d81c84"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    suspend fun requestLocationAndFetchWeather() {
        _weatherState.value = WeatherState.Loading // Always set to loading on refresh
        try {
            val location = getLocation()
            fetchWeatherByLocation(location.latitude, location.longitude)
        } catch (e: Exception) {
            setLocationNotFound()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): Location {
        return try {
            // First, try to get the current location.
            suspendCancellableCoroutine<Location> { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            continuation.resumeWithException(Exception("Current location is null"))
                        }
                    }
                    .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    .addOnCanceledListener { continuation.cancel() }
            }
        } catch (e: Exception) {
            // If getting current location fails, fall back to last known location.
            suspendCancellableCoroutine<Location> { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            continuation.resumeWithException(Exception("Last location is also null"))
                        }
                    }
                    .addOnFailureListener { e2 -> continuation.resumeWithException(e2) }
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun fetchWeatherByLocation(lat: Double, lon: Double) {
        // The loading state is already set by the calling function.
        try {
            val weatherResponse = RetrofitClient.instance.getCurrentWeatherByLocation(lat, lon, apiKey)

            val locationName = withContext(Dispatchers.IO) {
                 try {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.locality ?: weatherResponse.name
                } catch (_: Exception) {
                    weatherResponse.name // Fallback to API name if Geocoder fails
                }
            }


            val iconCode = weatherResponse.weather.firstOrNull()?.icon ?: "01d"

            _weatherState.value = WeatherState.Success(
                location = "$locationName, PH",
                temperature = "${String.format(Locale.US, "%.1f", weatherResponse.main.temp)}°C",
                condition = weatherResponse.weather.firstOrNull()?.main ?: "Clear",
                iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png",
                lat = lat,
                lon = lon
            )
        } catch (_: Exception) {
            _weatherState.value = WeatherState.Error("Failed to load weather. Check connection.")
        }
    }

    fun setLocationPermissionDenied() { _weatherState.value = WeatherState.Error("Permission denied. Enable location in settings.") }
    fun setLocationNotFound() { _weatherState.value = WeatherState.Error("GPS signal lost. Ensure location is on.") }
}
