package com.example.emergencycommunicationsystem.data.network

import com.example.emergencycommunicationsystem.data.models.SignUpRequest
import com.example.emergencycommunicationsystem.data.models.SignUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("signup.php") // Assuming your PHP script is named signup.php
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>
}
