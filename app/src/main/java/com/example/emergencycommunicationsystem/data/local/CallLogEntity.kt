package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "call_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["roomName"]),
        Index(value = ["createdAt"])
    ]
)
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val callType: String = "internet", // "internet" or "emergency"
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Int = 0, // in seconds
    val status: String = "active", // "active", "ended", "missed", "cancelled"
    val roomName: String,
    val isAdminCall: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
