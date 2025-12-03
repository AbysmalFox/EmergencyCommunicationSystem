package com.example.emergencycommunicationsystem.ui.screens

import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.ui.components.SectionTitle
import java.util.Locale

// --- Theme Colors ---
val deepNavyBlue = Color(0xFF1A1E29)
val vibrantSafetyOrange = Color(0xFFFF7F00)
val subtleGray = Color(0xFF2C313D)
val textWhite = Color.White.copy(alpha = 0.9f)
val textGray = Color.White.copy(alpha = 0.6f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentScreen(
    weatherState: WeatherState,
    onBackPressed: () -> Unit
) {
    var incidentDetails by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    val incidentTypes = mapOf(
        "Fire" to Icons.Filled.LocalFireDepartment,
        "Flood" to Icons.Filled.Flood,
        "Medical" to Icons.Filled.MedicalServices,
        "Crime" to Icons.Filled.LocalPolice
    )
    var selectedIncidentType by remember { mutableStateOf(incidentTypes.keys.first()) }
    val urgencyLevels = listOf("Low", "Medium", "High")
    var selectedUrgency by remember { mutableStateOf(urgencyLevels.first()) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Incident", color = textWhite) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = deepNavyBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Submit Report */ },
                containerColor = vibrantSafetyOrange,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Submit Report",
                    tint = Color.White
                )
            }
        },
        containerColor = deepNavyBlue
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    "Report an Incident",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textWhite
                )
            }
            item { MapPreview(weatherState) }
            item { IncidentTypeSelector(incidentTypes, selectedIncidentType) { selectedIncidentType = it } }
            item { UrgencySelector(urgencyLevels, selectedUrgency) { selectedUrgency = it } }
            item {
                ModernTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = "Your Name (Optional)",
                    placeholder = "Defaults to Anonymous"
                )
            }
            item {
                ModernTextField(
                    value = incidentDetails,
                    onValueChange = { incidentDetails = it },
                    label = "Details of Incident",
                    placeholder = "Provide as much detail as possible...",
                    modifier = Modifier.height(120.dp)
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected image preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, vibrantSafetyOrange),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = vibrantSafetyOrange)
                    ) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = "Add Photo", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(if (imageUri == null) "Attach Photo" else "Change Photo")
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) } // Space for FAB
        }
    }
}

@Composable
fun MapPreview(weatherState: WeatherState) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("Detecting location...") }

    LaunchedEffect(weatherState) {
        if (weatherState is WeatherState.Success) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(weatherState.lat, weatherState.lon, 1)
                address = results?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
            } catch (e: Exception) {
                address = "Could not determine address"
            }
        } else {
            address = "Location not available"
        }
    }

    SectionTitle("Location", color = textWhite)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = subtleGray)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for a map view
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location Pin",
                    tint = vibrantSafetyOrange,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = address,
                    color = textGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun IncidentTypeSelector(
    incidentTypes: Map<String, ImageVector>,
    selectedType: String,
    onTypeSelect: (String) -> Unit
) {
    SectionTitle("Type of Incident", color = textWhite)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        incidentTypes.forEach { (type, icon) ->
            val isSelected = type == selectedType
            val backgroundColor = if (isSelected) vibrantSafetyOrange else subtleGray
            val contentColor = if (isSelected) Color.White else textWhite
            val borderColor = if (isSelected) vibrantSafetyOrange else Color.Transparent

            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(16.dp))
                    .clickable { onTypeSelect(type) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, borderColor),
                colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = icon, contentDescription = type, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(type, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun UrgencySelector(
    urgencyLevels: List<String>,
    selectedUrgency: String,
    onUrgencySelect: (String) -> Unit
) {
    SectionTitle("Urgency Level", color = textWhite)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        urgencyLevels.forEach { level ->
            val isSelected = level == selectedUrgency
            val buttonColors = if (isSelected) {
                ButtonDefaults.buttonColors(containerColor = vibrantSafetyOrange, contentColor = Color.White)
            } else {
                ButtonDefaults.outlinedButtonColors(containerColor = subtleGray, contentColor = textWhite)
            }
            val modifier = if (isSelected) Modifier.weight(1f).shadow(8.dp, RoundedCornerShape(12.dp)) else Modifier.weight(1f)

            Button(
                onClick = { onUrgencySelect(level) },
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                colors = buttonColors,
                border = if (!isSelected) BorderStroke(1.dp, textGray) else null,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(level, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    SectionTitle(label, color = textWhite)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = textGray) },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = subtleGray,
            unfocusedContainerColor = subtleGray,
            focusedBorderColor = vibrantSafetyOrange,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = vibrantSafetyOrange,
            unfocusedLabelColor = textGray,
            cursorColor = vibrantSafetyOrange,
            focusedTextColor = textWhite,
            unfocusedTextColor = textWhite,
        )
    )
}
