package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.CreateConversationRequest
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SendMessageRequest

class MessagingRepository {
    private suspend fun apiService(): MessagingApiService = ApiClient.messagingApiService()

    suspend fun createConversation(alertId: Int, userId: Int): Int {
        val request = CreateConversationRequest(alert_id = alertId, user_id = userId)
        val response = apiService().createConversation(request)
        return response.conversation?.id ?: 0
    }

    suspend fun sendMessage(conversationId: Int, userId: Int, messageText: String, nonce: String): Boolean {
        val request = SendMessageRequest(conversation_id = conversationId, user_id = userId, content = messageText, nonce = nonce)
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

    suspend fun listConversations(alertId: Int): List<Conversation> {
        return apiService().listConversations(alertId)
    }
}