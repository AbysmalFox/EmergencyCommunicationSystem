package com.example.emergencycommunicationsystem.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.QuickReply
import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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

    private val _quickReplies = MutableStateFlow<List<QuickReply>>(emptyList())
    val quickReplies: StateFlow<List<QuickReply>> = _quickReplies.asStateFlow()

    private var pollingJob: Job? = null // 1. Add a Job property

    fun initializeConversation(alertId: Int, userId: Int) {
        // Stop any previous polling before starting a new one
        stopPolling()

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val convId = messagingRepository.createConversation(alertId, userId)

                if (convId > 0) {
                    _conversationId.value = convId
                    // Start polling for messages
                    startPolling(convId)
                    _quickReplies.value = getInitialOptions()
                } else {
                    throw Exception("Failed to retrieve a valid conversation ID.")
                }

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "HTTP Error ${e.code()}: $errorBody"
                _isLoading.value = false // Ensure loading stops on error
            } catch (e: IOException) {
                _errorMessage.value = "Network Error: Please check your connection to the server."
                _isLoading.value = false // Ensure loading stops on error
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize conversation: ${e.message}"
                _isLoading.value = false // Ensure loading stops on critical failure
            }
        }
    }

    private fun startPolling(conversationId: Int) {
        pollingJob = viewModelScope.launch {
            var isInitialFetch = true
            while (isActive) { // 2. Loop only while the coroutine is active
                try {
                    val lastMessageId = if (isInitialFetch) 0 else _messages.value.lastOrNull()?.id ?: 0
                    val fetchedMessages = messagingRepository.fetchMessages(conversationId, lastMessageId)

                    if (fetchedMessages.isNotEmpty()) {
                        if (isInitialFetch) {
                            _messages.value = fetchedMessages
                        } else {
                            _messages.value = (_messages.value + fetchedMessages).distinctBy { it.id }
                        }
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load messages: ${e.message}"
                    // Optional: stop polling on error
                    stopPolling()
                } finally {
                    if(isInitialFetch) {
                        _isLoading.value = false // Stop loading indicator after first fetch
                        isInitialFetch = false
                    }
                }
                delay(3000) // 3. Wait for 3 seconds before the next poll
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel() // 4. Cancel the existing job
        pollingJob = null
    }

    // 5. This is the most important part for fixing the ANR on back press
    override fun onCleared() {
        super.onCleared()
        stopPolling() // Ensure polling is stopped when the ViewModel is destroyed
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
                    _quickReplies.value = emptyList()
                    // The polling loop will fetch the sent message automatically
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

    fun onQuickReplyClicked(reply: QuickReply) {
        updateMessageInput(reply.text)
    }

    private fun getInitialOptions() = listOf(
        QuickReply("What is this specific disaster?", "disaster_details"),
        QuickReply("What time was the alert issued?", "disaster_time"),
        QuickReply("Where is this news from?", "news_source"),
        QuickReply("I need immediate assistance!", "immediate_assistance")
    )

    fun updateMessageInput(text: String) {
        _messageInput.value = text
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
