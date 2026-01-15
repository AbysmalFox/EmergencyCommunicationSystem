package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.max
import kotlin.math.min

/**
 * Category icon for alerts
 * Uses local Tabler vector drawables - works offline
 */
@Composable
fun CategoryIcon(
    alert: Alert,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val localeContext = getLocaleContext()
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    // Check for categories that use Tabler icons
    val isFire = categoryId == 4 || "fire" in categoryStr || "wildfire" in title
    val isHealth = categoryId == 3 || "health" in categoryStr
    val isGeneral = categoryId == 5 || "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title
    
    when {
        isFire -> {
            // Use Tabler flame icon for Fire
            Icon(
                painter = painterResource(id = R.drawable.ic_tabler_flame),
                contentDescription = "Fire",
                modifier = modifier,
                tint = tint
            )
        }
        isHealth -> {
            // Use Tabler clipboard-heart icon for Health
            Icon(
                painter = painterResource(id = R.drawable.ic_tabler_clipboard_heart),
                contentDescription = "Health",
                modifier = modifier,
                tint = tint
            )
        }
        isGeneral -> {
            // Use Tabler message-2-exclamation icon for General/Emergency
            Icon(
                painter = painterResource(id = R.drawable.ic_tabler_message_2_exclamation),
                contentDescription = "General",
                modifier = modifier,
                tint = tint
            )
        }
        else -> {
            // Use regular ImageVector icons for other categories
            Icon(
                imageVector = getIconForCategory(alert),
                contentDescription = getCategoryName(alert, localeContext),
                modifier = modifier,
                tint = tint
            )
        }
    }
}

@Composable
fun getIconForCategory(alert: Alert): ImageVector {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        // Category 1: Weather - Cloud icon
        categoryId == 1 || "weather" in categoryStr || "typhoon" in title || "storm" in title || "rain" in title || "heat" in title || "wind" in title -> AppIcons.Weather
        // Category 2: Earthquake - Warning/Alert icon
        categoryId == 2 || "earthquake" in categoryStr || "tremor" in title -> AppIcons.Earthquake
        // Category 4: Fire - Fire icon
        categoryId == 4 || "fire" in categoryStr || "wildfire" in title -> AppIcons.Fire
        // Category 5: General/Emergency - Info icon
        categoryId == 5 || "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title -> AppIcons.Info
        // Category 3: Health (if exists) - Health icon
        categoryId == 3 || "health" in categoryStr -> AppIcons.Health
        // Security - Security icon
        "security" in categoryStr -> AppIcons.Security
        // Water/Flood - Water drop icon
        "water" in categoryStr || "flood" in title -> AppIcons.Flood
        // Default fallback - Info icon
        else -> AppIcons.Info
    }
}

