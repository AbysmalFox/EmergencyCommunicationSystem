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
            val initialBotMessage = createBotMessage(chatbotText("greeting", alertTitle))
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
                    _messages.value += createBotMessage(chatbotText("connected"))
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
                    _messages.value += createBotMessage(chatbotText("navigating"))
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
            "disaster" in englishMsg -> chatbotText("disaster_level", alertTitle)
            "issued" in englishMsg -> chatbotText("issued")
            "source" in englishMsg -> chatbotText("source")
            "assistance" in englishMsg -> chatbotText("immediate")
            else -> chatbotText("basic")
        }
        return response
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
        return listOf(
            QuickReply(chatbotText("qr_what_to_do"), "what_to_do", "📋"),
            QuickReply(chatbotText("qr_disaster"), "disaster", "🔎"),
            QuickReply(chatbotText("qr_issued"), "disaster_time", "🕒"),
            QuickReply(chatbotText("qr_source"), "source", "📰"),
            QuickReply(chatbotText("qr_assistance"), "assistance", "🆘"),
            QuickReply(chatbotText("qr_contact"), "contact_responder", "💬"),
            QuickReply(chatbotText("qr_contacts"), "emergency_contacts", "📞")
        )
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
        val botMessage = createBotMessage(responseText)

        _messages.value += botMessage
        _quickReplies.value = newReplies
    }

    fun updateMessageInput(text: String) { _messageInput.value = text }
    fun clearError() { _errorMessage.value = null }
    override fun onCleared() { super.onCleared(); stopPolling() }

    private suspend fun getBotResponse(payload: String): Pair<String, List<QuickReply>> {
        return when (payload) {
            "initial_greeting" -> chatbotText("greeting_generic") to getInitialOptions()
            "disaster_details" -> chatbotText("persistent_disaster") to getInitialOptions()
            "disaster_time" -> chatbotText("persistent_time") to getInitialOptions()
            "news_source" -> chatbotText("persistent_source") to getInitialOptions()
            "immediate_assistance" -> chatbotText("persistent_help") to getAssistanceOptions()
            "call_done" -> chatbotText("persistent_done") to getInitialOptions()
            "initial" -> chatbotText("persistent_initial") to getInitialOptions()
            else -> chatbotText("persistent_sorry") to getInitialOptions()
        }
    }
    
    private suspend fun getInitialOptions(): List<QuickReply> {
        return listOf(
            QuickReply(chatbotText("qr_disaster"), "disaster_details", "🔎"),
            QuickReply(chatbotText("qr_issued"), "disaster_time", "🕒"),
            QuickReply(chatbotText("qr_source"), "news_source", "📰"),
            QuickReply(chatbotText("qr_immediate"), "immediate_assistance", "🆘")
        )
    }
    
    private suspend fun getAssistanceOptions(): List<QuickReply> {
        return listOf(
            QuickReply(chatbotText("qr_call"), "call_done"),
            QuickReply(chatbotText("qr_back"), "initial")
        )
    }

    private fun chatbotText(key: String, arg: String? = null): String {
        val lang = currentLanguage.lowercase()
        fun t(en: String, fil: String, es: String, bcl: String, ceb: String, war: String, ilo: String): String {
            return when (lang) {
                "fil" -> fil
                "es" -> es
                "bcl" -> bcl
                "ceb" -> ceb
                "war" -> war
                "ilo" -> ilo
                else -> en
            }
        }

        val text = when (key) {
            "greeting" -> t(
                "Hello 👋 I am an automated assistant for the '%s' alert. Please select an option below.",
                "Hello 👋 Ako ay automated assistant para sa '%s' na alert. Pumili ng option sa ibaba.",
                "Hola 👋 Soy un asistente automatizado para la alerta '%s'. Selecciona una opcion abajo.",
                "Hello 👋 Ako an automated assistant para sa '%s' na alert. Paki pili nin opsyon sa ibaba.",
                "Hello 👋 Ako ang automated assistant para sa '%s' nga alert. Palihug pagpili ug kapilian sa ubos.",
                "Hello 👋 Ako an automated assistant para han '%s' nga alert. Palihog pagpili hin opsyon ha ubos.",
                "Hello 👋 Siak ti automated assistant para iti '%s' nga alert. Agpili iti maysa nga opsyon iti baba."
            )
            "connected" -> t(
                "You are being connected to a live responder.",
                "Ikokonekta ka na sa live responder.",
                "Ahora te conectaremos con un respondedor en vivo.",
                "Ikokonekta ka na sa live responder.",
                "I-konekta ka namo sa live responder.",
                "Iko-konek ka na namon ngadto han live responder.",
                "I-konek da ka itan iti live responder."
            )
            "navigating" -> t(
                "Navigating to emergency contacts.",
                "Pumupunta sa emergency contacts.",
                "Navegando a contactos de emergencia.",
                "Padagos sa emergency contacts.",
                "Padulong sa emergency contacts.",
                "Padulong ha emergency contacts.",
                "Mapan iti emergency contacts."
            )
            "disaster_level" -> t(
                "This is a Level 3 disaster alert regarding '%s'.",
                "Ito ay Level 3 disaster alert tungkol sa '%s'.",
                "Esta es una alerta de desastre nivel 3 sobre '%s'.",
                "Ini iyo an Level 3 disaster alert manungod sa '%s'.",
                "Kini usa ka Level 3 disaster alert bahin sa '%s'.",
                "Ini in usa nga Level 3 disaster alert mahitungod han '%s'.",
                "Daytoy ket Level 3 disaster alert maipanggep iti '%s'."
            )
            "issued" -> t(
                "This alert was issued recently. Please check official channels for precise timing.",
                "Kamakailan inilabas ang alert na ito. Tingnan ang official channels para sa eksaktong oras.",
                "Esta alerta se emitio recientemente. Consulta los canales oficiales para la hora exacta.",
                "Bagong labas ini na alert. Tingnan an official channels para sa eksaktong oras.",
                "Bag-o lang ni gi-isyu nga alert. Tan-awa ang official channels para sa tukmang oras.",
                "Bag-o pa ini nga alert nga iginpagawas. Kitaa an official channels para han eksakto nga oras.",
                "Nailuaran laeng daytoy nga alert. Kitaem dagiti official channels para iti eksakto nga oras."
            )
            "source" -> t(
                "The source for this type of alert is usually the local government or a national agency.",
                "Karaniwang pinagmumulan ng ganitong alert ang lokal na pamahalaan o pambansang ahensya.",
                "La fuente de este tipo de alerta suele ser el gobierno local o una agencia nacional.",
                "An source kan ini na klase nin alert kadalasan lokal na gobyerno o nasyonal na ahensya.",
                "Ang source sa ing-ani nga alert kasagaran lokal nga gobyerno o nasyonal nga ahensya.",
                "An source hini nga klase hin alert kasagaran lokal nga gobyerno o nasyonal nga ahensya.",
                "Ti paggapuan ti kasta nga alert kadawyan a lokal a gobyerno wenno nasyonal nga ahensya."
            )
            "immediate" -> t(
                "If you need immediate assistance, please use the 'EMERGENCY CALL' button on the main dashboard.",
                "Kung kailangan mo ng agarang tulong, gamitin ang 'EMERGENCY CALL' sa main dashboard.",
                "Si necesitas ayuda inmediata, usa el boton 'EMERGENCY CALL' en el panel principal.",
                "Kun kaipuhan mo nin apuradong tabang, gamiton an 'EMERGENCY CALL' sa main dashboard.",
                "Kung kinahanglan kag dayon nga tabang, gamita ang 'EMERGENCY CALL' sa main dashboard.",
                "Kun kinahanglan mo dayon hin bulig, gamita an 'EMERGENCY CALL' ha main dashboard.",
                "No masapulmo ti dagus a tulong, usarem ti 'EMERGENCY CALL' iti main dashboard."
            )
            "basic" -> t(
                "I can only provide basic information. For more details, please contact emergency services.",
                "Basic na impormasyon lang ang maibibigay ko. Para sa detalye, makipag-ugnayan sa emergency services.",
                "Solo puedo brindar informacion basica. Para mas detalles, contacta a los servicios de emergencia.",
                "Basic na impormasyon sana an maibibigay ko. Para sa detalye, makipag-ugnayan sa emergency services.",
                "Basic nga impormasyon ra akong mahatag. Para sa detalye, kontaka ang emergency services.",
                "Basic nga impormasyon la an maihahatag ko. Para hin detalye, kumontak ha emergency services.",
                "Basic nga impormasyon laeng ti maitedko. Para iti detalye, makikontak iti emergency services."
            )
            "greeting_generic" -> t(
                "Hello 👋 I am an automated assistant. Please select an option below.",
                "Hello 👋 Ako ay automated assistant. Pumili ng option sa ibaba.",
                "Hola 👋 Soy un asistente automatizado. Selecciona una opcion abajo.",
                "Hello 👋 Ako an automated assistant. Paki pili nin opsyon sa ibaba.",
                "Hello 👋 Ako ang automated assistant. Palihug pagpili ug kapilian sa ubos.",
                "Hello 👋 Ako an automated assistant. Palihog pagpili hin opsyon ha ubos.",
                "Hello 👋 Siak ti automated assistant. Agpili iti maysa nga opsyon iti baba."
            )
            "persistent_disaster" -> t(
                "This is a Typhoon alert for Signal #2. Strong winds are expected.",
                "Ito ay Typhoon alert para sa Signal #2. Inaasahan ang malalakas na hangin.",
                "Esta es una alerta de tifon para Senal #2. Se esperan vientos fuertes.",
                "Ini iyo an Typhoon alert para sa Signal #2. Inaasahan an makusog na hangin.",
                "Kini usa ka Typhoon alert para sa Signal #2. Gipaabot ang kusog nga hangin.",
                "Ini in Typhoon alert para ha Signal #2. Ginlalaoman an makusog nga hangin.",
                "Daytoy ket Typhoon alert para iti Signal #2. Inaasahan ti napigsa nga angin."
            )
            "persistent_time" -> t(
                "The alert was issued within the last hour.",
                "Inilabas ang alert sa nakaraang isang oras.",
                "La alerta se emitio en la ultima hora.",
                "Piggwa an alert sa nagligad na oras.",
                "Ang alert gi-isyu sulod sa miaging usa ka oras.",
                "An alert iginpagawas han naglabay nga usa ka oras.",
                "Nailuaran daytoy nga alert iti napalabas nga maysa nga oras."
            )
            "persistent_source" -> "Source: National Disaster Risk Reduction and Management Council (NDRRMC)."
            "persistent_help" -> t(
                "For immediate help, contact the national emergency hotline at 911.",
                "Para sa agarang tulong, tumawag sa national emergency hotline 911.",
                "Para ayuda inmediata, llama al 911.",
                "Para sa apuradong tabang, tumawag sa 911.",
                "Para sa dayon nga tabang, tawag sa 911.",
                "Para han dayon nga bulig, tawag ha 911.",
                "Para iti dagus a tulong, tumawag iti 911."
            )
            "persistent_done" -> t(
                "Thank you for confirming. How else may I help?",
                "Salamat sa kumpirmasyon. Paano pa kita matutulungan?",
                "Gracias por confirmar. Como mas puedo ayudarte?",
                "Salamat sa pagkumpirma. Pano pa taka matutulungan?",
                "Salamat sa pagkumpirma. Unsa pa akong matabang nimo?",
                "Salamat han pagkumpirma. Paonan-o pa ako makakabulig?",
                "Agyamanak iti panangikumpirma. Ania pay ti maitulongko?"
            )
            "persistent_initial" -> t(
                "How else can I help you regarding this alert?",
                "Ano pa ang maitutulong ko tungkol sa alert na ito?",
                "Como mas puedo ayudarte sobre esta alerta?",
                "Ano pa an maitatabang ko manungod sa alert na ini?",
                "Unsa pa akong matabang nimo bahin ani nga alert?",
                "Ano pa an maibubulig ko mahitungod hini nga alert?",
                "Ania pay ti maitulongko maipanggep iti daytoy nga alert?"
            )
            "persistent_sorry" -> t(
                "Sorry, I can't help with that.",
                "Paumanhin, hindi ko matutulungan iyan.",
                "Lo siento, no puedo ayudar con eso.",
                "Pasensya, dae ko yan matutulungan.",
                "Pasayloa, dili ko makatabang ana.",
                "Pasayloa, diri ako makakabulig hito.",
                "Pakawanennak, saanak a makatulong iti dayta."
            )
            "qr_what_to_do" -> t("What to do?", "Ano ang dapat gawin?", "Que debo hacer?", "Ano an dapat gibuhon?", "Unsa ang buhaton?", "Ano an bubuhaton?", "Ania ti aramiden?")
            "qr_disaster" -> t("What is this disaster?", "Ano ang sakunang ito?", "Que desastre es este?", "Ano ini na kalamidad?", "Unsa ni nga kalamidad?", "Ano ini nga kalamidad?", "Ania daytoy a didigra?")
            "qr_issued" -> t("When was this issued?", "Kailan ito inilabas?", "Cuando se emitio esto?", "Nuarin ini piggwa?", "Kanus-a kini gi-isyu?", "San-o ini iginpagawas?", "Kaano a naipablaak daytoy?")
            "qr_source" -> t("What is the source?", "Ano ang pinagmulan?", "Cual ang fuente?", "Sain ini naggikan?", "Unsa ang tinubdan?", "Ano an gigikanan?", "Ania ti paggapuan?")
            "qr_assistance" -> t("I need assistance", "Kailangan ko ng tulong", "Necesito ayuda", "Kaipuhan ko nin tabang", "Nanginahanglan ko og tabang", "Kinahanglan ko hin bulig", "Masapulko ti tulong")
            "qr_contact" -> t("Contact a responder", "Kumontak sa responder", "Contactar a un respondedor", "Makipag-kontak sa responder", "Kontaka ang responder", "Kontaka an responder", "Makikontak iti responder")
            "qr_contacts" -> t("Go to Emergency Contacts", "Pumunta sa Emergency Contacts", "Ir a Contactos de Emergencia", "Padagos sa Emergency Contacts", "Adto sa Emergency Contacts", "Kadto ha Emergency Contacts", "Mapan iti Emergency Contacts")
            "qr_immediate" -> t("I need immediate assistance!", "Kailangan ko ng agarang tulong!", "Necesito ayuda inmediata!", "Kaipuhan ko nin apuradong tabang!", "Nanginahanglan ko dayon og tabang!", "Kinahanglan ko dayon hin bulig!", "Masapulko ti dagus a tulong!")
            "qr_call" -> t("Okay, I will call 911.", "Sige, tatawag ako sa 911.", "Esta bien, llamare al 911.", "Sige, matawag ako sa 911.", "Sige, motawag ko sa 911.", "Sige, matatawag ako ha 911.", "Wen, tumawagak iti 911.")
            "qr_back" -> t("Take me back.", "Ibalik mo ako.", "Regresame.", "Ibalik ako.", "Ibalik ko.", "Ibalik ako.", "I-subliak.")
            else -> key
        }
        return if (arg != null && text.contains("%s")) String.format(text, arg) else text
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

