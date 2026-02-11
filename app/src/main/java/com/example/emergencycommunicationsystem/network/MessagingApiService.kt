package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.ConversationResponse
import com.example.emergencycommunicationsystem.data.models.ConversationsListResponse
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.MessageResponse
import com.example.emergencycommunicationsystem.data.models.MessagesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data classes to represent the JSON request bodies
data class CreateConversationRequest(
    val user_id: String,
    val user_name: String,
    val user_email: String? = null,
    val user_phone: String? = null,
    val user_location: String? = null,
    val user_concern: String? = null,
    val is_guest: Int = 1,
    val device_info: String? = null
)

data class SendMessageRequest(
    val conversation_id: Int,
    val sender_id: String,
    val sender_name: String,
    val sender_type: String,
    val message_text: String
)

interface MessagingApiService {
    @POST("conversations/create.php")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): ConversationResponse

    @POST("messages/send.php")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): MessageResponse

    @GET("messages/list.php")
    suspend fun fetchMessages(
        @Query("conversation_id") conversationId: Int,
        @Query("last_message_id") lastMessageId: Int = 0
    ): MessagesResponse

    @GET("conversations/list.php")
    suspend fun listConversations(
        @Query("user_id") userId: String? = null,
        @Query("role") role: String? = null
    ): ConversationsListResponse
}
// Note: listConversations might need a proper response wrapper if it's more than just a list.
// Based on PHP, it returns success=true, message="OK", conversations=[...]
// So a better return type would be a new ConversationsListResponse.
