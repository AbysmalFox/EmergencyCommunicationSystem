package com.example.emergencycommunicationsystem.data.network

import com.example.emergencycommunicationsystem.data.models.ForecastResponse
import com.example.emergencycommunicationsystem.data.models.WeatherResponse
import com.example.emergencycommunicationsystem.network.MessagingApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    suspend fun getCurrentWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecastByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}


object RetrofitClient {

    // Create a logging interceptor for debugging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs request and response bodies
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val weatherRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.WEATHER_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val messagingRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.MESSAGING_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherService: WeatherService by lazy {
        weatherRetrofit.create(WeatherService::class.java)
    }

    val messagingService: MessagingApiService by lazy {
        messagingRetrofit.create(MessagingApiService::class.java)
    }
}
