package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: Wind,
    val visibility: Int
)
data class Main(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val humidity: Int
)
data class Weather(val main: String, val icon: String)
data class Wind(val speed: Double)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>
)

data class City(
    val name: String,
    val country: String
)

@Suppress("unused")
data class LatLng(val lat: Double, val lon: Double)

data class Alert(
    @SerializedName("id")
    val id: Int,

    @SerializedName("category")
    val category: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("source")
    val source: String?,

    @SerializedName("timestamp")
    val timestamp: String?
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
        var address: String? = null // Added mutable address property
    ) : WeatherState
    data class Error(val message: String) : WeatherState
}
