package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incident_outbox")
data class IncidentOutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int,
    val incidentType: String,
    val urgency: String,
    val details: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val reporterName: String?,
    val imagePath: String?,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
    val lastError: String? = null
)
