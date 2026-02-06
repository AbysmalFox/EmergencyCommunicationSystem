package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("device_id")
    val deviceId: String
)
