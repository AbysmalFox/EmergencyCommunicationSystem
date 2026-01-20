package com.example.emergencycommunicationsystem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.local.AppDatabase
import com.example.emergencycommunicationsystem.data.local.WeatherEntity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.data.network.WeatherApiClient
import com.example.emergencycommunicationsystem.util.GeminiWeatherService
import com.example.emergencycommunicationsystem.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val apiKey = "de9f8eb51584955d6d6fe607c9d81c84" 
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val weatherDao = AppDatabase.getDatabase(application).weatherDao()
    
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
            return 
        }
        
        val context = getApplication<Application>().applicationContext
        
        // If offline, try to load from cache immediately to provide instant feedback
        if (!NetworkUtils.isNetworkAvailable(context)) {
            loadWeatherFromCache()
            return
        }

        _weatherState.value = WeatherState.Loading 
        try {
            val location = getLocation()
            val language = UserPrefs.getLanguage(context).first()
            fetchWeatherByLocation(location.latitude, location.longitude, language)
        } catch (_: Exception) {
            // If location fails but we have cached data, show cache instead of error
            if (!loadWeatherFromCache()) {
                setLocationNotFound()
            }
        }
    }

    private suspend fun loadWeatherFromCache(): Boolean {
        val cached = weatherDao.getCachedWeather()
        return if (cached != null) {
            _weatherState.value = WeatherState.Success(
                location = cached.location,
                temperature = cached.temperature,
                condition = cached.condition,
                iconUrl = cached.iconUrl,
                lat = cached.lat,
                lon = cached.lon,
                advice = cached.advice,
                feelsLike = cached.feelsLike,
                humidity = cached.humidity,
                windSpeed = cached.windSpeed,
                visibility = cached.visibility,
                isOffline = true
            )
            hasLoadedData = true
            true
        } else {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): Location {
        return try {
            suspendCancellableCoroutine<Location> { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) continuation.resume(location)
                        else continuation.resumeWithException(Exception("Current location is null"))
                    }
                    .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    .addOnCanceledListener { continuation.cancel() }
            }
        } catch (_: Exception) {
            suspendCancellableCoroutine<Location> { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) continuation.resume(location)
                        else continuation.resumeWithException(Exception("Last location is null"))
                    }
                    .addOnFailureListener { e2 -> continuation.resumeWithException(e2) }
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun fetchWeatherByLocation(lat: Double, lon: Double, language: String = "en") {
        try {
            val weatherResponse = WeatherApiClient.weatherService.getCurrentWeatherByLocation(lat, lon, apiKey)
            val forecastResponse = try {
                WeatherApiClient.weatherService.getForecastByLocation(lat, lon, apiKey)
            } catch (_: Exception) { null }

            val locationName = withContext(Dispatchers.IO) {
                 try {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.locality ?: weatherResponse.name
                } catch (_: Exception) {
                    weatherResponse.name
                }
            }

            val iconCode = weatherResponse.weather.firstOrNull()?.icon ?: "01d"
            val condition = weatherResponse.weather.firstOrNull()?.main ?: "Clear"

            val weatherAdvice = withContext(Dispatchers.IO) {
                GeminiWeatherService.getWeatherAdvice(
                    condition = condition,
                    temp = weatherResponse.main.temp,
                    feelsLike = weatherResponse.main.feelsLike,
                    humidity = weatherResponse.main.humidity,
                    windSpeed = weatherResponse.wind.speed,
                    visibility = weatherResponse.visibility,
                    location = locationName,
                    language = language,
                    templateFallback = {
                        getTemplateWeatherAdvice(condition, weatherResponse.main.temp, weatherResponse.main.feelsLike, 
                            weatherResponse.main.humidity, weatherResponse.wind.speed, weatherResponse.visibility)
                    }
                )
            }

            val successState = WeatherState.Success(
                location = "$locationName, PH",
                temperature = "${String.format(Locale.US, "%.1f", weatherResponse.main.temp)}°C",
                condition = condition,
                iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png",
                lat = lat,
                lon = lon,
                advice = weatherAdvice,
                feelsLike = "${String.format(Locale.US, "%.1f", weatherResponse.main.feelsLike)}°C",
                humidity = "${weatherResponse.main.humidity}%",
                windSpeed = "${String.format(Locale.US, "%.1f", weatherResponse.wind.speed)} km/h",
                visibility = "${weatherResponse.visibility / 1000} km",
                forecastData = forecastResponse?.list?.take(48) ?: emptyList(),
                isOffline = false
            )

            // Save to Cache
            viewModelScope.launch(Dispatchers.IO) {
                weatherDao.cacheWeather(WeatherEntity(
                    location = successState.location,
                    temperature = successState.temperature,
                    condition = successState.condition,
                    iconUrl = successState.iconUrl,
                    lat = successState.lat,
                    lon = successState.lon,
                    advice = successState.advice,
                    feelsLike = successState.feelsLike,
                    humidity = successState.humidity,
                    windSpeed = successState.windSpeed,
                    visibility = successState.visibility
                ))
            }

            _weatherState.value = successState
        } catch (e: Exception) {
            // Fallback to cache on any error
            if (!loadWeatherFromCache()) {
                _weatherState.value = WeatherState.Error(e.message ?: "Failed to fetch weather")
            }
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

    private fun getTemplateWeatherAdvice(condition: String, temp: Double, feelsLike: Double, humidity: Int, windSpeed: Double, visibility: Int): String {
        val app = getApplication<Application>()
        val feelsLikeDescription = when {
            feelsLike > temp + 2 -> app.getString(R.string.weather_advice_feels_much_hotter)
            feelsLike < temp - 2 -> app.getString(R.string.weather_advice_feels_much_cooler)
            else -> ""
        }
        val humidityDescription = if (humidity > 75) app.getString(R.string.weather_advice_humidity_high) else ""
        val windDescription = when {
            windSpeed > 15 -> app.getString(R.string.weather_advice_wind_strong)
            windSpeed > 5 -> app.getString(R.string.weather_advice_wind_breezy)
            else -> ""
        }
        val visibilityDescription = if (visibility < 1000) app.getString(R.string.weather_advice_visibility_low) else ""
        
        val baseReplies = when (condition.lowercase()) {
            "clear" -> listOf(app.getString(R.string.weather_advice_clear_1), app.getString(R.string.weather_advice_clear_2))
            "clouds" -> listOf(app.getString(R.string.weather_advice_clouds_1), app.getString(R.string.weather_advice_clouds_2))
            "rain" -> listOf(app.getString(R.string.weather_advice_rain_1), app.getString(R.string.weather_advice_rain_2))
            else -> listOf(app.getString(R.string.weather_advice_unusual_1), app.getString(R.string.weather_advice_unusual_2))
        }

        return (listOf(baseReplies.random()) + listOfNotNull(feelsLikeDescription.takeIf { it.isNotEmpty() }, 
            humidityDescription.takeIf { it.isNotEmpty() }, windDescription.takeIf { it.isNotEmpty() }, 
            visibilityDescription.takeIf { it.isNotEmpty() })).joinToString(" ")
    }
}
