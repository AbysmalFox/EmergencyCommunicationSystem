package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.AuthResponse
import com.example.emergencycommunicationsystem.data.LoginRequest // <-- ADD THIS IMPORT
import com.example.emergencycommunicationsystem.data.ProfileDataRequest
import com.example.emergencycommunicationsystem.data.ProfileDataResponse
import com.example.emergencycommunicationsystem.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register.php")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login.php") // <-- ADD THIS METHOD
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("profile_data.php")
    suspend fun getProfileData(@Body request: ProfileDataRequest): Response<ProfileDataResponse>
}