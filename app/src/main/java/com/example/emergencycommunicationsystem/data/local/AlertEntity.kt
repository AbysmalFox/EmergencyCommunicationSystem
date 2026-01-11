package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.emergencycommunicationsystem.data.models.Alert

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: Int,
    val category: String?,
    val title: String?,
    val content: String?,
    val source: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: String?
)

/**
 * Extension function to convert Network model to Database model.
 */
fun Alert.toEntity(): AlertEntity {
    return AlertEntity(
        id = this.id,
        category = this.category,
        title = this.title,
        content = this.content,
        source = this.source,
        location = this.location,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp
    )
}

/**
 * Extension function to convert Database model back to Network/UI model.
 */
fun AlertEntity.toDomain(): Alert {
    return Alert(
        id = this.id,
        category = this.category,
        title = this.title,
        content = this.content,
        source = this.source,
        location = this.location,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp
    )
}
