package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.models.Conversation
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.CreateConversationRequest
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SendMessageRequest

class MessagingRepository {
    private val apiService: MessagingApiService
        get() = ApiClient.messagingApiService

    suspend fun createConversation(alertId: Int, userId: Int): Int {
        val request = CreateConversationRequest(alert_id = alertId, user_id = userId)
        val response = apiService.createConversation(request)
        // Correctly get the ID from the nested conversation object, or return 0 on failure.
        return response.conversation?.id ?: 0
    }

    suspend fun sendMessage(conversationId: Int, userId: Int, messageText: String, nonce: String): Boolean {
        // THE FIX: The key for the user's ID must be "user_id" to match the backend script.
        val request = SendMessageRequest(conversation_id = conversationId, user_id = userId, content = messageText, nonce = nonce)
        val response = apiService.sendMessage(request)
        return response.success
    }

    suspend fun fetchMessages(conversationId: Int, lastMessageId: Int = 0): List<Message> {
        // The API now returns a MessagesResponse object.
        val response = apiService.fetchMessages(conversationId, lastMessageId)
        if (response.success) {
            // We extract the list of messages from the response.
            return response.messages
        } else {
            // If the API reports an error, throw an exception.
            throw Exception(response.error ?: "API returned an error while fetching messages.")
        }
    }

    suspend fun listConversations(alertId: Int): List<Conversation> {
        return apiService.listConversations(alertId)
    }
}
