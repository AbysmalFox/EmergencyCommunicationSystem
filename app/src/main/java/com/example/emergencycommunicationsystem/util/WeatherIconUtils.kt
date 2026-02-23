package com.example.emergencycommunicationsystem.util

import com.example.emergencycommunicationsystem.R

object WeatherIconUtils {
    fun getWeatherAnimation(condition: String): Int {
        return when (condition.lowercase()) {
            // Use a neutral icon for clear conditions to avoid showing a sun marker on map.
            "clear" -> R.drawable.weather_broken_clouds
            "clouds" -> R.drawable.weather_broken_clouds
            "rain", "drizzle" -> R.drawable.weather_rain
            "thunderstorm" -> R.drawable.weather_thunderstorm
            "snow" -> R.drawable.weather_snow
            "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> R.drawable.weather_mist
            else -> R.drawable.weather_default
        }
    }
}
