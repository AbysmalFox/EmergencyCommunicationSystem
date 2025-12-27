package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.emergencycommunicationsystem.viewmodel.AlertsUiState
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel

@Composable
fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Weather" -> Icons.Default.Cloud
        "Health" -> Icons.Default.LocalHospital
        "Security" -> Icons.Default.Security
        "Earthquake" -> Icons.Default.House
        "Fire" -> Icons.Default.Fireplace
        else -> Icons.Default.Info
    }
}

@Composable
fun getColorForCategory(category: String): Color {
    return when (category) {
        "Weather" -> Color(0xFF4A90E2) // Blue
        "Health" -> Color(0xFF50E3C2) // Teal
        "Security" -> Color(0xFFD0021B) // Red
        "Earthquake" -> Color(0xFF7B4F2C) // Brown
        "Fire" -> Color(0xFFF5A623) // Orange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun AlertItem(alert: Alert, onMessageClick: (Alert) -> Unit = {}) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Icon(
                    imageVector = getIconForCategory(alert.category),
                    contentDescription = alert.category,
                    modifier = Modifier.size(40.dp).align(Alignment.Top),
                    tint = getColorForCategory(alert.category)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.category.uppercase(),
                        color = getColorForCategory(alert.category),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = alert.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.content,
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
                        text = alert.source,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = alert.timestamp,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        if (AuthManager.getUserId() != -1) {
                            onMessageClick(alert)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    onMessageClick: ((Alert) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

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
        when (val uiState = state) {
            is AlertsUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading alerts...",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is AlertsUiState.Success -> {
                val filteredAlerts = uiState.alerts.filter { it.title != "General Inquiry" }

                if (filteredAlerts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
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
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(
                            16.dp,
                            bottom = 136.dp + 16.dp
                        ), // Reserve space for floating bottom nav (136.dp) + original bottom padding (16.dp)
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAlerts) { alert ->
                            AlertItem(alert = alert, onMessageClick = { onMessageClick?.invoke(alert) })
                        }
                    }
                }
            }

            is AlertsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
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
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadAlerts() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
