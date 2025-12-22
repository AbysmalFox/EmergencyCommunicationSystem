package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.network.RetrofitClient
import com.example.emergencycommunicationsystem.network.MessagingApiService

class MessagingRepository {
    private val apiService: MessagingApiService

    init {
        apiService = RetrofitClient.messagingService
    }

    suspend fun createConversation(alertId: Int, userId: Int): Int {
        val response = apiService.createConversation(alertId, userId)
        return response.conversationId
    }

    suspend fun sendMessage(conversationId: Int, senderId: Int, messageText: String): Boolean {
        val response = apiService.sendMessage(conversationId, senderId, messageText)
        return response.success
    }

    suspend fun fetchMessages(conversationId: Int, lastMessageId: Int = 0): List<Message> {
        return apiService.fetchMessages(conversationId, lastMessageId)
    }

    suspend fun listConversations(alertId: Int): List<Conversation> {
        return apiService.listConversations(alertId)
    }
}

