package com.example.emergencycommunicationsystem.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.ui.theme.ChatBrandTeal
import com.example.emergencycommunicationsystem.ui.theme.ChatHeaderTeal
import com.example.emergencycommunicationsystem.ui.theme.ChatIncomingBubbleDark
import com.example.emergencycommunicationsystem.ui.theme.ChatIncomingBubbleLight
import com.example.emergencycommunicationsystem.ui.theme.ChatBrandMint
import com.example.emergencycommunicationsystem.ui.theme.ChatOutgoingBubbleDark
import com.example.emergencycommunicationsystem.ui.theme.ChatOutgoingBubbleLight
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
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val isDarkTheme = isSystemInDarkTheme()

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    ) { padding ->
        // Use a Box without scaffold padding to allow the header to go under the status bar
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Curved Header - Removing top white space by ignoring Scaffold padding
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp), // Slightly reduced height
                    color = ChatHeaderTeal,
                    shape = CurvedHeaderShape()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding() // Adds internal padding for icons but keeps background at top
                            .padding(horizontal = 16.dp)
                            .padding(top = 0.dp, bottom = 25.dp), // Content moved higher by reducing top and increasing bottom padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = AppIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Surface(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alertTitle ?: localeContext.getString(R.string.chat_title),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (alertId != 999) localeContext.getString(R.string.automated_assistant) else localeContext.getString(R.string.live_responder),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (isLoading && messages.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ChatBrandTeal)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        items(messages, key = { it.messageId }) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == viewModel.userId
                            )
                        }
                    }
                }

                // Curved Footer with Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp) // Height includes navigation bar area
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (isDarkTheme) Color(0xFF1E1E1E) else ChatBrandMint,
                        shape = CurvedFooterShape()
                    ) {
                        if (alertId == 999) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding() // Background stays at bottom, content is padded
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 10.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { /* Handle attachments */ }) {
                                    Icon(
                                        imageVector = AppIcons.AttachFile,
                                        contentDescription = "Attach",
                                        tint = if (isDarkTheme) Color.White else ChatBrandTeal
                                    )
                                }

                                TextField(
                                    value = messageInput,
                                    onValueChange = { viewModel.updateMessageInput(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    placeholder = { 
                                        Text(
                                            localeContext.getString(R.string.type_message), 
                                            fontSize = 14.sp,
                                            color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray
                                        ) 
                                    },
                                    shape = RoundedCornerShape(25.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White,
                                        unfocusedContainerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White,
                                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    enabled = !isSending
                                )

                                IconButton(
                                    onClick = {
                                        viewModel.sendPersistentMessage(userName ?: "User")
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            color = if (isSending || messageInput.isBlank())
                                                (if (isDarkTheme) Color.White.copy(alpha = 0.3f) else ChatBrandTeal.copy(alpha = 0.5f))
                                            else
                                                (if (isDarkTheme) Color.White else ChatBrandTeal),
                                            shape = CircleShape
                                        ),
                                    enabled = !isSending && messageInput.isNotBlank()
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = if (isDarkTheme) Color.Black else Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = AppIcons.Send,
                                            contentDescription = localeContext.getString(R.string.send_button),
                                            tint = if (isDarkTheme) Color.Black else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Re-apply Snackbar placement if needed, or rely on Scaffold which we kept
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val isDarkTheme = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else ChatBrandTeal.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = message.senderName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else ChatBrandTeal,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 16.dp else 2.dp,
                        bottomEnd = if (isCurrentUser) 2.dp else 16.dp
                    ))
                    .background(
                        color = if (isCurrentUser) {
                            if (isDarkTheme) ChatOutgoingBubbleDark else ChatOutgoingBubbleLight
                        } else {
                            if (isDarkTheme) ChatIncomingBubbleDark else ChatIncomingBubbleLight
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCurrentUser) 16.dp else 2.dp,
                            bottomEnd = if (isCurrentUser) 2.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
                    .widthIn(max = 260.dp)
            ) {
                Column {
                    Text(
                        text = message.messageText,
                        color = if (isCurrentUser) Color.White else if (isDarkTheme) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                }
            }
            
            Text(
                text = formatTime(message.createdAt),
                fontSize = 10.sp,
                color = if (isDarkTheme) Color.LightGray.copy(alpha = 0.6f) else Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                color = if (isDarkTheme) ChatOutgoingBubbleDark else ChatBrandTeal
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun formatTime(timestamp: String): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val odt = if (timestamp.contains("T")) {
                java.time.OffsetDateTime.parse(timestamp)
            } else {
                java.time.LocalDateTime.parse(timestamp.replace(" ", "T")).atOffset(java.time.ZoneOffset.UTC)
            }
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

class CurvedHeaderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.8f)
            quadraticBezierTo(
                size.width * 0.75f, size.height,
                size.width * 0.5f, size.height * 0.9f
            )
            quadraticBezierTo(
                size.width * 0.25f, size.height * 0.8f,
                0f, size.height
            )
            close()
        }
        return Outline.Generic(path)
    }
}

class CurvedFooterShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, size.height * 0.2f)
            quadraticBezierTo(
                size.width * 0.75f, 0f,
                size.width * 0.5f, size.height * 0.15f
            )
            quadraticBezierTo(
                size.width * 0.25f, size.height * 0.3f,
                0f, size.height * 0.1f
            )
            close()
        }
        return Outline.Generic(path)
    }
}
