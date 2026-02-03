package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "call_messages",
    foreignKeys = [
        ForeignKey(
            entity = CallLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["callLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["callLogId"]),
        Index(value = ["senderId"]),
        Index(value = ["timestamp"])
    ]
)
data class CallMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val callLogId: Int,
    val senderId: Int,
    val senderType: String, // "user" or "admin"
    val message: String,
    val messageType: String = "text", // "text", "image", "location"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
