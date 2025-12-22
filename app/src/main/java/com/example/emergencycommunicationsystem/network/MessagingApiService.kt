package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.ConversationResponse
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.MessageResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessagingApiService {
    @FormUrlEncoded
    @POST("conversations/create")
    suspend fun createConversation(
        @Field("alert_id") alertId: Int,
        @Field("user_id") userId: Int
    ): ConversationResponse

    @FormUrlEncoded
    @POST("messages/send")
    suspend fun sendMessage(
        @Field("conversation_id") conversationId: Int,
        @Field("sender_id") senderId: Int,
        @Field("message") message: String
    ): MessageResponse

    @GET("messages/list")
    suspend fun fetchMessages(
        @Query("conversation_id") conversationId: Int,
        @Query("last_message_id") lastMessageId: Int = 0
    ): List<Message>

    @GET("conversations/list")
    suspend fun listConversations(
        @Query("alert_id") alertId: Int
    ): List<Conversation>
}

