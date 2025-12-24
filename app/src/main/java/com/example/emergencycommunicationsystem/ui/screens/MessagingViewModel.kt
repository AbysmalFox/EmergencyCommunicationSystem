package com.example.emergencycommunicationsystem.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
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
            _errorMessage.value = null
            try {
                // Step 1: Create or get the conversation ID
                val convId = messagingRepository.createConversation(alertId, userId)

                if (convId > 0) {
                    _conversationId.value = convId
                    // Step 2: ONLY if we have a valid ID, fetch the initial messages.
                    fetchInitialMessages(convId)
                    // Step 3: Start polling for new messages
                    startPolling(convId)
                } else {
                    throw Exception("Failed to retrieve a valid conversation ID from the server.")
                }

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "HTTP Error ${e.code()}: $errorBody"
            } catch (e: IOException) {
                _errorMessage.value = "Network Error: Please check your connection to the server."
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize conversation: ${e.message}"
                _isLoading.value = false // Stop loading on critical failure
            }
        }
    }

    private suspend fun fetchInitialMessages(conversationId: Int) {
        try {
            val initialMessages = messagingRepository.fetchMessages(conversationId)
            _messages.value = initialMessages
            if (initialMessages.isNotEmpty()) {
                lastMessageId = initialMessages.maxOf { it.id }
            }
        } catch (e: Exception) {
            // The error from this will be caught by the outer try-catch block
            // in initializeConversation, so we just re-throw it.
            throw e
        } finally {
            _isLoading.value = false // End loading after the first fetch
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
                    // Network error - continue polling silently
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

        val convId = _conversationId.value
        if (convId == null || convId <= 0) {
            _errorMessage.value = "Cannot send message: Invalid conversation ID."
            return
        }

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
                    // Fetch messages immediately after sending to get the latest state
                    delay(500) // Give the server a moment to process
                    val updatedMessages = messagingRepository.fetchMessages(convId, lastMessageId)
                     if (updatedMessages.isNotEmpty()) {
                        val fullMessageList = (_messages.value + updatedMessages).distinctBy { it.id }.sortedBy { it.id }
                        _messages.value = fullMessageList
                        lastMessageId = fullMessageList.maxOf { it.id }
                    }
                } else {
                    _errorMessage.value = "Failed to send message. Server reported failure."
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Send Error ${e.code()}: $errorBody"
            } catch (e: IOException) {
                _errorMessage.value = "Network error while sending. Check connection."
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
