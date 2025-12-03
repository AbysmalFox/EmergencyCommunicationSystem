package com.example.emergencycommunicationsystem.data

// Request body for registration
data class RegisterRequest(
    val name: String, // <--- CHANGED FROM 'username' TO 'name'
    val email: String,
    val password: String
)

// Response body from the backend for auth operations
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null, // Backend sends 'id', use Int
    val username: String? = null,
    val email: String? = null,
    val token: String? = null // For future session management
)

// You might also need this for future login, but for now, focus on register
data class LoginRequest(
    val email: String,
    val password: String
)