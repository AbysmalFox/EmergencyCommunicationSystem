package com.example.emergencycommunicationsystem.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.emergencycommunicationsystem.util.localizedStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.ui.components.SafeOverlay
import com.example.emergencycommunicationsystem.ui.components.CompactAlertCard
import com.example.emergencycommunicationsystem.ui.components.EmergencyCallButton
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.StatusDanger
import com.example.emergencycommunicationsystem.ui.theme.StatusSafe
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.util.getAlertSeverity
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.ui.theme.themeShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

import com.example.emergencycommunicationsystem.util.TextToSpeechHelper

// The primary teal color from Alertaraqc widget
val AlertaraTeal = Color(0xFF508684)
val AlertaraTealLight = Color(0xFF669997)
val AlertaraTealAccent = Color(0xFFB2DFDB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEmergencyCallClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    onMessageClick: () -> Unit = {},
    onAlertClick: (Int) -> Unit = {},
    onEmergencyGuidesClick: () -> Unit = {},
    weatherViewModel: WeatherViewModel
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val animationState = remember { MutableTransitionState(false).apply { targetState = true } }

    val pullToRefreshState = rememberPullToRefreshState()
    
    // TTS Helper
    val ttsHelper = remember { TextToSpeechHelper(context) }
    // Clean up TTS on dispose
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }
    
    // Alerts ViewModel
    val alertsViewModel: AlertsViewModel = viewModel()
    val alertsState by alertsViewModel.uiState.collectAsState(initial = Resource.Loading)
    val weatherState by weatherViewModel.weatherState.collectAsState(initial = WeatherState.Loading)
    
    val currentLanguage by com.example.emergencycommunicationsystem.data.UserPrefs.getLanguage(context).collectAsState(initial = "en")

    // Get user location for distance calculation
    val userLocation = when (val state = weatherState) {
        is WeatherState.Success -> Pair(state.lat, state.lon)
        else -> null
    }
    val userLat = userLocation?.first
    val userLon = userLocation?.second

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        scope.launch {
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                weatherViewModel.requestLocationAndFetchWeather()
            } else {
                weatherViewModel.requestLocationAndFetchWeather()
            }
        }
    }

    LaunchedEffect(Unit, currentLanguage) {
        if (!weatherViewModel.hasLoadedData) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                weatherViewModel.requestLocationAndFetchWeather()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        } else if (weatherViewModel.lastUsedLanguage != currentLanguage) {
             // Language changed, force reload
             weatherViewModel.reloadWeather(currentLanguage)
        }
        alertsViewModel.loadAlerts()
    }

    // state to control the safe overlay
    var showSafeOverlay by remember { mutableStateOf(false) }

    // Use Main Theme palette
    val isDarkMode = ThemeManager.isDarkMode()
    val screenBgColor = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize().background(screenBgColor)) {
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    try {
                        weatherViewModel.requestLocationAndFetchWeather()
                        alertsViewModel.loadAlerts()
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            val bottomNavReserved = 140.dp
            val listState = rememberLazyListState()
            
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomNavReserved, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Section with Stats
                    item {
                        AnimatedVisibility(
                            visibleState = animationState,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                                    slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(durationMillis = 500))
                        ) {
                            DashboardHeroSection(
                                alertsState = alertsState,
                                weatherState = weatherState,
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                    
                    // Emergency Call Button - Prominent
                    item {
                        AnimatedVisibility(
                            visibleState = animationState,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 100)) +
                                    slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 100))
                        ) {
                            EmergencyCallButton(onClick = onEmergencyCallClick)
                        }
                    }
                    
                    // Quick Actions Grid
                    item {
                        AnimatedVisibility(
                            visibleState = animationState,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200)) +
                                    slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 200))
                        ) {
                            QuickActionsGrid(
                                onReportClick = onReportIncidentClick,
                                onSafeClick = { if (!showSafeOverlay) showSafeOverlay = true },
                                onMessageClick = onMessageClick,
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                    
                    // Active Alerts Section - ULTRA COMPACT
                    item {
                        AnimatedVisibility(
                            visibleState = animationState,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 300)) +
                                    slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 300))
                        ) {
                            ModernAlertsSection(
                                alertsState = alertsState,
                                userLat = userLat,
                                userLon = userLon,
                                onAlertClick = onAlertClick,
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                    
                    // Weather & Instructions Row
                    item {
                        AnimatedVisibility(
                            visibleState = animationState,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 400)) +
                                    slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 400))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Weather Widget - Enhanced
                                when (val state = weatherState) {
                                    is WeatherState.Success -> {
                                        CompactWeatherCard(state, isDarkMode, ttsHelper, currentLanguage)
                                    }
                                    else -> {}
                                }
                                
                                // Emergency Instructions - Enhanced
                                val alerts = (alertsState as? Resource.Success)?.data ?: emptyList()
                                CompactInstructionsCard(
                                    alerts = alerts,
                                    onClick = onEmergencyGuidesClick,
                                    isDarkMode = isDarkMode
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                
                // Scroll Indicator
                ScrollIndicator(
                    listState = listState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }
        }

        // Overlay is drawn on top
        SafeOverlay(visible = showSafeOverlay, onDismiss = { showSafeOverlay = false })
    }
}

