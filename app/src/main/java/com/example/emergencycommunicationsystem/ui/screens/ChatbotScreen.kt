package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.models.QuickReply
import com.example.emergencycommunicationsystem.ui.theme.ChatHeaderTeal
import com.example.emergencycommunicationsystem.ui.theme.ChatFooterMintLight
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    viewModel: MessagingViewModel,
    alertTitle: String?,
    onBackPressed: () -> Unit,
    onNavigateToPersistentChat: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit
) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val quickReplies by viewModel.quickReplies.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val showBotTyping = !isLoading &&
        quickReplies.isEmpty() &&
        messages.isNotEmpty() &&
        messages.last().senderId == viewModel.userId

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collectLatest {
            when(it) {
                is NavigationRequest.ToPersistentChat -> onNavigateToPersistentChat()
                is NavigationRequest.ToEmergencyContacts -> onNavigateToEmergencyContacts()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // We handle status/nav bar padding manually in this screen; consume Scaffold insets to satisfy lint.
                .consumeWindowInsets(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Thinner Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    color = ChatHeaderTeal,
                    shape = CurvedHeaderShape()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp)
                            .padding(top = 6.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = AppIcons.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(modifier = Modifier.size(32.dp).clip(CircleShape), color = Color.White.copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = AppIcons.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = localeContext.getString(R.string.auto_reply_bot), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            val subtitle = remember(alertTitle) {
                                val status = localeContext.getString(R.string.bot_status_online)
                                if (alertTitle.isNullOrBlank()) status else "$status • $alertTitle"
                            }
                            Text(text = subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 1)
                        }
                    }
                }

                if (isLoading && messages.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ChatHeaderTeal)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 0.dp, bottom = 8.dp)
                    ) {
                        items(messages, key = { it.messageId }) { message ->
                            MessageBubble(message = message, isCurrentUser = message.senderId == viewModel.userId)
                        }
                        if (showBotTyping) {
                            item(key = "bot_typing") {
                                BotTypingIndicator()
                            }
                        }
                    }
                }

                // Compact Connected Curved Footer
                Box(modifier = Modifier.fillMaxWidth().background(ChatFooterMintLight)) {
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (quickReplies.isEmpty()) 110.dp else 170.dp),
                            color = ChatFooterMintLight, 
                            shape = CurvedFooterShape()
                        ) {}
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (quickReplies.isNotEmpty()) {
                                Text(
                                    text = localeContext.getString(R.string.suggested_answers),
                                    color = ChatHeaderTeal.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    quickReplies.forEach { reply ->
                                        FilterChip(
                                            selected = false,
                                            onClick = { viewModel.onTemporaryQuickReplyClicked(reply) },
                                            label = {
                                                Text(
                                                    text = quickReplyLabel(reply),
                                                    maxLines = 1,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color.White,
                                                labelColor = ChatHeaderTeal
                                            ),
                                            border = BorderStroke(1.dp, ChatHeaderTeal.copy(alpha = 0.35f)),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextField(
                                    value = messageInput,
                                    onValueChange = { viewModel.updateMessageInput(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp, max = 120.dp),
                                    placeholder = {
                                        Text(
                                            text = localeContext.getString(R.string.type_message),
                                            fontSize = 14.sp,
                                            style = TextStyle(lineHeight = 18.sp)
                                        )
                                    },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp),
                                    singleLine = false,
                                    maxLines = 4,
                                    enabled = !isSending
                                )

                                IconButton(
                                    onClick = { viewModel.sendTemporaryMessage(messageInput) },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            color = if (isSending || messageInput.isBlank()) ChatHeaderTeal.copy(alpha = 0.5f) else ChatHeaderTeal,
                                            shape = CircleShape
                                        ),
                                    enabled = !isSending && messageInput.isNotBlank()
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = AppIcons.Send,
                                            contentDescription = localeContext.getString(R.string.send_button),
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun quickReplyLabel(reply: QuickReply): String {
    val icon = reply.icon?.trim().orEmpty()
    val text = reply.text?.trim().orEmpty()
    return when {
        icon.isNotEmpty() && text.isNotEmpty() -> "$icon $text"
        text.isNotEmpty() -> text
        else -> icon
    }
}

@Composable
private fun BotTypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val d1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), repeatMode = RepeatMode.Reverse),
        label = "d1"
    )
    val d2 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, delayMillis = 140), repeatMode = RepeatMode.Reverse),
        label = "d2"
    )
    val d3 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, delayMillis = 280), repeatMode = RepeatMode.Reverse),
        label = "d3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            color = ChatHeaderTeal.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.SmartToy,
                    contentDescription = null,
                    tint = ChatHeaderTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(alpha = d1)
                Dot(alpha = d2)
                Dot(alpha = d3)
            }
        }
    }
}

@Composable
private fun Dot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(ChatHeaderTeal.copy(alpha = alpha))
    )
}