@Composable
fun getColorForCategory(alert: Alert): Color {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        // Category 1: Weather - Blue
        categoryId == 1 || "weather" in categoryStr || "typhoon" in title || "storm" in title || "rain" in title || "heat" in title || "wind" in title -> Color(0xFF4A90E2)
        // Category 2: Earthquake - Orange/Brown
        categoryId == 2 || "earthquake" in categoryStr || "tremor" in title -> Color(0xFFF57C00)
        // Category 4: Fire - Orange/Red
        categoryId == 4 || "fire" in categoryStr || "wildfire" in title -> Color(0xFFF5A623)
        // Category 5: General/Emergency - Teal/Cyan
        categoryId == 5 || "general" in categoryStr || "emergency" in categoryStr || "traffic" in title || "road" in title || "power" in title -> Color(0xFF50E3C2)
        // Category 3: Health - Teal
        categoryId == 3 || "health" in categoryStr -> Color(0xFF50E3C2)
        // Security - Red
        "security" in categoryStr -> Color(0xFFD0021B)
        // Water/Flood - Blue
        "water" in categoryStr || "flood" in title -> Color(0xFF4A90E2)
        // Default
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Determine severity level based on alert category and content
 * Returns: "High", "Medium", or "Low"
 */
fun getAlertSeverity(alert: Alert): String {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
    
    val category = alert.category?.lowercase(Locale.getDefault()) ?: ""
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val content = alert.content?.lowercase(Locale.getDefault()) ?: ""
    
    // High severity indicators (category 2 = Earthquake, category 4 = Fire)
    val highSeverityKeywords = listOf("fire", "flood", "earthquake", "tsunami", "typhoon", "storm", "crime", "security", "evacuate", "urgent", "emergency", "escape")
    
    // Medium severity indicators
    val mediumSeverityKeywords = listOf("warning", "caution", "advisory", "health", "weather", "traffic", "accident")
    
    val allText = "$category $title $content"
    
    return when {
        // Category 2 (Earthquake) and 4 (Fire) are high severity
        categoryId == 2 || categoryId == 4 -> "High"
        // Category 1 (Weather) is medium severity
        categoryId == 1 -> "Medium"
        // Category 5 (General/Emergency) - check content for urgency
        categoryId == 5 -> if (highSeverityKeywords.any { it in allText }) "High" else "Medium"
        highSeverityKeywords.any { it in allText } -> "High"
        mediumSeverityKeywords.any { it in allText } -> "Medium"
        else -> "Low"
    }
}

/**
 * Get severity color (Yellow/Orange/Red)
 */
@Composable
fun getSeverityColor(severity: String): Color {
    return when (severity) {
        "High" -> Color(0xFFD32F2F) // Red
        "Medium" -> Color(0xFFF57C00) // Orange
        "Low" -> Color(0xFFFBC02D) // Yellow
        else -> Color(0xFF757575) // Gray (default)
    }
}

/**
 * Get category name from numeric ID or fallback to string/title analysis
 * Returns English category name - will be translated in composable
 */
private fun getCategoryNameEn(alert: Alert): String {
    // Handle numeric category IDs from API (category comes as string "1", "2", "4", "5")
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        categoryId == 1 -> "Weather"
        categoryId == 2 -> "Earthquake"
        categoryId == 3 -> "Health"
        categoryId == 4 -> "Fire"
        categoryId == 5 -> "General"
        "weather" in title || "rain" in title || "storm" in title || "heat" in title || "wind" in title -> "Weather"
        "earthquake" in title || "tremor" in title -> "Earthquake"
        "fire" in title || "wildfire" in title -> "Fire"
        "health" in title -> "Health"
        "traffic" in title || "road" in title || "power" in title || "emergency" in title -> "General"
        else -> "General"
    }
}

/**
 * Get category name with translation support
 */
@Composable
fun getCategoryName(alert: Alert, localeContext: android.content.Context): String {
    val context = LocalContext.current
    val categoryNameEn = getCategoryNameEn(alert)
    
    // Get current language preference
    val currentLanguage by com.example.emergencycommunicationsystem.data.UserPrefs.getLanguage(context)
        .collectAsState(initial = "en")
    
    // State for translated category name
    var translatedCategory by remember { mutableStateOf(categoryNameEn) }
    
    // Translate category name when language changes
    LaunchedEffect(categoryNameEn, currentLanguage) {
        if (currentLanguage != "en") {
            translatedCategory = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                categoryNameEn,
                currentLanguage
            )
        } else {
            translatedCategory = categoryNameEn
        }
    }
    
    return translatedCategory
}

/**
 * Get alert type from a single alert
 */
private fun getAlertTypeFromAlert(alert: Alert): String {
    val categoryId = try {
        alert.category?.toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
    
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        categoryId == 2 || "earthquake" in categoryStr || "earthquake" in title || "tremor" in title -> "earthquake"
        categoryId == 4 || "fire" in categoryStr || "fire" in title || "wildfire" in title -> "fire"
        categoryId == 1 || "weather" in categoryStr || "flood" in title || "typhoon" in title || "storm" in title || "rain" in title -> "flood"
        else -> "general"
    }
}

/**
* Get compact emergency instructions for a single alert (returns English, will be translated in composable)
 */
private fun getCompactInstructions(alertType: String, @Suppress("UNUSED_PARAMETER") timeOfDay: String): Pair<String, List<String>> {
    return when (alertType) {
        "earthquake" -> Pair(
            "Duck, Cover, Hold",
            listOf("Drop to hands and knees", "Cover head and neck", "Hold on to sturdy furniture", "Stay away from windows")
        )
        "fire" -> Pair(
            "Do not use elevators",
            listOf("Use stairs only", "Stay low to avoid smoke", "Feel doors before opening", "Evacuate immediately")
        )
        "flood" -> Pair(
            "Move to higher ground",
            listOf("Avoid floodwaters", "Go to highest floor", "Turn off electricity if safe", "Listen to broadcasts")
        )
        else -> Pair(
            "Stay Calm",
            listOf("Follow authorities", "Keep contacts ready", "Stay informed", "Help others if safe")
        )
    }
}

