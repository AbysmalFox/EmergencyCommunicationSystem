package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.AuthResponse
import com.example.emergencycommunicationsystem.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register.php")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    // For future:
    // @POST("login.php")
    // suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>
}