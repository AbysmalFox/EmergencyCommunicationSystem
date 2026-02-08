package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

// Request for user registration
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    @SerializedName("share_location") val shareLocation: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)

// Request for user login
data class LoginRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String
)

// Generic response for auth operations (Register, Login)
data class AuthResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("user_id") val userId: Int? = null,
    val token: String? = null,
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null, // Added phone to the response
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

// Request to update user profile
data class UpdateProfileRequest(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val email: String,
    val phone: String
)

// Response for profile update
data class UpdateProfileResponse(
    val success: Boolean,
    val message: String?,
    val data: Any? = null
)

// Request for changing password
data class ChangePasswordRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

// Response for changing password
data class ChangePasswordResponse(
    val success: Boolean,
    val message: String
)
