package com.example.emergencycommunicationsystem.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class MessagingViewModel(
    private val alertId: Int,
    val userId: Int, // Public so the screen can access it
    private val messagingRepository: MessagingRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(true)
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

    init {
        initializeConversation()
    }

    private fun initializeConversation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // This now correctly gets a UNIQUE conversation ID from the fixed backend
                val convId = messagingRepository.createConversation(alertId, userId)
                if (convId > 0) {
                    _conversationId.value = convId
                    // FIRST, load the full history for this conversation
                    loadInitialMessages(convId)
                    // THEN, start polling for new messages
                    startPolling(convId)
                    if (alertId != 999 && messages.value.isEmpty()) {
                        handleBotLogic("initial_greeting")
                    }
                } else {
                    throw Exception("Failed to create or retrieve a valid conversation.")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false // This will be set false after initial load
            }
        }
    }

    // NEW FUNCTION to load the full history once.
    private fun loadInitialMessages(conversationId: Int) {
        viewModelScope.launch {
            try {
                // This fetches ALL messages for this specific conversation.
                // Your backend messages/list.php MUST filter by conversation_id.
                val history = messagingRepository.fetchMessages(conversationId, 0) // lastId = 0 fetches all
                _messages.value = history.sortedBy { it.id }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load message history."
            }
        }
    }

    // SIMPLIFIED AND CORRECTED POLLING LOGIC
    private fun startPolling(conversationId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000) // Poll every 3 seconds
                try {
                    // Get the ID of the latest message we have from the server
                    val lastId = _messages.value.filter { it.id > 0 }.maxOfOrNull { it.id } ?: 0
                    val newMessages = messagingRepository.fetchMessages(conversationId, lastId)

                    if (newMessages.isNotEmpty()) {
                        val currentMessages = _messages.value.toMutableList()

                        // A much safer way to merge optimistic and new messages
                        // 1. Remove all optimistic messages
                        currentMessages.removeAll { it.id < 0 }

                        // 2. Add all new messages from the server
                        currentMessages.addAll(newMessages)

                        // 3. Set the new state, ensuring no duplicates and correct order
                        _messages.value = currentMessages.distinctBy { it.id }.sortedBy { it.id }
                    }
                } catch (e: Exception) {
                    // Don't stop polling on a single network failure
                    Log.e("MessagingViewModel", "Polling failed: ${e.message}")
                }
            }
        }
    }


    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun sendMessage(userName: String) {
        val convId = _conversationId.value ?: return
        if (messageInput.value.isBlank()) return

        val tempId = Random.nextInt(Int.MIN_VALUE, 0)
        val text = messageInput.value
        val nonce = UUID.randomUUID().toString()
        val optimisticMessage = Message(
            tempId,
            convId,
            userId,
            userName,
            text,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            nonce = nonce
        )

        _messageInput.value = ""
        _messages.value += optimisticMessage
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            try {
                messagingRepository.sendMessage(convId, userId, text, nonce)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message."
                _messages.value = _messages.value.filterNot { it.id == tempId }
                _messageInput.value = text
            }
        }
    }

    fun onQuickReplyClicked(reply: QuickReply, userName: String) {
        val convId = _conversationId.value ?: return
        val replyText = reply.text ?: return

        val tempId = Random.nextInt(Int.MIN_VALUE, 0)
        val nonce = UUID.randomUUID().toString()
        val optimisticMessage = Message(
            id = tempId,
            conversationId = convId,
            senderId = userId,
            senderName = userName,
            messageText = replyText,
            sentAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            nonce = nonce
        )
        _messages.value += optimisticMessage
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            try {
                messagingRepository.sendMessage(convId, userId, replyText, nonce)
                handleBotLogic(reply.payload)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message."
                _messages.value = _messages.value.filterNot { it.id == tempId }
                _quickReplies.value = getInitialOptions()
            }
        }
    }

    private suspend fun handleBotLogic(payload: String?) {
        payload ?: return

        delay(750)

        val (responseText, newReplies) = getBotResponse(payload)

        val botMessage = Message(
            id = Random.nextInt(Int.MIN_VALUE, 0),
            conversationId = _conversationId.value ?: 0,
            senderId = 0,
            senderName = "Auto-Reply Bot",
            messageText = responseText,
            sentAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        _messages.value += botMessage
        _quickReplies.value = newReplies
    }

    fun updateMessageInput(text: String) { _messageInput.value = text }
    fun clearError() { _errorMessage.value = null }
    override fun onCleared() { super.onCleared(); stopPolling() }

    private fun getBotResponse(payload: String): Pair<String, List<QuickReply>> {
        return when (payload) {
            "initial_greeting" -> "Hello 👋 I am an automated assistant. Please select an option below." to getInitialOptions()
            "disaster_details" -> "This is a Typhoon alert for Signal #2. Strong winds are expected." to getInitialOptions()
            "disaster_time" -> "The alert was issued within the last hour." to getInitialOptions()
            "news_source" -> "Source: National Disaster Risk Reduction and Management Council (NDRRMC)." to getInitialOptions()
            "immediate_assistance" -> "For immediate help, contact the national emergency hotline at 911." to getAssistanceOptions()
            "call_done" -> "Thank you for confirming. How else may I help?" to getInitialOptions()
            "initial" -> "How else can I help you regarding this alert?" to getInitialOptions()
            else -> "Sorry, I can't help with that." to getInitialOptions()
        }
    }
    private fun getInitialOptions() = listOf(
        QuickReply("What is this disaster?", "disaster_details", "🔎"),
        QuickReply("When was this issued?", "disaster_time", "🕒"),
        QuickReply("What is the source?", "news_source", "📰"),
        QuickReply("I need immediate assistance!", "immediate_assistance", "🆘")
    )
    private fun getAssistanceOptions() = listOf(
        QuickReply("Okay, I will call 911.", "call_done"),
        QuickReply("Take me back.", "initial")
    )
}

class MessagingViewModelFactory(
    private val alertId: Int,
    private val userId: Int,
    private val repository: MessagingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagingViewModel::class.java)) {
            return MessagingViewModel(alertId, userId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
