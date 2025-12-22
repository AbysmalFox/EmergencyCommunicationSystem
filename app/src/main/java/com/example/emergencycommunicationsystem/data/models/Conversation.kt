package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

data class Conversation(
    @SerializedName("id")
    val id: Int,
    @SerializedName("alert_id")
    val alertId: Int,
    @SerializedName("created_by")
    val createdBy: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class ConversationResponse(
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("success")
    val success: Boolean
)

