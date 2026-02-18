package com.example.emergencycommunicationsystem.ui.screens

import android.Manifest
import android.os.Build
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import com.example.emergencycommunicationsystem.util.localizedStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.emergencycommunicationsystem.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.ui.components.SafeOverlay
import com.example.emergencycommunicationsystem.ui.components.CompactAlertCard
import com.example.emergencycommunicationsystem.ui.components.InternetCallSlider
import com.example.emergencycommunicationsystem.ui.components.WeatherWidget
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.StatusDanger
import com.example.emergencycommunicationsystem.ui.theme.StatusSafe
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.getAlertSeverity
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import com.example.emergencycommunicationsystem.viewmodel.AlertWithDistance
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.ui.theme.themeShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.example.emergencycommunicationsystem.util.WeatherIconUtils
import com.example.emergencycommunicationsystem.util.TextToSpeechHelper

// The primary teal color from Alertaraqc widget
val AlertaraTeal = Color(0xFF508684)
val AlertaraTealLight = Color(0xFF669997)
val AlertaraTealAccent = Color(0xFFB2DFDB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEmergencyCallClick: () -> Unit,
    onInternetCallClick: () -> Unit,
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

    LaunchedEffect(userLocation) {
        userLocation?.let { (lat, lon) ->
            alertsViewModel.updateUserLocation(lat, lon)
        }
    }

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
            
            LazyColumn(
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
                
                // Primary Emergency Call Slider (Blue) - Removed the upper white slider as requested
                item {
                    AnimatedVisibility(
                        visibleState = animationState,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 150)) +
                                slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 150))
                    ) {
                        InternetCallSlider(
                            onCallInitiated = onInternetCallClick,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                            onEmergencyCallPageClick = onEmergencyCallClick,
                            isDarkMode = isDarkMode,
                            currentLanguage = currentLanguage
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
                        Column {
                            HorizontalRule()
                            ModernAlertsSection(
                                alertsState = alertsState,
                                onAlertClick = onAlertClick,
                                isDarkMode = isDarkMode
                            )
                            HorizontalRule()
                        }
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
                            WeatherWidget(
                                state = weatherState,
                                onRetry = { scope.launch { weatherViewModel.requestLocationAndFetchWeather() } }
                            )
                            
                            // Emergency Instructions - Enhanced
                            val alerts = (alertsState as? Resource.Success)?.data?.map { it.alert } ?: emptyList()
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
        }

        // Overlay is drawn on top
        SafeOverlay(visible = showSafeOverlay, onDismiss = { showSafeOverlay = false })
    }
}

/**
 * Custom Horizontal Rule
 */
