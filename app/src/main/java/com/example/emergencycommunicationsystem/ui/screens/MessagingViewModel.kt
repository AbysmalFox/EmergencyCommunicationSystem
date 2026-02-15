package com.example.emergencycommunicationsystem.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.QuickReply
import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import com.example.emergencycommunicationsystem.util.TranslationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
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
    private val alertId: Int,
    val userId: String,
    private val userName: String,
    private val userEmail: String? = null,
    private val userPhone: String? = null,
    private val messagingRepository: MessagingRepository,
    private val alertTitle: String,
    private val currentLanguage: String = "en"
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _conversationId = MutableStateFlow<Int?>(null)

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _quickReplies = MutableStateFlow<List<QuickReply>>(emptyList())
    val quickReplies: StateFlow<List<QuickReply>> = _quickReplies.asStateFlow()

    private var pollingJob: Job? = null

    private val _navigationChannel = Channel<NavigationRequest>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    private val isTemporaryChat = (alertId != 999) || (userId.isEmpty() || userId == "0")

    init {
        if (isTemporaryChat) {
            initializeTemporaryChat()
        } else {
            initializePersistentConversation()
        }
    }

    // --- Temporary Chat Logic ---
    private fun initializeTemporaryChat() {
        _isLoading.value = false
        viewModelScope.launch {
            val greeting = "Hello 👋 I am an automated assistant for the '$alertTitle' alert. Please select an option below."
            val translatedGreeting = TranslationService.translate(greeting, currentLanguage)
            val initialBotMessage = createBotMessage(translatedGreeting)
            _messages.value = listOf(initialBotMessage)
            _quickReplies.value = getTemporaryInitialOptions()
        }
    }

    fun onTemporaryQuickReplyClicked(reply: QuickReply) {
        val text = reply.text ?: return
        val payload = reply.payload

        when (payload) {
            "contact_responder" -> {
                viewModelScope.launch { 
                    _navigationChannel.send(NavigationRequest.ToPersistentChat) 
                    val userMessage = createUserMessage(text, "User")
                    _messages.value += userMessage
                    _quickReplies.value = emptyList()
                    delay(600)
                    val msg = TranslationService.translate("You are being connected to a live responder.", currentLanguage)
                    _messages.value += createBotMessage(msg)
                }
                return
            }
            "emergency_contacts" -> {
                viewModelScope.launch { 
                    _navigationChannel.send(NavigationRequest.ToEmergencyContacts) 
                    val userMessage = createUserMessage(text, "User")
                    _messages.value += userMessage
                    _quickReplies.value = emptyList()
                    delay(600)
                    val msg = TranslationService.translate("Navigating to emergency contacts.", currentLanguage)
                    _messages.value += createBotMessage(msg)
                }
                return
            }
        }

        sendTemporaryMessage(text)
    }

    fun sendTemporaryMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = createUserMessage(text, "User")
        _messages.value += userMessage
        _messageInput.value = ""
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            delay(600)
            val botResponse = getTemporaryBotResponse(text)
            _messages.value += createBotMessage(botResponse)
            _quickReplies.value = getTemporaryInitialOptions()
        }
    }

    private suspend fun getTemporaryBotResponse(userMessage: String): String {
        val englishMsg = TranslationService.translate(userMessage, "en", currentLanguage).lowercase()

        val response = when {
            "what to do" in englishMsg -> getActionableAdvice(alertTitle)
            "disaster" in englishMsg -> "This is a Level 3 disaster alert regarding '$alertTitle'."
            "issued" in englishMsg -> "This alert was issued recently. Please check official channels for precise timing."
            "source" in englishMsg -> "The source for this type of alert is usually the local government or a national agency."
            "assistance" in englishMsg -> "If you need immediate assistance, please use the 'EMERGENCY CALL' button on the main dashboard."
            else -> "I can only provide basic information. For more details, please contact emergency services."
        }
        
        return TranslationService.translate(response, currentLanguage)
    }

    private suspend fun getActionableAdvice(title: String): String {
        val t = title.lowercase()
        val advice = when {
            "flood" in t -> "Move to higher ground immediately. Do not walk or drive through flood waters. Disconnect electrical appliances."
            "fire" in t -> "Evacuate immediately. Stay low to avoid smoke. Do not use elevators. Call the fire department."
            "earthquake" in t -> "Drop, Cover, and Hold On. Stay away from windows. If outside, find a clear spot away from buildings."
            "typhoon" in t || "storm" in t -> "Stay indoors. Secure windows and doors. Prepare an emergency kit with food and water."
            "volcanic" in t -> "Protect your eyes and skin from ash. Stay indoors with windows closed. Wear a mask."
            "tsunami" in t -> "Move to high ground or inland immediately. Do not return to the coast until authorities say it is safe."
            "heat" in t -> "Stay hydrated. Avoid direct sunlight. Wear loose clothing. Check on vulnerable individuals."
            "health" in t -> "Follow medical advice. Isolate if necessary. Wash hands frequently. Seek medical help if symptoms worsen."
            else -> "Stay calm. Follow instructions from local authorities. Keep your emergency kit ready and monitor official news."
        }
        return advice
    }

    private suspend fun getTemporaryInitialOptions(): List<QuickReply> {
        val options = listOf(
            QuickReply("What to do?", "what_to_do", "📋"),
            QuickReply("What is this disaster?", "disaster", "🔎"),
            QuickReply("When was this issued?", "disaster_time", "🕒"),
            QuickReply("What is the source?", "source", "📰"),
            QuickReply("I need assistance", "assistance", "🆘"),
            QuickReply("Contact a responder", "contact_responder", "💬"),
            QuickReply("Go to Emergency Contacts", "emergency_contacts", "📞")
        )
        return options.map { 
            it.copy(text = TranslationService.translate(it.text ?: "", currentLanguage))
        }
    }

    private fun createBotMessage(text: String) = Message(
        messageId = -Random.nextInt(1, 1000000),
        conversationId = _conversationId.value ?: 0,
        senderId = "0",
        senderName = "Auto-Reply Bot",
        senderType = "admin",
        messageText = text,
        ipAddress = null,
        deviceInfo = null,
        isRead = 1,
        createdAt = getCurrentTimestamp()
    )

    private fun createUserMessage(text: String, userName: String) = Message(
        messageId = -Random.nextInt(1, 1000000),
        conversationId = _conversationId.value ?: 0,
        senderId = userId,
        senderName = userName,
        senderType = "user",
        messageText = text,
        ipAddress = null,
        deviceInfo = null,
        isRead = 1,
        createdAt = getCurrentTimestamp()
    )

    private fun getCurrentTimestamp(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // --- Persistent Chat Logic ---
    private fun initializePersistentConversation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val deviceInfo = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                Log.d("MessagingViewModel", "Initializing persistent chat: userId=$userId, userName=$userName, email=$userEmail")
                val convId = messagingRepository.createConversation(
                    userId = userId,
                    userName = userName,
                    userEmail = userEmail,
                    userPhone = userPhone,
                    userConcern = alertTitle,
                    isGuest = userId.startsWith("guest"),
                    deviceInfo = deviceInfo
                )
                if (convId > 0) {
                    _conversationId.value = convId
                    loadInitialMessages(convId)
                    startPolling(convId)
                    _quickReplies.value = getInitialOptions()
                } else {
                    throw Exception("Failed to create or retrieve a valid conversation.")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadInitialMessages(conversationId: Int) {
        viewModelScope.launch {
            try {
                val history = messagingRepository.fetchMessages(conversationId, 0)
                _messages.value = history.sortedBy { it.messageId }
            } catch (_: Exception) {
                _errorMessage.value = "Failed to load message history."
            }
        }
    }

    private fun startPolling(conversationId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                try {
                    val lastId = _messages.value.filter { it.messageId > 0 }.maxOfOrNull { it.messageId } ?: 0
                    val newMessages = messagingRepository.fetchMessages(conversationId, lastId)

                    if (newMessages.isNotEmpty()) {
                        val currentMessages = _messages.value.toMutableList()
                        currentMessages.removeAll { it.messageId < 0 } // Remove optimistic messages
                        currentMessages.addAll(newMessages)
                        _messages.value = currentMessages.distinctBy { it.messageId }.sortedBy { it.messageId }
                    }
                } catch (e: Exception) {
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

        val text = messageInput.value
        val optimisticMessage = createUserMessage(text, userName)
        val tempId = optimisticMessage.messageId

        _messageInput.value = ""
        _messages.value += optimisticMessage
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            try {
                _isSending.value = true
                Log.d("MessagingViewModel", "Sending to API: convId=$convId, senderId=$userId, name=$userName, text=$text")
                val success = messagingRepository.sendMessage(
                    conversationId = convId,
                    senderId = userId,
                    senderName = userName,
                    senderType = "user",
                    messageText = text
                )
                if (!success) throw Exception("Failed to send")
            } catch (_: Exception) {
                _errorMessage.value = "Failed to send message."
                _messages.value = _messages.value.filterNot { it.messageId == tempId }
                _messageInput.value = text
            } finally {
                _isSending.value = false
            }
        }
    }

    fun sendImageMessage(context: Context, uri: Uri, userName: String) {
        val convId = _conversationId.value ?: return
        
        viewModelScope.launch {
            try {
                _isSending.value = true
                
                // 1. Create a local copy of the image to send as a file
                val file = getFileFromUri(context, uri)
                if (file == null) {
                    _errorMessage.value = "Failed to process image."
                    return@launch
                }
                
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                Log.d("MessagingViewModel", "Sending image to API: convId=$convId, senderId=$userId, name=$userName")
                
                val success = messagingRepository.sendImageMessage(
                    conversationId = convId,
                    senderId = userId,
                    senderName = userName,
                    senderType = "user",
                    imagePart = imagePart
                )
                
                if (!success) throw Exception("Failed to send image")
                
                // Refresh messages after sending
                loadInitialMessages(convId)
                
            } catch (e: Exception) {
                Log.e("MessagingViewModel", "Image send failed", e)
                _errorMessage.value = "Failed to send image: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e("MessagingViewModel", "Error creating file from URI", e)
            null
        }
    }

    fun onPersistentQuickReplyClicked(reply: QuickReply, userName: String) {
        val convId = _conversationId.value ?: return
        val replyText = reply.text ?: return
        val payload = reply.payload

        val optimisticMessage = createUserMessage(replyText, userName)
        val tempId = optimisticMessage.messageId
        
        _messages.value += optimisticMessage
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            try {
                messagingRepository.sendMessage(
                    conversationId = convId,
                    senderId = userId,
                    senderName = userName,
                    senderType = "user",
                    messageText = replyText
                )
                handleBotLogic(payload)
            } catch (_: Exception) {
                _errorMessage.value = "Failed to send message."
                _messages.value = _messages.value.filterNot { it.messageId == tempId }
                _quickReplies.value = getInitialOptions()
            }
        }
    }

    private suspend fun handleBotLogic(payload: String?) {
        payload ?: return
        delay(750)

        val (responseText, newReplies) = getBotResponse(payload)
        val translatedResponse = TranslationService.translate(responseText, currentLanguage)
        val botMessage = createBotMessage(translatedResponse)

        _messages.value += botMessage
        _quickReplies.value = newReplies
    }

    fun updateMessageInput(text: String) { _messageInput.value = text }
    fun clearError() { _errorMessage.value = null }
    override fun onCleared() { super.onCleared(); stopPolling() }

    private suspend fun getBotResponse(payload: String): Pair<String, List<QuickReply>> {
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
    
    private suspend fun getInitialOptions(): List<QuickReply> {
        val options = listOf(
            QuickReply("What is this disaster?", "disaster_details", "🔎"),
            QuickReply("When was this issued?", "disaster_time", "🕒"),
            QuickReply("What is the source?", "news_source", "📰"),
            QuickReply("I need immediate assistance!", "immediate_assistance", "🆘")
        )
        return options.map { 
            it.copy(text = TranslationService.translate(it.text ?: "", currentLanguage))
        }
    }
    
    private suspend fun getAssistanceOptions(): List<QuickReply> {
        val options = listOf(
            QuickReply("Okay, I will call 911.", "call_done"),
            QuickReply("Take me back.", "initial")
        )
        return options.map { 
            it.copy(text = TranslationService.translate(it.text ?: "", currentLanguage))
        }
    }
}

class MessagingViewModelFactory(
    private val alertId: Int,
    private val userId: String,
    private val userName: String,
    private val userEmail: String? = null,
    private val userPhone: String? = null,
    private val alertTitle: String,
    private val repository: MessagingRepository,
    private val currentLanguage: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagingViewModel::class.java)) {
            return MessagingViewModel(
                alertId = alertId,
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                userPhone = userPhone,
                alertTitle = alertTitle,
                messagingRepository = repository,
                currentLanguage = currentLanguage
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
