package com.example.emergencycommunicationsystem.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // IMPORTANT: SET THE CORRECT BASE_URL FOR YOUR XAMPP SERVER
    // For Android EMULATOR:
    private const val BASE_URL_EMULATOR = "http://10.0.2.2/PHP/api/"
    // For PHYSICAL Android DEVICE (on the same Wi-Fi as your PC, with PC IP 192.168.1.4):
    private const val BASE_URL_PHYSICAL_DEVICE = "http://192.168.1.4/PHP/api/"

    // Ensure this is set to EMULATOR if you are using an emulator
    private const val CURRENT_BASE_URL = BASE_URL_EMULATOR // <--- Set for emulator

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
            .baseUrl(CURRENT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}