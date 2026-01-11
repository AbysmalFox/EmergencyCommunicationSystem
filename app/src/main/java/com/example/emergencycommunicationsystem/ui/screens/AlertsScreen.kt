package com.example.emergencycommunicationsystem.ui.screens

import android.util.Log
import android.widget.Toast
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
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import java.util.Locale

@Composable
fun getIconForCategory(alert: Alert): ImageVector {
    val category = alert.category?.lowercase(Locale.getDefault()) ?: ""
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    return when {
        "weather" in category || "typhoon" in title || "storm" in title || "rain" in title -> Icons.Default.Cloud
        "health" in category -> Icons.Default.LocalHospital
        "security" in category -> Icons.Default.Security
        "earthquake" in category || "tremor" in title -> Icons.Default.House
        "fire" in category || "wildfire" in title -> Icons.Default.Fireplace
        "water" in category || "water" in title || "flood" in title -> Icons.Default.WaterDrop
        else -> Icons.Default.Info
    }
}

@Composable
fun getColorForCategory(alert: Alert): Color {
    val category = alert.category?.lowercase(Locale.getDefault()) ?: ""
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    return when {
        "weather" in category || "typhoon" in title || "storm" in title || "rain" in title -> Color(0xFF4A90E2)
        "health" in category -> Color(0xFF50E3C2)
        "security" in category -> Color(0xFFD0021B)
        "earthquake" in category || "tremor" in title -> Color(0xFF7B4F2C)
        "fire" in category || "wildfire" in title -> Color(0xFFF5A623)
        "water" in category || "water" in title || "flood" in title -> Color(0xFF4A90E2)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    onMessageClick: (id: String, title: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Icon(
                    imageVector = getIconForCategory(alert),
                    contentDescription = alert.category ?: "Alert Category",
                    modifier = Modifier.size(40.dp).align(Alignment.Top),
                    tint = getColorForCategory(alert)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (alert.category ?: "General").uppercase(),
                        color = getColorForCategory(alert),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = alert.title ?: "No Title",
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
                        text = alert.source ?: "Unknown Source",
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
                            Toast.makeText(context, "Please log in to send a message", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Message",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Message")
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
    val state by viewModel.uiState.collectAsState()
    val isRefreshing = state is Resource.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadAlerts() }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Alerts & Notifications") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
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
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = "No Alerts",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No new alerts",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Your community alerts will appear here.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Failed to load alerts",
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
            Text("Retry")
        }
    }
}