/**
 * Modern Real-time clock for Quezon City (Asia/Manila)
 */
@Composable
fun QCRealTimeClock(useLightColor: Boolean = false) {
    var currentTime by remember { 
        mutableStateOf(Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))) 
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
            delay(1000)
        }
    }
    
    val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault()).apply { 
        timeZone = TimeZone.getTimeZone("Asia/Manila")
    }
    val secondsFormatter = SimpleDateFormat(":ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Manila")
    }
    val amPmFormatter = SimpleDateFormat(" a", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Manila")
    }
    val dateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Manila")
    }
    
    val baseTextColor = if (useLightColor) Color.White else MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = if (useLightColor) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    val accentColor = if (useLightColor) AlertaraTealAccent else MaterialTheme.colorScheme.primary
    
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(if (useLightColor) Color(0xFFFF5252) else StatusDanger)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = localizedStringResource(R.string.live_qc),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (useLightColor) Color.White else StatusDanger,
                letterSpacing = 0.5.sp
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = timeFormatter.format(currentTime.time),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = baseTextColor,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = secondsFormatter.format(currentTime.time),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = amPmFormatter.format(currentTime.time),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(
            text = dateFormatter.format(currentTime.time),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = secondaryTextColor.copy(alpha = 0.5f)
        )
    }
}

/**
 * Modern Hero Section with Statistics
 */
@Composable
fun DashboardHeroSection(
    alertsState: Resource<List<Alert>>,
    weatherState: WeatherState,
    isDarkMode: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroBgAnimation")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // A subtle diagonal light sweep effect
    val animatedBrush = Brush.linearGradient(
        colors = listOf(
            AlertaraTeal,
            AlertaraTeal,
            AlertaraTealLight,
            AlertaraTeal,
            AlertaraTeal
        ),
        start = Offset(shimmerTranslate - 1000f, shimmerTranslate - 1000f),
        end = Offset(shimmerTranslate, shimmerTranslate)
    )

    val alertCount = when (alertsState) {
        is Resource.Success -> alertsState.data.size
        else -> 0
    }
    
    val highPriorityCount = when (alertsState) {
        is Resource.Success -> alertsState.data.count { getAlertSeverity(it) == "High" }
        else -> 0
    }
    
    val temperature = when (weatherState) {
        is WeatherState.Success -> weatherState.temperature.substringBefore("°")
        else -> "--"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(
                elevation = if (isDarkMode) 12.dp else 8.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AlertaraTeal // Base color fallback
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedBrush) // The animated gradient background
                .padding(20.dp)
        ) {
            // Header Row with Branding and Real-time Clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alertaraqc Branding
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertara",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = "qc",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = AlertaraTealAccent, // Light teal accent
                        letterSpacing = (-1.5).sp
                    )
                }
                
                QCRealTimeClock(useLightColor = true)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    label = localizedStringResource(R.string.active_alerts_title),
                    value = alertCount.toString(),
                    icon = AppIcons.Alerts,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    onTeal = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    label = localizedStringResource(R.string.high_priority),
                    value = highPriorityCount.toString(),
                    icon = AppIcons.Warning,
                    color = Color(0xFFFFD54F), // Amber for high priority
                    modifier = Modifier.weight(1f),
                    onTeal = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    label = localizedStringResource(R.string.temp),
                    value = "$temperature°",
                    icon = AppIcons.Thermostat,
                    color = Color(0xFF81D4FA), // Light blue for temp
                    modifier = Modifier.weight(1f),
                    onTeal = true
                )
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onTeal: Boolean = false
) {
    Column(
        modifier = modifier
            .background(
                color = if (onTeal) Color.White.copy(alpha = 0.15f) else color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 4.dp, 
                    bottomEnd = 16.dp, 
                    bottomStart = 4.dp
                )
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (onTeal) Color.White else MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (onTeal) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
    }
}


/**
 * Quick Actions Grid - Modern Card Layout
 */
