package com.example.emergencycommunicationsystem.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.emergencycommunicationsystem.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.ui.components.ActionGrid
import com.example.emergencycommunicationsystem.ui.components.SafeOverlay
import com.example.emergencycommunicationsystem.ui.components.WeatherWidget
import com.example.emergencycommunicationsystem.ui.components.EmergencyInstructions
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

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

    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                try {
                    weatherViewModel.requestLocationAndFetchWeather()
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        // Reserve bottom padding so the last content (weather widget) scrolls above the floating nav
        val bottomNavReserved = 140.dp // Increased padding
        val listState = rememberLazyListState()
        
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = bottomNavReserved),
                verticalArrangement = Arrangement.spacedBy(20.dp) // Better spacing
            ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                            slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(durationMillis = 500))
                ) {
                    val localeContext = getLocaleContext()
                    val bgColor = MaterialTheme.colorScheme.background
                    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
                    
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = if (isDarkMode) {
                                        listOf(Color.Transparent, Color.Transparent)
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    }
                                )
                            )
                            .padding(vertical = 16.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            localeContext.getString(R.string.emergency_dashboard),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            localeContext.getString(R.string.dashboard_subtitle),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200)) +
                            slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 200))
                ) {
                    ActionGrid(
                        onEmergencyCallClick = onEmergencyCallClick,
                        onReportClick = onReportIncidentClick,
                        onSafeClick = { if (!showSafeOverlay) showSafeOverlay = true },
                        onMessageClick = onMessageClick
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
                    ActiveAlertsSection(
                        alertsState = alertsState,
                        userLat = userLat,
                        userLon = userLon,
                        onAlertClick = onAlertClick
                    )
                }
            }
            
            // Emergency Instructions Section
            item {
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 400))
                ) {
                    when (val state = alertsState) {
                        is com.example.emergencycommunicationsystem.util.Resource.Success -> {
                            EmergencyInstructions(
                                alerts = state.data,
                                userLat = userLat,
                                userLon = userLon,
                                onClick = onEmergencyGuidesClick
                            )
                        }
                        else -> {
                            EmergencyInstructions(
                                alerts = emptyList(),
                                userLat = userLat,
                                userLon = userLon,
                                onClick = onEmergencyGuidesClick
                            )
                        }
                    }
                }
            }
            
            item {
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 500)) +
                            slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 500))
                ) {
                    WeatherWidget(weatherState)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) } // Reduced spacing
            }
            
            // Scroll Indicator
            ScrollIndicator(
                listState = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }

        // Overlay is drawn on top
        SafeOverlay(visible = showSafeOverlay, onDismiss = { showSafeOverlay = false })
    }
}

/**
 * Custom Scroll Indicator Component
 * Shows a visual indicator on the right side to represent scroll position
 */
@Composable
private fun ScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    // Force recomposition when scroll state changes
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    
    // Only show if there are items and we can scroll
    if (visibleItemsInfo.isEmpty() || totalItemsCount == 0 || totalItemsCount <= visibleItemsInfo.size) {
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
    // 3. We're very close to the end of the viewport (within 100px)
    val viewportEndOffset = layoutInfo.viewportEndOffset
    val lastItemEndPosition = (lastVisibleItem?.offset ?: 0) + (lastVisibleItem?.size ?: 0)
    val distanceFromBottom = viewportEndOffset - lastItemEndPosition
    
    val isAtBottom = lastVisibleItemIndex >= totalItemsCount - 2 || // Within last 2 items
                     (lastVisibleItemIndex >= totalItemsCount - 1 && distanceFromBottom <= 150) // Last item and close to bottom
    
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
        val itemBasedProgress = (firstItem.index + firstItemProgress) / (totalItemsCount - 1).toFloat()
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
    val visibleRange = visibleItemCount / totalItemsCount.toFloat()
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