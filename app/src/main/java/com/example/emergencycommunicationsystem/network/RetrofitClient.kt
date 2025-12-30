package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.utils.DeviceUtils
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Define the possible base URLs
    private const val BASE_URL_EMULATOR = "http://10.0.2.2/PHP/api/"
    // vvv THIS IS THE LINE TO CHANGE vvv
    private const val BASE_URL_PHYSICAL_DEVICE = "http://192.168.1.6/PHP/api/" // <-- YOUR CORRECT PHYSICAL NETWORK IP
    // ^^^ THIS IS THE LINE TO CHANGE ^^^

    /**
     * Dynamically determines the correct base URL.
     */
    private fun getBaseUrl(): String {
        return if (DeviceUtils.isEmulator()) {
            BASE_URL_EMULATOR
        } else {
            BASE_URL_PHYSICAL_DEVICE // This will now use the correct IP on your phone
        }
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val alertsApiService: AlertsApiService by lazy {
        retrofit.create(AlertsApiService::class.java)
    }

    fun getClient(): Retrofit {
        return retrofit
    }
}