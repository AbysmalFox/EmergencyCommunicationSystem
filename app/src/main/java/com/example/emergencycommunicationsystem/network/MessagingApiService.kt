package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.ConversationResponse
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.MessageResponse
import com.example.emergencycommunicationsystem.data.models.MessagesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data classes to represent the JSON request bodies
data class CreateConversationRequest(val alert_id: Int, val user_id: Int)
// THE FIX: The parameter name is now "user_id" to match the repository and the backend script.
data class SendMessageRequest(val conversation_id: Int, val user_id: Int, val content: String, val nonce: String?)

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
        @Query("conversation_id") conversationId: Int, // Use "conversation_id" to match PHP
        @Query("last_message_id") lastMessageId: Int = 0
    ): MessagesResponse

    @GET("conversations/list.php")
    suspend fun listConversations(
        @Query("alert_id") alertId: Int
    ): List<Conversation>
}
