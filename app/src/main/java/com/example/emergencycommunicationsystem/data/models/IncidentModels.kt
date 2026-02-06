package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class IncidentReport(
    @SerializedName("id") val id: Int,
    @SerializedName("incident_type") val type: String,
    @SerializedName("urgency") val urgency: String,
    @SerializedName("details") val details: String,
    @SerializedName("status") val status: String, // e.g., "Submitted", "Under Review", "Resolved"
    @SerializedName("created_at") val dateReported: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String?
)

data class UserReportsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("reports") val reports: List<IncidentReport>,
    @SerializedName("message") val message: String?
)
