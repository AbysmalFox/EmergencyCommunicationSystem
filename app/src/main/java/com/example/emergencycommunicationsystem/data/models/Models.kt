package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("name") val name: String,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int
)
data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int
)
data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("icon") val icon: String
)
data class Wind(@SerializedName("speed") val speed: Double)

data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: City
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>
)

data class City(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)

@Suppress("unused")
data class LatLng(@SerializedName("lat") val lat: Double, @SerializedName("lon") val lon: Double)

data class Alert(
    @SerializedName("id")
    val id: Int,

    @SerializedName("category_id")
    val categoryId: Int? = 0,

    @SerializedName("category")
    val category: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("area")
    val area: String? = null,

    @SerializedName("content")
    val content: String?,

    @SerializedName("source")
    val source: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("created_at")
    val timestamp: String?,

    @SerializedName("is_viewed")
    val isViewed: Int = 0,

    @SerializedName("is_acknowledged")
    val isAcknowledged: Boolean = false,

    @SerializedName("severity")
    val severity: String? = "Low"
)

data class Poll(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String
)

sealed interface WeatherState {
    data object Loading : WeatherState
    data class Success(
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
        var address: String? = null,
        val isOffline: Boolean = false 
    ) : WeatherState
    data class Error(val message: String) : WeatherState
}

/**
 * Safe Zone types for emergency situations
 */
enum class SafeZoneType {
    HOSPITAL,
    EVACUATION_CENTER
}

/**
 * Safe Zone data model for hospitals and evacuation centers
 */
data class SafeZone(
    val id: String,
    val name: String,
    val type: SafeZoneType,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val contact: String? = null,
    val capacity: Int? = null // For evacuation centers
)