/**
 * Compact Emergency Instructions for individual alerts
 */
@Composable
fun CompactEmergencyInstructions(
    alert: Alert,
    modifier: Modifier = Modifier
) {
    val localeContext = getLocaleContext()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val alertType = getAlertTypeFromAlert(alert)
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val timeOfDay = when (currentHour) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        in 18..22 -> "evening"
        else -> "night"
    }
    val (mainInstructionEn, stepsEn) = getCompactInstructions(alertType, timeOfDay)
    
    // Get current language preference
    val currentLanguage by com.example.emergencycommunicationsystem.data.UserPrefs.getLanguage(context)
        .collectAsState(initial = "en")
    
    // State for translated strings
    var translatedMainInstruction by remember { mutableStateOf(mainInstructionEn) }
    var translatedSteps by remember { mutableStateOf(stepsEn) }
    var translatedLabel by remember { mutableStateOf("What To Do:") }
    
    // Translate strings when language changes
    LaunchedEffect(alertType, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                // Translate "What To Do:" label
                translatedLabel = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                    "What To Do:",
                    currentLanguage
                )
                
                // Translate main instruction
                translatedMainInstruction = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                    mainInstructionEn,
                    currentLanguage
                )
                
                // Translate steps
                translatedSteps = com.example.emergencycommunicationsystem.util.TranslationService.translateBatch(
                    stepsEn,
                    currentLanguage
                )
            }
        } else {
            translatedLabel = "What To Do:"
            translatedMainInstruction = mainInstructionEn
            translatedSteps = stepsEn
        }
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = AppIcons.Info,
                    contentDescription = "Instructions",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🧠 $translatedLabel $translatedMainInstruction",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Compact steps (show first 3)
            translatedSteps.take(3).forEach { step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AlertItem(
    alert: Alert,
    onMessageClick: (id: String, title: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val localeContext = getLocaleContext()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                CategoryIcon(
                    alert = alert,
                    modifier = Modifier.size(40.dp).align(Alignment.Top),
                    tint = getColorForCategory(alert)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val categoryName = getCategoryName(alert, localeContext)
                    Text(
                        text = categoryName.uppercase(),
                        color = getColorForCategory(alert),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = alert.title ?: localeContext.getString(R.string.no_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.content ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Emergency Instructions Section
            Spacer(modifier = Modifier.height(12.dp))
            CompactEmergencyInstructions(alert = alert)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = alert.source ?: localeContext.getString(R.string.unknown_source),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = alert.timestamp ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        val userId = AuthManager.getUserId()
                        if (userId > 0) {
                            onMessageClick(alert.id.toString(), alert.title ?: "Chat")
                        } else {
                            Toast.makeText(context, localeContext.getString(R.string.please_login_to_send_message), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Message,
                        contentDescription = localeContext.getString(R.string.message),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localeContext.getString(R.string.message))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    onMessageClick: ((alertId: String, alertTitle: String) -> Unit)? = null
) {
    val localeContext = getLocaleContext()
    val state by viewModel.uiState.collectAsState()
    val isRefreshing = state is Resource.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadAlerts() }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Custom reduced-height TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .height(60.dp) // Reduced from default 64dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        localeContext.getString(R.string.alerts_and_notifications),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 110.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            when (val resource = state) {
                is Resource.Loading -> {
                    // Show full screen loader only if we have no data yet
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is Resource.Success -> {
                    val alerts = resource.data.filter { it.title != "General Inquiry" }
                    if (alerts.isEmpty()) {
                        EmptyAlertsView()
                    } else {
                        val listState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 136.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(alerts, key = { it.id }) { alert ->
                                    AlertItem(alert = alert) { alertId, alertTitle ->
                                        onMessageClick?.invoke(alertId, alertTitle)
                                    }
                                }
                            }
                            
                            // Scroll Indicator - positioned on the right edge
                            ScrollIndicator(
                                listState = listState,
                                itemCount = alerts.size,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 4.dp)
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    ErrorView(message = resource.message) { viewModel.loadAlerts() }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Custom Scroll Indicator Component
 * Shows a visual indicator on the right side to represent scroll position
 */
@Composable
fun ScrollIndicator(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    // Force recomposition when scroll state changes
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    
    // Only show if there are items and we can scroll
    if (visibleItemsInfo.isEmpty() || itemCount == 0 || itemCount <= visibleItemsInfo.size) {
        return
    }
    
    val firstVisibleItemIndex = visibleItemsInfo.firstOrNull()?.index ?: 0
    val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index ?: 0
    
    // Get scroll position information
    val firstVisibleItem = visibleItemsInfo.firstOrNull()
    val lastVisibleItem = visibleItemsInfo.lastOrNull()
    
    // Check if we're at the bottom - more lenient detection
    // Consider at bottom if:
    // 1. Last visible item is within the last 2 items, OR
    // 2. Last visible item is the last item, OR
    // 3. We're very close to the end of the viewport (within 150px)
    val viewportEndOffset = layoutInfo.viewportEndOffset
    val lastItemEndPosition = (lastVisibleItem?.offset ?: 0) + (lastVisibleItem?.size ?: 0)
    val distanceFromBottom = viewportEndOffset - lastItemEndPosition
    
    val isAtBottom = lastVisibleItemIndex >= itemCount - 2 || // Within last 2 items
                     (lastVisibleItemIndex >= itemCount - 1 && distanceFromBottom <= 150) // Last item and close to bottom
    
    // Calculate scroll progress based on first visible item
    // Account for partial scrolling within items
    val scrollProgress = if (isAtBottom) {
        1.0f // Force to bottom when detected
    } else {
        val firstItem = firstVisibleItem ?: return
        val firstItemProgress = if (firstItem.size > 0) {
            (-firstItem.offset).toFloat() / firstItem.size.toFloat()
        } else {
            0f
        }
        val itemBasedProgress = (firstItem.index + firstItemProgress) / (itemCount - 1).toFloat()
        // Boost progress when near the end to make it reach bottom faster
        val boostedProgress = if (itemBasedProgress > 0.8f) {
            // When past 80%, accelerate to reach 1.0 faster
            itemBasedProgress + (1.0f - itemBasedProgress) * 0.3f
        } else {
            itemBasedProgress
        }
        boostedProgress.coerceIn(0f, 1f)
    }
    
    // Calculate visible range as percentage of total items
    val visibleItemCount = (lastVisibleItemIndex - firstVisibleItemIndex + 1).toFloat()
    val visibleRange = visibleItemCount / itemCount.toFloat()
    val thumbHeightFraction = max(0.2f, min(0.5f, visibleRange)) // Min 20%, max 50% of track
    
    // Calculate thumb position (0.0 to 1.0 - thumbHeightFraction)
    // When at bottom, thumb should be at the bottom of the track
    val maxThumbPosition = 1f - thumbHeightFraction
    val thumbPositionFraction = if (isAtBottom) {
        maxThumbPosition // Position at bottom
    } else {
        (scrollProgress * maxThumbPosition).coerceIn(0f, maxThumbPosition)
    }
    
    BoxWithConstraints(
        modifier = modifier
            .width(6.dp) // Made wider for better visibility
            .fillMaxHeight()
    ) {
        val trackHeight = this.maxHeight
        val thumbHeight = trackHeight * thumbHeightFraction
        val thumbOffset = trackHeight * thumbPositionFraction
        
        // Track (background) - more visible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(3.dp)
                )
        )
        
        // Thumb (scroll indicator) - more visible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbHeight)
                .offset(y = thumbOffset)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), // More opaque
                    shape = RoundedCornerShape(3.dp)
                )
        )
    }
}

@Composable
fun EmptyAlertsView() {
    val localeContext = getLocaleContext()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = AppIcons.NotificationsOff,
            contentDescription = localeContext.getString(R.string.no_new_alerts),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            localeContext.getString(R.string.no_new_alerts),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            localeContext.getString(R.string.community_alerts_will_appear_here),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    val localeContext = getLocaleContext()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = localeContext.getString(R.string.failed_to_load_alerts),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(localeContext.getString(R.string.retry))
        }
    }
}