@Composable
fun HorizontalRule(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

/**
 * Modern Real-time clock for Quezon City (Asia/Manila)
 */
@Composable
fun QCRealTimeClock(useLightColor: Boolean = false) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

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
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Manila")
        }
    }
    val secondsFormatter = remember {
        SimpleDateFormat(":ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Manila")
        }
    }
    val amPmFormatter = remember {
        SimpleDateFormat(" a", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Manila")
        }
    }
    val dateFormatter = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Manila")
        }
    }

    val date = remember(currentTime) { Date(currentTime) }

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
                text = timeFormatter.format(date),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = baseTextColor,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = secondsFormatter.format(date),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = amPmFormatter.format(date),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(
            text = dateFormatter.format(date),
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
    alertsState: Resource<List<AlertWithDistance>>,
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
        is Resource.Success -> alertsState.data.map { it.alert }.count { getAlertSeverity(it) == "High" }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max), // Ensures all children take the height of the tallest child
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    label = localizedStringResource(R.string.active_alerts_title),
                    value = alertCount.toString(),
                    icon = painterResource(id = R.drawable.ic_tabler_bell_ringing),
                    color = Color.White,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onTeal = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    label = localizedStringResource(R.string.urgency_high),
                    value = highPriorityCount.toString(),
                    icon = painterResource(id = R.drawable.ic_tabler_file_alert),
                    color = Color(0xFFFFD54F), // Amber for high priority
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onTeal = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    label = localizedStringResource(R.string.temp),
                    value = "$temperature°",
                    icon = painterResource(id = R.drawable.ic_tabler_thermometer),
                    color = Color(0xFF81D4FA), // Light blue for temp
                    modifier = Modifier.weight(1f).fillMaxHeight(),
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
    icon: androidx.compose.ui.graphics.painter.Painter,
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
            painter = icon,
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
    onEmergencyCallPageClick: () -> Unit,
    isDarkMode: Boolean,
    currentLanguage: String = "en"
) {
    // Use "Chat with Responder" as requested
    val chatTitle = localizedStringResource(R.string.message)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Report Incident & Chat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = localizedStringResource(R.string.report_incident),
                icon = R.drawable.ic_tabler_file_alert,
                containerColor = if (isDarkMode) Color(0xFFD32F2F).copy(alpha = 0.85f) else Color(0xFFE57373),
                contentColor = Color.White,
                onClick = onReportClick,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
            QuickActionCard(
                title = chatTitle,
                icon = R.drawable.ic_tabler_message_plus,
                containerColor = AlertaraTeal,
                contentColor = Color.White,
                onClick = onMessageClick,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
        }
        
        // Row 2: I Am Safe & Emergency Call Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // I Am Safe Card
            QuickActionCard(
                title = localizedStringResource(R.string.i_am_safe),
                icon = R.drawable.ic_tabler_shield_check,
                containerColor = if (isDarkMode) Color(0xFF388E3C).copy(alpha = 0.85f) else Color(0xFF81C784),
                contentColor = Color.White,
                onClick = onSafeClick,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode,
                isFullWidth = false
            )

            // Emergency Call Menu Card
            QuickActionCard(
                title = "EMERGENCY CONTACTS", 
                icon = R.drawable.ic_tabler_phone, 
                containerColor = if (isDarkMode) Color(0xFFF57C00).copy(alpha = 0.85f) else Color(0xFFFFB74D),
                contentColor = Color.White,
                onClick = onEmergencyCallPageClick,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode,
                isFullWidth = false
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    @androidx.annotation.DrawableRes icon: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    isFullWidth: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    // Standardized Rounded Rectangle Shape
    val standardShape = RoundedCornerShape(16.dp)
    
    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .height(if (isFullWidth) 70.dp else 110.dp) // Lower height for full width button
            .themeShadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = standardShape
            ),
        shape = standardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        // Remove border for solid color buttons, or keep it subtle
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        if (isFullWidth) {
            // Horizontal layout for full width button
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Vertical layout for square-ish buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Modern Alerts Section - Updated to be even more compact
 */
@Composable
fun ModernAlertsSection(
    alertsState: Resource<List<AlertWithDistance>>,
    onAlertClick: (Int) -> Unit,
    isDarkMode: Boolean
) {
    val activeAlertsBg = if (isDarkMode) Color(0xFF0B1A15) else Color(0xFFCE9A9A)
    val activeAlertsCardBg = if (isDarkMode) Color(0xFF10251E) else Color(0xFFDAFFE7)
    val activeAlertsCardBorder = if (isDarkMode) Color(0xFF244237) else Color(0xFF8FB0A3)
    val cardStroke = MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkMode) 0.2f else 0.08f)
    val gold = Color(0xFFF4C55E)
    val deepNavy = Color(0xFF0B1E2D)
    val ocean = Color(0xFF0E6B6A)
    val headerBrush = Brush.linearGradient(
        colors = listOf(
            if (isDarkMode) Color(0xFF0D2B26) else Color(0xFF1D6E63),
            if (isDarkMode) Color(0xFF10352F) else Color(0xFF207C70)
        )
    )
    val ambientBrush = Brush.linearGradient(
        colors = listOf(
            if (isDarkMode) Color(0xFF081611) else Color(0xFFA8C3B8),
            if (isDarkMode) Color(0xFF0B1F19) else Color(0xFF397C64)
        )
    )
    val indicatorTransition = rememberInfiniteTransition(label = "indicatorTransition")
    val alpha1 by indicatorTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0.1f at 0
                1f at 800
                0.1f at 1600
                0.1f at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )
    val alpha2 by indicatorTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0.1f at 400
                1f at 1200
                0.1f at 2000
                0.1f at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )
    val alpha3 by indicatorTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0.1f at 800
                1f at 1600
                0.1f at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha3"
    )

    when (alertsState) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            }
        }
        is Resource.Success -> {
            val alertsWithDistance = alertsState.data
            if (alertsWithDistance.isNotEmpty()) {
                val totalAlerts = alertsWithDistance.size
                val highPriorityCount = alertsWithDistance.count { getAlertSeverity(it.alert) == "High" }
                val alertsToShow = alertsWithDistance.take(5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .themeShadow(
                                elevation = if (isDarkMode) 14.dp else 10.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = activeAlertsBg),
                        border = BorderStroke(1.dp, cardStroke)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ambientBrush)
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(headerBrush)
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(gold.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = AppIcons.Alerts,
                                                    contentDescription = null,
                                                    tint = gold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = localizedStringResource(R.string.active_alerts_title),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = (-0.2).sp,
                                                    color = Color.White
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF2ECC71))
                                                    )
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = localizedStringResource(R.string.live_monitoring),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White.copy(alpha = 0.9f)
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Surface(
                                                color = gold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(999.dp),
                                                border = BorderStroke(1.dp, gold.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = localizedStringResource(R.string.alerts_active_count, totalAlerts),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = gold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = gold,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.clickable { onAlertClick(0) }
                                            ) {
                                                Text(
                                                    text = localizedStringResource(R.string.view_all),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = deepNavy,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (highPriorityCount > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Keep the "high priority" indicator visually inside the Live Monitoring header.
                                            val highChipBg = Color(0xFFE53935).copy(alpha = 0.18f)
                                            val highChipBorder = Color(0xFFFFCDD2).copy(alpha = 0.45f)
                                            Surface(
                                                color = highChipBg,
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, highChipBorder)
                                            ) {
                                                Text(
                                                    text = localizedStringResource(R.string.high_priority_count, highPriorityCount),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFFCDD2),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            Text(
                                                text = localizedStringResource(R.string.priority_feed),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    // Issued timestamp moved into each alert card
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(start = 8.dp, end = 40.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(alertsToShow) { alertWithDistance ->
                                        val issued = alertWithDistance.alert.timestamp?.let { formatAlertRelativeTime(it) }
                                        CompactAlertCard(
                                            alert = alertWithDistance.alert,
                                            distanceKm = alertWithDistance.distanceKm,
                                            severity = getAlertSeverity(alertWithDistance.alert),
                                            issuedText = issued,
                                            onClick = { onAlertClick(alertWithDistance.alert.id) },
                                            containerColorOverride = activeAlertsCardBg,
                                            borderColorOverride = activeAlertsCardBorder,
                                            modifier = Modifier.width(280.dp)
                                        )
                                    }
                                }
                                
                                // Refined scrolling indicator - modern alternative to ">>>"
                                Row(
                                    modifier = Modifier
                                        .padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((-11).dp)
                                ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = (if (isDarkMode) Color(0xFF00B0FF) else Color.Black).copy(alpha = alpha1),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = (if (isDarkMode) Color(0xFF00B0FF) else Color.Black).copy(alpha = alpha2),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = (if (isDarkMode) Color(0xFF00B0FF) else Color.Black).copy(alpha = alpha3),
                                            modifier = Modifier.size(22.dp)
                                        )
                                }
                            }

                            Row(
                                modifier = Modifier.padding(top = 10.dp, start = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = localizedStringResource(R.string.swipe_to_browse),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White.copy(alpha = 0.8f)
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
    speedMillis: Long = 20,
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

private fun formatAlertRelativeTime(timestamp: String): String {
    return try {
        val nowMillis = System.currentTimeMillis()
        val alertMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val odt = OffsetDateTime.parse(timestamp.replace(" ", "T") + "Z")
            odt.toInstant().toEpochMilli()
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestamp)
            date?.time ?: nowMillis
        }

        val diffMillis = (nowMillis - alertMillis).coerceAtLeast(0L)
        val minutes = (diffMillis / 60000L).toInt()
        val hours = (diffMillis / 3600000L).toInt()
        val days = (diffMillis / 86400000L).toInt()

        when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min${if (minutes == 1) "" else "s"} ago"
            hours < 24 -> "$hours hr${if (hours == 1) "" else "s"} ago"
            days < 7 -> "$days d ago"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())
                formatter.format(java.time.Instant.ofEpochMilli(alertMillis))
            }
            else -> {
                val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
                formatter.format(Date(alertMillis))
            }
        }
    } catch (e: Exception) {
        "recently"
    }
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
                                    imageVector = if (isSpeaking) AppIcons.MicOff else AppIcons.VolumeUp,
                                    contentDescription = "Read Aloud",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${weatherState.temperature.substringBefore(".")}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )

                    Text(
                        text = weatherState.location,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(WeatherIconUtils.getWeatherAnimation(weatherState.condition))
                                .crossfade(true)
                                .build(),
                            contentDescription = weatherState.condition,
                            modifier = Modifier.size(54.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val conditionText = when (weatherState.condition.lowercase()) {
                        "clear" -> localizedStringResource(R.string.weather_sunny)
                        "clouds" -> localizedStringResource(R.string.weather_cloudy)
                        "rain" -> localizedStringResource(R.string.weather_rainy)
                        "drizzle" -> localizedStringResource(R.string.weather_drizzle)
                        "thunderstorm" -> localizedStringResource(R.string.weather_thunderstorm)
                        "snow" -> localizedStringResource(R.string.weather_snow)
                        "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> localizedStringResource(R.string.weather_mist)
                        else -> weatherState.condition
                    }

                    Text(
                        text = conditionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
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
                WeatherDetailItem(AppIcons.Wind, localizedStringResource(R.string.wind), weatherState.windSpeed, false, isDarkMode)
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
                                painter = painterResource(id = R.drawable.report),
                                contentDescription = "AI Assistant",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.1f)),
                                contentScale = ContentScale.Crop
                            )
                            
                            if (weatherState.isOffline) {
                                Icon(
                                    imageVector = AppIcons.CloudOff,
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
                // Reserve space for the right-side illustration so text never overlaps it.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 92.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tabler_message_2_exclamation),
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
