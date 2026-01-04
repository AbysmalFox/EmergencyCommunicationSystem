package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id")
    val id: Int,
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("sender_id")
    val senderId: Int,

    @SerializedName("senderName")
    val senderName: String?,

    @SerializedName("messageText")
    val messageText: String?,

    @SerializedName("sent_at")
    val sentAt: String?,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("nonce")
    val nonce: String? = null
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

data class MessagesResponse(
    val success: Boolean,
    val messages: List<Message>,
    val error: String? = null
)
