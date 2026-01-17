package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.*
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.getIconForCategory
import com.example.emergencycommunicationsystem.util.getColorForCategory
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import kotlinx.coroutines.launch
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
fun CompactEmergencyInstructions(alert: Alert, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Instructions logic
    val alertType = when {
        alert.category?.contains("weather", true) == true || alert.title?.contains("typhoon", true) == true -> "flood"
        alert.category?.contains("fire", true) == true -> "fire"
        alert.category?.contains("earthquake", true) == true -> "earthquake"
        else -> "general"
    }
    
    val stepsEn = when (alertType) {
        "flood" -> listOf("Avoid floodwaters", "Go to highest floor", "Turn off electricity if safe")
        "fire" -> listOf("Use stairs only", "Stay low to avoid smoke", "Feel doors before opening")
        "earthquake" -> listOf("Drop to hands and knees", "Cover head and neck", "Hold on to sturdy furniture")
        else -> listOf("Follow local guidance", "Stay tuned for updates")
    }

    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    var translatedSteps by remember { mutableStateOf(stepsEn) }
    var translatedLabel by remember { mutableStateOf("What To Do:") }
    
    LaunchedEffect(alertType, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedLabel = TranslationService.translate("What To Do:", currentLanguage)
                translatedSteps = TranslationService.translateBatch(stepsEn, currentLanguage)
            }
        } else {
            translatedLabel = "What To Do:"; translatedSteps = stepsEn
        }
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant, // The "Satisfying Green" tint from Theme.kt
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🧠 $translatedLabel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            translatedSteps.take(3).forEach { step ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        text = "• ", 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Black, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant 
                    )
                    Text(text = step, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    onMessageClick: (id: String, title: String) -> Unit
) {
    val localeContext = getLocaleContext()
    val categoryColor = getColorForCategory(alert)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(alert = alert, modifier = Modifier.size(24.dp), tint = categoryColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val categoryName = getCategoryName(alert, localeContext)
                    Text(
                        text = categoryName.uppercase(),
                        color = categoryColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = alert.title ?: localeContext.getString(R.string.no_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = alert.content ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            CompactEmergencyInstructions(alert = alert)
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = alert.source ?: localeContext.getString(R.string.unknown_source),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = alert.timestamp ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Button(
                    onClick = { onMessageClick(alert.id.toString(), alert.title ?: "Chat") },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(imageVector = AppIcons.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = localeContext.getString(R.string.message), fontWeight = FontWeight.Bold)
                }
            }
        }
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
    val isRefreshing = state is Resource.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing, 
        onRefresh = { viewModel.loadAlerts() }
    )

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
                    val alerts = resource.data.filter { it.title != "General Inquiry" }
                    if (alerts.isEmpty()) {
                        EmptyAlertsView()
                    } else {
                        val listState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(alerts, key = { it.id }) { alert ->
                                    AlertItem(alert = alert) { id, title ->
                                        onMessageClick?.invoke(id, title)
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(100.dp)) }
                            }
                            
                            ScrollIndicator(
                                listState = listState, 
                                itemCount = alerts.size, 
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                            )
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
fun getCategoryName(alert: Alert, localeContext: android.content.Context): String {
    val context = LocalContext.current
    val categoryId = try { alert.category?.toIntOrNull() ?: 0 } catch (_: Exception) { 0 }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val nameEn = when {
        categoryId == 1 -> "Weather"; categoryId == 2 -> "Earthquake"; categoryId == 3 -> "Health"; categoryId == 4 -> "Fire"; categoryId == 5 -> "General"
        "weather" in title || "rain" in title -> "Weather"; "earthquake" in title -> "Earthquake"
        "fire" in title -> "Fire"; "health" in title -> "Health"
        else -> "General"
    }
    val currentLang by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    var translated by remember { mutableStateOf(nameEn) }
    LaunchedEffect(nameEn, currentLang) {
        translated = if (currentLang != "en") TranslationService.translate(nameEn, currentLang) else nameEn
    }
    return translated
}

@Composable
fun ScrollIndicator(listState: LazyListState, itemCount: Int, modifier: Modifier = Modifier) {
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    if (layoutInfo.visibleItemsInfo.isEmpty() || itemCount == 0 || itemCount <= layoutInfo.visibleItemsInfo.size) return
    
    val progress = (layoutInfo.visibleItemsInfo.first().index.toFloat() / (itemCount - 1).toFloat()).coerceIn(0f, 1f)
    
    Box(
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight(0.6f)
            .padding(vertical = 40.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .offset(y = (progress * 100).dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        )
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
