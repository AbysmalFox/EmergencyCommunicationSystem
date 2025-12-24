package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the actual conversation object that is nested inside the response.
 */
data class Conversation(
    val id: Int,
    @SerializedName("alert_id") val alertId: Int,
    @SerializedName("created_by") val createdBy: Int,
    @SerializedName("created_at") val createdAt: String
)

/**
 * Correctly models the top-level JSON response from the create.php script,
 * which contains a nested [Conversation] object.
 */
data class ConversationResponse(
    val success: Boolean,
    val message: String,
    val conversation: Conversation? // This is the nested object
)
