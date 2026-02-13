package com.example.emergencycommunicationsystem.data

data class WeatherResponse(val main: Main, val weather: List<Weather>, val name: String)
data class Main(val temp: Double)
data class Weather(val main: String, val icon: String)

data class LatLng(val lat: Double, val lon: Double)
data class Alert(
    val id: String,
    val category: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val source: String
)
