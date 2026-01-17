package com.example.emergencycommunicationsystem.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.emergencycommunicationsystem.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.ui.components.SafeOverlay
import com.example.emergencycommunicationsystem.ui.components.CompactAlertCard
import com.example.emergencycommunicationsystem.ui.components.EmergencyCallButton
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.SoftShadow
import com.example.emergencycommunicationsystem.ui.theme.StatusDanger
import com.example.emergencycommunicationsystem.ui.theme.StatusSafe
import com.example.emergencycommunicationsystem.ui.theme.StatusWarning
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// The primary teal color from Alertaraqc widget
val AlertaraTeal = Color(0xFF508684)
val AlertaraTealLight = Color(0xFF669997)
val AlertaraTealAccent = Color(0xFFB2DFDB)

// General background colors (darker for better hierarchy)
val AlertaraBgLight = Color(0xFF2D5A57) // Darker greenish background for better visual hierarchy
val AlertaraBgDark = Color(0xFF121F1E)

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
    
    // Alerts ViewModel
    val alertsViewModel: AlertsViewModel = viewModel()
    val alertsState by alertsViewModel.uiState.collectAsState(initial = Resource.Loading)
    val weatherState by weatherViewModel.weatherState.collectAsState(initial = WeatherState.Loading)
    
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
                weatherViewModel.setLocationPermissionDenied()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!weatherViewModel.hasLoadedData) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                weatherViewModel.requestLocationAndFetchWeather()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
        alertsViewModel.loadAlerts()
    }

    // state to control the safe overlay
    var showSafeOverlay by remember { mutableStateOf(false) }

    // Use explicit system theme detection
    val isDarkMode = isSystemInDarkTheme()
    val screenBgColor = if (isDarkMode) AlertaraBgDark else AlertaraBgLight

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
                    
                    // Active Alerts Section
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
                                        CompactWeatherCard(state, isDarkMode)
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
                text = "LIVE QC",
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
            .shadow(
                elevation = if (isDarkMode) 12.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.1f),
                ambientColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.06f)
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
                    label = "Active Alerts",
                    value = alertCount.toString(),
                    icon = AppIcons.Alerts,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    onTeal = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    label = "High Priority",
                    value = highPriorityCount.toString(),
                    icon = AppIcons.Warning,
                    color = Color(0xFFFFD54F), // Amber for high priority
                    modifier = Modifier.weight(1f),
                    onTeal = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    label = "Temp",
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
                shape = RoundedCornerShape(16.dp)
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
    val localeContext = getLocaleContext()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = localeContext.getString(R.string.report_incident),
            icon = R.drawable.ic_tabler_file_alert,
            color = StatusDanger,
            onClick = onReportClick,
            modifier = Modifier.weight(1f),
            isDarkMode = isDarkMode
        )
        QuickActionCard(
            title = localeContext.getString(R.string.i_am_safe),
            icon = R.drawable.ic_tabler_shield_check,
            color = StatusSafe,
            onClick = onSafeClick,
            modifier = Modifier.weight(1f),
            isDarkMode = isDarkMode
        )
        QuickActionCard(
            title = "Message",
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
    Card(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.08f),
                ambientColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AlertaraTeal
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/**
 * Modern Alerts Section
 */
@Composable
fun ModernAlertsSection(
    alertsState: Resource<List<Alert>>,
    userLat: Double?,
    userLon: Double?,
    onAlertClick: (Int) -> Unit,
    isDarkMode: Boolean
) {
    when (alertsState) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(strokeWidth = 3.dp)
            }
        }
        is Resource.Success -> {
            val alerts = alertsState.data.take(3)
            if (alerts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = AlertaraTeal,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Alerts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${alertsState.data.size}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        alerts.forEach { alert ->
                            val distanceKm = if (userLat != null && userLon != null &&
                                alert.latitude != null && alert.longitude != null) {
                                LocationUtils.calculateDistance(
                                    userLat, userLon,
                                    alert.latitude, alert.longitude
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
        is Resource.Error -> {}
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
 * Enhanced Weather Card with Hourly Forecast and More Details
 */
@Composable
fun CompactWeatherCard(
    weatherState: WeatherState.Success,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AlertaraTeal
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Section: Temperature and Main Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weather",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = weatherState.location,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "${weatherState.temperature.substringBefore(".")}°C",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Middle Section: Weather Details (Feels Like, Humidity, Wind)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(Icons.Default.LightMode, "Feels", "${weatherState.feelsLike.substringBefore(".")}°", true)
                WeatherDetailItem(Icons.Default.WaterDrop, "Humidity", weatherState.humidity, true)
                WeatherDetailItem(Icons.Default.Air, "Wind", weatherState.windSpeed, true)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Section: Hourly Forecast
            if (weatherState.forecastData.isNotEmpty()) {
                Text(
                    text = "Forecast",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(weatherState.forecastData.take(8)) { item ->
                        val timeFormatter = SimpleDateFormat("ha", Locale.getDefault())
                        val time = timeFormatter.format(Date(item.dt * 1000L))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = time, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.main.temp.toInt()}°",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Advice Bar with Typewriter Effect
            if (weatherState.advice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    TypewriterText(
                        text = weatherState.advice,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, label: String, value: String, onTeal: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (onTeal) Color.White else MaterialTheme.colorScheme.primary
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
    val contextualTip = remember(alerts) {
        val mostRecentAlert = alerts.firstOrNull()?.category?.lowercase() ?: ""
        when {
            mostRecentAlert.contains("flood") -> "Move to higher ground. Do not walk or drive through flood waters."
            mostRecentAlert.contains("fire") -> "Stay low to the ground. Evacuate immediately if instructed."
            mostRecentAlert.contains("earthquake") -> "Drop, Cover, and Hold On. Stay away from windows."
            mostRecentAlert.contains("storm") -> "Secure outdoor items. Stay indoors away from windows."
            else -> "Always keep an emergency kit ready with water, food, and medicine."
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AlertaraTeal
        )
    ) {
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
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Preparedness Guide",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TypewriterText(
                    text = contextualTip,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TAP FOR FULL GUIDES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
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
                    color = AlertaraTeal.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}
