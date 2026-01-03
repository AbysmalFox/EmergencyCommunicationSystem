package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

// Request for user registration
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    @SerializedName("share_location") val shareLocation: Boolean
)

// Request for user login
data class LoginRequest(
    val email: String,
    val password: String
)

// Generic response for auth operations (Register, Login)
data class AuthResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("user_id") val userId: Int? = null,
    val token: String? = null,
    // Added fields to match the flat JSON from login.php
    val username: String? = null,
    val email: String? = null,
    // The 'user' object is kept for other responses, but made nullable
    val user: User? = null
)

// Request to get user-specific data
data class ProfileDataRequest(
    @SerializedName("user_id") val userId: Int
)

// Response containing user profile data
data class ProfileDataResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)

// Represents a user object.
data class User(
    val name: String,
    val email: String,
    val phone: String? = null
)
