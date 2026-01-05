package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.AuthResponse
import com.example.emergencycommunicationsystem.data.LogoutRequest
import com.example.emergencycommunicationsystem.data.ProfileDataRequest
import com.example.emergencycommunicationsystem.data.ProfileDataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register.php")
    suspend fun registerUser(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<AuthResponse>

    @POST("login.php")
    suspend fun loginUser(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<AuthResponse>

    @POST("profile_data.php")
    suspend fun getProfileData(@Body request: ProfileDataRequest): ProfileDataResponse

    @POST("logout.php")
    suspend fun logout(@Body request: LogoutRequest): Response<AuthResponse>
}