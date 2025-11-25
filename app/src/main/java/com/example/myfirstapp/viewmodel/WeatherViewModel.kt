package com.example.myfirstapp.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.myfirstapp.data.models.WeatherState
import com.example.myfirstapp.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState
    private val apiKey = "de9f8eb51584955d6d6fe607c9d81c84"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    fun requestLocationAndFetchWeather() {
        if (weatherState.value !is WeatherState.Success) {
            _weatherState.value = WeatherState.Loading
        }

        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    fetchWeatherByLocation(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                           fetchWeatherByLocation(lastLocation.latitude, lastLocation.longitude)
                        } else {
                           setLocationNotFound()
                        }
                    }
                }
            }
            .addOnFailureListener {
                setLocationNotFound()
            }
    }

    @Suppress("DEPRECATION")
    fun fetchWeatherByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            if (weatherState.value !is WeatherState.Success) _weatherState.value = WeatherState.Loading
            try {
                val weatherResponse = RetrofitClient.instance.getCurrentWeatherByLocation(lat, lon, apiKey)
                
                val locationName = try {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.locality ?: weatherResponse.name
                } catch (_: Exception) {
                    weatherResponse.name // Fallback to API name if Geocoder fails
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
    }
    fun setLocationPermissionDenied() { _weatherState.value = WeatherState.Error("Permission denied. Enable location in settings.") }
    fun setLocationNotFound() { _weatherState.value = WeatherState.Error("GPS signal lost. Ensure location is on.") }
}
