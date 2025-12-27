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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private var pollingJob: Job? = null

    fun initializeConversation(alertId: Int, userId: Int) {
        stopPolling()
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val convId = messagingRepository.createConversation(alertId, userId)
                if (convId > 0) {
                    _conversationId.value = convId
                    startPolling(convId)
                    addBotMessage("Hello 👋 I am an automated assistant. Please select one of the options below so I can assist you regarding the current alert.")
                    _quickReplies.value = getInitialOptions()
                } else {
                    throw Exception("Failed to retrieve a valid conversation ID.")
                }
            } catch (e: HttpException) {
                _errorMessage.value = "HTTP Error ${e.code()}: ${e.response()?.errorBody()?.string()}"
                _isLoading.value = false
            } catch (e: IOException) {
                _errorMessage.value = "Network Error: Please check your connection to the server."
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize conversation: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun startPolling(conversationId: Int) {
        pollingJob = viewModelScope.launch {
            var isInitialFetch = true
            while (isActive) {
                try {
                    val lastMessageId = _messages.value.filter { it.id > 0 }.lastOrNull()?.id ?: 0
                    val fetchedMessages = messagingRepository.fetchMessages(conversationId, lastMessageId)
                    if (fetchedMessages.isNotEmpty()) {
                        val currentIds = _messages.value.map { it.id }.toSet()
                        val newMessages = fetchedMessages.filterNot { currentIds.contains(it.id) }
                        if (newMessages.isNotEmpty()) {
                            _messages.value = (_messages.value + newMessages).sortedBy { it.sentAt }
                        }
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load messages: ${e.message}"
                    stopPolling()
                } finally {
                    if (isInitialFetch) {
                        _isLoading.value = false
                        isInitialFetch = false
                    }
                }
                delay(3000)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    fun sendMessage(senderId: Int, userName: String) {
        if (_messageInput.value.isBlank()) return
        val convId = _conversationId.value ?: return
        val messageText = _messageInput.value
        _messageInput.value = "" // Clear input field immediately

        // 1. Optimistically add user's message
        val optimisticMessage = Message(
            id = System.currentTimeMillis().toInt(),
            conversationId = convId,
            senderId = senderId,
            senderName = userName,
            messageText = messageText,
            sentAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        _messages.value = _messages.value + optimisticMessage
        _quickReplies.value = emptyList() // Clear replies on manual send

        // 2. Send the message to the server
        viewModelScope.launch {
            _isSending.value = true
            try {
                messagingRepository.sendMessage(convId, senderId, messageText)
                // Polling will handle updating the message list from the server
            } catch (e: Exception) {
                _errorMessage.value = "Error sending message: ${e.message}"
                // On failure, remove the optimistic message and restore the input
                _messages.value = _messages.value.filterNot { it.id == optimisticMessage.id }
                _messageInput.value = messageText
            } finally {
                _isSending.value = false
            }
        }
    }

    fun onQuickReplyClicked(reply: QuickReply, userId: Int, userName: String) {
        val convId = _conversationId.value ?: return

        // 1. Optimistically add user's message to the UI
        val optimisticMessage = Message(
            id = System.currentTimeMillis().toInt(), // Temporary ID
            conversationId = convId,
            senderId = userId,
            senderName = userName,
            messageText = reply.text,
            sentAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        _messages.value = _messages.value + optimisticMessage
        _quickReplies.value = emptyList() // Hide replies immediately

        // 2. Send the message to the server and handle bot response
        viewModelScope.launch {
            _isSending.value = true
            try {
                val success = messagingRepository.sendMessage(convId, userId, reply.text)
                if (success) {
                    // Polling will replace the temporary message with the real one.
                    // Now, get and show the bot's response.
                    val botResponse = getBotResponse(reply.payload)
                    addBotMessage(botResponse.first)
                    _quickReplies.value = botResponse.second // Show new replies
                } else {
                    _errorMessage.value = "Failed to send reply."
                    _messages.value = _messages.value.filterNot { it.id == optimisticMessage.id } // Remove optimistic message
                    _quickReplies.value = getInitialOptions() // Restore options
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error sending message: ${e.message}"
                _messages.value = _messages.value.filterNot { it.id == optimisticMessage.id }
                _quickReplies.value = getInitialOptions()
            } finally {
                _isSending.value = false
            }
        }
    }

    private suspend fun addBotMessage(text: String) {
        delay(1000) // Delay to simulate bot typing
        val botMessage = Message(
            id = System.currentTimeMillis().toInt(),
            conversationId = _conversationId.value ?: 0,
            senderId = 0, // 0 for bot
            senderName = "Auto-Reply Bot",
            messageText = text,
            sentAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        _messages.value = _messages.value + botMessage
    }

    private fun getBotResponse(payload: String): Pair<String, List<QuickReply>> {
        return when (payload) {
            "disaster_details" -> "This is a Typhoon alert for Signal #2. It means strong winds are expected." to getInitialOptions()
            "disaster_time" -> "The alert was issued within the last hour. For real-time updates, please monitor official news channels." to getInitialOptions()
            "news_source" -> "This information is from the National Disaster Risk Reduction and Management Council (NDRRMC)." to getInitialOptions()
            "immediate_assistance" -> "For immediate help, contact the national emergency hotline at 911." to getAssistanceOptions()
            "call_done" -> "Thank you for confirming. Is there anything else I can assist with?" to getInitialOptions()
            "initial" -> "How else can I help you regarding this alert?" to getInitialOptions()
            else -> "Sorry, I don't have information on that. Please select from the available options." to getInitialOptions()
        }
    }

    private fun getInitialOptions() = listOf(
        QuickReply("🔎 What is this specific disaster?", "disaster_details"),
        QuickReply("🕒 What time was the alert issued?", "disaster_time"),
        QuickReply("📰 Where is this news from?", "news_source"),
        QuickReply("🆘 I need immediate assistance!", "immediate_assistance")
    )

    private fun getAssistanceOptions() = listOf(
        QuickReply("Okay, I will call 911.", "call_done"),
        QuickReply("Take me back to the main menu.", "initial")
    )

    fun updateMessageInput(text: String) {
        _messageInput.value = text
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
