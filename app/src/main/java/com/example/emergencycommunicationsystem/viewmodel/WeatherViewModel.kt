package com.example.emergencycommunicationsystem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.example.emergencycommunicationsystem.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.data.network.WeatherApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState
    private val apiKey = "de9f8eb51584955d6d6fe607c9d81c84" // NOTE: This key is likely invalid.
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    var hasLoadedData: Boolean = false
        private set

    init {
        if (apiKey == "YOUR_API_KEY" || apiKey.isBlank()) {
            _weatherState.value = WeatherState.Error("API key is missing. Please add it in WeatherViewModel.")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun requestLocationAndFetchWeather() {
        if (_weatherState.value is WeatherState.Error && (apiKey == "YOUR_API_KEY" || apiKey.isBlank())) {
            return // Do not proceed if API key is missing
        }
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
            val weatherResponse = WeatherApiClient.weatherService.getCurrentWeatherByLocation(lat, lon, apiKey)
            val forecastResponse = try {
                WeatherApiClient.weatherService.getForecastByLocation(lat, lon, apiKey)
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
                temperature = "${String.format(Locale.US, "%.1f", weatherResponse.main.temp)}°C",
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
                feelsLike = "${String.format(Locale.US, "%.1f", weatherResponse.main.feelsLike)}°C",
                humidity = "${weatherResponse.main.humidity}%",
                windSpeed = "${String.format(Locale.US, "%.1f", weatherResponse.wind.speed)} km/h",
                visibility = "${weatherResponse.visibility / 1000} km",
                forecastData = forecastResponse?.list?.take(48) ?: emptyList() // Take first 48 items (2 days of 3-hourly data)
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() // Don't use in production
            val errorCode = e.code()
            val errorMessage = when(errorCode) {
                401 -> "Invalid API key. Please replace it in WeatherViewModel."
                404 -> "Weather data not found for this location."
                else -> "HTTP Error $errorCode: $errorBody"
            }
            _weatherState.value = WeatherState.Error(errorMessage)
        } catch (e: IOException) {
            _weatherState.value = WeatherState.Error("Network error. Please check your connection.")
        } catch (e: Exception) {
             _weatherState.value = WeatherState.Error("An unexpected error occurred: ${e.message}")
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
        val app = getApplication<Application>()

        val feelsLikeDescription = when {
            feelsLike > temp + 2 -> app.getString(R.string.weather_advice_feels_much_hotter)
            feelsLike < temp - 2 -> app.getString(R.string.weather_advice_feels_much_cooler)
            else -> ""
        }

        val humidityDescription = when {
            humidity > 75 -> app.getString(R.string.weather_advice_humidity_high)
            else -> ""
        }

        val windDescription = when {
            windSpeed > 15 -> app.getString(R.string.weather_advice_wind_strong)
            windSpeed > 5 -> app.getString(R.string.weather_advice_wind_breezy)
            else -> ""
        }

        val visibilityDescription = when (visibility) {
            in 0..999 -> app.getString(R.string.weather_advice_visibility_low)
            else -> ""
        }

        val baseReplies = when (condition.lowercase()) {
            "clear" -> listOf(
                app.getString(R.string.weather_advice_clear_1),
                app.getString(R.string.weather_advice_clear_2)
            )
            "clouds" -> listOf(
                app.getString(R.string.weather_advice_clouds_1),
                app.getString(R.string.weather_advice_clouds_2)
            )
            "rain" -> listOf(
                app.getString(R.string.weather_advice_rain_1),
                app.getString(R.string.weather_advice_rain_2)
            )
            "drizzle" -> listOf(
                app.getString(R.string.weather_advice_drizzle_1),
                app.getString(R.string.weather_advice_drizzle_2)
            )
            "thunderstorm" -> listOf(
                app.getString(R.string.weather_advice_thunderstorm_1),
                app.getString(R.string.weather_advice_thunderstorm_2)
            )
            "snow" -> listOf(
                app.getString(R.string.weather_advice_snow_1),
                app.getString(R.string.weather_advice_snow_2)
            )
            "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> listOf(
                app.getString(R.string.weather_advice_low_visibility_1),
                app.getString(R.string.weather_advice_low_visibility_2)
            )
            else -> listOf(
                app.getString(R.string.weather_advice_unusual_1),
                app.getString(R.string.weather_advice_unusual_2)
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
