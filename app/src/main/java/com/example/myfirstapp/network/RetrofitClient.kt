package com.example.myfirstapp.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IMPORTANT:
    // - For Android Emulator: "http://10.0.2.2/PHP/api/"
    // - For Physical Device (on same Wi-Fi): "http://192.168.1.4/PHP/api/" (Your PC's IP)
    private const val BASE_URL = "http://192.168.1.4/PHP/api/" // <-- Use your PC's IP here!

    private val gson = GsonBuilder()
        .setLenient() // Helps with some JSON parsing quirks, if any
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}