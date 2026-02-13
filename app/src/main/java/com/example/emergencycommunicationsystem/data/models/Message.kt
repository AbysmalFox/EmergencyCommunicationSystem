package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message_id")
    val messageId: Int,

    @SerializedName("conversation_id")
    val conversationId: Int,

    @SerializedName("sender_id")
    val senderId: String,

    @SerializedName("sender_name")
    val senderName: String,

    @SerializedName("sender_type")
    val senderType: String, // 'user' or 'admin'

    @SerializedName("message_text")
    val messageText: String,

    @SerializedName("ip_address")
    val ipAddress: String?,

    @SerializedName("device_info")
    val deviceInfo: String?,

    @SerializedName("is_read")
    val isRead: Int,

    @SerializedName("created_at")
    val createdAt: String
)

data class MessageResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("data")
    val data: Message? = null
)

data class MessagesResponse(
    val success: Boolean,
    val messages: List<Message>,
    val error: String? = null
)