@Composable
fun QuickActionsGrid(
    onReportClick: () -> Unit,
    onSafeClick: () -> Unit,
    onMessageClick: () -> Unit,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = localizedStringResource(R.string.report_incident),
            icon = R.drawable.ic_tabler_file_alert,
            color = StatusDanger,
            onClick = onReportClick,
            modifier = Modifier.weight(1f),
            isDarkMode = isDarkMode
        )
        QuickActionCard(
            title = localizedStringResource(R.string.i_am_safe),
            icon = R.drawable.ic_tabler_shield_check,
            color = StatusSafe,
            onClick = onSafeClick,
            modifier = Modifier.weight(1f),
            isDarkMode = isDarkMode
        )
        QuickActionCard(
            title = localizedStringResource(R.string.message),
            icon = R.drawable.ic_tabler_message_plus,
            color = AlertaraTeal,
            onClick = onMessageClick,
            modifier = Modifier.weight(1f),
            isDarkMode = isDarkMode
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    @androidx.annotation.DrawableRes icon: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    // Unique leaf-like shape
    val uniqueShape = RoundedCornerShape(
        topStart = 4.dp, 
        topEnd = 24.dp, 
        bottomStart = 24.dp, 
        bottomEnd = 4.dp
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .themeShadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = uniqueShape
            ),
        shape = uniqueShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Modern Alerts Section - Updated to be even more compact
 */
@Composable
fun ModernAlertsSection(
    alertsState: Resource<List<Alert>>,
    userLat: Double?,
    userLon: Double?,
    onAlertClick: (Int) -> Unit,
    isDarkMode: Boolean
) {
    val primaryColor = if (isDarkMode) MaterialTheme.colorScheme.primary else Color(0xFF34635D)

    when (alertsState) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            }
        }
        is Resource.Success -> {
            val alerts = alertsState.data
            if (alerts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Alerts,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localizedStringResource(R.string.active_alerts_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        Text(
                            text = localizedStringResource(R.string.view_all),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.clickable { /* Navigate */ }
                        )
                    }
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(alerts.take(5)) { alert ->
                            val distanceKm = if (userLat != null && userLon != null &&
                                alert.latitude != null && alert.longitude != null) {
                                LocationUtils.calculateDistance(userLat, userLon, alert.latitude, alert.longitude)
                            } else null
                            
                            Box(modifier = Modifier.width(220.dp)) {
                                CompactAlertCard(
                                    alert = alert,
                                    distanceKm = distanceKm,
                                    severity = getAlertSeverity(alert),
                                    onClick = { onAlertClick(alert.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

/**
 * Typewriter Text Animation Component
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    speedMillis: Long = 40,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
) {
    var textToDisplay by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        textToDisplay = ""
        text.forEach { char ->
            textToDisplay += char
            delay(speedMillis)
        }
    }

    Text(
        text = textToDisplay,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize,
        lineHeight = lineHeight
    )
}

/**
 * Enhanced Weather Card with Hourly Forecast
 */
@Composable
fun CompactWeatherCard(
    weatherState: WeatherState.Success,
    isDarkMode: Boolean,
    ttsHelper: TextToSpeechHelper? = null,
    languageCode: String = "en"
) {
    val primaryColor = if (isDarkMode) MaterialTheme.colorScheme.primary else Color(0xFF34635D)
    val isSpeaking by ttsHelper?.isSpeaking?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Section: Info on left, Temperature + Icon on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = localizedStringResource(R.string.weather_forecast),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (weatherState.isOffline) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color.Red.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = localizedStringResource(R.string.label_offline),
                                    color = Color.Red,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        // TTS Button
                        Spacer(modifier = Modifier.width(8.dp))
                        if (weatherState.advice.isNotEmpty()) {
                            IconButton(
                                onClick = { ttsHelper?.speak(weatherState.advice, languageCode) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) AppIcons.MicOff else AppIcons.VolumeUp, // Using VolumeUp/MicOff as placeholder
                                    contentDescription = "Read Aloud",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = weatherState.location,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${weatherState.temperature.substringBefore(".")}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AlertaraTeal.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = weatherState.iconUrl,
                            contentDescription = weatherState.condition,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Middle Section: Weather Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(Icons.Default.LightMode, localizedStringResource(R.string.feels_like), "${weatherState.feelsLike.substringBefore(".")}°", false, isDarkMode)
                WeatherDetailItem(Icons.Default.WaterDrop, localizedStringResource(R.string.humidity), weatherState.humidity, false, isDarkMode)
                WeatherDetailItem(Icons.Default.Air, localizedStringResource(R.string.wind), weatherState.windSpeed, false, isDarkMode)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Section: Hourly Forecast
            if (weatherState.forecastData.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(weatherState.forecastData.take(8)) { item ->
                        val timeFormatter = SimpleDateFormat("ha", Locale.getDefault())
                        val time = timeFormatter.format(Date(item.dt * 1000L))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.main.temp.toInt()}°",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Advice Bar with Reporter Image and AI Text
            if (weatherState.advice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryColor.copy(alpha = 0.06f))
                        .border(1.dp, primaryColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box {
                            Image(
                                painter = painterResource(id = R.drawable.reporter),
                                contentDescription = "AI Assistant",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.1f)),
                                contentScale = ContentScale.Crop
                            )
                            
                            if (weatherState.isOffline) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(Color.White, CircleShape)
                                        .padding(2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            TypewriterText(
                                text = weatherState.advice,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, label: String, value: String, onTeal: Boolean = false, isDarkMode: Boolean = false) {
    val primaryColor = if (isDarkMode) MaterialTheme.colorScheme.primary else Color(0xFF34635D)
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (onTeal) Color.White else primaryColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label, 
                fontSize = 9.sp, 
                color = if (onTeal) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold, 
                color = if (onTeal) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Enhanced Emergency Guide Card with Contextual Tips
 */
@Composable
fun CompactInstructionsCard(
    alerts: List<Alert>,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val mostRecentAlert = alerts.firstOrNull()?.category?.lowercase() ?: ""
    val contextualTip = when {
        mostRecentAlert.contains("flood") -> localizedStringResource(R.string.tip_flood)
        mostRecentAlert.contains("fire") -> localizedStringResource(R.string.tip_fire)
        mostRecentAlert.contains("earthquake") -> localizedStringResource(R.string.tip_earthquake)
        mostRecentAlert.contains("storm") -> localizedStringResource(R.string.tip_storm)
        else -> localizedStringResource(R.string.tip_default)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.guide_img),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(120.dp)
                    .offset(x = 10.dp, y = 1.dp),
                contentScale = ContentScale.Fit
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = null,
                            tint = if (isDarkMode) MaterialTheme.colorScheme.primary else Color(0xFF34635D),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizedStringResource(R.string.preparedness_guide),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TypewriterText(
                        text = contextualTip,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizedStringResource(R.string.tap_for_full_guides),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDarkMode) MaterialTheme.colorScheme.primary else Color(0xFF34635D).copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Custom Scroll Indicator Component
 */
@Composable
private fun ScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    
    if (visibleItemsInfo.isEmpty() || totalItemsCount == 0 || totalItemsCount <= visibleItemsInfo.size) {
        return
    }
    
    val firstVisibleItem = visibleItemsInfo.firstOrNull()
    val lastVisibleItem = visibleItemsInfo.lastOrNull()
    val viewportEndOffset = layoutInfo.viewportEndOffset
    val lastItemEndPosition = (lastVisibleItem?.offset ?: 0) + (lastVisibleItem?.size ?: 0)
    val distanceFromBottom = viewportEndOffset - lastItemEndPosition
    
    val isAtBottom = (lastVisibleItem?.index ?: 0) >= totalItemsCount - 2 ||
                     ((lastVisibleItem?.index ?: 0) >= totalItemsCount - 1 && distanceFromBottom <= 150)
    
    val scrollProgress = if (isAtBottom) {
        1.0f
    } else {
        val firstItem = firstVisibleItem ?: return
        val firstItemProgress = if (firstItem.size > 0) {
            (-firstItem.offset).toFloat() / firstItem.size.toFloat()
        } else {
            0f
        }
        val itemBasedProgress = (firstItem.index + firstItemProgress) / (totalItemsCount - 1).toFloat()
        val boostedProgress = if (itemBasedProgress > 0.8f) {
            itemBasedProgress + (1.0f - itemBasedProgress) * 0.3f
        } else {
            itemBasedProgress
        }
        boostedProgress.coerceIn(0f, 1f)
    }
    
    val visibleItemCount = ((lastVisibleItem?.index ?: 0) - (firstVisibleItem?.index ?: 0) + 1).toFloat()
    val visibleRange = visibleItemCount / totalItemsCount.toFloat()
    val thumbHeightFraction = max(0.2f, min(0.5f, visibleRange))
    
    val maxThumbPosition = 1f - thumbHeightFraction
    val thumbPositionFraction = if (isAtBottom) {
        maxThumbPosition
    } else {
        (scrollProgress * maxThumbPosition).coerceIn(0f, maxThumbPosition)
    }
    
    BoxWithConstraints(
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight()
    ) {
        val trackHeight = this.maxHeight
        val thumbHeight = trackHeight * thumbHeightFraction
        val thumbOffset = trackHeight * thumbPositionFraction
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbHeight)
                .offset(y = thumbOffset)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}
