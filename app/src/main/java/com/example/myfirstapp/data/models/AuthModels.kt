package com.example.myfirstapp.data.models

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    val email: String,
    val password: String
)

data class SignUpResponse(
    val status: String, // e.g., "success" or "error"
    val message: String
)
