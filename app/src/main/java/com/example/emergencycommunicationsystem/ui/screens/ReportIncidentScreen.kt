
package com.example.emergencycommunicationsystem.ui.screens

import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.data.models.WeatherState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentScreen(
    weatherState: WeatherState,
    onBackPressed: () -> Unit
) {
    var incidentDetails by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val incidentTypes = mapOf(
        "Fire" to Icons.Default.LocalFireDepartment,
        "Flood" to Icons.Default.Flood,
        "Medical" to Icons.Default.MedicalServices,
        "Crime" to Icons.Default.LocalPolice
    )
    var selectedIncidentType by remember { mutableStateOf(incidentTypes.keys.first()) }

    val urgencyLevels = listOf("Low", "Medium", "High")
    var selectedUrgency by remember { mutableStateOf(urgencyLevels[0]) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp) // Adjusted padding
            ) {
                item { MapPreview(weatherState) }

                item {
                    IncidentTypeSelector(
                        incidentTypes = incidentTypes,
                        selectedType = selectedIncidentType,
                        onTypeSelect = { selectedIncidentType = it }
                    )
                }

                item {
                    UrgencySelector(
                        urgencyLevels = urgencyLevels,
                        selectedUrgency = selectedUrgency,
                        onUrgencySelect = { selectedUrgency = it }
                    )
                }

                item {
                    FormTextField(
                        value = reporterName,
                        onValueChange = { reporterName = it },
                        label = "Your Name (Optional)",
                        placeholder = "Defaults to Anonymous"
                    )
                }

                item {
                    FormTextField(
                        value = incidentDetails,
                        onValueChange = { incidentDetails = it },
                        label = "Details of Incident",
                        placeholder = "Provide as much detail as possible...",
                        modifier = Modifier.height(120.dp)
                    )
                }

                item {
                    ImageAttachment(
                        imageUri = imageUri,
                        onAttachImage = { imagePickerLauncher.launch("image/*") }
                    )
                }
            }

            Button(
                onClick = { /* TODO: Submit Report */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Submit Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
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

    Column {
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Pin",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    Column {
        Text(
            text = "Type of Incident",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            incidentTypes.forEach { (type, icon) ->
                val isSelected = type == selectedType
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { onTypeSelect(type) },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(imageVector = icon, contentDescription = type, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(type, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                    }
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
    val lowUrgencyColor = Color(0xFF3B82F6) // A calm blue
    val mediumUrgencyColor = Color(0xFFF59E0B) // A cautionary orange
    val highUrgencyColor = MaterialTheme.colorScheme.error // The theme's error color for high alert

    Column {
        Text(
            text = "Urgency Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            urgencyLevels.forEach { level ->
                val isSelected = level == selectedUrgency
                val (containerColor, contentColor) = when {
                    isSelected && level == "Low" -> lowUrgencyColor to Color.White
                    isSelected && level == "Medium" -> mediumUrgencyColor to Color.White
                    isSelected && level == "High" -> highUrgencyColor to Color.White
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Button(
                    onClick = { onUrgencySelect(level) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                    elevation = if (isSelected) ButtonDefaults.buttonElevation(4.dp) else null
                ) {
                    Text(level, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ImageAttachment(
    imageUri: Uri?,
    onAttachImage: () -> Unit
) {
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
                    .clip(MaterialTheme.shapes.large)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedButton(
            onClick = onAttachImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(if (imageUri == null) "Attach Photo" else "Change Photo")
        }
    }
}
