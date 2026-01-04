package com.example.emergencycommunicationsystem.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.data.models.QuickReply
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    viewModel: MessagingViewModel,
    alertId: Int,
    alertTitle: String?,
    userName: String?,
    onBackPressed: () -> Unit,
    onNavigateToPersistentChat: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val quickReplies by viewModel.quickReplies.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = alertTitle ?: "Chat",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (alertId != 999) "Automated Assistant" else "Live Responder",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == viewModel.userId
                            )
                        }
                    }

                    QuickReplyPanel(
                        replies = quickReplies,
                        onReplyClick = { reply ->
                            if (alertId == 999) {
                                viewModel.onPersistentQuickReplyClicked(reply, userName ?: "User")
                            } else {
                                viewModel.onTemporaryQuickReplyClicked(reply)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageInput,
                            onValueChange = { viewModel.updateMessageInput(it) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            placeholder = { Text("Type a message...") },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            enabled = !isSending
                        )

                        IconButton(
                            onClick = {
                                if (alertId == 999) {
                                    viewModel.sendPersistentMessage(userName ?: "User")
                                } else {
                                    viewModel.sendTemporaryMessage(messageInput)
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = if (isSending || messageInput.isBlank())
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            enabled = !isSending && messageInput.isNotBlank()
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
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

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(replies) { reply ->
            Button(
                onClick = { onReplyClick(reply) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = reply.icon ?: "")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = reply.text ?: "", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName ?: "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCurrentUser)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (message.icon != null && !isCurrentUser) {
                        Text(text = message.icon)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = message.messageText ?: "",
                        color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatTime(message.sentAt ?: ""),
                    fontSize = 9.sp,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun formatTime(timestamp: String): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val odt = java.time.OffsetDateTime.parse(timestamp.replace(" ", "T") + "Z")
            odt.format(DateTimeFormatter.ofPattern("h:mm a"))
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestamp)
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            formatter.format(date!!)
        }
    } catch (e: Exception) {
        timestamp // Fallback to raw timestamp
    }
}
