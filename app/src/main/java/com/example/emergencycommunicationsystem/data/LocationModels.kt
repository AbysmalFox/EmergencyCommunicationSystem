package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

/**
 * Represents the request body for updating a user's location.
 */
data class LocationUpdateRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("address")
    val address: String?,
    @SerializedName("accuracy")
    val accuracy: Float?
)

data class LocationUpdateResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
)
