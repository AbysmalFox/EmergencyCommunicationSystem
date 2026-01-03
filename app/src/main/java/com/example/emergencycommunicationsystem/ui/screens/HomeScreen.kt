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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.ui.components.ActionGrid
import com.example.emergencycommunicationsystem.ui.components.SafeOverlay
import com.example.emergencycommunicationsystem.ui.components.WeatherWidget
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEmergencyCallClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    onMessageClick: () -> Unit = {},
    weatherViewModel: WeatherViewModel
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val animationState = remember { MutableTransitionState(false).apply { targetState = true } }

    val pullToRefreshState = rememberPullToRefreshState()

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
        val bottomNavReserved = 136.dp // navOverlayHeight (92) + navOverlayLift (36) + extra (8)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = bottomNavReserved),
            verticalArrangement = Arrangement.spacedBy(27.dp) // Reduced spacing
        ) {
            item {
                Spacer(modifier = Modifier.height(9.dp)) // Reduced spacing
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                            slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(durationMillis = 500))
                ) {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            stringResource(R.string.emergency_dashboard),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            stringResource(R.string.dashboard_subtitle),
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.7f)
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
            item {
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(durationMillis = 500, delayMillis = 400))
                ) {
                    WeatherWidget(weatherViewModel.weatherState.collectAsState().value)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) } // Reduced spacing
        }

        // Overlay is drawn on top
        SafeOverlay(visible = showSafeOverlay, onDismiss = { showSafeOverlay = false })
    }
}
