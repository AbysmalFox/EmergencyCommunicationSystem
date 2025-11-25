package com.example.myfirstapp.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapp.data.models.LatLng
import com.example.myfirstapp.data.models.WeatherState
import com.example.myfirstapp.ui.components.SegmentedButtonRow
import com.example.myfirstapp.ui.components.SectionTitle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentScreen(
    weatherState: WeatherState,
    onBackPressed: () -> Unit
) {
    // --- Incident state ---
    var incidentDetails by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    val incidentTypes = listOf("Fire", "Flood", "Medical", "Crime")
    var selectedIncidentType by remember { mutableStateOf(incidentTypes.first()) }
    val urgencyLevels = listOf("Low", "Medium", "High")
    var selectedUrgency by remember { mutableStateOf(urgencyLevels.first()) }

    // --- User location from weatherState ---
    val userLocation: LatLng? = if (weatherState is WeatherState.Success) {
        LatLng(weatherState.lat, weatherState.lon)
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Incident") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header ---
            item {
                Text(
                    "Report an Incident",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Your current location will be attached automatically.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Map ---
            item {
                LocationWidget(weatherState)
            }

            // --- Incident Type ---
            item {
                SectionTitle("Type of Incident")
                SegmentedButtonRow(incidentTypes, selectedIncidentType) { selectedIncidentType = it }
            }

            // --- Urgency Level ---
            item {
                SectionTitle("Urgency Level")
                SegmentedButtonRow(urgencyLevels, selectedUrgency) { selectedUrgency = it }
            }

            // --- Reporter Name ---
            item {
                SectionTitle("Your Name (Optional)")
                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Defaults to Anonymous") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // --- Incident Details ---
            item {
                SectionTitle("Details of Incident")
                OutlinedTextField(
                    value = incidentDetails,
                    onValueChange = { incidentDetails = it },
                    label = { Text("Provide as much detail as possible...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // --- Attach Photo Button ---
            item {
                OutlinedButton(
                    onClick = { /* TODO: Attach photo */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Attach Photo", color = MaterialTheme.colorScheme.primary)
                }
            }

            // --- Action Buttons ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onBackPressed,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { /* TODO: Submit report */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Submit Report")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationWidget(weatherState: WeatherState) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("Detecting location...") }

    // Only attempt to get address if weatherState has coordinates
    LaunchedEffect(weatherState) {
        if (weatherState is WeatherState.Success) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val result = geocoder.getFromLocation(weatherState.lat, weatherState.lon, 1)
                address = result?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
            } catch (e: Exception) {
                address = "Unable to get address"
            }
        } else {
            address = "Location not available"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(address, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
