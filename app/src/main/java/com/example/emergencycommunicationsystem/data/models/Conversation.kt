package com.example.emergencycommunicationsystem.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the actual conversation object that is nested inside the response.
 */
data class Conversation(
    @SerializedName("conversation_id")
    val conversationId: Int,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("user_name")
    val userName: String?,
    
    @SerializedName("user_email")
    val userEmail: String?,
    
    @SerializedName("user_phone")
    val userPhone: String?,
    
    @SerializedName("user_location")
    val userLocation: String?,
    
    @SerializedName("user_concern")
    val userConcern: String?,
    
    @SerializedName("is_guest")
    val isGuest: Int,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("last_message")
    val lastMessage: String?,
    
    @SerializedName("last_message_time")
    val lastMessageTime: String?,
    
    @SerializedName("assigned_to")
    val assignedTo: Int?,
    
    @SerializedName("device_info")
    val deviceInfo: String?,
    
    @SerializedName("ip_address")
    val ipAddress: String?,
    
    @SerializedName("user_agent")
    val userAgent: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Correctly models the top-level JSON response from the conversations API.
 */
data class ConversationResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("data")
    val conversation: Conversation? // Matches the 'data' key used in PHP
)

data class ConversationsListResponse(
    val success: Boolean,
    val message: String,
    val conversations: List<Conversation>
)
