package com.example.emergencycommunicationsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true),
        Index(value = ["isOnline"]),
        Index(value = ["lastSeen"])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val phone: String? = null,
    val fullName: String? = null,
    val avatar: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
