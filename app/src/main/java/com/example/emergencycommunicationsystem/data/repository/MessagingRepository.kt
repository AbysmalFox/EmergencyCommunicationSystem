package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.CreateConversationRequest
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SendMessageRequest

class MessagingRepository {
    private suspend fun apiService(): MessagingApiService = ApiClient.messagingApiService()

    suspend fun createConversation(
        userId: String, 
        userName: String,
        userEmail: String? = null,
        userPhone: String? = null,
        userLocation: String? = null,
        userConcern: String? = null,
        isGuest: Boolean = true
    ): Int {
        val request = CreateConversationRequest(
            user_id = userId,
            user_name = userName,
            user_email = userEmail,
            user_phone = userPhone,
            user_location = userLocation,
            user_concern = userConcern,
            is_guest = if (isGuest) 1 else 0
        )
        val response = apiService().createConversation(request)
        return response.conversation?.conversationId ?: 0
    }

    suspend fun sendMessage(
        conversationId: Int, 
        senderId: String, 
        senderName: String,
        senderType: String,
        messageText: String
    ): Boolean {
        val request = SendMessageRequest(
            conversation_id = conversationId,
            sender_id = senderId,
            sender_name = senderName,
            sender_type = senderType,
            message_text = messageText
        )
        val response = apiService().sendMessage(request)
        return response.success
    }

    suspend fun fetchMessages(conversationId: Int, lastMessageId: Int = 0): List<Message> {
        val response = apiService().fetchMessages(conversationId, lastMessageId)
        if (response.success) {
            return response.messages
        } else {
            throw Exception(response.error ?: "API returned an error while fetching messages.")
        }
    }

    suspend fun listConversations(userId: String? = null, role: String? = null): List<Conversation> {
        val response = apiService().listConversations(userId, role)
        return if (response.success) {
            response.conversations
        } else {
            emptyList()
        }
    }
}
