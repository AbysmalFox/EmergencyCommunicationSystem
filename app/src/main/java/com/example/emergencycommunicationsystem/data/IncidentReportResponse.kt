package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

/**
 * Represents the JSON response from the report_incident.php script.
 */
data class IncidentReportResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)
