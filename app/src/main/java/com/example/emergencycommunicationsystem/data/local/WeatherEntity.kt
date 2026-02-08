package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.example.emergencycommunicationsystem.data.models.ForecastItem

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0, // Store only one latest entry
    val location: String,
    val temperature: String,
    val condition: String,
    val iconUrl: String,
    val lat: Double,
    val lon: Double,
    val advice: String,
    val feelsLike: String,
    val humidity: String,
    val windSpeed: String,
    val visibility: String,
    val forecastData: List<ForecastItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
