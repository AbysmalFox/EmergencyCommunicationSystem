package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.emergencycommunicationsystem.ui.theme.*
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.util.getLocaleContext

// --- Data Models ---

enum class Sender {
    USER, BOT
}

data class ChatMessage(
    val text: String,
    val sender: Sender,
    val id: Long = System.currentTimeMillis()
)

data class QuickReply(
    val text: String,
    val payload: String // To identify the action
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val quickReplies: List<QuickReply> = emptyList(),
    val isBotTyping: Boolean = false
)

// --- ViewModel ---

class AutoReplyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        initializeChat()
    }

    private fun initializeChat() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBotTyping = true) }
            delay(1000) // Simulate bot "thinking"
            _uiState.update {
                it.copy(
                    isBotTyping = false,
                    messages = listOf(
                        ChatMessage("Hello 👋 I am an automated assistant. Please select one of the options below so I can assist you regarding the current alert.", Sender.BOT)
                    ),
                    quickReplies = getInitialOptions()
                )
            }
        }
    }

    fun onQuickReplyClicked(reply: QuickReply) {
        // Add user message
        val userMessage = ChatMessage(reply.text, Sender.USER)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                quickReplies = emptyList() // Hide quick replies after selection
            )
        }

        // Process bot response
        viewModelScope.launch {
            _uiState.update { it.copy(isBotTyping = true) }
            delay(1200) // Simulate typing

            val botResponse = getBotResponse(reply.payload)
            _uiState.update {
                it.copy(
                    isBotTyping = false,
                    messages = it.messages + botResponse.first,
                    quickReplies = botResponse.second
                )
            }
        }
    }

    private fun getBotResponse(payload: String): Pair<ChatMessage, List<QuickReply>> {
        return when (payload) {
            "disaster_details" -> {
                ChatMessage("This is a Typhoon alert for Signal #2. It means strong winds are expected. Please secure your homes and stay indoors.", Sender.BOT) to getInitialOptions()
            }
            "disaster_time" -> {
                ChatMessage("The alert was issued within the last hour. For real-time updates, please monitor official news channels.", Sender.BOT) to getInitialOptions()
            }
            "news_source" -> {
                ChatMessage("This information is sourced from the National Disaster Risk Reduction and Management Council (NDRRMC).", Sender.BOT) to getInitialOptions()
            }
            "immediate_assistance" -> {
                ChatMessage("For immediate help, please contact the national emergency hotline at 911. Stay calm and follow instructions from authorities.", Sender.BOT) to getAssistanceOptions()
            }
            "call_done" -> {
                ChatMessage("Thank you for confirming. Help is on the way if needed. Is there anything else I can assist with?", Sender.BOT) to getInitialOptions()
            }
            "initial" -> {
                ChatMessage("How else can I help you regarding this alert?", Sender.BOT) to getInitialOptions()
            }
            else -> {
                ChatMessage("I am sorry, I do not have information on that. Please select from the available options.", Sender.BOT) to getInitialOptions()
            }
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
}

// --- UI Components ---

@Composable
fun AutoReplyChatScreen(viewModel: AutoReplyViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Scroll to the bottom when a new message appears
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = { ChatHeader() },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                if (uiState.isBotTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            
            // Quick replies at the bottom, but more compact
            QuickReplyPanel(
                replies = uiState.quickReplies,
                onReplyClick = viewModel::onQuickReplyClicked
            )
        }
    }
}

@Composable
fun ChatHeader() {
    val localeContext = getLocaleContext()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localeContext.getString(com.example.emergencycommunicationsystem.R.string.auto_reply_bot),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF31A24C), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localeContext.getString(com.example.emergencycommunicationsystem.R.string.bot_status_online),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUserMessage = message.sender == Sender.USER
    val bubbleColor = if (isUserMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUserMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUserMessage) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(bubbleColor, shape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing-indicator")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(300, delayMillis = 0), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = ""
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(300, delayMillis = 150), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = ""
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(300, delayMillis = 300), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = ""
    )
    val dots = listOf(dot1, dot2, dot3)

    Box(
        modifier = Modifier
            .width(60.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            dots.forEach { animatedValue ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = animatedValue }
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), CircleShape)
                )
            }
        }
    }
}


@Composable
fun QuickReplyPanel(
    replies: List<QuickReply>,
    onReplyClick: (QuickReply) -> Unit
) {
    if (replies.isEmpty()) return
    val localeContext = getLocaleContext()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = localeContext.getString(com.example.emergencycommunicationsystem.R.string.suggested_answers),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                replies.forEach { reply ->
                    FilterChip(
                        selected = false,
                        onClick = { onReplyClick(reply) },
                        label = {
                            Text(
                                text = reply.text,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

