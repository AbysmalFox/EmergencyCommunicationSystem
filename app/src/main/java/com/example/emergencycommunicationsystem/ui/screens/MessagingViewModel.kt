package com.example.emergencycommunicationsystem.ui.screens

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
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
                val convId = messagingRepository.createConversation(alertId, userId)
                if (convId > 0) {
                    _conversationId.value = convId
                    startPolling(convId)
                    if (alertId != 999) {
                        addBotMessage("Hello 👋 I am an automated assistant. Please select an option below.")
                        _quickReplies.value = getInitialOptions()
                    }
                } else {
                    throw Exception("Failed to create conversation.")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startPolling(conversationId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val lastId = _messages.value.filter { it.id > 0 }.lastOrNull()?.id ?: 0
                    val newMessages = messagingRepository.fetchMessages(conversationId, lastId)
                    if (newMessages.isNotEmpty()) {
                        val currentIds = _messages.value.map { it.id }.toSet()
                        _messages.value += newMessages.filterNot { currentIds.contains(it.id) }
                    }
                } catch (e: Exception) {
                    stopPolling()
                }
                delay(3000)
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
        val optimisticMessage = Message(tempId, convId, userId, userName, text, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        _messageInput.value = ""
        _messages.value += optimisticMessage
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            try {
                messagingRepository.sendMessage(convId, userId, text)
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
        val replyPayload = reply.payload ?: return

        val tempId = Random.nextInt(Int.MIN_VALUE, 0)
        val optimisticMessage = Message(tempId, convId, userId, userName, replyText, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        _messages.value += optimisticMessage
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            try {
                val success = messagingRepository.sendMessage(convId, userId, replyText)
                if (success) {
                    val (responseText, newReplies) = getBotResponse(replyPayload)
                    addBotMessage(responseText)
                    _quickReplies.value = newReplies
                } else {
                     throw Exception("API returned false")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process reply."
                _messages.value = _messages.value.filterNot { it.id == tempId }
                _quickReplies.value = getInitialOptions()
            }
        }
    }

    private suspend fun addBotMessage(text: String) {
        delay(500) 
        val botMessage = Message(Random.nextInt(Int.MIN_VALUE, 0), _conversationId.value ?: 0, 0, "Auto-Reply Bot", text, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        _messages.value += botMessage
    }

    fun updateMessageInput(text: String) { _messageInput.value = text }
    fun clearError() { _errorMessage.value = null }
    override fun onCleared() { super.onCleared(); stopPolling() }
    
    private fun getBotResponse(payload: String): Pair<String, List<QuickReply>> {
        return when (payload) {
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
        QuickReply("🔎 What is this disaster?", "disaster_details"),
        QuickReply("🕒 When was this issued?", "disaster_time"),
        QuickReply("📰 What is the source?", "news_source"),
        QuickReply("🆘 I need immediate assistance!", "immediate_assistance")
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
