package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id")
    val id: Int,
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("sender_name")
    val senderName: String,
    @SerializedName("message")
    val messageText: String,
    @SerializedName("sent_at")
    val sentAt: String
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

