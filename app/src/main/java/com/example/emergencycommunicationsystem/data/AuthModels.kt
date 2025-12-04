package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

// Request body for registration
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Response body from the backend for auth operations
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int? = null, // Mapped from "user_id"
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("token") val token: String? = null
)

// Request body for login
data class LoginRequest(
    val email: String,
    val password: String
)

// Request body for getting profile data
data class ProfileDataRequest(
    val userId: Int
)

// Response for getting profile data
data class ProfileDataResponse(
    val success: Boolean,
    val message: String,
    val username: String? = null,
    val email: String? = null
)
