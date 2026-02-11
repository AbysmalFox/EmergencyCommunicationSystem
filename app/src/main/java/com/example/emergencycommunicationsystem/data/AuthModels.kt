package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

// Request for user registration
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String,
    @SerializedName("share_location") val shareLocation: Boolean,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("address") val address: String? = null
)

// Request for user login
data class LoginRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("password") val password: String
)

// Generic response for auth operations (Register, Login)
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null, // Added phone to the response
    @SerializedName("user") val user: User? = null
)

// Request to get user-specific data
data class ProfileDataRequest(
    @SerializedName("user_id") val userId: Int
)

// Response containing user profile data
data class ProfileDataResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User? = null
)

// Represents a user object.
data class User(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null
)

// Request to update user profile
data class UpdateProfileRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String
)

// Response for profile update
data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Any? = null
)

// Request for changing password
data class ChangePasswordRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

// Response for changing password
data class ChangePasswordResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
