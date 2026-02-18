package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.models.CallLog
import com.example.emergencycommunicationsystem.data.repository.CallRepository
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.util.getLocaleContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(onBackPressed: () -> Unit) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val userId = AuthManager.getUserId()
    val repository = remember { CallRepository(localeContext.applicationContext) }
    var callLogs by remember { mutableStateOf<List<CallLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        isLoading = true
        error = null
        val remoteResult = repository.getUserCallHistoryRemote(userId = userId, limit = 100, offset = 0)
        if (remoteResult.isSuccess) {
            callLogs = remoteResult.getOrNull().orEmpty()
        } else {
            // Fallback to local history if remote fails
            repository.getUserCallLogs(userId).collect { local ->
                callLogs = local
                isLoading = false
            }
            error = remoteResult.exceptionOrNull()?.message
            return@LaunchedEffect
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localeContext.getString(R.string.call_history_label), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(AppIcons.ArrowBack, contentDescription = localeContext.getString(R.string.language_settings_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                callLogs.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = AppIcons.EmergencyCall,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            localeContext.getString(R.string.call_history_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            if (!error.isNullOrBlank()) {
                                Text(
                                    text = error ?: "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        items(callLogs) { log ->
                            CallHistoryItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallHistoryItem(log: CallLog) {
    val timestamp = log.endTime ?: log.startTime
    val dateText = remember(timestamp) {
        val formatter = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.getDefault())
        formatter.format(Date(timestamp))
    }
    val statusText = log.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    val durationText = if (log.duration > 0) "${log.duration}s" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Room: ${log.roomName}", fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = statusText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(text = dateText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Duration: $durationText", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
