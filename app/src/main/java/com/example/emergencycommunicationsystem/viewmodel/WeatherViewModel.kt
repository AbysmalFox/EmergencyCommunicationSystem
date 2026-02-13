package com.example.emergencycommunicationsystem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import com.example.emergencycommunicationsystem.util.LocaleManager
import com.example.emergencycommunicationsystem.BuildConfig

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val weatherDao = AppDatabase.getDatabase(application).weatherDao()
    
    var hasLoadedData: Boolean = false
        private set
    
    var lastUsedLanguage: String? = null
        private set

    init {
        if (isApiKeyMissing()) {
            _weatherState.value = WeatherState.Error("API Key is missing. Please add your OpenWeather API key to local.properties.")
        }
    }

    private fun isApiKeyMissing(): Boolean = apiKey.isBlank() || apiKey.contains("PLACEHOLDER") || apiKey == "null"

    @SuppressLint("MissingPermission")
    suspend fun requestLocationAndFetchWeather() {
        if (isApiKeyMissing()) return
        
        val context = getApplication<Application>().applicationContext
        val language = UserPrefs.getLanguage(context).first()

        // 1. Internet Check
        if (!NetworkUtils.isNetworkAvailable(context)) {
            if (loadWeatherFromCache()) return
            _weatherState.value = WeatherState.Error("No internet connection available.")
            return
        }

        _weatherState.value = WeatherState.Loading 
        
        try {
            // 2. Try to get location with an 8-second timeout
            val location = withTimeoutOrNull(8000L) {
                if (hasLocationPermission()) {
                    getLocation()
                } else {
                    null
                }
            }
            
            // 3. Logic: If location found, use it. If not, use cache. If no cache, use Quezon City.
            if (location != null) {
                fetchWeatherByLocation(location.latitude, location.longitude, language)
            } else if (!loadWeatherFromCache()) {
                // Fallback to Quezon City coordinates (Ensures user sees weather even if GPS fails)
                fetchWeatherByLocation(14.6760, 121.0437, language)
            }
        } catch (e: Exception) {
            if (!loadWeatherFromCache()) {
                _weatherState.value = WeatherState.Error("Failed to update weather: ${e.localizedMessage}")
            }
        } finally {
            hasLoadedData = true
        }
    }

    private fun hasLocationPermission(): Boolean {
        val context = getApplication<Application>().applicationContext
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
                forecastData = cached.forecastData,
                isOffline = true
            )
            hasLoadedData = true
            true
        } else {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): Location? {
        return try {
            // Priority 1: Last Known (Fast)
            val lastLoc = suspendCancellableCoroutine<Location?> { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resume(null) }
            }
            
            if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 600000) return lastLoc

            // Priority 2: Fresh fix (Balanced)
            suspendCancellableCoroutine<Location?> { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resume(null) }
                    .addOnCanceledListener { continuation.resume(null) }
            }
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    suspend fun fetchWeatherByLocation(lat: Double, lon: Double, language: String = "en") {
        lastUsedLanguage = language
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
            
            val forecastInfo = forecastResponse?.list?.take(5)?.joinToString("; ") {
                val time = SimpleDateFormat("ha", Locale.US).format(Date(it.dt * 1000L))
                "$time: ${it.main.temp.toInt()}°C, ${it.weather.firstOrNull()?.main}"
            } ?: ""

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
                    forecastInfo = forecastInfo,
                    templateFallback = {
                        val locale = LocaleManager.getLocaleFromCode(language)
                        val config = android.content.res.Configuration(getApplication<Application>().resources.configuration)
                        config.setLocale(locale)
                        val localeContext = getApplication<Application>().createConfigurationContext(config)
                        getTemplateWeatherAdvice(localeContext, condition, weatherResponse.main.temp, weatherResponse.main.feelsLike, 
                            weatherResponse.main.humidity, weatherResponse.wind.speed, weatherResponse.visibility)
                    }
                )
            }

            val successState = WeatherState.Success(
                location = if (locationName.contains("Quezon", ignoreCase = true)) "Quezon City, PH" else "$locationName, PH",
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
                    visibility = successState.visibility,
                    forecastData = successState.forecastData
                ))
            }

            _weatherState.value = successState
        } catch (e: Exception) {
            if (!loadWeatherFromCache()) {
                val msg = when(e) {
                    is IOException -> "Connection error. Check your internet."
                    is HttpException -> if (e.code() == 401) "Invalid API Key. Update local.properties." else "Service unavailable (${e.code()})."
                    else -> e.localizedMessage ?: "Failed to load weather."
                }
                _weatherState.value = WeatherState.Error(msg)
            }
        }
    }

    suspend fun reloadWeather(language: String) {
        if (lastUsedLanguage != language) {
            hasLoadedData = false
            _weatherState.value = WeatherState.Loading
            requestLocationAndFetchWeather()
        }
    }
    
    private fun getTemplateWeatherAdvice(context: android.content.Context, condition: String, temp: Double, feelsLike: Double, humidity: Int, windSpeed: Double, visibility: Int): String {
        val feelsLikeDescription = when {
            feelsLike > temp + 2 -> context.getString(R.string.weather_advice_feels_much_hotter)
            feelsLike < temp - 2 -> context.getString(R.string.weather_advice_feels_much_cooler)
            else -> ""
        }
        val humidityDescription = if (humidity > 75) context.getString(R.string.weather_advice_humidity_high) else ""
        val windDescription = when {
            windSpeed > 15 -> context.getString(R.string.weather_advice_wind_strong)
            windSpeed > 5 -> context.getString(R.string.weather_advice_wind_breezy)
            else -> ""
        }
        val visibilityDescription = if (visibility < 1000) context.getString(R.string.weather_advice_visibility_low) else ""
        
        val baseReplies = when (condition.lowercase()) {
            "clear" -> listOf(context.getString(R.string.weather_advice_clear_1), context.getString(R.string.weather_advice_clear_2))
            "clouds" -> listOf(context.getString(R.string.weather_advice_clouds_1), context.getString(R.string.weather_advice_clouds_2))
            "rain" -> listOf(context.getString(R.string.weather_advice_rain_1), context.getString(R.string.weather_advice_rain_2))
            else -> listOf(context.getString(R.string.weather_advice_unusual_1), context.getString(R.string.weather_advice_unusual_2))
        }

        return (listOf(baseReplies.random()) + listOfNotNull(feelsLikeDescription.takeIf { it.isNotEmpty() }, 
            humidityDescription.takeIf { it.isNotEmpty() }, windDescription.takeIf { it.isNotEmpty() }, 
            visibilityDescription.takeIf { it.isNotEmpty() })).joinToString(" ")
    }
}
