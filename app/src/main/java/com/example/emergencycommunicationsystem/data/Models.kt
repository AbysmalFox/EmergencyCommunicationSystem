package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("name") val name: String
)
data class Main(@SerializedName("temp") val temp: Double)
data class Weather(@SerializedName("main") val main: String, @SerializedName("icon") val icon: String)

data class LatLng(@SerializedName("lat") val lat: Double, @SerializedName("lon") val lon: Double)
data class Alert(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("source") val source: String
)
