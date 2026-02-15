package com.example.emergencycommunicationsystem.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.ui.theme.ChatHeaderTeal
import com.example.emergencycommunicationsystem.ui.theme.ChatFooterMintLight
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderChatScreen(
    viewModel: MessagingViewModel,
    alertTitle: String?,
    userName: String?,
    onBackPressed: () -> Unit,
    onNavigateToPersistentChat: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit
) {
    val context = LocalContext.current
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendImageMessage(context, it, userName ?: "User")
        }
    }

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
                // Thinner Header - contents aligned to TOP
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    color = ChatHeaderTeal,
                    shape = CurvedHeaderShape()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = AppIcons.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(32.dp)
                                .clip(CircleShape), 
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = AppIcons.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                            Text(text = alertTitle ?: localeContext.getString(R.string.chat_title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = localeContext.getString(R.string.live_responder), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
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
                    }
                }

                // Compact Footer
                Box(modifier = Modifier.fillMaxWidth().background(ChatFooterMintLight)) {
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(92.dp),
                            color = ChatFooterMintLight, 
                            shape = CurvedFooterShape()
                        ) {}
                        
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { imagePickerLauncher.launch("image/*") }, 
                                modifier = Modifier.size(42.dp),
                                enabled = !isSending
                            ) {
                                Icon(imageVector = AppIcons.AttachFile, contentDescription = "Attach", tint = ChatHeaderTeal, modifier = Modifier.size(24.dp))
                            }
                            
                            TextField(
                                value = messageInput,
                                onValueChange = { viewModel.updateMessageInput(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp, max = 120.dp), // Controlled height to prevent vertical cutoff
                                placeholder = { 
                                    Text(
                                        text = localeContext.getString(R.string.type_message), 
                                        fontSize = 15.sp,
                                        style = TextStyle(lineHeight = 20.sp)
                                    ) 
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
                                singleLine = false, // Allows vertical centering and multi-line growth
                                maxLines = 3,
                                enabled = !isSending
                            )
                            
                            IconButton(
                                onClick = { viewModel.sendPersistentMessage(userName ?: "User") },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        color = if (isSending || messageInput.isBlank()) ChatHeaderTeal.copy(alpha = 0.5f) else ChatHeaderTeal,
                                        shape = CircleShape
                                    ),
                                enabled = !isSending && messageInput.isNotBlank()
                            ) {
                                if (isSending) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                else Icon(imageVector = AppIcons.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
