package com.example.emergencycommunicationsystem.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MessagingViewModel(
    private val messagingRepository: MessagingRepository = MessagingRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _conversationId = MutableStateFlow<Int?>(null)
    val conversationId: StateFlow<Int?> = _conversationId

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private var lastMessageId = 0
    private var isPolling = false

    fun initializeConversation(alertId: Int, userId: Int) {
        if (_conversationId.value != null) return // Already initialized

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val convId = messagingRepository.createConversation(alertId, userId)
                _conversationId.value = convId
                // Fetch initial messages
                val initialMessages = messagingRepository.fetchMessages(convId)
                _messages.value = initialMessages
                if (initialMessages.isNotEmpty()) {
                    lastMessageId = initialMessages.maxOf { it.id }
                }
                // Start polling
                startPolling(convId)
            } catch (e: IOException) {
                _errorMessage.value = "Network error. Please check your connection."
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize conversation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startPolling(conversationId: Int) {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            while (isPolling) {
                try {
                    delay(4000) // Poll every 4 seconds
                    val newMessages = messagingRepository.fetchMessages(conversationId, lastMessageId)
                    if (newMessages.isNotEmpty()) {
                        val updatedMessages = (_messages.value + newMessages).distinctBy { it.id }.sortedBy { it.id }
                        _messages.value = updatedMessages
                        lastMessageId = updatedMessages.maxOf { it.id }
                    }
                } catch (e: IOException) {
                    // Network error - continue polling
                } catch (e: Exception) {
                    // Silently continue polling on other errors
                }
            }
        }
    }

    fun sendMessage(senderId: Int) {
        if (_messageInput.value.isBlank()) {
            _errorMessage.value = "Message cannot be empty"
            return
        }

        val convId = _conversationId.value ?: return

        viewModelScope.launch {
            _isSending.value = true
            try {
                val success = messagingRepository.sendMessage(
                    convId,
                    senderId,
                    _messageInput.value
                )
                if (success) {
                    _messageInput.value = ""
                    _errorMessage.value = null
                    // Fetch messages immediately after sending
                    delay(500)
                    val updatedMessages = messagingRepository.fetchMessages(convId)
                    _messages.value = updatedMessages
                    if (updatedMessages.isNotEmpty()) {
                        lastMessageId = updatedMessages.maxOf { it.id }
                    }
                } else {
                    _errorMessage.value = "Failed to send message"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error while sending"
            } catch (e: Exception) {
                _errorMessage.value = "Error sending message: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    fun updateMessageInput(text: String) {
        _messageInput.value = text
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false
    }
}

