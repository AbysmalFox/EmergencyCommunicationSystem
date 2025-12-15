package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.models.Alert
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

/**
 * DTO for the alerts list response coming from your PHP backend.
 *
 * Adjust the @SerializedName values or property names if your JSON
 * uses different keys (e.g., "data" instead of "alerts").
 */
data class AlertsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("alerts") val alerts: List<Alert> = emptyList()
)

interface AlertsApiService {

    // Matches PHP endpoint: http://<server>/PHP/api/alerts.php
    @GET("alerts.php")
    suspend fun getAlerts(): Response<AlertsResponse>
}


