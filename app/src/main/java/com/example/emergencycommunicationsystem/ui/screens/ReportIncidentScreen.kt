package com.example.emergencycommunicationsystem.ui.screens

import android.graphics.Color as AndroidColor
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.viewmodel.ReportIncidentViewModel
import com.example.emergencycommunicationsystem.viewmodel.ReportState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentScreen(
    weatherState: WeatherState,
    onBackPressed: () -> Unit,
    reportViewModel: ReportIncidentViewModel = viewModel()
) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    var incidentDetails by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val incidentTypes = mapOf(
        localeContext.getString(R.string.incident_fire) to AppIcons.LocalFireDepartment,
        localeContext.getString(R.string.incident_flood) to AppIcons.Flood,
        localeContext.getString(R.string.incident_medical) to AppIcons.MedicalServices,
        localeContext.getString(R.string.incident_accident) to AppIcons.Traffic,
        localeContext.getString(R.string.incident_earthquake) to AppIcons.Earthquake,
        localeContext.getString(R.string.incident_other) to AppIcons.Info
    )
    var selectedIncidentType by remember { mutableStateOf(incidentTypes.keys.first()) }

    val urgencyLevels = listOf(
        localeContext.getString(R.string.urgency_low),
        localeContext.getString(R.string.urgency_medium),
        localeContext.getString(R.string.urgency_high)
    )
    var selectedUrgency by remember { mutableStateOf(urgencyLevels[0]) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    val reportState by reportViewModel.reportState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(reportState) {
        when (val state = reportState) {
            is ReportState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                reportViewModel.resetState()
                onBackPressed()
            }
            is ReportState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                reportViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            // Custom reduced-height TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .height(40.dp) // Reduced from default 64dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        localeContext.getString(R.string.report_incident),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 4.dp)
            )
                }
            }
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
                item { MapView(weatherState) }

                item {
                    IncidentTypeSelector(
                        incidentTypes = incidentTypes,
                        selectedType = selectedIncidentType,
                        onTypeSelect = { selectedIncidentType = it },
                        title = localeContext.getString(R.string.type_of_incident)
                    )
                }

                item {
                    UrgencySelector(
                        urgencyLevels = urgencyLevels,
                        selectedUrgency = selectedUrgency,
                        onUrgencySelect = { selectedUrgency = it },
                        title = localeContext.getString(R.string.urgency_level)
                    )
                }

                item {
                    FormTextField(
                        value = reporterName,
                        onValueChange = { reporterName = it },
                        label = localeContext.getString(R.string.reporter_name_label),
                        placeholder = localeContext.getString(R.string.reporter_name_placeholder)
                    )
                }

                item {
                    FormTextField(
                        value = incidentDetails,
                        onValueChange = { incidentDetails = it },
                        label = localeContext.getString(R.string.details_label),
                        placeholder = localeContext.getString(R.string.details_placeholder),
                        modifier = Modifier.height(120.dp)
                    )
                }

                item {
                    ImageAttachment(
                        imageUri = imageUri,
                        onAttachImage = { imagePickerLauncher.launch("image/*") },
                        attachLabel = localeContext.getString(R.string.attach_photo),
                        changeLabel = localeContext.getString(R.string.change_photo)
                    )
                }
            }

            Button(
                onClick = {
                    val userId = AuthManager.getUserId()
                    if (userId > 0 && weatherState is WeatherState.Success) {
                        reportViewModel.submitReport(
                            context = context,
                            userId = userId,
                            incidentType = selectedIncidentType,
                            urgency = selectedUrgency,
                            details = incidentDetails,
                            latitude = weatherState.lat,
                            longitude = weatherState.lon,
                            address = weatherState.address,
                            reporterName = reporterName,
                            imageUri = imageUri
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = reportState !is ReportState.Loading
            ) {
                if (reportState is ReportState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = localeContext.getString(R.string.submit_report),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MapView(weatherState: WeatherState) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val context = LocalContext.current
    var address by remember { mutableStateOf(localeContext.getString(R.string.detecting_location)) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val locationPoint = if (weatherState is WeatherState.Success) {
        GeoPoint(weatherState.lat, weatherState.lon)
    } else {
        GeoPoint(0.0, 0.0) // Default location
    }

    val blinkingDrawable = remember {
        val redDot = ShapeDrawable(OvalShape()).apply {
            paint.color = AndroidColor.RED
            intrinsicWidth = 32
            intrinsicHeight = 32
        }
        val transparentDot = ShapeDrawable(OvalShape()).apply {
            paint.color = AndroidColor.TRANSPARENT
            intrinsicWidth = 32
            intrinsicHeight = 32
        }

        AnimationDrawable().apply {
            addFrame(redDot, 500)
            addFrame(transparentDot, 500)
            isOneShot = false
        }
    }

    LaunchedEffect(weatherState) {
        if (weatherState is WeatherState.Success) {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(weatherState.lat, weatherState.lon, 1)
                address = results?.firstOrNull()?.getAddressLine(0) ?: localeContext.getString(R.string.address_not_found)
                weatherState.address = address
            } catch (e: Exception) {
                address = localeContext.getString(R.string.could_not_determine_address)
            }
        }
    }

    Column {
        Text(
            text = localeContext.getString(R.string.location_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            AndroidView(
                factory = {
                    MapView(it).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(locationPoint)
                        mapView = this
                    }
                },
                update = {
                    it.controller.setCenter(locationPoint)
                    val marker = Marker(it).apply {
                        position = locationPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = blinkingDrawable
                    }
                    it.overlays.clear()
                    it.overlays.add(marker)
                    it.invalidate()
                    blinkingDrawable.start()
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = address, style = MaterialTheme.typography.bodyMedium)
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onPause()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentTypeSelector(
    incidentTypes: Map<String, ImageVector>,
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    title: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                leadingIcon = {
                    Icon(
                        imageVector = incidentTypes[selectedType] ?: AppIcons.Info,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                incidentTypes.forEach { (type, icon) ->
                    DropdownMenuItem(
                        text = { Text(text = type) },
                        leadingIcon = {
                            Icon(imageVector = icon, contentDescription = null)
                        },
                        onClick = {
                            onTypeSelect(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UrgencySelector(
    urgencyLevels: List<String>,
    selectedUrgency: String,
    onUrgencySelect: (String) -> Unit,
    title: String
) {
    val lowUrgencyColor = Color(0xFF3B82F6) // A calm blue
    val mediumUrgencyColor = Color(0xFFF59E0B) // A cautionary orange
    val highUrgencyColor = MaterialTheme.colorScheme.error // The theme's error color for high alert

    Column {
        Text(
            text = title,
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
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.6f)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ImageAttachment(
    imageUri: Uri?,
    onAttachImage: () -> Unit,
    attachLabel: String,
    changeLabel: String
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
            Icon(AppIcons.AddPhoto, contentDescription = "Add Photo", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(if (imageUri == null) attachLabel else changeLabel)
        }
    }
}
