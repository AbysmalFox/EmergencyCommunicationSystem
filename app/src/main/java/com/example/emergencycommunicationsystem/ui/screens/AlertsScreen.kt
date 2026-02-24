package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.emergencycommunicationsystem.util.NetworkUtils
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CategoryIcon(
    alert: Alert,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val categoryId = alert.categoryId ?: 0
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    val iconRes = when {
        categoryId == 4 || "fire" in categoryStr || "wildfire" in title -> R.drawable.ic_tabler_flame
        categoryId == 3 || "health" in categoryStr -> R.drawable.ic_tabler_clipboard_heart
        "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title -> R.drawable.ic_tabler_message_2_exclamation
        else -> null
    }

    if (iconRes != null) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, modifier = modifier, tint = tint)
    } else if (categoryId == 5 || "security" in categoryStr || "crime" in title) {
        Icon(imageVector = AppIcons.Security, contentDescription = null, modifier = modifier, tint = tint)
    } else {
        Icon(imageVector = getIconForCategory(alert), contentDescription = null, modifier = modifier, tint = tint)
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    currentLanguage: String = "en",
    onAcknowledge: (Int) -> Unit,
    onUndo: (Int) -> Unit,
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

    var isExpanded by remember { mutableStateOf(false) }
    val content = alert.content ?: ""
    val isLongContent = content.length > 150

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { if (isLongContent) isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(alert = alert, modifier = Modifier.size(20.dp), tint = categoryColor)
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val categoryName = getCategoryDisplayName(alert)
                        
                        Text(
                            text = "${categoryName.uppercase()} - ${alert.category?.uppercase() ?: ""}",
                            color = categoryColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = alert.timestamp ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.End
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alert.title ?: localeContext.getString(R.string.no_title),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        val severity = getAlertSeverity(alert)
                        val severityColor = getSeverityColor(severity)
                        Surface(
                            color = severityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = severity.uppercase(),
                                color = severityColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (!alert.area.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_tabler_map_pin),
                                contentDescription = null,
                                tint = categoryColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = alert.area,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(modifier = Modifier.animateContentSize()) {
                val displayContent = when {
                    // Avoid mixed-language blocks (e.g., EN message + translated content) on non-English UI.
                    currentLanguage != "en" && !alert.content.isNullOrBlank() -> alert.content
                    !alert.message.isNullOrBlank() && !alert.content.isNullOrBlank() -> {
                        if (alert.message.trim() == alert.content.trim()) alert.content
                        else "${alert.message}\n\n${alert.content}"
                    }
                    !alert.content.isNullOrBlank() -> alert.content
                    !alert.message.isNullOrBlank() -> alert.message
                    else -> ""
                }

                Text(
                    text = displayContent,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 17.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = if (isExpanded) androidx.compose.ui.text.style.TextOverflow.Clip else androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                if (isLongContent) {
                    Text(
                        text = if (isExpanded) "See less" else "See more",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { isExpanded = !isExpanded }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (!alert.isAcknowledged) {
                Button(
                    onClick = { onAcknowledge(alert.id) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertaraBackground,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("I RECEIVED THIS ALERT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Acknowledged", color = StatusSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    androidx.compose.material3.IconButton(
                        onClick = { onUndo(alert.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Undo,
                            contentDescription = "Undo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localeContext.getString(
                        R.string.source_label_format,
                        alert.source ?: localeContext.getString(R.string.unknown_source)
                    ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                    Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AlertItemLine(
    alert: Alert,
    onAcknowledge: (Int) -> Unit,
    onUndo: (Int) -> Unit,
    onMessageClick: (id: String, title: String) -> Unit
) {
    val categoryColor = getColorForCategory(alert)
    val localeContext = getLocaleContext()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMessageClick(alert.id.toString(), alert.title ?: "Chat") }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(alert = alert, modifier = Modifier.size(16.dp), tint = categoryColor)
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title ?: localeContext.getString(R.string.no_title),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = alert.timestamp?.substringAfter(" ")?.substringBeforeLast(":") ?: alert.timestamp ?: "",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        if (!alert.isAcknowledged) {
            androidx.compose.material3.IconButton(
                onClick = { onAcknowledge(alert.id) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Acknowledge",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Acknowledged",
                    tint = StatusSafe,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                androidx.compose.material3.IconButton(
                    onClick = { onUndo(alert.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Undo,
                        contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Icon(
            imageVector = AppIcons.Message,
            contentDescription = "Chat",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    weatherViewModel: com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel = viewModel(),
    alertId: String? = null,
    onMessageClick: ((alertId: String, alertTitle: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val localeContext = getLocaleContext()
    val state by viewModel.uiState.collectAsState()
    val activePoll by viewModel.activePoll.collectAsState()
    val isRefreshing = state is Resource.Loading
    val savedCompactMode by UserPrefs.isAlertsCompactMode(context).collectAsState(initial = false)
    var isCompactMode by rememberSaveable { mutableStateOf(savedCompactMode) }
    var selectedAlertId by remember { mutableStateOf<Int?>(null) }
    var consumedDeepLinkAlertId by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    val lastSyncMillis by UserPrefs.getAlertsLastSyncMillis(context).collectAsState(initial = 0L)
    val alerts = (state as? Resource.Success)?.data
        ?.map { it.alert }
        ?.filter { it.title != "General Inquiry" }
        ?: emptyList()
    val selectedAlert = selectedAlertId?.let { id -> alerts.firstOrNull { it.id == id } }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline = remember(state) { NetworkUtils.isNetworkAvailable(context) }
    val now = remember { System.currentTimeMillis() }
    val ageMinutes = if (lastSyncMillis > 0L) ((now - lastSyncMillis) / 60000L).toInt() else Int.MAX_VALUE
    val showCacheBadge = !isOnline || ageMinutes >= 15

    LaunchedEffect(savedCompactMode) {
        isCompactMode = savedCompactMode
    }

    // Sync location from WeatherViewModel
    val weatherState by weatherViewModel.weatherState.collectAsState()
    LaunchedEffect(weatherState) {
        if (weatherState is com.example.emergencycommunicationsystem.data.models.WeatherState.Success) {
            val success = weatherState as com.example.emergencycommunicationsystem.data.models.WeatherState.Success
            viewModel.updateUserLocation(success.lat, success.lon)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.example.emergencycommunicationsystem.viewmodel.AlertsEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoAcknowledge(event.alertId)
                    }
                }
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing, 
        onRefresh = { viewModel.loadAlerts() }
    )

    LaunchedEffect(alertId, alerts) {
        if (alertId.isNullOrBlank() || alerts.isEmpty() || consumedDeepLinkAlertId == alertId) return@LaunchedEffect

        val deepLinkedAlertId = alertId.toIntOrNull() ?: return@LaunchedEffect
        val index = alerts.indexOfFirst { it.id == deepLinkedAlertId }
        if (index == -1) return@LaunchedEffect

        listState.animateScrollToItem(index)
        selectedAlertId = deepLinkedAlertId
        consumedDeepLinkAlertId = alertId
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

                if (showCacheBadge) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (lastSyncMillis > 0L) "Cached ${ageMinutes}m" else "Cached",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                androidx.compose.material3.IconButton(
                    onClick = {
                        val updated = !isCompactMode
                        isCompactMode = updated
                        coroutineScope.launch {
                            UserPrefs.saveAlertsCompactMode(context, updated)
                        }
                    },
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
                    if (alerts.isEmpty()) {
                        EmptyAlertsView()
                    } else {
                        LazyColumn(
                            state = listState,
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
                                            AlertItemLine(
                                                alert = alert,
                                                onAcknowledge = { viewModel.acknowledgeAlert(it) },
                                                onUndo = { viewModel.undoAcknowledge(it) }
                                            ) { id, title ->
                                                onMessageClick?.invoke(id, title)
                                            }
                                        } else {
                                            AlertItem(
                                                alert = alert, 
                                                currentLanguage = currentLanguage,
                                                onAcknowledge = { viewModel.acknowledgeAlert(it) },
                                                onUndo = { viewModel.undoAcknowledge(it) }
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

            selectedAlert?.let { alert ->
                AlertDetailDialog(
                    alert = alert,
                    onDismiss = { selectedAlertId = null },
                    onMessageClick = {
                        onMessageClick?.invoke(alert.id.toString(), alert.title ?: "Chat")
                    }
                )
            }
        }
    }
}

@Composable
private fun AlertDetailDialog(
    alert: Alert,
    onDismiss: () -> Unit,
    onMessageClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = alert.title ?: "Alert Details",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alert.timestamp?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
                Text(
                    text = when {
                        !alert.content.isNullOrBlank() -> alert.content
                        !alert.message.isNullOrBlank() -> alert.message
                        else -> "No additional details."
                    },
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onMessageClick) {
                Icon(
                    imageVector = AppIcons.SmartToy,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ask our Chatbot",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
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
