package com.example.emergencycommunicationsystem.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.QuickReply
import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

sealed class NavigationRequest {
    object ToPersistentChat : NavigationRequest()
    object ToEmergencyContacts : NavigationRequest()
}

class MessagingViewModel(
    private val alertId: Int, // The ViewModel already receives this
    val userId: Int, // Public so the screen can access it
    private val messagingRepository: MessagingRepository,
    private val alertTitle: String // Add alertTitle to use in bot messages
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

    private val _navigationChannel = Channel<NavigationRequest>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    // THIS IS THE KEY: A flag to determine the mode.
    private val isTemporaryChat = (alertId != 999) // 999 is persistent, all others are temporary

    init {
        if (isTemporaryChat) {
            // Start the temporary chatbot session
            initializeTemporaryChat()
        } else {
            // Start the existing persistent chat session
            initializePersistentConversation()
        }
    }

    // --- Temporary Chat Logic ---
    private fun initializeTemporaryChat() {
        _isLoading.value = false
        // Start the conversation with a dynamic bot greeting
        val initialBotMessage = createBotMessage(
            "Hello 👋 I am an automated assistant for the '$alertTitle' alert. Please select an option below."
        )
        _messages.value = listOf(initialBotMessage)
        _quickReplies.value = getTemporaryInitialOptions()
    }

    fun onTemporaryQuickReplyClicked(reply: QuickReply) {
        val text = reply.text ?: return

        // Handle special navigation cases
        when (reply.payload) {
            "contact_responder" -> {
                viewModelScope.launch { _navigationChannel.send(NavigationRequest.ToPersistentChat) }
                val userMessage = createUserMessage(text)
                _messages.value += userMessage
                _quickReplies.value = emptyList()
                viewModelScope.launch {
                    delay(600)
                    _messages.value += createBotMessage("You are being connected to a live responder.")
                }
                return
            }
            "emergency_contacts" -> {
                viewModelScope.launch { _navigationChannel.send(NavigationRequest.ToEmergencyContacts) }
                val userMessage = createUserMessage(text)
                _messages.value += userMessage
                _quickReplies.value = emptyList()
                viewModelScope.launch {
                    delay(600)
                    _messages.value += createBotMessage("Navigating to emergency contacts.")
                }
                return
            }
        }

        // For regular replies, just send the message.
        sendTemporaryMessage(text)
    }

    fun sendTemporaryMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add the user's message to the UI
        val userMessage = createUserMessage(text)
        _messages.value += userMessage
        _messageInput.value = ""
        _quickReplies.value = emptyList() // Hide replies while bot is "thinking"

        // 2. Add the bot's response after a short delay
        viewModelScope.launch {
            delay(600) // Natural delay
            val botResponse = getTemporaryBotResponse(text)
            _messages.value += createBotMessage(botResponse)
            _quickReplies.value = getTemporaryInitialOptions() // Restore the options
        }
    }

    private fun getTemporaryBotResponse(userMessage: String): String {
        return when {
            "disaster" in userMessage.lowercase() -> "This is a Level 3 disaster alert regarding '$alertTitle'."
            "issued" in userMessage.lowercase() -> "This alert was issued recently. Please check official channels for precise timing."
            "source" in userMessage.lowercase() -> "The source for this type of alert is usually the local government or a national agency."
            "assistance" in userMessage.lowercase() -> "If you need immediate assistance, please use the 'EMERGENCY CALL' button on the main dashboard."
            else -> "I can only provide basic information. For more details, please contact emergency services."
        }
    }

    private fun getTemporaryInitialOptions() = listOf(
        QuickReply("What is this disaster?", "disaster", "🔎"),
        QuickReply("When was this issued?", "issued", "🕒"),
        QuickReply("What is the source?", "source", "📰"),
        QuickReply("I need assistance", "assistance", "🆘"),
        QuickReply("Contact a responder", "contact_responder", "💬"),
        QuickReply("Go to Emergency Contacts", "emergency_contacts", "📞")
    )

    // Helper functions to create messages in memory
    private fun createBotMessage(text: String) = Message(-Random.nextInt(), 0, 0, "Auto-Reply Bot", text, getCurrentTimestamp(), null)
    private fun createUserMessage(text: String) = Message(-Random.nextInt(), 0, userId, "You", text, getCurrentTimestamp(), null)
    private fun getCurrentTimestamp(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())


    // --- Persistent Chat Logic (Your Existing Code) ---
    private fun initializePersistentConversation() {
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

    fun sendPersistentMessage(userName: String) {
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

    fun onPersistentQuickReplyClicked(reply: QuickReply, userName: String) {
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
    private val alertTitle: String, // <-- ADD THIS
    private val repository: MessagingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagingViewModel::class.java)) {
            return MessagingViewModel(
                alertId = alertId,
                userId = userId,
                alertTitle = alertTitle, // <-- PASS IT HERE
                messagingRepository = repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
