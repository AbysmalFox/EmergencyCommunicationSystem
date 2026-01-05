package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

/**
 * Represents the request body for updating a user's location.
 */
data class LocationUpdateRequest(
    @SerializedName("user_id")
    val userId: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val accuracy: Float?
)

data class LocationUpdateResponse(
    val success: Boolean,
    val message: String?
)
