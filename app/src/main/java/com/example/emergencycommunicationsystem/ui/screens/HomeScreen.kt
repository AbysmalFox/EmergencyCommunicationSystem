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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.emergencycommunicationsystem.ui.components.WeatherWidget
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onEmergencyCallClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    weatherViewModel: WeatherViewModel
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val animationState = remember { MutableTransitionState(false).apply { targetState = true } }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                weatherViewModel.requestLocationAndFetchWeather()
                isRefreshing = false
            }
        }
    )

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(27.dp) // Reduced spacing
        ) {
            item {
                Spacer(modifier = Modifier.height(9.dp)) // Reduced spacing
                AnimatedVisibility(
                    visibleState = animationState,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                            slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(durationMillis = 500))
                ) {
                    Column {
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
                    ActionGrid(onEmergencyCallClick, onReportClick = onReportIncidentClick, onSafeClick = {}) 
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

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}
