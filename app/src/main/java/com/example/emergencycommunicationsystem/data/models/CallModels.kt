package com.example.emergencycommunicationsystem.data.models

/**
 * Data models for Internet Call functionality
 */

data class CallLog(
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

data class CallMessage(
    val id: Int = 0,
    val callLogId: Int,
    val senderId: Int,
    val senderType: String, // "user" or "admin"
    val message: String,
    val messageType: String = "text", // "text", "image", "location"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class UserProfile(
    val id: Int,
    val username: String,
    val email: String,
    val phone: String? = null,
    val fullName: String? = null,
    val avatar: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
)

data class CallState(
    val isActive: Boolean = false,
    val isConnecting: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val duration: Int = 0,
    val startTime: Long? = null,
    val roomName: String? = null,
    val remoteUser: UserProfile? = null,
    val messages: List<CallMessage> = emptyList()
)
