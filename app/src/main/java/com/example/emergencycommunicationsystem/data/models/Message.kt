package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id")
    val id: Int,
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("sender_id")
    val senderId: Int,

    // This now matches the "AS senderName" alias from the PHP script
    @SerializedName("senderName")
    val senderName: String,

    // This now matches the "AS messageText" alias from the PHP script
    @SerializedName("messageText")
    val messageText: String,

    @SerializedName("sent_at")
    val sentAt: String
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

// This class correctly models the response from messages/list.php
data class MessagesResponse(
    val success: Boolean,
    val messages: List<Message>,
    val error: String? = null
)
