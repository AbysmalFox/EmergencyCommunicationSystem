package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.ui.components.CompactAlertCard
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.getAlertSeverity

@Composable
fun ActiveAlertsSection(
    alertsState: Resource<List<Alert>>,
    userLat: Double?,
    userLon: Double?,
    onAlertClick: (Int) -> Unit
) {
    val localeContext = getLocaleContext()
    
    when (val state = alertsState) {
        is Resource.Loading -> {
            // Show loading indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
        is Resource.Success -> {
            val alerts = state.data.take(3) // Show only top 3 alerts
            
            if (alerts.isNotEmpty()) {
                val bgColor = MaterialTheme.colorScheme.background
                val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isDarkMode) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localeContext.getString(com.example.emergencycommunicationsystem.R.string.active_alerts_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.3).sp
                        )
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${state.data.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        alerts.forEach { alert ->
                            val distanceKm = if (userLat != null && userLon != null && 
                                alert.latitude != null && alert.longitude != null) {
                                LocationUtils.calculateDistance(
                                    userLat, userLon,
                                    alert.latitude!!, alert.longitude!!
                                )
                            } else null
                            
                            val severity = getAlertSeverity(alert)
                            
                            CompactAlertCard(
                                alert = alert,
                                distanceKm = distanceKm,
                                severity = severity,
                                onClick = { onAlertClick(alert.id) }
                            )
                        }
                    }
                }
            }
        }
        is Resource.Error -> {
            // Show error message (optional)
        }
    }
}
