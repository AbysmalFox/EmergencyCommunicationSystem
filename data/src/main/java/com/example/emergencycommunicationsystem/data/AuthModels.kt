package com.example.emergencycommunicationsystem.data

data class UpdateProfileRequest(
    val user_id: Int,
    val username: String,
    val email: String,
    val phone: String
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String?
)
