package com.example.emergencycommunicationsystem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.data.network.RetrofitClient
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
    var hasLoadedData: Boolean = false
        private set

    @SuppressLint("MissingPermission")
    suspend fun requestLocationAndFetchWeather() {
        _weatherState.value = WeatherState.Loading // Always set to loading on refresh
        try {
            val location = getLocation()
            fetchWeatherByLocation(location.latitude, location.longitude)
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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
            val forecastResponse = try {
                RetrofitClient.instance.getForecastByLocation(lat, lon, apiKey)
            } catch (_: Exception) {
                // If forecast fails, continue with current weather only
                null
            }

            val locationName = withContext(Dispatchers.IO) {
                 try {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.locality ?: weatherResponse.name
                } catch (_: Exception) {
                    weatherResponse.name // Fallback to API name if Geocoder fails
                }
            }


            val iconCode = weatherResponse.weather.firstOrNull()?.icon ?: "01d"
            val condition = weatherResponse.weather.firstOrNull()?.main ?: "Clear"

            _weatherState.value = WeatherState.Success(
                location = "$locationName, PH",
                temperature = "${String.format(Locale.US, "%.1f", weatherResponse.main.temp)}\u00b0C",
                condition = condition,
                iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png",
                lat = lat,
                lon = lon,
                advice = getWeatherAdvice(
                    condition = condition,
                    temp = weatherResponse.main.temp,
                    feelsLike = weatherResponse.main.feelsLike,
                    humidity = weatherResponse.main.humidity,
                    windSpeed = weatherResponse.wind.speed,
                    visibility = weatherResponse.visibility
                ),
                feelsLike = "${String.format(Locale.US, "%.1f", weatherResponse.main.feelsLike)}\u00b0C",
                humidity = "${weatherResponse.main.humidity}%",
                windSpeed = "${String.format(Locale.US, "%.1f", weatherResponse.wind.speed)} km/h",
                visibility = "${weatherResponse.visibility / 1000} km",
                forecastData = forecastResponse?.list?.take(48) ?: emptyList() // Take first 48 items (2 days of 3-hourly data)
            )
        } catch (_: Exception) {
            _weatherState.value = WeatherState.Error("Failed to load weather. Check connection.")
        } finally {
            hasLoadedData = true
        }
    }

    fun setLocationPermissionDenied() {
         _weatherState.value = WeatherState.Error("Permission denied. Enable location in settings.")
         hasLoadedData = true
    }
    fun setLocationNotFound() {
        _weatherState.value = WeatherState.Error("GPS signal lost. Ensure location is on.")
        hasLoadedData = true
    }

    private fun getWeatherAdvice(
    condition: String,
    temp: Double,
    feelsLike: Double,
    humidity: Int,
    windSpeed: Double,
    visibility: Int
): String {
    val feelsLikeDescription = when {
        feelsLike > temp + 2 -> "It feels much hotter than the actual temperature."
        feelsLike < temp - 2 -> "It feels much cooler than the actual temperature."
        else -> ""
    }

    val humidityDescription = when {
        humidity > 75 -> "The humidity is high, so expect some stickiness."
        else -> ""
    }

    val windDescription = when {
        windSpeed > 15 -> "Winds are quite strong, so be careful of flying debris."
        windSpeed > 5 -> "It’s a bit breezy."
        else -> ""
    }

    val visibilityDescription = when (
        visibility
    ) {
        in 0..999 -> "Visibility is low. Be extra careful when driving or walking."
        else -> ""
    }

    val baseReplies = when (condition.lowercase()) {
        "clear" -> listOf(
            "It’s a bright, sunny day. A great time for outdoor plans—just don’t forget sunscreen and stay hydrated.",
            "Clear skies today! Perfect for going out, but the sun might get strong later, so stay protected."
        )
        "clouds" -> listOf(
            "The sky is cloudy. The temperature might drop a bit, so a light jacket could be a good idea.",
            "It’s looking overcast. No rain yet, but keep an eye on the sky."
        )
        "rain" -> listOf(
            "It’s raining. Roads can get slippery, so move carefully and carry an umbrella or raincoat.",
            "Expect rainfall. Make sure you’re prepared with waterproof gear and take extra caution."
        )
        "drizzle" -> listOf(
            "A light drizzle is happening. It’s manageable, but you might want a jacket to stay dry.",
            "Expect some light rain. Nothing heavy, but enough to make things damp."
        )
        "thunderstorm" -> listOf(
            "A thunderstorm is expected. It’s best to stay indoors and avoid open areas.",
            "Thunderstorms incoming. Postpone outdoor plans and stay updated on alerts."
        )
        "snow" -> listOf(
            "Snow is falling. Dress warmly and be cautious—roads can be slippery.",
            "Expect snowy conditions. Bundle up and travel carefully if you need to go out."
        )
        "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> listOf(
            "Visibility is low due to current conditions. Drive slowly and stay alert.",
            "Low-visibility weather detected. Travel can be risky, so please be careful."
        )
        else -> listOf(
            "The weather seems unusual today. Keep an eye on updates and stay prepared.",
            "Conditions are a bit unpredictable. It’s best to stay alert and ready for anything."
        )
    }

    val adviceParts = listOfNotNull(
        baseReplies.random(),
        feelsLikeDescription.takeIf { it.isNotEmpty() },
        humidityDescription.takeIf { it.isNotEmpty() },
        windDescription.takeIf { it.isNotEmpty() },
        visibilityDescription.takeIf { it.isNotEmpty() }
    )

    return adviceParts.joinToString(" ")
}
}
