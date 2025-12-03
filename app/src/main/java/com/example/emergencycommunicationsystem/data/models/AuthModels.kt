package com.example.emergencycommunicationsystem.data.models

data class SignUpRequest(
    val email: String,
    val password: String
)

data class SignUpResponse(
    val status: String, // e.g., "success" or "error"
    val message: String
)
