package com.example.myfirstapp.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient // <-- ADD THIS IMPORT
import okhttp3.logging.HttpLoggingInterceptor // <-- ADD THIS IMPORT
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // <-- ADD THIS IMPORT

object RetrofitClient {
    // IMPORTANT:
    // - For Android Emulator: "http://10.0.2.2/PHP/api/"
    // - For Physical Device (on same Wi-Fi): "http://192.168.1.4/PHP/api/" (Your PC's IP)
    private const val BASE_URL = "http://192.168.1.4/PHP/api/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Create a logger for network requests
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Set to BODY to log request and response headers and body
        // Change to BASIC or HEADERS for less verbosity, or NONE for no logging
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create an OkHttpClient with the logging interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Add timeouts to catch slow responses
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- SET THE CUSTOM OKHTTPCLIENT
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}