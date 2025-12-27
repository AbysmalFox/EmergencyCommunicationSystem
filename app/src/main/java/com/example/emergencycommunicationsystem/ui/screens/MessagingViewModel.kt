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
                    val lastMessageId = if (isInitialFetch) 0 else _messages.value.lastOrNull()?.id ?: 0
                    val fetchedMessages = messagingRepository.fetchMessages(conversationId, lastMessageId)
                    if (fetchedMessages.isNotEmpty()) {
                        _messages.value = (_messages.value + fetchedMessages).distinctBy { it.id }
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

    // For manually typed messages
    fun sendMessage(senderId: Int) {
        if (_messageInput.value.isBlank()) return
        val convId = _conversationId.value ?: return

        viewModelScope.launch {
            _isSending.value = true
            _quickReplies.value = emptyList() // Clear replies on manual send
            try {
                messagingRepository.sendMessage(convId, senderId, _messageInput.value)
                _messageInput.value = ""
            } catch (e: Exception) {
                _errorMessage.value = "Error sending message: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    // For quick reply clicks
    fun onQuickReplyClicked(reply: QuickReply, userId: Int) {
        viewModelScope.launch {
            _isSending.value = true

            val convId = _conversationId.value ?: return@launch
            try {
                // 1. Send the user's message
                val success = messagingRepository.sendMessage(convId, userId, reply.text)

                if (success) {
                    // 2. Wait for polling to pick up the message, then show bot response
                    delay(1200) // Simulate bot 'thinking'
                    val botResponse = getBotResponse(reply.payload)
                    addBotMessage(botResponse.first)
                    _quickReplies.value = botResponse.second // 3. Show new replies
                } else {
                    _errorMessage.value = "Failed to send reply."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error sending message: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
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

    private suspend fun addBotMessage(text: String) {
        delay(500) // Short delay to make the bot response feel more natural
        val botMessage = Message(
            id = System.currentTimeMillis().toInt(), // Use timestamp for a unique-enough temporary ID
            conversationId = _conversationId.value ?: 0,
            senderId = 0, // 0 for bot
            senderName = "Auto-Reply Bot",
            messageText = text,
            sentAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        _messages.value = _messages.value + botMessage
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
