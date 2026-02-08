package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.emergencycommunicationsystem.data.models.Alert

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: Int,
    val categoryId: Int?,
    val category: String?,
    val title: String?,
    val message: String?,
    val area: String?,
    val content: String?,
    val source: String?,
    val location: String?,
    val status: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: String?,
    val isViewed: Int,
    val isAcknowledged: Boolean,
    val severity: String?
)

/**
 * Extension function to convert Network model to Database model.
 */
fun Alert.toEntity(): AlertEntity {
    return AlertEntity(
        id = this.id,
        categoryId = this.categoryId,
        category = this.category,
        title = this.title,
        message = this.message,
        area = this.area,
        content = this.content,
        source = this.source,
        location = this.location,
        status = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp,
        isViewed = this.isViewed,
        isAcknowledged = this.isAcknowledged,
        severity = this.severity
    )
}

/**
 * Extension function to convert Database model back to Network/UI model.
 */
fun AlertEntity.toDomain(): Alert {
    return Alert(
        id = this.id,
        categoryId = this.categoryId,
        category = this.category,
        title = this.title,
        message = this.message,
        area = this.area,
        content = this.content,
        source = this.source,
        location = this.location,
        status = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp,
        isViewed = this.isViewed,
        isAcknowledged = this.isAcknowledged,
        severity = this.severity ?: "Low"
    )
}
