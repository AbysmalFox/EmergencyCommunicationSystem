package com.example.emergencycommunicationsystem.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import java.util.Locale

@Composable
fun getIconForCategory(alert: Alert): ImageVector {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        // Category 1: Weather - Cloud icon
        categoryId == 1 || "weather" in categoryStr || "typhoon" in title || "storm" in title || "rain" in title || "heat" in title || "wind" in title -> Icons.Default.Cloud
        // Category 2: Earthquake - Warning/Alert icon
        categoryId == 2 || "earthquake" in categoryStr || "tremor" in title -> Icons.Default.Warning
        // Category 4: Fire - Fireplace icon
        categoryId == 4 || "fire" in categoryStr || "wildfire" in title -> Icons.Default.Fireplace
        // Category 5: General/Emergency - Info icon
        categoryId == 5 || "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title -> Icons.Default.Info
        // Category 3: Health (if exists) - Hospital icon
        categoryId == 3 || "health" in categoryStr -> Icons.Default.LocalHospital
        // Security - Security icon
        "security" in categoryStr -> Icons.Default.Security
        // Water/Flood - Water drop icon
        "water" in categoryStr || "flood" in title -> Icons.Default.WaterDrop
        // Default fallback - Info icon
        else -> Icons.Default.Info
    }
}

@Composable
fun getColorForCategory(alert: Alert): Color {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        // Category 1: Weather - Blue
        categoryId == 1 || "weather" in categoryStr || "typhoon" in title || "storm" in title || "rain" in title || "heat" in title || "wind" in title -> Color(0xFF4A90E2)
        // Category 2: Earthquake - Orange/Brown
        categoryId == 2 || "earthquake" in categoryStr || "tremor" in title -> Color(0xFFF57C00)
        // Category 4: Fire - Orange/Red
        categoryId == 4 || "fire" in categoryStr || "wildfire" in title -> Color(0xFFF5A623)
        // Category 5: General/Emergency - Teal/Cyan
        categoryId == 5 || "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title -> Color(0xFF50E3C2)
        // Category 3: Health - Teal
        categoryId == 3 || "health" in categoryStr -> Color(0xFF50E3C2)
        // Security - Red
        "security" in categoryStr -> Color(0xFFD0021B)
        // Water/Flood - Blue
        "water" in categoryStr || "flood" in title -> Color(0xFF4A90E2)
        // Default
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Determine severity level based on alert category and content
 * Returns: "High", "Medium", or "Low"
 */
fun getAlertSeverity(alert: Alert): String {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }
    
    val category = alert.category?.lowercase(Locale.getDefault()) ?: ""
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val content = alert.content?.lowercase(Locale.getDefault()) ?: ""
    
    // High severity indicators (category 2 = Earthquake, category 4 = Fire)
    val highSeverityKeywords = listOf("fire", "flood", "earthquake", "tsunami", "typhoon", "storm", "crime", "security", "evacuate", "urgent", "emergency", "escape")
    
    // Medium severity indicators
    val mediumSeverityKeywords = listOf("warning", "caution", "advisory", "health", "weather", "traffic", "accident")
    
    val allText = "$category $title $content"
    
    return when {
        // Category 2 (Earthquake) and 4 (Fire) are high severity
        categoryId == 2 || categoryId == 4 -> "High"
        // Category 1 (Weather) is medium severity
        categoryId == 1 -> "Medium"
        // Category 5 (General/Emergency) - check content for urgency
        categoryId == 5 -> if (highSeverityKeywords.any { it in allText }) "High" else "Medium"
        highSeverityKeywords.any { it in allText } -> "High"
        mediumSeverityKeywords.any { it in allText } -> "Medium"
        else -> "Low"
    }
}

/**
 * Get severity color (Yellow/Orange/Red)
 */
@Composable
fun getSeverityColor(severity: String): Color {
    return when (severity) {
        "High" -> Color(0xFFD32F2F) // Red
        "Medium" -> Color(0xFFF57C00) // Orange
        "Low" -> Color(0xFFFBC02D) // Yellow
        else -> Color(0xFF757575) // Gray (default)
    }
}

/**
 * Get category name from numeric ID or fallback to string/title analysis
 */
fun getCategoryName(alert: Alert, localeContext: android.content.Context): String {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        categoryId == 1 -> "Weather"
        categoryId == 2 -> "Earthquake"
        categoryId == 3 -> "Health"
        categoryId == 4 -> "Fire"
        categoryId == 5 -> "General"
        "weather" in title || "rain" in title || "storm" in title || "heat" in title || "wind" in title -> "Weather"
        "earthquake" in title || "tremor" in title -> "Earthquake"
        "fire" in title || "wildfire" in title -> "Fire"
        "health" in title -> "Health"
        "traffic" in title || "road" in title || "power" in title || "emergency" in title -> "General"
        else -> localeContext.getString(R.string.general)
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    onMessageClick: (id: String, title: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val localeContext = getLocaleContext()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Icon(
                    imageVector = getIconForCategory(alert),
                    contentDescription = getCategoryName(alert, localeContext),
                    modifier = Modifier.size(40.dp).align(Alignment.Top),
                    tint = getColorForCategory(alert)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getCategoryName(alert, localeContext).uppercase(),
                        color = getColorForCategory(alert),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = alert.title ?: localeContext.getString(R.string.no_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.content ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = alert.source ?: localeContext.getString(R.string.unknown_source),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = alert.timestamp ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        val userId = AuthManager.getUserId()
                        if (userId > 0) {
                            onMessageClick(alert.id.toString(), alert.title ?: "Chat")
                        } else {
                            Toast.makeText(context, localeContext.getString(R.string.please_login_to_send_message), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = localeContext.getString(R.string.message),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localeContext.getString(R.string.message))
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
            // Custom reduced-height TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .height(60.dp) // Reduced from default 64dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        localeContext.getString(R.string.alerts_and_notifications),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 110.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            when (val resource = state) {
                is Resource.Loading -> {
                    // Show full screen loader only if we have no data yet
                    if (resource is Resource.Loading && state !is Resource.Success) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is Resource.Success -> {
                    val alerts = resource.data.filter { it.title != "General Inquiry" }
                    if (alerts.isEmpty()) {
                        EmptyAlertsView()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, bottom = 136.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(alerts, key = { it.id }) { alert ->
                                AlertItem(alert = alert) { alertId, alertTitle ->
                                    onMessageClick?.invoke(alertId, alertTitle)
                                }
                            }
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
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = localeContext.getString(R.string.no_new_alerts),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            localeContext.getString(R.string.no_new_alerts),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            localeContext.getString(R.string.community_alerts_will_appear_here),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    val localeContext = getLocaleContext()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = localeContext.getString(R.string.failed_to_load_alerts),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(localeContext.getString(R.string.retry))
        }
    }
}
