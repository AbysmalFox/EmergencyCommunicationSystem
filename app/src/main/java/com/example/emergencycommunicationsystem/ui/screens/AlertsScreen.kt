package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.*
import com.example.emergencycommunicationsystem.util.*
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import java.util.Locale

@Composable
fun CategoryIcon(
    alert: Alert,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val categoryId = try { alert.category?.toIntOrNull() ?: 0 } catch (_: Exception) { 0 }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    val iconRes = when {
        categoryId == 4 || "fire" in categoryStr || "wildfire" in title -> R.drawable.ic_tabler_flame
        categoryId == 3 || "health" in categoryStr -> R.drawable.ic_tabler_clipboard_heart
        categoryId == 5 || "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title -> R.drawable.ic_tabler_message_2_exclamation
        else -> null
    }

    if (iconRes != null) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, modifier = modifier, tint = tint)
    } else {
        Icon(imageVector = getIconForCategory(alert), contentDescription = null, modifier = modifier, tint = tint)
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    currentLanguage: String = "en",
    onAcknowledge: (Int) -> Unit,
    onMessageClick: (id: String, title: String) -> Unit
) {
    val localeContext = getLocaleContext()
    val categoryColor = getColorForCategory(alert)

    val defaultMessage = "Ask our Chatbot"
    val resourceMessage = localeContext.getString(R.string.ask_chatbot)
    var buttonText by remember { mutableStateOf(resourceMessage) }

    LaunchedEffect(currentLanguage, resourceMessage) {
        if (currentLanguage != "en" && resourceMessage == defaultMessage) {
            buttonText = TranslationService.translate(defaultMessage, currentLanguage)
        } else {
            buttonText = resourceMessage
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(alert = alert, modifier = Modifier.size(18.dp), tint = categoryColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            var categoryName by remember { mutableStateOf("") }
                            val baseCategory = getCategoryDisplayName(alert)
                            
                            LaunchedEffect(baseCategory, currentLanguage) {
                                categoryName = if (currentLanguage != "en") {
                                    TranslationService.translate(baseCategory, currentLanguage)
                                } else {
                                    baseCategory
                                }
                            }
                            
                            Text(
                                text = categoryName.uppercase(),
                                color = categoryColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = alert.title ?: localeContext.getString(R.string.no_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                        Text(
                            text = alert.timestamp ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.content ?: "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Acknowledge Section
            if (alert.severity == "High" && !alert.isAcknowledged) {
                Button(
                    onClick = { onAcknowledge(alert.id) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("I RECEIVED THIS ALERT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (alert.isAcknowledged) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Acknowledged", color = StatusSafe, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.source ?: localeContext.getString(R.string.unknown_source),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                val isDark = ThemeManager.isDarkMode()
                val btnContainer = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                val btnContent = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary
                
                Button(
                    onClick = { onMessageClick(alert.id.toString(), alert.title ?: "Chat") },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnContainer,
                        contentColor = btnContent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = AppIcons.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AlertItemLine(
    alert: Alert,
    onMessageClick: (id: String, title: String) -> Unit
) {
    val categoryColor = getColorForCategory(alert)
    val localeContext = getLocaleContext()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMessageClick(alert.id.toString(), alert.title ?: "Chat") }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(alert = alert, modifier = Modifier.size(14.dp), tint = categoryColor)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = alert.title ?: localeContext.getString(R.string.no_title),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (alert.isAcknowledged) {
            Icon(Icons.Default.Check, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = alert.timestamp?.substringAfter(" ")?.substringBeforeLast(":") ?: alert.timestamp ?: "",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(), 
    onMessageClick: ((alertId: String, alertTitle: String) -> Unit)? = null
) {
    val localeContext = getLocaleContext()
    val state by viewModel.uiState.collectAsState()
    val activePoll by viewModel.activePoll.collectAsState()
    val isRefreshing = state is Resource.Loading
    var isCompactMode by remember { mutableStateOf(false) }
    val currentLanguage by UserPrefs.getLanguage(LocalContext.current).collectAsState(initial = "en")

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing, 
        onRefresh = { viewModel.loadAlerts() }
    )

    // Safety Poll Dialog
    activePoll?.let { poll ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissPoll() },
            title = { Text(poll.title, fontWeight = FontWeight.Bold) },
            text = { Text(poll.description) },
            confirmButton = {
                Button(
                    onClick = { viewModel.respondToPoll(poll.id, "safe") },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSafe)
                ) {
                    Text("I AM SAFE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.respondToPoll(poll.id, "help") }
                ) {
                    Text("I NEED HELP", color = StatusDanger)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .height(64.dp)
            ) {
                Text(
                    text = localeContext.getString(R.string.alerts_and_notifications),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                androidx.compose.material3.IconButton(
                    onClick = { isCompactMode = !isCompactMode },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isCompactMode) Icons.Default.ViewAgenda else Icons.AutoMirrored.Filled.List,
                        contentDescription = "Switch View",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).pullRefresh(pullRefreshState).fillMaxSize()) {
            when (val resource = state) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center), 
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is Resource.Success -> {
                    val alerts = resource.data.map { it.alert }.filter { it.title != "General Inquiry" }
                    if (alerts.isEmpty()) {
                        EmptyAlertsView()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(if (isCompactMode) 8.dp else 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(alerts, key = { it.id }) { alert ->
                                Box(modifier = Modifier.animateItem()) {
                                    AnimatedContent(
                                        targetState = isCompactMode,
                                        transitionSpec = {
                                            fadeIn(tween(500)) + expandVertically(tween(500)) togetherWith fadeOut(tween(500)) + shrinkVertically(tween(500))
                                        },
                                        label = "size_transition"
                                    ) { compact ->
                                        if (compact) {
                                            AlertItemLine(alert = alert) { id, title ->
                                                onMessageClick?.invoke(id, title)
                                            }
                                        } else {
                                            AlertItem(
                                                alert = alert, 
                                                currentLanguage = currentLanguage,
                                                onAcknowledge = { viewModel.acknowledgeAlert(it) }
                                            ) { id, title ->
                                                onMessageClick?.invoke(id, title)
                                            }
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }

                is Resource.Error -> {
                    ErrorView(message = resource.message) { viewModel.loadAlerts() }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing, 
                state = pullRefreshState, 
                modifier = Modifier.align(Alignment.TopCenter), 
                backgroundColor = MaterialTheme.colorScheme.surface, 
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyAlertsView() {
    val localeContext = getLocaleContext()
    Column(
        modifier = Modifier.fillMaxSize(), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = AppIcons.NotificationsOff, 
            contentDescription = null, 
            modifier = Modifier.size(72.dp), 
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = localeContext.getString(R.string.no_new_alerts), 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    val localeContext = getLocaleContext()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = localeContext.getString(R.string.failed_to_load_alerts), 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { 
            Text(localeContext.getString(R.string.retry)) 
        }
    }
}
