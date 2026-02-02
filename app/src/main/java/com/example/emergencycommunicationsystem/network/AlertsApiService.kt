package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.Poll
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * DTO for the alerts list response coming from your PHP backend.
 */
data class AlertsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("alerts") val alerts: List<Alert> = emptyList()
)

data class PollResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("poll") val poll: Poll? = null
)

interface AlertsApiService {

    // Matches PHP endpoint: alerts.php
    @GET("alerts.php")
    suspend fun getAlerts(@Query("user_id") userId: Int?): Response<AlertsResponse>

    // 1. For the Acknowledge Button
    @POST("acknowledge_alert.php")
    suspend fun acknowledgeAlert(
        @Body request: Map<String, Int> // e.g., {"alert_id": 1, "user_id": 123}
    ): Response<Map<String, Any>>

    // 2. For the "Are You Safe?" Poll
    @POST("respond_to_poll.php")
    suspend fun respondToSafePoll(
        @Body request: Map<String, Any> // e.g., {"poll_id": 1, "user_id": 123, "status": "safe"}
    ): Response<Map<String, Any>>

    // 3. To fetch active poll
    @GET("active_poll.php")
    suspend fun getActivePoll(@Query("user_id") userId: Int?): Response<PollResponse>
}
