package com.example.emergencycommunicationsystem.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
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
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

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
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = AppIcons.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(modifier = Modifier.size(32.dp).clip(CircleShape), color = Color.White.copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = AppIcons.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = alertTitle ?: localeContext.getString(R.string.chat_title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = localeContext.getString(R.string.automated_assistant), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
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

                // Compact Connected Curved Footer
                Box(modifier = Modifier.fillMaxWidth().background(ChatFooterMintLight)) {
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(75.dp),
                            color = ChatFooterMintLight, 
                            shape = CurvedFooterShape()
                        ) {}
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(75.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Automated Session",
                                color = ChatHeaderTeal.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
