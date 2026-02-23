package com.example.emergencycommunicationsystem.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.ui.components.PulsingCircleOverlay
import com.example.emergencycommunicationsystem.util.highKeywords
import com.example.emergencycommunicationsystem.util.LocationUtils
import kotlin.math.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.emergencycommunicationsystem.util.NavigationManager
import com.example.emergencycommunicationsystem.data.network.RoutingService
import com.example.emergencycommunicationsystem.data.network.WeatherApiClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.emergencycommunicationsystem.util.LogFilter
import com.example.emergencycommunicationsystem.util.WeatherIconUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emergencycommunicationsystem.data.models.SafeZone
import com.example.emergencycommunicationsystem.data.models.SafeZoneType
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.ui.theme.BrandTealAccent
import com.example.emergencycommunicationsystem.ui.theme.BrandDeepTeal
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.util.getLocaleContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

import com.example.emergencycommunicationsystem.viewmodel.AlertsViewModel
import com.example.emergencycommunicationsystem.viewmodel.MapViewModel
import com.example.emergencycommunicationsystem.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "MapScreen"
private const val QC_WEATHER_CACHE_PREFS = "qc_weather_map_cache"
private const val QC_WEATHER_CACHE_KEY = "qc_weather_entries_v1"

private class WeatherMarkerInfoWindow(mapView: MapView) :
    MarkerInfoWindow(R.layout.weather_marker_info_window, mapView) {

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val view = mView ?: return

        val titleView = view.findViewById<TextView>(R.id.weather_info_title)
        val snippetView = view.findViewById<TextView>(R.id.weather_info_snippet)
        val closeButton = view.findViewById<ImageButton>(R.id.weather_info_close)

        titleView.text = marker.title ?: "Weather"
        snippetView.text = marker.snippet ?: ""
        closeButton.setOnClickListener { close() }
    }

    override fun onClose() {
        val view = mView ?: return
        view.findViewById<TextView>(R.id.weather_info_title)?.text = ""
        view.findViewById<TextView>(R.id.weather_info_snippet)?.text = ""
        view.findViewById<ImageButton>(R.id.weather_info_close)?.setOnClickListener(null)
    }
}

private class FixedSizeAnimatableDrawable(
    private val inner: Drawable,
    private val sizePx: Int
) : Drawable(), Animatable {
    override fun draw(canvas: Canvas) {
        inner.bounds = bounds
        inner.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        inner.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        inner.colorFilter = colorFilter
    }

    @Suppress("DEPRECATION")
    override fun getOpacity(): Int = inner.opacity

    override fun getIntrinsicWidth(): Int = sizePx

    override fun getIntrinsicHeight(): Int = sizePx

    override fun start() {
        (inner as? Animatable)?.start()
    }

    override fun stop() {
        (inner as? Animatable)?.stop()
    }

    override fun isRunning(): Boolean = (inner as? Animatable)?.isRunning == true
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val alertsViewModel = viewModel<AlertsViewModel>()
    val mapViewModel = viewModel<MapViewModel>()
    
    val alertsState by alertsViewModel.uiState.collectAsState()
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    val localeContext = getLocaleContext()
    val userLocation by mapViewModel.userLocation.collectAsState()

    // State to hold MapView and Overlay for interaction
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var currentRoutePolyline by remember { mutableStateOf<Polyline?>(null) }

    // QC boundary reference (moved to top-level so DisposableEffect can clean it up)
    var qcBoundaryRef by remember { mutableStateOf<Polygon?>(null) }

    // QC-first initialization flag
    var hasInitialCameraSet by remember { mutableStateOf(false) }

    // Session flag: track if the user explicitly activated camera lock in this session
    var userActivatedCameraLock by remember { mutableStateOf(false) }
    
    // Map State from ViewModel
    val routeDestination by mapViewModel.routeDestination.collectAsState()
    val isCalculatingRoute by mapViewModel.isCalculatingRoute.collectAsState()
    val isCameraLocked by mapViewModel.isCameraLocked.collectAsState()
    
    // Map Filters State
    var showAlerts by remember { mutableStateOf(true) }
    var showWeather by remember { mutableStateOf(true) }
    var showHospitals by remember { mutableStateOf(true) }
    var showEvacuationCenters by remember { mutableStateOf(true) }
    val qcWeatherCache = remember { mutableStateMapOf<String, CachedQcWeather>() }
    
    // Navigation Manager from ViewModel
    val navigationManager = mapViewModel.navigationManager
    val navigationState by mapViewModel.navigationState.collectAsState()

    // 1. Handle Lifecycle (Critical for osmdroid location updates)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                // Ensure we stop location updates and clear overlays to avoid leaked resources
                locationOverlay?.disableMyLocation()
            } catch (e: Exception) {
                // Swallow to avoid crashing during teardown
            }

            try {
                // Remove overlays and detach the MapView safely
                mapView?.overlays?.clear()
                mapView?.onDetach()
            } catch (e: Exception) {
                // Ignore errors during cleanup
            }

            // Null out references to allow GC and prevent using stale objects after returning
            mapView = null
            locationOverlay = null
            qcBoundaryRef = null
            currentRoutePolyline = null
        }
    }

    // 2. Handle Permissions
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            if (granted) {
                locationOverlay?.enableMyLocation()
                mapViewModel.startLocationUpdates()
            }
        }
    )
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            mapViewModel.startLocationUpdates()
        }
    }

    // LaunchedEffect to observe userLocation changes and update route polyline
    LaunchedEffect(userLocation, routeDestination) {
        val currentUserLocation = userLocation ?: return@LaunchedEffect
        val selectedDestination = routeDestination ?: return@LaunchedEffect
        
        // Remove old polyline to prevent memory leaks and visual ghosting
        currentRoutePolyline?.let { oldPolyline ->
            mapView?.overlays?.remove(oldPolyline)
        }
        
        // Create new polyline: Restore detailed route if navigating, otherwise fallback
        val routeGeom = navigationState.routeGeometry
        val updatedPolyline = if (navigationState.isNavigating && routeGeom.isNotEmpty()) {
             Polyline().apply {
                 setPoints(routeGeom)
                 color = android.graphics.Color.parseColor("#FF9800") // Orange
                 width = 14.0f
                 isGeodesic = false
             }
        } else {
             fallbackToStraightLine(
                map = mapView ?: return@LaunchedEffect,
                userLocation = currentUserLocation,
                destination = selectedDestination,
                context = context,
                showToast = false
            )
        }
        
        // Add the new polyline to the map
        mapView?.overlays?.add(updatedPolyline)
        currentRoutePolyline = updatedPolyline
        mapView?.invalidate()
    }

    val isDarkMode = ThemeManager.isDarkMode()

    Box(modifier = Modifier.fillMaxSize()) {
        val qcPoints = getQuezonCityBoundaryPoints()

        // QC-first camera defaults
        val qcCenter = GeoPoint(14.6760, 121.0437)
        val initialZoom = 12.0

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    // Apply Dark Mode Tiles Filter
                    if (isDarkMode) {
                        this.overlayManager.tilesOverlay.setColorFilter(
                            android.graphics.ColorMatrixColorFilter(
                                floatArrayOf(
                                    -1f, 0f, 0f, 0f, 255f, // Red inversion
                                    0f, -1f, 0f, 0f, 255f, // Green inversion
                                    0f, 0f, -1f, 0f, 255f, // Blue inversion
                                    0f, 0f, 0f, 1f, 0f     // Alpha unchanged
                                )
                            )
                        )
                    } else {
                        this.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                    
                    // AGGRESSIVE tile loading optimization to minimize HWUI logs
                    try {
                        minZoomLevel = 12.0
                        maxZoomLevel = 18.0
                        isHorizontalMapRepetitionEnabled = false
                        isVerticalMapRepetitionEnabled = false
                        setBuiltInZoomControls(false)
                    } catch (e: Exception) {
                        LogFilter.w(TAG, "Could not apply all tile optimizations: ${e.message}")
                    }

                    // QC Boundary
                    val boundaryColor = if (isDarkMode) {
                        Color.White.copy(alpha = 0.6f).toArgb()
                    } else {
                        Color(0xFF008080).copy(alpha = 0.7f).toArgb()
                    }
                    val qcBoundary = Polygon().apply {
                        setPoints(qcPoints)
                        fillPaint.color = Color(0.2f, 0.2f, 1.0f, 0.2f).toArgb()
                        outlinePaint.color = boundaryColor
                        outlinePaint.strokeWidth = 2.0f
                    }
                    overlays.add(qcBoundary)
                    qcBoundaryRef = qcBoundary

                    // Zoom to QC initially ONLY if not restoring a state
                    val minLat = qcPoints.minOf { it.latitude }
                    val maxLat = qcPoints.maxOf { it.latitude }
                    val minLon = qcPoints.minOf { it.longitude }
                    val maxLon = qcPoints.maxOf { it.longitude }
                    val boundingBox = BoundingBox(maxLat, maxLon, minLat, minLon)
                    
                    post {
                        // Only zoom to QC once on first load (QC-first view)
                        if (!hasInitialCameraSet && routeDestination == null) {
                            // This forces the map to show the entire city boundary
                            zoomToBoundingBox(boundingBox, false)
                            controller.setZoom(12.0)
                            hasInitialCameraSet = true
                        }
                    }

                    // User Location
                    val overlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    overlay.enableMyLocation()
                    overlays.add(overlay)
                    locationOverlay = overlay
                }
            }
        )

        // Update QC boundary color when theme changes
        LaunchedEffect(isDarkMode) {
            qcBoundaryRef?.let { boundary ->
                val color = if (isDarkMode) Color.White.copy(alpha = 0.6f).toArgb() else Color(0xFF008080).copy(alpha = 0.7f).toArgb()
                boundary.outlinePaint.color = color
                mapView?.invalidate()
            }
        }
        
        // Load alerts when screen is displayed
        LaunchedEffect(Unit) {
            alertsViewModel.loadAlerts()
        }
        
        // Track previous alerts to avoid unnecessary marker updates
        var previousAlertIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
        
        // Update alert markers when alerts state changes
        LaunchedEffect(alertsState, currentLanguage, isDarkMode, showAlerts) {
            mapView?.let { mapView ->
                when (val state = alertsState) {
                    is com.example.emergencycommunicationsystem.util.Resource.Success -> {
                        val allAlerts = state.data.map { it.alert }.filter { it.latitude != null && it.longitude != null }
                        val currentAlerts = if (showAlerts) allAlerts else emptyList()
                        val currentAlertIds = currentAlerts.map { it.id }.toSet()
                        
                        // Always clean up alert visuals if toggled off or changed
                        val markersToRemove = mapView.overlays
                            .filterIsInstance<Marker>()
                            .filter { marker ->
                                marker.title?.contains("Alert") == true || marker.title?.contains("Alerto") == true
                            }
                            .toList()
                        markersToRemove.forEach { mapView.overlays.remove(it) }
                        val pulsesToRemove = mapView.overlays
                            .filterIsInstance<PulsingCircleOverlay>()
                            .toList()
                        pulsesToRemove.forEach { mapView.overlays.remove(it) }

                        // Re-add only if supposed to show
                        if (showAlerts) {
                            currentAlerts.forEach { alert ->
                                // Check for high priority keywords to use pulsing overlay
                                val isHighPriority = highKeywords.any { keyword -> 
                                    alert.title?.contains(keyword, ignoreCase = true) == true || 
                                    alert.content?.contains(keyword, ignoreCase = true) == true 
                                }

                                if (isHighPriority) {
                                    // Add pulsing effect overlay UNDER the marker
                                    val pulseOverlay = PulsingCircleOverlay(
                                        center = GeoPoint(alert.latitude!!, alert.longitude!!),
                                        color = com.example.emergencycommunicationsystem.util.getStaticColorForCategory(alert, isDarkMode).toArgb()
                                    )
                                    mapView.overlays.add(pulseOverlay)
                                }

                                val marker = Marker(mapView).apply {
                                    position = GeoPoint(alert.latitude!!, alert.longitude!!)
                                    
                                    val alertColor = com.example.emergencycommunicationsystem.util.getStaticColorForCategory(alert, isDarkMode)
                                    
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val translatedAlertLabel = localeContext.getString(R.string.map_alert)
                                        
                                        val translatedTitle = if (currentLanguage != "en" && alert.title != null) {
                                            com.example.emergencycommunicationsystem.util.TranslationService.translate(alert.title, currentLanguage)
                                        } else alert.title ?: localeContext.getString(R.string.no_title)
                                        
                                        title = "ðŸš¨ $translatedAlertLabel: $translatedTitle"
                                        
                                        val snippetText = alert.location ?: alert.content ?: localeContext.getString(R.string.community_alerts_will_appear_here)
                                        snippet = if (currentLanguage != "en") {
                                            com.example.emergencycommunicationsystem.util.TranslationService.translate(snippetText, currentLanguage)
                                        } else snippetText
                                    }
                                    
                                    // Use new Core Dot icon for high priority, else standard
                                    if (isHighPriority) {
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        icon = createPulseCoreIcon(mapView.context, alertColor.toArgb())
                                    } else {
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        icon = createAlertMarkerIcon(mapView.context, alertColor.toArgb())
                                    }
                                }
                                mapView.overlays.add(marker)
                            }
                        }
                        mapView.invalidate()
                        previousAlertIds = currentAlertIds
                    }
                    else -> {}
                }
            }
        }

        // Manage Safe Zone Markers (Hospitals & Evacuation Centers)
        LaunchedEffect(showHospitals, showEvacuationCenters, currentLanguage, isDarkMode) {
            mapView?.let { map ->
                // Remove existing safe zone markers
                val markersToRemove = map.overlays
                    .filterIsInstance<Marker>()
                    .filter { marker -> 
                        // Identify safe zone markers by their title/content conventions
                        val title = marker.title ?: ""
                        title.contains("Hospital") || title.contains("Evacuation") || 
                        title.contains("ðŸ¥") || title.contains("ðŸ›Ÿ")
                    }
                    .toList()
                markersToRemove.forEach { map.overlays.remove(it) }

                val safeZones = getQuezonCitySafeZones()
                
                safeZones.forEach { safeZone ->
                    val shouldShow = when(safeZone.type) {
                        SafeZoneType.HOSPITAL -> showHospitals
                        SafeZoneType.EVACUATION_CENTER -> showEvacuationCenters
                    }

                    if (shouldShow) {
                        val marker = Marker(map).apply {
                            position = GeoPoint(safeZone.latitude, safeZone.longitude)
                            
                            // Apply dynamic translation to safe zone info
                            CoroutineScope(Dispatchers.Main).launch {
                                val translatedType = if (currentLanguage != "en") {
                                    val typeStr = when (safeZone.type) {
                                        SafeZoneType.HOSPITAL -> "Hospital"
                                        SafeZoneType.EVACUATION_CENTER -> "Evacuation Center"
                                    }
                                    com.example.emergencycommunicationsystem.util.TranslationService.translate(typeStr, currentLanguage)
                                } else {
                                    when (safeZone.type) {
                                        SafeZoneType.HOSPITAL -> "Hospital"
                                        SafeZoneType.EVACUATION_CENTER -> "Evacuation Center"
                                    }
                                }
                                
                                val typeIcon = when (safeZone.type) {
                                    SafeZoneType.HOSPITAL -> "ðŸ¥"
                                    SafeZoneType.EVACUATION_CENTER -> "ðŸ›Ÿ"
                                }
                                
                                title = "$typeIcon $translatedType: ${safeZone.name}"
                                
                                snippet = if (safeZone.address != null) {
                                    if (currentLanguage != "en") com.example.emergencycommunicationsystem.util.TranslationService.translate(safeZone.address, currentLanguage)
                                    else safeZone.address
                                } else {
                                    val fallbackSnippet = when (safeZone.type) {
                                        SafeZoneType.HOSPITAL -> "Hospital - ${safeZone.contact ?: "Contact available"}"
                                        SafeZoneType.EVACUATION_CENTER -> "Evacuation Center${if (safeZone.capacity != null) " - Capacity: ${safeZone.capacity}" else ""}"
                                    }
                                    if (currentLanguage != "en") com.example.emergencycommunicationsystem.util.TranslationService.translate(fallbackSnippet, currentLanguage)
                                    else fallbackSnippet
                                }
                            }
                            
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = createSafeZoneMarkerIcon(map.context, safeZone.type)
                        }
                        map.overlays.add(marker)
                    }
                }
                map.invalidate()
            }
        }
        // Manage all weather markers in one synchronized batch so they appear together.
        LaunchedEffect(alertsState, userLocation, currentLanguage, showWeather) {
            mapView?.let { map ->
                val weatherMarkersToRemove = map.overlays
                    .filterIsInstance<Marker>()
                    .filter { marker ->
                        marker.title?.startsWith("ðŸŒ¦ Weather:") == true ||
                            marker.title?.startsWith("QC Weather:") == true ||
                            marker.title?.startsWith("QC Weather unavailable") == true
                    }
                    .toList()
                weatherMarkersToRemove.forEach { map.overlays.remove(it) }

                if (!showWeather) {
                    map.invalidate()
                    return@let
                }

                if (qcWeatherCache.isEmpty()) {
                    qcWeatherCache.putAll(loadPersistedQcWeatherCache(context))
                }

                val currentUserLocation = userLocation
                    ?: locationOverlay?.myLocation
                    ?: GeoPoint(14.6760, 121.0437)

                val successState = alertsState as? com.example.emergencycommunicationsystem.util.Resource.Success
                val weatherAlerts = successState?.data
                    ?.map { it.alert }
                    ?.filter { alert ->
                        alert.latitude != null &&
                            alert.longitude != null &&
                            isWeatherAlertForMap(alert)
                    }
                    ?: emptyList()

                val nearbyPlaces = detectNearbyQcWeatherPlaces(
                    userLat = currentUserLocation.latitude,
                    userLon = currentUserLocation.longitude,
                    maxDistanceMeters = 12000.0
                )

                val livePlaces = coroutineScope {
                    nearbyPlaces.map { place ->
                        async {
                            fetchLiveWeatherForQcPlace(place, qcWeatherCache)
                        }
                    }.awaitAll().filterNotNull()
                }

                persistQcWeatherCache(context, qcWeatherCache)
                val weatherInfoWindow = WeatherMarkerInfoWindow(map)

                weatherAlerts.forEach { alert ->
                    val condition = inferWeatherConditionForMap(alert)
                    val marker = Marker(map).apply {
                        position = GeoPoint(alert.latitude!! + 0.00025, alert.longitude!!)
                        title = "ðŸŒ¦ Weather: ${condition.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
                        val weatherSnippetSource = alert.content ?: alert.message ?: alert.location ?: "Weather update"
                        snippet = if (currentLanguage != "en") {
                            com.example.emergencycommunicationsystem.util.TranslationService.translate(weatherSnippetSource, currentLanguage)
                        } else {
                            weatherSnippetSource
                        }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        infoWindow = weatherInfoWindow
                        icon = createWeatherMarkerIcon(map.context, condition)
                        startWeatherIconAnimation(icon, map)
                    }
                    map.overlays.add(marker)
                }

                livePlaces.forEach { place ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(place.latitude, place.longitude)
                        title = "QC Weather: ${place.name}"
                        val forecastLabel = place.forecastCondition.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                        val baseSnippet = "${place.description} | +3h: ${place.forecastDescription} ($forecastLabel)\nLast updated: ${formatWeatherUpdatedTime(place.fetchedAtMillis)}"
                        snippet = if (currentLanguage != "en") {
                            com.example.emergencycommunicationsystem.util.TranslationService.translate(baseSnippet, currentLanguage)
                        } else {
                            baseSnippet
                        }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        infoWindow = weatherInfoWindow
                        icon = createWeatherMarkerIcon(map.context, place.condition)
                        startWeatherIconAnimation(icon, map)
                    }
                    map.overlays.add(marker)
                }

                if (livePlaces.isEmpty()) {
                    val offlineMessage = "No cached weather yet. Connect once to preload."
                    val marker = Marker(map).apply {
                        position = GeoPoint(currentUserLocation.latitude, currentUserLocation.longitude)
                        title = "QC Weather unavailable"
                        snippet = if (currentLanguage != "en") {
                            com.example.emergencycommunicationsystem.util.TranslationService.translate(offlineMessage, currentLanguage)
                        } else {
                            offlineMessage
                        }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        infoWindow = weatherInfoWindow
                        icon = createWeatherMarkerIcon(map.context, "clouds")
                    }
                    map.overlays.add(marker)
                }
                map.invalidate()
            }
        }
        // Map Legend (Always visible at TopEnd)
        MapLegend(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .widthIn(max = 160.dp), // Increased width for switches
            currentLanguage = currentLanguage,
            showAlerts = showAlerts,
            onShowAlertsChange = { showAlerts = it },
            showWeather = showWeather,
            onShowWeatherChange = { showWeather = it },
            showHospitals = showHospitals,
            onShowHospitalsChange = { showHospitals = it },
            showEvacuationCenters = showEvacuationCenters,
            onShowEvacuationCentersChange = { showEvacuationCenters = it }
        )

        // 5. Navigation Card or Directions Card (Overlays Legend at TopEnd)
        if (!isCalculatingRoute) {
            routeDestination?.let { destination ->
                if (navigationState.isNavigating) {
                    NavigationCard(
                        navigationState = navigationState,
                        destination = destination,
                        navigationManager = navigationManager,
                        currentLanguage = currentLanguage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .fillMaxWidth(0.85f),
                        onClose = {
                            navigationManager.stopNavigation()
                            currentRoutePolyline?.let { route ->
                                mapView?.overlays?.remove(route)
                                mapView?.invalidate()
                            }
                            currentRoutePolyline = null
                            mapViewModel.setRouteDestination(null)
                        }
                    )
                } else {
                    val currentUserLocation = userLocation ?: locationOverlay?.myLocation
                    currentUserLocation?.let { userLoc ->
                        val distance = LocationUtils.calculateDistance(
                            userLoc.latitude, userLoc.longitude,
                            destination.latitude, destination.longitude
                        )
                        val distanceText = LocationUtils.formatDistance(distance)
                        
                        DirectionsCard(
                            destination = destination,
                            distance = distanceText,
                            currentLanguage = currentLanguage,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 16.dp)
                                .fillMaxWidth(0.85f),
                            onClose = {
                                navigationManager.stopNavigation()
                                currentRoutePolyline?.let { route ->
                                    mapView?.overlays?.remove(route)
                                    mapView?.invalidate()
                                }
                                currentRoutePolyline = null
                                mapViewModel.setRouteDestination(null)
                            }
                        )
                    }
                }
            }
        }
        
        // 3. "Find Nearest Evacuation Center" Button
        FloatingActionButton(
            onClick = {
                val overlay = locationOverlay
                val map = mapView
                // Prioritize ViewModel location, fallback to Overlay location
                val currentUserLocation = userLocation ?: overlay?.myLocation
                
                if (map != null && currentUserLocation != null) {
                    val nearestEvac = findNearestEvacuationCenter(
                        userLat = currentUserLocation.latitude,
                        userLon = currentUserLocation.longitude
                    )
                    
                    if (nearestEvac != null) {
                        mapViewModel.setRouteDestination(null)
                        mapViewModel.setIsCalculatingRoute(true)
                        currentRoutePolyline?.let { map.overlays.remove(it) }
                        currentRoutePolyline = null
                        navigationManager.stopNavigation()
                        
                        CoroutineScope(Dispatchers.Main).launch {
                            val loadingMsg = localeContext.getString(R.string.map_calculating_route, nearestEvac.name)
                            Toast.makeText(context, loadingMsg, Toast.LENGTH_SHORT).show()
                            
                            val result = RoutingService.getRoute(
                                originLat = currentUserLocation.latitude,
                                originLon = currentUserLocation.longitude,
                                destLat = nearestEvac.latitude,
                                destLon = nearestEvac.longitude
                            )
                            
                            result.onSuccess { routeResponse ->
                                if (routeResponse.routes.isNotEmpty()) {
                                    val route = routeResponse.routes[0]
                                    val routeGeometry = try {
                                        val geometryStr = when (val geom = route.geometry) {
                                            is String -> geom
                                            is Map<*, *> -> com.google.gson.Gson().toJson(geom)
                                            else -> null
                                        }
                                        if (geometryStr != null) RoutingService.decodeGeometry(geometryStr)
                                        else decodeStepGeometries(route)
                                    } catch (e: Exception) { emptyList() }
                                    
                                    if (routeGeometry.isNotEmpty()) {
                                        val routePolyline = Polyline().apply {
                                            setPoints(routeGeometry)
                                            color = Color(0xFFFF9800).toArgb()
                                            width = 14.0f
                                            isGeodesic = false
                                        }
                                        map.overlays.add(routePolyline)
                                        currentRoutePolyline = routePolyline
                                        
                                        navigationManager.startNavigation(
                                            originLat = currentUserLocation.latitude,
                                            originLon = currentUserLocation.longitude,
                                            destLat = nearestEvac.latitude,
                                            destLon = nearestEvac.longitude,
                                            onSuccess = {
                                                mapViewModel.setRouteDestination(nearestEvac)
                                                mapViewModel.setIsCalculatingRoute(false)
                                            },
                                            onError = { error ->
                                                mapViewModel.setRouteDestination(nearestEvac)
                                                mapViewModel.setIsCalculatingRoute(false)
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    val errorPrefix = localeContext.getString(R.string.map_navigation_error)
                                                    Toast.makeText(context, "$errorPrefix: $error", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        )
                                        
                                        val allPoints = routeGeometry + GeoPoint(currentUserLocation.latitude, currentUserLocation.longitude)
                                        val boundingBox = BoundingBox(allPoints.maxOf { it.latitude }, allPoints.maxOf { it.longitude }, allPoints.minOf { it.latitude }, allPoints.minOf { it.longitude })
                                        map.post { map.zoomToBoundingBox(boundingBox, true, 250) }
                                    } else {
                                        val routePolyline = Polyline().apply {
                                            setPoints(listOf(GeoPoint(currentUserLocation.latitude, currentUserLocation.longitude), GeoPoint(nearestEvac.latitude, nearestEvac.longitude)))
                                            color = Color(0xFFFF9800).toArgb()
                                            width = 12.0f
                                            isGeodesic = true
                                        }
                                        map.overlays.add(routePolyline)
                                        currentRoutePolyline = routePolyline
                                        mapViewModel.setRouteDestination(nearestEvac)
                                        mapViewModel.setIsCalculatingRoute(false)
                                        
                                        val allPoints = listOf(GeoPoint(currentUserLocation.latitude, currentUserLocation.longitude), GeoPoint(nearestEvac.latitude, nearestEvac.longitude))
                                        val boundingBox = BoundingBox(allPoints.maxOf { it.latitude }, allPoints.maxOf { it.longitude }, allPoints.minOf { it.latitude }, allPoints.minOf { it.longitude })
                                        map.post { map.zoomToBoundingBox(boundingBox, true, 250) }
                                    }
                                    map.invalidate()
                                }
                            }.onFailure { error ->
                                val errorMsg = error.message ?: ""
                                CoroutineScope(Dispatchers.Main).launch {
                                    val translatedError = if (errorMsg.contains("No internet")) {
                                        localeContext.getString(R.string.map_no_internet)
                                    } else {
                                        localeContext.getString(R.string.map_routing_unavailable)
                                    }
                                    Toast.makeText(context, translatedError, Toast.LENGTH_LONG).show()
                                }
                                val fallbackPolyline = fallbackToStraightLine(map, currentUserLocation, nearestEvac, localeContext, showToast = false)
                                map.overlays.add(fallbackPolyline)
                                currentRoutePolyline = fallbackPolyline
                                mapViewModel.setRouteDestination(nearestEvac)
                                
                                // Zoom to show start and end points
                                val allPoints = listOf(GeoPoint(currentUserLocation.latitude, currentUserLocation.longitude), GeoPoint(nearestEvac.latitude, nearestEvac.longitude))
                                val boundingBox = BoundingBox(allPoints.maxOf { it.latitude }, allPoints.maxOf { it.longitude }, allPoints.minOf { it.latitude }, allPoints.minOf { it.longitude })
                                map.post { map.zoomToBoundingBox(boundingBox, true, 250) }
                                map.invalidate()
                                
                                mapViewModel.setIsCalculatingRoute(false)
                            }
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            val noEvacMsg = localeContext.getString(R.string.map_no_evac_found)
                            Toast.makeText(localeContext, noEvacMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        val locNotAvailMsg = localeContext.getString(R.string.map_loc_not_available)
                        Toast.makeText(localeContext, locNotAvailMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 200.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tabler_home_search),
                contentDescription = localeContext.getString(R.string.map_finding_nearest)
            )
        }
        
        // 4. "My Location" / Recenter Button
        FloatingActionButton(
            onClick = {
                val overlay = locationOverlay
                val map = mapView
                
                // Toggle lock state
                val newState = !isCameraLocked
                mapViewModel.setCameraLocked(newState)
                userActivatedCameraLock = newState // Mark explicitly if user toggled lock
                
                if (newState) {
                    if (userLocation != null) {
                        map?.controller?.animateTo(userLocation)
                    } else if (overlay?.myLocation != null) {
                        map?.controller?.animateTo(overlay.myLocation)
                    }
                    map?.controller?.setZoom(18.0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 130.dp),
            containerColor = if (isCameraLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ) {
            Icon(
                AppIcons.MyLocation, 
                contentDescription = if (isCameraLocked) "Unlock Camera" else "Center on Location",
                tint = if (isCameraLocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Camera Lock Logic - Restore position when map becomes available or location updates
        LaunchedEffect(userLocation, isCameraLocked, mapView) {
            val map = mapView
            // Only auto-follow when the camera is locked AND initial camera has been set
            // AND when the user explicitly activated the lock this session (prevents unexpected auto-follow)
            if (isCameraLocked && userActivatedCameraLock && userLocation != null && map != null && hasInitialCameraSet) {
                map.controller.animateTo(userLocation)
                if (map.zoomLevelDouble < 15.0) {
                     map.controller.setZoom(18.0)
                }
            }
        }
        
        // Route Zoom Logic - Restore zoom when returning to screen with active route
        LaunchedEffect(routeDestination, mapView, userLocation) {
            val map = mapView
            val dest = routeDestination
            val userLoc = userLocation
            
            if (map != null && dest != null && userLoc != null) {
                // Determine points to include in bounding box
                val points = mutableListOf<GeoPoint>()
                points.add(userLoc)
                points.add(GeoPoint(dest.latitude, dest.longitude))
                
                if (navigationState.routeGeometry.isNotEmpty()) {
                    points.addAll(navigationState.routeGeometry)
                }
                
                val boundingBox = BoundingBox(
                    points.maxOf { it.latitude }, 
                    points.maxOf { it.longitude }, 
                    points.minOf { it.latitude }, 
                    points.minOf { it.longitude }
                )
                
                // Use post to ensure map layout is ready
                map.post { 
                    map.zoomToBoundingBox(boundingBox, true, 250) 
                }
            }
        }

        // Real-time Dynamic Polyline Update
        LaunchedEffect(userLocation, navigationState.isNavigating, currentRoutePolyline) {
            if (navigationState.isNavigating && userLocation != null && currentRoutePolyline != null) {
                val currentPos = userLocation!!
                val routeGeom = navigationState.routeGeometry
                
                if (routeGeom.isNotEmpty()) {
                    // Update polyline to start from current location
                    // We simply connect current location to the remaining route
                    // Ideally, we find the closest point, but connecting to the start of the remaining geometry (or slightly ahead) works visually
                    
                    // Simple approach: [Current Pos] + [Route Geometry]
                    // Better approach: Slice route geometry? 
                    // Since NavigationManager doesn't expose "current geometry index", we'll just update the start point 
                    // or if it's a straight line (fallback), update the start.
                    
                    // If it's a straight line (size 2), just update start
                    if (currentRoutePolyline?.points?.size == 2 && routeGeom.isEmpty()) { // Fallback case
                         routeDestination?.let { dest ->
                             currentRoutePolyline?.setPoints(listOf(currentPos, GeoPoint(dest.latitude, dest.longitude)))
                         }
                    } else {
                        // For complex routes, we really should slice it, but without index it's hard.
                        // User request: "Shrinking Line". 
                        // Let's rely on NavigationManager's instruction updates for text, 
                        // and for the line, we can try to find the closest point index here.
                        
                        val closestPoint = routeGeom.minByOrNull { point -> 
                            val dLat = point.latitude - currentPos.latitude
                            val dLon = point.longitude - currentPos.longitude
                            dLat * dLat + dLon * dLon // Euclidean distance squared is enough for comparison
                        }
                        
                        closestPoint?.let { closest ->
                            val index = routeGeom.indexOf(closest)
                            if (index != -1 && index < routeGeom.size) {
                                val newPoints = mutableListOf<GeoPoint>()
                                newPoints.add(currentPos)
                                newPoints.addAll(routeGeom.subList(index, routeGeom.size))
                                currentRoutePolyline?.setPoints(newPoints)
                            }
                        }
                    }
                    mapView?.invalidate()
                } else {
                     // Fallback for straight line or if geometry missing
                     routeDestination?.let { dest ->
                         currentRoutePolyline?.setPoints(listOf(currentPos, GeoPoint(dest.latitude, dest.longitude)))
                         mapView?.invalidate()
                     }
                }
            }
        }
    }
}

/**
 * Map Legend component
 */
@Composable
fun MapLegend(
    modifier: Modifier = Modifier, 
    currentLanguage: String = "en",
    showAlerts: Boolean = true,
    onShowAlertsChange: (Boolean) -> Unit = {},
    showWeather: Boolean = true,
    onShowWeatherChange: (Boolean) -> Unit = {},
    showHospitals: Boolean = true,
    onShowHospitalsChange: (Boolean) -> Unit = {},
    showEvacuationCenters: Boolean = true,
    onShowEvacuationCentersChange: (Boolean) -> Unit = {}
) {
    val isDarkMode = ThemeManager.isDarkMode()
    val backgroundColor = if (isDarkMode) Color(0xFF1E1E1E).copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val contentColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else BrandTealAccent.copy(alpha = 0.5f)
    
    val translatedLegend = localeContext.getString(R.string.map_legend)
    val translatedAlert = localeContext.getString(R.string.map_alert)
    val translatedWeather = "Weather"
    val translatedHospital = localeContext.getString(R.string.map_hospital)
    val translatedEvac = localeContext.getString(R.string.map_evacuation)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, borderColor)
    ) { 
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = translatedLegend,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                thickness = 1.dp,
                color = contentColor.copy(alpha = 0.3f)
            )
            
            LegendItem(
                color = Color.Red, 
                label = translatedAlert, 
                textColor = contentColor, 
                isCircle = true, 
                isPin = false,
                isChecked = showAlerts,
                onToggle = onShowAlertsChange
            )
            LegendItem(
                color = Color(0xFFFFB300),
                label = translatedWeather,
                textColor = contentColor,
                isCircle = true,
                isChecked = showWeather,
                onToggle = onShowWeatherChange
            )
            LegendItem(
                color = Color(0xFF4CAF50), 
                label = translatedHospital, 
                textColor = contentColor, 
                isCircle = false,
                isChecked = showHospitals,
                onToggle = onShowHospitalsChange
            )
            LegendItem(
                color = Color(0xFF2196F3), 
                label = translatedEvac, 
                textColor = contentColor, 
                isCircle = false,
                isChecked = showEvacuationCenters,
                onToggle = onShowEvacuationCentersChange
            )
        }
    }
}

@Composable
fun LegendItem(
    color: Color, 
    label: String, 
    textColor: Color, 
    isCircle: Boolean = true,
    isPin: Boolean = false,
    isChecked: Boolean = true,
    onToggle: (Boolean) -> Unit = {}
) {
    val isDarkMode = ThemeManager.isDarkMode()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null, // Handled by Row click
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = if (isDarkMode) MaterialTheme.colorScheme.primary else BrandTealAccent,
                uncheckedColor = textColor.copy(alpha = 0.6f),
                checkmarkColor = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else Color.White
            )
        )

        val shape = when {
            isPin -> RoundedCornerShape(2.dp) // For Diamond rotation
            isCircle -> CircleShape
            else -> RoundedCornerShape(4.dp) // Square for Safe Zones
        }
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .rotate(if (isPin) 45f else 0f) // Diamond shape in legend
                .background(if (isChecked) color else color.copy(alpha = 0.4f), shape)
                .border(1.dp, if (isChecked) textColor.copy(alpha = 0.5f) else textColor.copy(alpha = 0.2f), shape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (isChecked) textColor else textColor.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Directions Card
 */
@Composable
fun DirectionsCard(
    destination: SafeZone,
    distance: String,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val isDarkMode = ThemeManager.isDarkMode()
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else BrandTealAccent.copy(alpha = 0.3f)
    
    val translatedDirections = localeContext.getString(R.string.map_directions)
    val translatedTo = localeContext.getString(R.string.map_to)
    val translatedDistanceLabel = localeContext.getString(R.string.map_distance)
    val translatedCapacityLabel = localeContext.getString(R.string.map_capacity)
    val translatedRouteInstr = localeContext.getString(R.string.map_route_instruction)
    
    var translatedAddress by remember(destination.address) { mutableStateOf(destination.address ?: "") }
    var translatedDestName by remember(destination.name) { mutableStateOf(destination.name) }
    
    LaunchedEffect(currentLanguage, destination.address, destination.name) {
        if (currentLanguage != "en") {
            destination.address?.let { addr ->
                launch { translatedAddress = com.example.emergencycommunicationsystem.util.TranslationService.translate(addr, currentLanguage) }
            }
            launch { translatedDestName = com.example.emergencycommunicationsystem.util.TranslationService.translate(destination.name, currentLanguage) }
        } else {
            translatedAddress = destination.address ?: ""
            translatedDestName = destination.name
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, borderColor)
    ) { 
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(text = translatedDirections, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), thickness = 0.5.dp)
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "$translatedTo: $translatedDestName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (translatedAddress.isNotEmpty()) {
                    Text(text = translatedAddress, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "$translatedDistanceLabel: $distance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    destination.capacity?.let { cap ->
                        Text(text = "- $translatedCapacityLabel: $cap", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)) {
                Text(text = translatedRouteInstr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/**
 * Navigation Card
 */
@Composable
fun NavigationCard(
    navigationState: com.example.emergencycommunicationsystem.data.models.NavigationState,
    destination: SafeZone,
    navigationManager: com.example.emergencycommunicationsystem.util.NavigationManager,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val localeContext = com.example.emergencycommunicationsystem.util.getLocaleContext()
    val isDarkMode = ThemeManager.isDarkMode()
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else BrandTealAccent.copy(alpha = 0.3f)
    
    val translatedNavLabel = localeContext.getString(R.string.map_navigation)
    val translatedRemaining = localeContext.getString(R.string.map_remaining)
    val translatedEtaLabel = localeContext.getString(R.string.map_eta)
    val translatedThen = localeContext.getString(R.string.map_then)
    
    var translatedCurInstr by remember(navigationState.currentInstruction?.instruction) { mutableStateOf(navigationState.currentInstruction?.instruction ?: "") }
    var translatedNextInstr by remember(navigationState.nextInstruction?.instruction) { mutableStateOf(navigationState.nextInstruction?.instruction ?: "") }
    var translatedDestName by remember(destination.name) { mutableStateOf(destination.name) }

    LaunchedEffect(currentLanguage, navigationState.currentInstruction, navigationState.nextInstruction, destination.name) {
        if (currentLanguage != "en") {
            navigationState.currentInstruction?.instruction?.let { instr ->
                launch { translatedCurInstr = com.example.emergencycommunicationsystem.util.TranslationService.translate(instr, currentLanguage) }
            }
            navigationState.nextInstruction?.instruction?.let { instr ->
                launch { translatedNextInstr = com.example.emergencycommunicationsystem.util.TranslationService.translate(instr, currentLanguage) }
            }
            launch { translatedDestName = com.example.emergencycommunicationsystem.util.TranslationService.translate(destination.name, currentLanguage) }
        } else {
            translatedCurInstr = navigationState.currentInstruction?.instruction ?: ""
            translatedNextInstr = navigationState.nextInstruction?.instruction ?: ""
            translatedDestName = destination.name
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(text = translatedNavLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = localeContext.getString(R.string.stop), modifier = Modifier.size(16.dp))
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), thickness = 0.5.dp)
            Text(text = translatedDestName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            if (translatedCurInstr.isNotEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = translatedCurInstr, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.width(6.dp))
                        navigationState.currentInstruction?.let { instr ->
                            Text(text = LocationUtils.formatDistance(instr.distance / 1000.0), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "$translatedRemaining:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = LocationUtils.formatDistance(navigationState.remainingDistance / 1000.0), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "$translatedEtaLabel:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = navigationManager.formatETA(navigationState.remainingDuration), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (translatedNextInstr.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Text(text = "$translatedThen: $translatedNextInstr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * Creates a diamond-shaped marker icon for alerts with an exclamation mark.
 */
private fun createAlertMarkerIcon(context: android.content.Context, colorInt: Int): android.graphics.drawable.Drawable {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = android.graphics.Paint().apply {
        color = colorInt
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    
    val strokePaint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Draw Diamond Shape
    val path = android.graphics.Path()
    path.moveTo(size / 2f, 4f) // Top
    path.lineTo(size - 4f, size / 2f) // Right
    path.lineTo(size / 2f, size - 4f) // Bottom
    path.lineTo(4f, size / 2f) // Left
    path.close()
    
    // Draw Shadow
    canvas.drawPath(path, android.graphics.Paint().apply {
        color = Color.Black.toArgb()
        alpha = 60
        maskFilter = android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    })
    
    canvas.drawPath(path, paint)
    canvas.drawPath(path, strokePaint)
    
    // Draw "!" in center
    val textPaint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        textSize = 34f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    
    val centerX = size / 2f
    val centerY = size / 2f
    val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2)
    
    canvas.drawText("!", centerX, textY, textPaint)
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Creates a marker icon for safe zones (hospitals = green, evacuation centers = blue)
 * Visuals: Rounded Square with White Border and Shadow effect to differentiate from circular Alerts.
 */
private fun createSafeZoneMarkerIcon(context: android.content.Context, type: SafeZoneType): android.graphics.drawable.Drawable {
    val size = 64 // Increased size for better visibility
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Choose color based on type
    val zoneColor = when (type) {
        SafeZoneType.HOSPITAL -> Color(0xFF43A047) // Green 600
        SafeZoneType.EVACUATION_CENTER -> Color(0xFF1E88E5) // Blue 600
    }
    
    // Shadow Paint
    val shadowPaint = android.graphics.Paint().apply {
        color = Color.Black.toArgb()
        alpha = 80 // Semi-transparent black
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
        maskFilter = android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    // Main Fill Paint
    val paint = android.graphics.Paint().apply {
        color = zoneColor.toArgb()
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Border Paint
    val strokePaint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    val rectSize = 48f // Size of the main shape
    val offset = (size - rectSize) / 2f
    val cornerRadius = 12f
    
    // Draw Shadow (slightly offset)
    val shadowRect = android.graphics.RectF(offset + 2, offset + 4, offset + rectSize + 2, offset + rectSize + 4)
    canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)
    
    // Draw Main Shape (Rounded Rect)
    val mainRect = android.graphics.RectF(offset, offset, offset + rectSize, offset + rectSize)
    canvas.drawRoundRect(mainRect, cornerRadius, cornerRadius, paint)
    canvas.drawRoundRect(mainRect, cornerRadius, cornerRadius, strokePaint)
    
    // Add icon symbol (H for Hospital, E for Evacuation)
    // Using a simple cross for Hospital and a person/shelter symbol for Evac would be better, but text is robust.
    // Let's use "+" for Hospital if possible, or bold "H".
    val textPaint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        textSize = 28f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    
    val symbol = when (type) {
        SafeZoneType.HOSPITAL -> "H" // Or "+" if preferred
        SafeZoneType.EVACUATION_CENTER -> "E"
    }
    
    // Center text vertically
    val centerX = size / 2f
    val centerY = size / 2f
    val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2)
    
    canvas.drawText(symbol, centerX, textY, textPaint)
    
    // Add small "Plus" badge for Hospital to make it distinct
    if (type == SafeZoneType.HOSPITAL) {
       // Optional: Draw a small white cross in the corner or just stick with "H"
    }
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Creates a simple solid circular core icon for pulsing alerts.
 */
private fun createPulseCoreIcon(context: android.content.Context, colorInt: Int): android.graphics.drawable.Drawable {
    val size = 40 // Smaller than the standard marker
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = android.graphics.Paint().apply {
        color = colorInt
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    
    val borderPaint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius - 2f, paint)
    canvas.drawCircle(radius, radius, radius - 2f, borderPaint)
    
    return BitmapDrawable(context.resources, bitmap)
}

private fun createWeatherMarkerIcon(
    context: android.content.Context,
    condition: String
): android.graphics.drawable.Drawable {
    val resId = WeatherIconUtils.getWeatherAnimation(condition)
    val drawable = ContextCompat.getDrawable(context, resId)?.mutate()
        ?: return createPulseCoreIcon(context, Color(0xFFFFB300).toArgb())

    val size = 172
    if (drawable is AnimationDrawable) {
        val scaledAnimation = AnimationDrawable().apply { isOneShot = drawable.isOneShot }
        for (i in 0 until drawable.numberOfFrames) {
            val frame = drawable.getFrame(i)
            val duration = drawable.getDuration(i)
            val scaledFrame = renderDrawableAsBitmapDrawable(context, frame, size)
            scaledAnimation.addFrame(scaledFrame, duration)
        }
        scaledAnimation.setBounds(0, 0, size, size)
        return scaledAnimation
    }

    // Preserve animatable drawables (e.g., AnimatedVectorDrawable) so motion is not lost.
    if (drawable is Animatable) {
        val wrapped = FixedSizeAnimatableDrawable(drawable, size)
        wrapped.setBounds(0, 0, size, size)
        return wrapped
    }

    return renderDrawableAsBitmapDrawable(context, drawable, size)
}

private fun startWeatherIconAnimation(
    drawable: android.graphics.drawable.Drawable?,
    mapView: MapView
) {
    if (drawable == null) return
    drawable.callback = object : android.graphics.drawable.Drawable.Callback {
        override fun invalidateDrawable(who: android.graphics.drawable.Drawable) {
            mapView.invalidate()
        }

        override fun scheduleDrawable(who: android.graphics.drawable.Drawable, what: Runnable, `when`: Long) {
            val delay = (`when` - android.os.SystemClock.uptimeMillis()).coerceAtLeast(0L)
            mapView.postDelayed(what, delay)
        }

        override fun unscheduleDrawable(who: android.graphics.drawable.Drawable, what: Runnable) {
            mapView.removeCallbacks(what)
        }
    }

    when (drawable) {
        is Animatable -> drawable.start()
        is AnimationDrawable -> drawable.start()
    }

    mapView.invalidate()
}

private fun renderDrawableAsBitmapDrawable(
    context: android.content.Context,
    drawable: android.graphics.drawable.Drawable,
    size: Int
): android.graphics.drawable.Drawable {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return BitmapDrawable(context.resources, bitmap)
}

private fun isWeatherAlertForMap(alert: com.example.emergencycommunicationsystem.data.models.Alert): Boolean {
    val text = listOf(alert.category, alert.title, alert.content, alert.message)
        .filterNotNull()
        .joinToString(" ")
        .lowercase()

    return text.contains("weather") ||
        text.contains("rain") ||
        text.contains("drizzle") ||
        text.contains("storm") ||
        text.contains("typhoon") ||
        text.contains("flood") ||
        text.contains("thunder") ||
        text.contains("cloud") ||
        text.contains("mist") ||
        text.contains("fog") ||
        text.contains("haze") ||
        text.contains("snow") ||
        text.contains("clear")
}

private fun inferWeatherConditionForMap(alert: com.example.emergencycommunicationsystem.data.models.Alert): String {
    val text = listOf(alert.category, alert.title, alert.content, alert.message)
        .filterNotNull()
        .joinToString(" ")
        .lowercase()

    return when {
        text.contains("thunder") || text.contains("lightning") -> "thunderstorm"
        text.contains("snow") -> "snow"
        text.contains("drizzle") -> "drizzle"
        text.contains("rain") || text.contains("flood") || text.contains("storm") || text.contains("typhoon") -> "rain"
        text.contains("mist") || text.contains("fog") || text.contains("haze") || text.contains("smoke") || text.contains("dust") -> "mist"
        text.contains("cloud") || text.contains("overcast") -> "clouds"
        text.contains("clear") || text.contains("sunny") || text.contains("hot") -> "clear"
        else -> "clouds"
    }
}

private data class QcWeatherPlaceSeed(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double
)

private data class QcWeatherPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val condition: String,
    val forecastCondition: String,
    val forecastDescription: String,
    val description: String,
    val radiusMeters: Double,
    val fetchedAtMillis: Long
)

private data class CachedQcWeather(
    val condition: String,
    val forecastCondition: String,
    val forecastDescription: String,
    val description: String,
    val fetchedAtMillis: Long
)

private fun getQcWeatherPlaces(): List<QcWeatherPlaceSeed> {
    return listOf(
        QcWeatherPlaceSeed(
            name = "La Mesa Watershed",
            latitude = 14.7097,
            longitude = 121.0759,
            radiusMeters = 4200.0
        ),
        QcWeatherPlaceSeed(
            name = "UP Diliman",
            latitude = 14.6539,
            longitude = 121.0685,
            radiusMeters = 3400.0
        ),
        QcWeatherPlaceSeed(
            name = "Cubao",
            latitude = 14.6196,
            longitude = 121.0548,
            radiusMeters = 3000.0
        ),
        QcWeatherPlaceSeed(
            name = "Novaliches",
            latitude = 14.7213,
            longitude = 121.0437,
            radiusMeters = 3600.0
        ),
        QcWeatherPlaceSeed(
            name = "Batasan",
            latitude = 14.6760,
            longitude = 121.0942,
            radiusMeters = 3200.0
        ),
        QcWeatherPlaceSeed(
            name = "Commonwealth",
            latitude = 14.6768,
            longitude = 121.0965,
            radiusMeters = 3300.0
        ),
        QcWeatherPlaceSeed(
            name = "Fairview",
            latitude = 14.7011,
            longitude = 121.0710,
            radiusMeters = 3200.0
        ),
        QcWeatherPlaceSeed(
            name = "Katipunan",
            latitude = 14.6386,
            longitude = 121.0748,
            radiusMeters = 3000.0
        ),
        QcWeatherPlaceSeed(
            name = "Tandang Sora",
            latitude = 14.6848,
            longitude = 121.0473,
            radiusMeters = 3000.0
        ),
        QcWeatherPlaceSeed(
            name = "Timog",
            latitude = 14.6341,
            longitude = 121.0332,
            radiusMeters = 2800.0
        ),
        QcWeatherPlaceSeed(
            name = "Kamuning",
            latitude = 14.6347,
            longitude = 121.0449,
            radiusMeters = 2800.0
        ),
        QcWeatherPlaceSeed(
            name = "Project 8",
            latitude = 14.6640,
            longitude = 121.0249,
            radiusMeters = 2800.0
        ),
        QcWeatherPlaceSeed(
            name = "Balintawak",
            latitude = 14.6578,
            longitude = 120.9907,
            radiusMeters = 3200.0
        )
    )
}

private fun detectNearbyQcWeatherPlaces(
    userLat: Double,
    userLon: Double,
    maxDistanceMeters: Double
): List<QcWeatherPlaceSeed> {
    val sortedByDistance = getQcWeatherPlaces()
        .map { place ->
            val distance = distanceMeters(
                userLat = userLat,
                userLon = userLon,
                placeLat = place.latitude,
                placeLon = place.longitude
            )
            place to distance
        }

    val strictNearby = sortedByDistance
        .filter { (place, distance) -> distance <= min(maxDistanceMeters, place.radiusMeters) }
        .sortedBy { (_, distance) -> distance }
        .map { (place, _) -> place }

    // Keep "nearby" behavior, but guarantee enough visible weather markers for the user.
    return if (strictNearby.size >= 6) {
        strictNearby.take(12)
    } else {
        sortedByDistance
            .sortedBy { (_, distance) -> distance }
            .map { (place, _) -> place }
            .take(10)
    }
}

private suspend fun fetchLiveWeatherForQcPlace(
    place: QcWeatherPlaceSeed,
    cache: MutableMap<String, CachedQcWeather>
): QcWeatherPlace? {
    val now = System.currentTimeMillis()
    val cacheTtlMillis = 10 * 60 * 1000L
    val cached = cache[place.name]

    if (cached != null && (now - cached.fetchedAtMillis) < cacheTtlMillis) {
        return QcWeatherPlace(
            name = place.name,
            latitude = place.latitude,
            longitude = place.longitude,
            condition = cached.condition,
            forecastCondition = cached.forecastCondition,
            forecastDescription = cached.forecastDescription,
            description = cached.description,
            radiusMeters = place.radiusMeters,
            fetchedAtMillis = cached.fetchedAtMillis
        )
    }

    if (BuildConfig.OPENWEATHER_API_KEY.isBlank()) {
        return cached?.let {
            QcWeatherPlace(
                name = place.name,
                latitude = place.latitude,
                longitude = place.longitude,
                condition = it.condition,
                forecastCondition = it.forecastCondition,
                forecastDescription = it.forecastDescription,
                description = it.description,
                radiusMeters = place.radiusMeters,
                fetchedAtMillis = it.fetchedAtMillis
            )
        }
    }

    return try {
        val response = WeatherApiClient.weatherService.getCurrentWeatherByLocation(
            lat = place.latitude,
            lon = place.longitude,
            apiKey = BuildConfig.OPENWEATHER_API_KEY
        )
        val forecastResponse = WeatherApiClient.weatherService.getForecastByLocation(
            lat = place.latitude,
            lon = place.longitude,
            apiKey = BuildConfig.OPENWEATHER_API_KEY
        )

        val condition = response.weather.firstOrNull()?.main?.lowercase() ?: "clouds"
        val description = response.weather.firstOrNull()?.main?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        } ?: "Weather unavailable"
        val nextForecast = forecastResponse.list.firstOrNull()
        val forecastCondition = nextForecast?.weather?.firstOrNull()?.main?.lowercase() ?: condition
        val forecastDescription = nextForecast?.weather?.firstOrNull()?.main?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        } ?: description
        val temperature = response.main.temp.toInt()
        val snippet = "Now: $description, ${temperature}\u00B0C"
        val markerCondition = selectMostRelevantConditionForMap(condition, forecastCondition)

        cache[place.name] = CachedQcWeather(
            condition = markerCondition,
            forecastCondition = forecastCondition,
            forecastDescription = forecastDescription,
            description = snippet,
            fetchedAtMillis = now
        )

        QcWeatherPlace(
            name = place.name,
            latitude = place.latitude,
            longitude = place.longitude,
            condition = markerCondition,
            forecastCondition = forecastCondition,
            forecastDescription = forecastDescription,
            description = snippet,
            radiusMeters = place.radiusMeters,
            fetchedAtMillis = now
        )
    } catch (_: Exception) {
        val fallback = cache[place.name]
        if (fallback != null) {
            QcWeatherPlace(
                name = place.name,
                latitude = place.latitude,
                longitude = place.longitude,
                condition = fallback.condition,
                forecastCondition = fallback.forecastCondition,
                forecastDescription = fallback.forecastDescription,
                description = fallback.description,
                radiusMeters = place.radiusMeters,
                fetchedAtMillis = fallback.fetchedAtMillis
            )
        } else {
            null
        }
    }
}

private fun formatWeatherUpdatedTime(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun selectMostRelevantConditionForMap(
    currentCondition: String,
    forecastCondition: String
): String {
    val severeConditions = setOf("thunderstorm", "rain", "drizzle", "snow", "mist")
    return if (forecastCondition in severeConditions) forecastCondition else currentCondition
}

private fun persistQcWeatherCache(
    context: android.content.Context,
    cache: Map<String, CachedQcWeather>
) {
    if (cache.isEmpty()) return
    val json = Gson().toJson(cache)
    context.getSharedPreferences(QC_WEATHER_CACHE_PREFS, android.content.Context.MODE_PRIVATE)
        .edit()
        .putString(QC_WEATHER_CACHE_KEY, json)
        .apply()
}

private fun loadPersistedQcWeatherCache(
    context: android.content.Context
): MutableMap<String, CachedQcWeather> {
    val prefs = context.getSharedPreferences(QC_WEATHER_CACHE_PREFS, android.content.Context.MODE_PRIVATE)
    val stored = prefs.getString(QC_WEATHER_CACHE_KEY, null) ?: return mutableMapOf()
    return try {
        val type = object : TypeToken<MutableMap<String, CachedQcWeather>>() {}.type
        Gson().fromJson<MutableMap<String, CachedQcWeather>>(stored, type) ?: mutableMapOf()
    } catch (_: Exception) {
        mutableMapOf()
    }
}

private fun distanceMeters(
    userLat: Double,
    userLon: Double,
    placeLat: Double,
    placeLon: Double
): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(placeLat - userLat)
    val dLon = Math.toRadians(placeLon - userLon)
    val lat1 = Math.toRadians(userLat)
    val lat2 = Math.toRadians(placeLat)

    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

/**
 * Helper function to decode step geometries as fallback
 */
private fun decodeStepGeometries(route: com.example.emergencycommunicationsystem.data.models.Route): List<org.osmdroid.util.GeoPoint> {
    return try {
        route.legs.flatMap { leg ->
            leg.steps.flatMap { step ->
                try {
                    val stepGeom = when (val geom = step.geometry) {
                        is String -> geom
                        is Map<*, *> -> {
                            val gson = com.google.gson.Gson()
                            gson.toJson(geom)
                        }
                        else -> null
                    }
                    if (stepGeom != null) {
                        com.example.emergencycommunicationsystem.data.network.RoutingService.decodeGeometry(stepGeom)
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    // Suppress individual step geometry errors to avoid spam
                    emptyList()
                }
            }
        }
    } catch (e: Exception) {
        LogFilter.e(TAG, "Error in decodeStepGeometries: ${e.message}", e)
        emptyList()
    }
}

/**
 * Helper function to fallback to straight line route
 * @param showToast If true, shows a toast message (set to false if toast is already shown)
 * @return The created Polyline so it can be tracked for removal
 */
private fun fallbackToStraightLine(
    map: org.osmdroid.views.MapView,
    userLocation: org.osmdroid.util.GeoPoint,
    destination: com.example.emergencycommunicationsystem.data.models.SafeZone,
    context: android.content.Context,
    showToast: Boolean = true
): org.osmdroid.views.overlay.Polyline {
    LogFilter.d(TAG, "Using fallback straight line route")
    val routePolyline = org.osmdroid.views.overlay.Polyline().apply {
        val routePoints = listOf(
            userLocation,
            org.osmdroid.util.GeoPoint(destination.latitude, destination.longitude)
        )
        setPoints(routePoints)
        color = androidx.compose.ui.graphics.Color(0xFFFF9800).toArgb() // Orange color for route
        width = 12.0f
        isGeodesic = true
    }
    
    // Note: The polyline is NOT added to the map here to prevent memory leaks
    // The caller is responsible for adding it to map.overlays
    
    if (showToast) {
        android.widget.Toast.makeText(
            context,
            context.getString(R.string.map_direct_route_fallback),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    return routePolyline
}

/**
 * Get list of safe zones (hospitals and evacuation centers) in Quezon City
 * Based on official Quezon City evacuation centers list
 */
private fun getQuezonCitySafeZones(): List<SafeZone> {
    return listOf(
        // QUEZON CITY-OWNED HOSPITALS
        SafeZone(
            id = "hosp_qc_1",
            name = "Novaliches District Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7200,
            longitude = 121.0600,
            address = "683 Quirino Highway, Barangay San Bartolome, Novaliches",
            contact = "8931-0307"
        ),
        SafeZone(
            id = "hosp_qc_2",
            name = "Quezon City General Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6500,
            longitude = 121.0500,
            address = "Seminary Road, Barangay Bahay Toro, Project 8",
            contact = "8863-0800"
        ),
        SafeZone(
            id = "hosp_qc_3",
            name = "Rosario Maclang Bautista General Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6900,
            longitude = 121.0900,
            address = "IBP Road, Batasan Hills",
            contact = "8835-2560"
        ),
        
        // NATIONAL GOVERNMENT-OWNED HOSPITALS
        SafeZone(
            id = "hosp_nat_1",
            name = "National Children's Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6250,
            longitude = 121.0250,
            address = "264 E. Rodriguez Sr. Avenue, Quezon City",
            contact = "(02) 8724-0656"
        ),
        SafeZone(
            id = "hosp_nat_2",
            name = "Philippine Orthopedic Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6200,
            longitude = 121.0200,
            address = "Maria Clara cor. Banawe Street, Quezon City",
            contact = "(02) 8711-4276 to 80"
        ),
        SafeZone(
            id = "hosp_nat_3",
            name = "Quirino Memorial Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6400,
            longitude = 121.0450,
            address = "JP Rizal cor. P. Tuazon Sts, Project 4",
            contact = "(02) 5304-9800"
        ),
        SafeZone(
            id = "hosp_nat_4",
            name = "Lung Center of the Philippines",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6500,
            longitude = 121.0400,
            address = "Quezon Avenue, Barangay Central, Diliman",
            contact = "0917-5301331"
        ),
        SafeZone(
            id = "hosp_nat_5",
            name = "Philippine Children's Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6450,
            longitude = 121.0350,
            address = "Quezon Avenue cor. Sen. Miriam Defensor-Santiago Avenue, Bagong Pag-asa",
            contact = "Contact via email"
        ),
        SafeZone(
            id = "hosp_nat_6",
            name = "National Kidney and Transplant Institute",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6250,
            longitude = 121.0250,
            address = "East Avenue, Diliman",
            contact = "931-03000, 981-0400"
        ),
        SafeZone(
            id = "hosp_nat_7",
            name = "Philippine Heart Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6300,
            longitude = 121.0300,
            address = "East Avenue, Diliman",
            contact = "925-2430"
        ),
        SafeZone(
            id = "hosp_nat_8",
            name = "East Avenue Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6400,
            longitude = 121.0400,
            address = "East Avenue, Diliman",
            contact = "(02) 8928-0611 to 24"
        ),
        
        // ADDITIONAL PRIVATE HOSPITALS IN QUEZON CITY
        SafeZone(
            id = "hosp_priv_1",
            name = "Skyline Hospital and Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7500,
            longitude = 121.0700,
            address = "Gaya-Gaya, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_2",
            name = "Healthway QualiMed Hospital San Jose",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7400,
            longitude = 121.0650,
            address = "Tungkong Mangga, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_3",
            name = "North Caloocan Doctors Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7450,
            longitude = 121.0600,
            address = "Tungkong Mangga, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_4",
            name = "Caloocan City North Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7350,
            longitude = 121.0550,
            address = "Near Loma de Gato, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_5",
            name = "Reynold M. Sta. Ana Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7200,
            longitude = 121.0500,
            address = "Novaliches Proper, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_6",
            name = "Fairview General Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.7150,
            longitude = 121.0550,
            address = "Novaliches Proper, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_7",
            name = "Diliman Doctors Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6800,
            longitude = 121.0800,
            address = "Batasan Hills, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_8",
            name = "Providence Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6400,
            longitude = 121.0300,
            address = "Balingasa, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_9",
            name = "United Doctors Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6350,
            longitude = 121.0250,
            address = "Balingasa, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_10",
            name = "Fe Del Mundo Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6300,
            longitude = 121.0200,
            address = "Balingasa, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_11",
            name = "UERM Medical Center",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6000,
            longitude = 121.0000,
            address = "Sampaloc, Quezon City",
            contact = "Contact available"
        ),
        SafeZone(
            id = "hosp_priv_12",
            name = "Villarosa Hospital",
            type = SafeZoneType.HOSPITAL,
            latitude = 14.6500,
            longitude = 121.1000,
            address = "Marikina, Quezon City",
            contact = "Contact available"
        ),
        
        // DISTRICT 1 - Major Evacuation Centers
        SafeZone(
            id = "evac_d1_1",
            name = "Quezon City Memorial Circle",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6500,
            longitude = 121.0500,
            address = "Elliptical Road, Quezon City",
            capacity = 5000
        ),
        SafeZone(
            id = "evac_d1_2",
            name = "Amoranto Sports Complex",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6300,
            longitude = 121.0300,
            address = "Roces Avenue, Quezon City",
            capacity = 3000
        ),
        SafeZone(
            id = "evac_d1_3",
            name = "Project 6 Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6450,
            longitude = 121.0400,
            address = "Project 6, Quezon City",
            capacity = 800
        ),
        SafeZone(
            id = "evac_d1_4",
            name = "Quirino High School & Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6350,
            longitude = 121.0350,
            address = "Quirino 2-B, Quezon City",
            capacity = 1200
        ),
        SafeZone(
            id = "evac_d1_5",
            name = "San Antonio Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6400,
            longitude = 121.0450,
            address = "Katipunan, Quezon City",
            capacity = 600
        ),
        
        // DISTRICT 2 - Major Evacuation Centers
        SafeZone(
            id = "evac_d2_1",
            name = "Bagong Silangan Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7000,
            longitude = 121.1000,
            address = "Bagong Silangan, Quezon City",
            capacity = 1000
        ),
        SafeZone(
            id = "evac_d2_2",
            name = "Bagong Silangan High School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7020,
            longitude = 121.1020,
            address = "Bagong Silangan, Quezon City",
            capacity = 1200
        ),
        SafeZone(
            id = "evac_d2_3",
            name = "Batasan National High School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6900,
            longitude = 121.0900,
            address = "Batasan Hills, Quezon City",
            capacity = 1500
        ),
        SafeZone(
            id = "evac_d2_4",
            name = "Commonwealth Covered Court",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6800,
            longitude = 121.0800,
            address = "Commonwealth, Quezon City",
            capacity = 500
        ),
        SafeZone(
            id = "evac_d2_5",
            name = "Payatas Multi-Purpose Hall",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7100,
            longitude = 121.1100,
            address = "Payatas, Quezon City",
            capacity = 800
        ),
        
        // DISTRICT 3 - Major Evacuation Centers
        SafeZone(
            id = "evac_d3_1",
            name = "Araneta Coliseum",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6200,
            longitude = 121.0200,
            address = "Araneta Center, Quezon City",
            capacity = 10000
        ),
        SafeZone(
            id = "evac_d3_2",
            name = "Quirino High School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6250,
            longitude = 121.0250,
            address = "Amihan, Quezon City",
            capacity = 1200
        ),
        SafeZone(
            id = "evac_d3_3",
            name = "E. Rodriguez Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6150,
            longitude = 121.0150,
            address = "E. Rodriguez, Quezon City",
            capacity = 800
        ),
        SafeZone(
            id = "evac_d3_4",
            name = "Camp Aguinaldo Open Space",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6100,
            longitude = 121.0100,
            address = "Camp Aguinaldo, Quezon City",
            capacity = 2000
        ),
        SafeZone(
            id = "evac_d3_5",
            name = "SM Cubao Parking",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6180,
            longitude = 121.0180,
            address = "Cubao, Quezon City",
            capacity = 3000
        ),
        
        // DISTRICT 4 - Major Evacuation Centers
        SafeZone(
            id = "evac_d4_1",
            name = "Quezon City Hall",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6500,
            longitude = 121.0500,
            address = "Elliptical Road, Quezon City",
            capacity = 1500
        ),
        SafeZone(
            id = "evac_d4_2",
            name = "Kamuning Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6400,
            longitude = 121.0400,
            address = "Kamuning, Quezon City",
            capacity = 600
        ),
        SafeZone(
            id = "evac_d4_3",
            name = "Don Alejandro Rocess Sr. High School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6450,
            longitude = 121.0450,
            address = "Obrero, Quezon City",
            capacity = 1000
        ),
        SafeZone(
            id = "evac_d4_4",
            name = "Pinyahan Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6350,
            longitude = 121.0350,
            address = "Pinyahan, Quezon City",
            capacity = 700
        ),
        SafeZone(
            id = "evac_d4_5",
            name = "Teachers Village East Barangay Hall",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6300,
            longitude = 121.0300,
            address = "Teachers Village East, Quezon City",
            capacity = 400
        ),
        
        // DISTRICT 5 - Major Evacuation Centers
        SafeZone(
            id = "evac_d5_1",
            name = "Fairlane Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7200,
            longitude = 121.0600,
            address = "Fairview, Quezon City",
            capacity = 800
        ),
        SafeZone(
            id = "evac_d5_2",
            name = "Novaliches Proper Barangay Hall",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7300,
            longitude = 121.0700,
            address = "Novaliches Proper, Quezon City",
            capacity = 500
        ),
        SafeZone(
            id = "evac_d5_3",
            name = "San Bartolome Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7100,
            longitude = 121.0500,
            address = "San Bartolome, Quezon City",
            capacity = 700
        ),
        SafeZone(
            id = "evac_d5_4",
            name = "Sta. Monica Covered Court",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7150,
            longitude = 121.0550,
            address = "Sta. Monica, Quezon City",
            capacity = 400
        ),
        SafeZone(
            id = "evac_d5_5",
            name = "Greater Lagro Centennial Park",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.7250,
            longitude = 121.0650,
            address = "Greater Lagro, Quezon City",
            capacity = 2000
        ),
        
        // DISTRICT 6 - Major Evacuation Centers
        SafeZone(
            id = "evac_d6_1",
            name = "Apolonio Samson Elementary School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6800,
            longitude = 121.0400,
            address = "Apolonio Samson, Quezon City",
            capacity = 800
        ),
        SafeZone(
            id = "evac_d6_2",
            name = "Culiat High School",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6700,
            longitude = 121.0300,
            address = "Culiat, Quezon City",
            capacity = 1200
        ),
        SafeZone(
            id = "evac_d6_3",
            name = "Tandang Sora Barangay Hall",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6750,
            longitude = 121.0350,
            address = "Tandang Sora, Quezon City",
            capacity = 500
        ),
        SafeZone(
            id = "evac_d6_4",
            name = "Talipapa Barangay Hall",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6650,
            longitude = 121.0250,
            address = "Talipapa, Quezon City",
            capacity = 400
        ),
        SafeZone(
            id = "evac_d6_5",
            name = "UP Campus Amorsolo Building",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6600,
            longitude = 121.0200,
            address = "UP Campus, Quezon City",
            capacity = 1000
        ),
        
        // Additional Major Facilities
        SafeZone(
            id = "evac_major_1",
            name = "SM North EDSA",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6550,
            longitude = 121.0550,
            address = "North EDSA, Quezon City",
            capacity = 8000
        ),
        SafeZone(
            id = "evac_major_2",
            name = "Quezon City Sports Complex",
            type = SafeZoneType.EVACUATION_CENTER,
            latitude = 14.6400,
            longitude = 121.0450,
            address = "E. Rodriguez Sr. Ave, Quezon City",
            capacity = 3000
        )
    )
}

/**
 * Find the nearest evacuation center to the user's location
 * @param userLat User's latitude
 * @param userLon User's longitude
 * @return The nearest SafeZone of type EVACUATION_CENTER, or null if none found
 */
private fun findNearestEvacuationCenter(userLat: Double, userLon: Double): SafeZone? {
    val safeZones = getQuezonCitySafeZones()
    val evacuationCenters = safeZones.filter { it.type == SafeZoneType.EVACUATION_CENTER }
    
    if (evacuationCenters.isEmpty()) return null
    
    return evacuationCenters.minByOrNull { evac ->
        LocationUtils.calculateDistance(
            userLat, userLon,
            evac.latitude, evac.longitude
        )
    }
}

private fun getQuezonCityBoundaryPoints(): ArrayList<GeoPoint> {
    return arrayListOf(
        GeoPoint(14.7646242, 121.1095933),
        GeoPoint(14.7639251, 121.1093054),
        GeoPoint(14.7631436, 121.1090833),
        GeoPoint(14.7627981, 121.1073723),
        GeoPoint(14.7622963, 121.105793),
        GeoPoint(14.7618357, 121.104773),
        GeoPoint(14.7638675, 121.1025355),
        GeoPoint(14.7655348, 121.1016249),
        GeoPoint(14.7654178, 121.1012409),
        GeoPoint(14.7651862, 121.0997995),
        GeoPoint(14.7640376, 121.0997537),
        GeoPoint(14.7626015, 121.0990606),
        GeoPoint(14.7623292, 121.0984063),
        GeoPoint(14.7615898, 121.0964583),
        GeoPoint(14.7615413, 121.0956111),
        GeoPoint(14.7609386, 121.0948137),
        GeoPoint(14.7598163, 121.0934468),
        GeoPoint(14.7591997, 121.0925497),
        GeoPoint(14.7585362, 121.091745),
        GeoPoint(14.7579449, 121.0907068),
        GeoPoint(14.7582575, 121.0896539),
        GeoPoint(14.7582657, 121.089366),
        GeoPoint(14.7579696, 121.0887985),
        GeoPoint(14.758085, 121.0857106),
        GeoPoint(14.7578089, 121.0856433),
        GeoPoint(14.7566921, 121.0853354),
        GeoPoint(14.7558102, 121.0851033),
        GeoPoint(14.7556543, 121.08507),
        GeoPoint(14.7552569, 121.0850078),
        GeoPoint(14.753781, 121.0849007),
        GeoPoint(14.7533543, 121.0848696),
        GeoPoint(14.7520288, 121.0847854),
        GeoPoint(14.7518499, 121.0847557),
        GeoPoint(14.7517425, 121.0847244),
        GeoPoint(14.7516349, 121.0846896),
        GeoPoint(14.7514516, 121.0846162),
        GeoPoint(14.7511728, 121.0844538),
        GeoPoint(14.7508641, 121.0842517),
        GeoPoint(14.7495766, 121.0833299),
        GeoPoint(14.748611, 121.082698),
        GeoPoint(14.7484806, 121.0826085),
        GeoPoint(14.7483083, 121.0824692),
        GeoPoint(14.7479453, 121.082152),
        GeoPoint(14.7464257, 121.0806645),
        GeoPoint(14.7463022, 121.0805133),
        GeoPoint(14.7461923, 121.0802811),
        GeoPoint(14.7461772, 121.0802603),
        GeoPoint(14.7456529, 121.0785924),
        GeoPoint(14.7455823, 121.0784592),
        GeoPoint(14.7455143, 121.0783473),
        GeoPoint(14.7454372, 121.0782561),
        GeoPoint(14.7453116, 121.0781445),
        GeoPoint(14.7452281, 121.0780846),
        GeoPoint(14.7451322, 121.0780318),
        GeoPoint(14.7450374, 121.0779908),
        GeoPoint(14.7449288, 121.0779571),
        GeoPoint(14.7447783, 121.0779317),
        GeoPoint(14.7444754, 121.0779129),
        GeoPoint(14.7428592, 121.0778333),
        GeoPoint(14.742725, 121.0778258),
        GeoPoint(14.7425895, 121.0778078),
        GeoPoint(14.7424549, 121.0777577),
        GeoPoint(14.7423599, 121.0777091),
        GeoPoint(14.7422779, 121.0776449),
        GeoPoint(14.7421861, 121.0775529),
        GeoPoint(14.7421411, 121.0774749),
        GeoPoint(14.7420979, 121.0773718),
        GeoPoint(14.7420616, 121.0772585),
        GeoPoint(14.7420002, 121.0770302),
        GeoPoint(14.7423243, 121.0769046),
        GeoPoint(14.7423099, 121.075878),
        GeoPoint(14.7421927, 121.0663291),
        GeoPoint(14.7421837, 121.0587677),
        GeoPoint(14.742157, 121.0531742),
        GeoPoint(14.7422036, 121.0464397),
        GeoPoint(14.7421201, 121.0404931),
        GeoPoint(14.740294, 121.0385103),
        GeoPoint(14.7380574, 121.0362582),
        GeoPoint(14.732682, 121.0308457),
        GeoPoint(14.7298826, 121.0280557),
        GeoPoint(14.7292097, 121.0273872),
        GeoPoint(14.7275181, 121.0257601),
        GeoPoint(14.7243718, 121.0224236),
        GeoPoint(14.7225911, 121.0205352),
        GeoPoint(14.7204784, 121.0183472),
        GeoPoint(14.7159085, 121.0136441),
        GeoPoint(14.708755, 121.0161294),
        GeoPoint(14.7033858, 121.0179631),
        GeoPoint(14.7032227, 121.0178562),
        GeoPoint(14.7030583, 121.0177166),
        GeoPoint(14.7029552, 121.0176377),
        GeoPoint(14.7028717, 121.0175811),
        GeoPoint(14.7027566, 121.0175192),
        GeoPoint(14.7026572, 121.0174702),
        GeoPoint(14.7024994, 121.0173968),
        GeoPoint(14.7023908, 121.0173523),
        GeoPoint(14.7022658, 121.0173277),
        GeoPoint(14.7021902, 121.0173175),
        GeoPoint(14.7020925, 121.0173206),
        GeoPoint(14.7019482, 121.0173586),
        GeoPoint(14.7018209, 121.017406),
        GeoPoint(14.7015462, 121.0175321),
        GeoPoint(14.7013391, 121.0176311),
        GeoPoint(14.7011888, 121.0177186),
        GeoPoint(14.7010692, 121.0177798),
        GeoPoint(14.7009477, 121.0178264),
        GeoPoint(14.700854, 121.0178489),
        GeoPoint(14.7007532, 121.0178713),
        GeoPoint(14.7006363, 121.0179141),
        GeoPoint(14.7005441, 121.0179549),
        GeoPoint(14.7004239, 121.0180124),
        GeoPoint(14.7003174, 121.018091),
        GeoPoint(14.700236, 121.0181807),
        GeoPoint(14.7001, 121.0183224),
        GeoPoint(14.7000049, 121.0184405),
        GeoPoint(14.6999203, 121.0185728),
        GeoPoint(14.6998547, 121.0187004),
        GeoPoint(14.6997471, 121.0188854),
        GeoPoint(14.6996618, 121.0190209),
        GeoPoint(14.6995651, 121.0191466),
        GeoPoint(14.6994575, 121.0192927),
        GeoPoint(14.6993709, 121.0194197),
        GeoPoint(14.6992806, 121.0195085),
        GeoPoint(14.6991921, 121.0195921),
        GeoPoint(14.6990902, 121.0196704),
        GeoPoint(14.6989858, 121.0197382),
        GeoPoint(14.6988904, 121.0197961),
        GeoPoint(14.6987631, 121.0198784),
        GeoPoint(14.6986358, 121.0199679),
        GeoPoint(14.6985307, 121.0200508),
        GeoPoint(14.6983862, 121.0201442),
        GeoPoint(14.6982838, 121.0201949),
        GeoPoint(14.6982042, 121.0202416),
        GeoPoint(14.6981558, 121.0202798),
        GeoPoint(14.6980973, 121.0203443),
        GeoPoint(14.6980514, 121.0204206),
        GeoPoint(14.6980196, 121.020516),
        GeoPoint(14.6979757, 121.020643),
        GeoPoint(14.6979171, 121.0207727),
        GeoPoint(14.6978611, 121.0208957),
        GeoPoint(14.6978134, 121.0209951),
        GeoPoint(14.6977491, 121.0210892),
        GeoPoint(14.6976861, 121.0211655),
        GeoPoint(14.6976332, 121.0212261),
        GeoPoint(14.6976025, 121.021264),
        GeoPoint(14.6975702, 121.0213077),
        GeoPoint(14.6975206, 121.0213722),
        GeoPoint(14.6974601, 121.021434),
        GeoPoint(14.6973621, 121.0215334),
        GeoPoint(14.697292, 121.0215985),
        GeoPoint(14.6972017, 121.0216795),
        GeoPoint(14.6971036, 121.0217683),
        GeoPoint(14.6970521, 121.0218144),
        GeoPoint(14.6969891, 121.0218605),
        GeoPoint(14.6969299, 121.0219065),
        GeoPoint(14.6968344, 121.0219631),
        GeoPoint(14.6967224, 121.0220388),
        GeoPoint(14.6966453, 121.0221),
        GeoPoint(14.6965823, 121.0221395),
        GeoPoint(14.6964766, 121.0222198),
        GeoPoint(14.6964124, 121.022277),
        GeoPoint(14.6963283, 121.0223573),
        GeoPoint(14.6962698, 121.0224159),
        GeoPoint(14.6961978, 121.0225119),
        GeoPoint(14.6961406, 121.0225922),
        GeoPoint(14.6960578, 121.0227317),
        GeoPoint(14.6960094, 121.0228403),
        GeoPoint(14.695942, 121.0229732),
        GeoPoint(14.6958681, 121.0231009),
        GeoPoint(14.6958108, 121.0231772),
        GeoPoint(14.6957688, 121.0232193),
        GeoPoint(14.6957115, 121.0232595),
        GeoPoint(14.6956536, 121.0232819),
        GeoPoint(14.6955696, 121.0233088),
        GeoPoint(14.695448, 121.0233417),
        GeoPoint(14.6953194, 121.0233832),
        GeoPoint(14.6952131, 121.0234181),
        GeoPoint(14.6950737, 121.0234641),
        GeoPoint(14.6949763, 121.023503),
        GeoPoint(14.694828, 121.0235438),
        GeoPoint(14.6947173, 121.0235661),
        GeoPoint(14.6946339, 121.0235964),
        GeoPoint(14.6945734, 121.0236339),
        GeoPoint(14.694497, 121.0236885),
        GeoPoint(14.6944219, 121.0237557),
        GeoPoint(14.6943761, 121.0238096),
        GeoPoint(14.6943252, 121.0238965),
        GeoPoint(14.6942717, 121.0239992),
        GeoPoint(14.6942189, 121.0240985),
        GeoPoint(14.6941495, 121.0241953),
        GeoPoint(14.6940845, 121.0242933),
        GeoPoint(14.6940152, 121.0243756),
        GeoPoint(14.6939579, 121.0244374),
        GeoPoint(14.6938891, 121.0244795),
        GeoPoint(14.6938267, 121.0245063),
        GeoPoint(14.6937656, 121.0245263),
        GeoPoint(14.6936733, 121.0245493),
        GeoPoint(14.6935868, 121.0245618),
        GeoPoint(14.6935199, 121.0245644),
        GeoPoint(14.6934197, 121.0245497),
        GeoPoint(14.6932933, 121.0245146),
        GeoPoint(14.693196, 121.0244686),
        GeoPoint(14.6930177, 121.0243684),
        GeoPoint(14.6928724, 121.0242836),
        GeoPoint(14.692687, 121.0241839),
        GeoPoint(14.6924433, 121.0240682),
        GeoPoint(14.691906, 121.0239012),
        GeoPoint(14.6911428, 121.0238923),
        GeoPoint(14.6909064, 121.0237582),
        GeoPoint(14.6907147, 121.0235056),
        GeoPoint(14.6905977, 121.0229565),
        GeoPoint(14.6903954, 121.0221324),
        GeoPoint(14.6903804, 121.0216672),
        GeoPoint(14.6884807, 121.0223396),
        GeoPoint(14.6851812, 121.0192022),
        GeoPoint(14.6806545, 121.014895),
        GeoPoint(14.6710675, 121.0058529),
        GeoPoint(14.667334, 121.0022246),
        GeoPoint(14.6653244, 121.0003125),
        GeoPoint(14.664741, 120.9997577),
        GeoPoint(14.6643627, 120.9994174),
        GeoPoint(14.663877, 120.9994138),
        GeoPoint(14.6634339, 120.9994033),
        GeoPoint(14.661943, 120.9993861),
        GeoPoint(14.6581224, 120.999302),
        GeoPoint(14.6581072, 120.9992982),
        GeoPoint(14.6573354, 120.9991025),
        GeoPoint(14.6568231, 120.9989016),
        GeoPoint(14.6566755, 120.9987949),
        GeoPoint(14.6563956, 120.9985902),
        GeoPoint(14.6561778, 120.9984358),
        GeoPoint(14.6551673, 120.9976659),
        GeoPoint(14.6543814, 120.9972619),
        GeoPoint(14.6539536, 120.9970642),
        GeoPoint(14.6528858, 120.9965706),
        GeoPoint(14.6521912, 120.9962495),
        GeoPoint(14.6507248, 120.9955689),
        GeoPoint(14.6497136, 120.9951615),
        GeoPoint(14.6480502, 120.9945753),
        GeoPoint(14.6474992, 120.9943354),
        GeoPoint(14.6471239, 120.994172),
        GeoPoint(14.647084, 120.9941546),
        GeoPoint(14.6468884, 120.9940588),
        GeoPoint(14.645824, 120.9934932),
        GeoPoint(14.6455495, 120.9933546),
        GeoPoint(14.6450106, 120.9931041),
        GeoPoint(14.644469, 120.9928718),
        GeoPoint(14.6442386, 120.9928787),
        GeoPoint(14.6438027, 120.9928964),
        GeoPoint(14.6436994, 120.9928758),
        GeoPoint(14.6433075, 120.9926892),
        GeoPoint(14.6428751, 120.9925111),
        GeoPoint(14.642419, 120.9923392),
        GeoPoint(14.6419929, 120.9921201),
        GeoPoint(14.6415352, 120.9919297),
        GeoPoint(14.6410924, 120.9917593),
        GeoPoint(14.6406945, 120.9915513),
        GeoPoint(14.6402168, 120.9913863),
        GeoPoint(14.6398421, 120.9912629),
        GeoPoint(14.6398144, 120.9913141),
        GeoPoint(14.6385471, 120.9920194),
        GeoPoint(14.6379133, 120.9923657),
        GeoPoint(14.6374219, 120.9925993),
        GeoPoint(14.6362678, 120.9921888),
        GeoPoint(14.6359804, 120.9930436),
        GeoPoint(14.6350728, 120.9927488),
        GeoPoint(14.634629, 120.9925998),
        GeoPoint(14.6305282, 120.9912426),
        GeoPoint(14.6262495, 120.9898201),
        GeoPoint(14.6261549, 120.9897783),
        GeoPoint(14.6260342, 120.9896951),
        GeoPoint(14.6259579, 120.9896955),
        GeoPoint(14.625934, 120.9896983),
        GeoPoint(14.6258983, 120.9897026),
        GeoPoint(14.625838, 120.989722),
        GeoPoint(14.6257597, 120.9897691),
        GeoPoint(14.6256977, 120.9898287),
        GeoPoint(14.6255638, 120.9899835),
        GeoPoint(14.6252791, 120.9903521),
        GeoPoint(14.6251559, 120.9905112),
        GeoPoint(14.6251302, 120.9905417),
        GeoPoint(14.6245355, 120.9913147),
        GeoPoint(14.624469, 120.991401),
        GeoPoint(14.624397, 120.9914942),
        GeoPoint(14.6241634, 120.9917968),
        GeoPoint(14.6235329, 120.9926137),
        GeoPoint(14.6226129, 120.9938057),
        GeoPoint(14.6217104, 120.9949749),
        GeoPoint(14.6214035, 120.9953714),
        GeoPoint(14.6209761, 120.9959497),
        GeoPoint(14.6208793, 120.996077),
        GeoPoint(14.6208149, 120.9961595),
        GeoPoint(14.6207256, 120.9962762),
        GeoPoint(14.6206346, 120.9963925),
        GeoPoint(14.6200392, 120.997134),
        GeoPoint(14.6199419, 120.9972545),
        GeoPoint(14.6198882, 120.997321),
        GeoPoint(14.6197598, 120.9974816),
        GeoPoint(14.619578, 120.9976689),
        GeoPoint(14.6193355, 120.9978929),
        GeoPoint(14.6170829, 121.0009647),
        GeoPoint(14.6150944, 121.003646),
        GeoPoint(14.6139723, 121.0052731),
        GeoPoint(14.6125167, 121.0069471),
        GeoPoint(14.6115939, 121.0081408),
        GeoPoint(14.6107331, 121.0092936),
        GeoPoint(14.6098411, 121.0104299),
        GeoPoint(14.607205, 121.0139822),
        GeoPoint(14.6061298, 121.0153858),
        GeoPoint(14.6053799, 121.0163648),
        GeoPoint(14.6044948, 121.0175128),
        GeoPoint(14.6043722, 121.0176183),
        GeoPoint(14.6036079, 121.0185805),
        GeoPoint(14.6029514, 121.0193839),
        GeoPoint(14.6028204, 121.0195915),
        GeoPoint(14.6031741, 121.0196633),
        GeoPoint(14.603941, 121.0198942),
        GeoPoint(14.6045802, 121.0201956),
        GeoPoint(14.6052367, 121.0205743),
        GeoPoint(14.6058371, 121.0209541),
        GeoPoint(14.6064302, 121.0213826),
        GeoPoint(14.6071501, 121.0219474),
        GeoPoint(14.6077435, 121.022237),
        GeoPoint(14.6082751, 121.0222199),
        GeoPoint(14.6085433, 121.0220838),
        GeoPoint(14.6088317, 121.0219135),
        GeoPoint(14.609016, 121.0217177),
        GeoPoint(14.6092493, 121.0214532),
        GeoPoint(14.6094049, 121.0212748),
        GeoPoint(14.6095448, 121.0214352),
        GeoPoint(14.6104505, 121.0219307),
        GeoPoint(14.6113174, 121.0225558),
        GeoPoint(14.6120983, 121.0230435),
        GeoPoint(14.613178, 121.0232341),
        GeoPoint(14.6133529, 121.0232654),
        GeoPoint(14.6135373, 121.0232946),
        GeoPoint(14.6137345, 121.0234014),
        GeoPoint(14.6138021, 121.0235014),
        GeoPoint(14.6138471, 121.0236239),
        GeoPoint(14.6138698, 121.0237484),
        GeoPoint(14.6137399, 121.0243076),
        GeoPoint(14.6134936, 121.0249404),
        GeoPoint(14.6131828, 121.0250526),
        GeoPoint(14.6127011, 121.0252071),
        GeoPoint(14.6125184, 121.0253272),
        GeoPoint(14.6123868, 121.0255013),
        GeoPoint(14.6123059, 121.0259875),
        GeoPoint(14.6123391, 121.0269145),
        GeoPoint(14.6123474, 121.0275067),
        GeoPoint(14.6122481, 121.0281632),
        GeoPoint(14.6120554, 121.0286223),
        GeoPoint(14.6120778, 121.0288587),
        GeoPoint(14.6121481, 121.0289744),
        GeoPoint(14.612472, 121.0292577),
        GeoPoint(14.6128516, 121.0292359),
        GeoPoint(14.6129809, 121.0293482),
        GeoPoint(14.6130842, 121.0294838),
        GeoPoint(14.6131828, 121.0298162),
        GeoPoint(14.6131495, 121.0301013),
        GeoPoint(14.6129786, 121.0304507),
        GeoPoint(14.6129253, 121.0308805),
        GeoPoint(14.6126096, 121.0310722),
        GeoPoint(14.6122656, 121.0312621),
        GeoPoint(14.6121154, 121.0313434),
        GeoPoint(14.6118989, 121.0314635),
        GeoPoint(14.611683, 121.0316733),
        GeoPoint(14.6115037, 121.0320248),
        GeoPoint(14.6110659, 121.0325136),
        GeoPoint(14.6108919, 121.0327126),
        GeoPoint(14.6107088, 121.0328741),
        GeoPoint(14.6099756, 121.0334107),
        GeoPoint(14.6096889, 121.0336672),
        GeoPoint(14.6095485, 121.0339011),
        GeoPoint(14.6094571, 121.0343474),
        GeoPoint(14.609438, 121.0346766),
        GeoPoint(14.6094131, 121.0348568),
        GeoPoint(14.6089671, 121.0352143),
        GeoPoint(14.6088145, 121.0353238),
        GeoPoint(14.6086928, 121.0356082),
        GeoPoint(14.6086822, 121.0359383),
        GeoPoint(14.6086815, 121.0362119),
        GeoPoint(14.6086678, 121.0363622),
        GeoPoint(14.608499, 121.0368149),
        GeoPoint(14.608417, 121.0368975),
        GeoPoint(14.6079957, 121.0368916),
        GeoPoint(14.6076347, 121.0370345),
        GeoPoint(14.6067543, 121.0372834),
        GeoPoint(14.6064446, 121.0376133),
        GeoPoint(14.6063141, 121.0377321),
        GeoPoint(14.6063813, 121.0378158),
        GeoPoint(14.6065489, 121.0380076),
        GeoPoint(14.6066134, 121.038087),
        GeoPoint(14.6069836, 121.038506),
        GeoPoint(14.6071185, 121.0386585),
        GeoPoint(14.6071521, 121.0386965),
        GeoPoint(14.6071658, 121.0387121),
        GeoPoint(14.6073116, 121.0388707),
        GeoPoint(14.6075688, 121.0391963),
        GeoPoint(14.6076583, 121.0393201),
        GeoPoint(14.6078858, 121.0396159),
        GeoPoint(14.6083622, 121.040243),
        GeoPoint(14.6087073, 121.0407497),
        GeoPoint(14.6088153, 121.0409096),
        GeoPoint(14.6088794, 121.0410549),
        GeoPoint(14.609006, 121.0413743),
        GeoPoint(14.6093009, 121.0421317),
        GeoPoint(14.6095839, 121.0428448),
        GeoPoint(14.609642, 121.0429556),
        GeoPoint(14.6096503, 121.0429708),
        GeoPoint(14.6096537, 121.0430241),
        GeoPoint(14.609655, 121.0430551),
        GeoPoint(14.6096476, 121.043084),
        GeoPoint(14.6095823, 121.043249),
        GeoPoint(14.6095191, 121.0433434),
        GeoPoint(14.6089572, 121.0440186),
        GeoPoint(14.6089231, 121.0441123),
        GeoPoint(14.6088989, 121.0442439),
        GeoPoint(14.6088481, 121.0445993),
        GeoPoint(14.6088322, 121.0447027),
        GeoPoint(14.6087768, 121.0450626),
        GeoPoint(14.6087541, 121.0451961),
        GeoPoint(14.6086802, 121.0456728),
        GeoPoint(14.6086687, 121.0457618),
        GeoPoint(14.6086706, 121.0458196),
        GeoPoint(14.6086757, 121.0458646),
        GeoPoint(14.6086864, 121.0459073),
        GeoPoint(14.6087435, 121.0461319),
        GeoPoint(14.6087584, 121.0462033),
        GeoPoint(14.6087762, 121.0462801),
        GeoPoint(14.6088721, 121.0466595),
        GeoPoint(14.608939, 121.0469243),
        GeoPoint(14.6089577, 121.0469987),
        GeoPoint(14.6089712, 121.0470529),
        GeoPoint(14.6089875, 121.0471288),
        GeoPoint(14.609092, 121.0475866),
        GeoPoint(14.6091336, 121.0477546),
        GeoPoint(14.6091441, 121.0477992),
        GeoPoint(14.6092028, 121.0480342),
        GeoPoint(14.6092525, 121.048233),
        GeoPoint(14.6092755, 121.0483294),
        GeoPoint(14.6093052, 121.0484539),
        GeoPoint(14.609382, 121.0488039),
        GeoPoint(14.6094162, 121.0489723),
        GeoPoint(14.6094959, 121.0493306),
        GeoPoint(14.6095204, 121.0494407),
        GeoPoint(14.6096421, 121.049922),
        GeoPoint(14.6096748, 121.0500514),
        GeoPoint(14.607049, 121.0510734),
        GeoPoint(14.6063175, 121.0513718),
        GeoPoint(14.6062072, 121.051396),
        GeoPoint(14.6058821, 121.0514962),
        GeoPoint(14.6048031, 121.051977),
        GeoPoint(14.6046499, 121.0516597),
        GeoPoint(14.6043748, 121.0517929),
        GeoPoint(14.6045402, 121.0521673),
        GeoPoint(14.6065867, 121.0567956),
        GeoPoint(14.6066703, 121.0569881),
        GeoPoint(14.6066534, 121.0569959),
        GeoPoint(14.602265, 121.0590045),
        GeoPoint(14.601912, 121.0591491),
        GeoPoint(14.6017034, 121.0592271),
        GeoPoint(14.6013506, 121.0593395),
        GeoPoint(14.6009617, 121.0594461),
        GeoPoint(14.6005758, 121.0595389),
        GeoPoint(14.6002475, 121.0596084),
        GeoPoint(14.5996641, 121.0596867),
        GeoPoint(14.599452, 121.0597074),
        GeoPoint(14.5991796, 121.0597277),
        GeoPoint(14.5988943, 121.0597399),
        GeoPoint(14.5986502, 121.0597438),
        GeoPoint(14.5983444, 121.0597432),
        GeoPoint(14.5981082, 121.0597365),
        GeoPoint(14.5978708, 121.0597212),
        GeoPoint(14.5976371, 121.0596993),
        GeoPoint(14.5973922, 121.0596703),
        GeoPoint(14.5971525, 121.0596363),
        GeoPoint(14.5969132, 121.0595967),
        GeoPoint(14.5962171, 121.0594743),
        GeoPoint(14.5953564, 121.0592133),
        GeoPoint(14.5940416, 121.0587576),
        GeoPoint(14.5932156, 121.058484),
        GeoPoint(14.5927896, 121.0583341),
        GeoPoint(14.592365, 121.0581667),
        GeoPoint(14.591349, 121.0578276),
        GeoPoint(14.5911365, 121.0577585),
        GeoPoint(14.589369, 121.0572211),
        GeoPoint(14.5896463, 121.0582621),
        GeoPoint(14.5900235, 121.0596451),
        GeoPoint(14.5904899, 121.0614237),
        GeoPoint(14.5905503, 121.0616432),
        GeoPoint(14.5905758, 121.0617941),
        GeoPoint(14.5919521, 121.0680469),
        GeoPoint(14.5930667, 121.0695316),
        GeoPoint(14.5933839, 121.0698755),
        GeoPoint(14.5934856, 121.0704484),
        GeoPoint(14.593464, 121.0706848),
        GeoPoint(14.5932389, 121.0723414),
        GeoPoint(14.5930164, 121.0738133),
        GeoPoint(14.5926956, 121.0760398),
        GeoPoint(14.5924751, 121.0774771),
        GeoPoint(14.5923335, 121.07788),
        GeoPoint(14.5920822, 121.0785544),
        GeoPoint(14.5916782, 121.0796285),
        GeoPoint(14.5916496, 121.0797276),
        GeoPoint(14.5916175, 121.0798384),
        GeoPoint(14.5915772, 121.0799433),
        GeoPoint(14.5905369, 121.0826503),
        GeoPoint(14.5903997, 121.0830518),
        GeoPoint(14.5921634, 121.0827285),
        GeoPoint(14.5951453, 121.0823165),
        GeoPoint(14.596288, 121.0823855),
        GeoPoint(14.5972293, 121.0824407),
        GeoPoint(14.5989494, 121.082531),
        GeoPoint(14.6017929, 121.0823531),
        GeoPoint(14.6023855, 121.0823594),
        GeoPoint(14.6026332, 121.0824594),
        GeoPoint(14.6030984, 121.0828519),
        GeoPoint(14.6032684, 121.0832517),
        GeoPoint(14.6033745, 121.083786),
        GeoPoint(14.6033011, 121.0846416),
        GeoPoint(14.6028411, 121.0856732),
        GeoPoint(14.6022288, 121.0863878),
        GeoPoint(14.6014334, 121.0870479),
        GeoPoint(14.6003282, 121.0874234),
        GeoPoint(14.599318, 121.0879024),
        GeoPoint(14.5990613, 121.0884909),
        GeoPoint(14.599072, 121.0895263),
        GeoPoint(14.5992902, 121.0899858),
        GeoPoint(14.5996752, 121.0902434),
        GeoPoint(14.6001564, 121.0904543),
        GeoPoint(14.6011754, 121.0904275),
        GeoPoint(14.6024379, 121.0900155),
        GeoPoint(14.6041655, 121.0889512),
        GeoPoint(14.6054058, 121.0883546),
        GeoPoint(14.6060925, 121.0880242),
        GeoPoint(14.6066771, 121.0876246),
        GeoPoint(14.6069989, 121.0873671),
        GeoPoint(14.607435, 121.0869916),
        GeoPoint(14.6076539, 121.0866938),
        GeoPoint(14.6090753, 121.0846661),
        GeoPoint(14.6104304, 121.082733),
        GeoPoint(14.6115981, 121.0810672),
        GeoPoint(14.6124462, 121.0799561),
        GeoPoint(14.6138249, 121.079012),
        GeoPoint(14.6141584, 121.0788997),
        GeoPoint(14.6155269, 121.0784392),
        GeoPoint(14.6160455, 121.078399),
        GeoPoint(14.616765, 121.0784541),
        GeoPoint(14.6173291, 121.0786891),
        GeoPoint(14.6177381, 121.0788822),
        GeoPoint(14.6181381, 121.0782067),
        GeoPoint(14.6181704, 121.0781522),
        GeoPoint(14.6182005, 121.0781009),
        GeoPoint(14.6182727, 121.0779778),
        GeoPoint(14.6195429, 121.0758218),
        GeoPoint(14.6203305, 121.0762267),
        GeoPoint(14.6208781, 121.0765039),
        GeoPoint(14.6213886, 121.0765189),
        GeoPoint(14.6218147, 121.0764557),
        GeoPoint(14.6228017, 121.0759409),
        GeoPoint(14.623032, 121.0758256),
        GeoPoint(14.6237732, 121.0750915),
        GeoPoint(14.6239809, 121.0752906),
        GeoPoint(14.6247014, 121.0751135),
        GeoPoint(14.6249965, 121.0750843),
        GeoPoint(14.6252375, 121.075037),
        GeoPoint(14.6264184, 121.0747689),
        GeoPoint(14.6279073, 121.0744536),
        GeoPoint(14.6280696, 121.0744066),
        GeoPoint(14.6286421, 121.074425),
        GeoPoint(14.628847, 121.0751483),
        GeoPoint(14.629031, 121.0758175),
        GeoPoint(14.6296256, 121.0769013),
        GeoPoint(14.6303523, 121.0771695),
        GeoPoint(14.6309563, 121.0774626),
        GeoPoint(14.6314838, 121.077469),
        GeoPoint(14.6316817, 121.0775373),
        GeoPoint(14.6322159, 121.0776147),
        GeoPoint(14.6324289, 121.0777748),
        GeoPoint(14.6325722, 121.0777259),
        GeoPoint(14.6328058, 121.0777695),
        GeoPoint(14.6327038, 121.0781852),
        GeoPoint(14.6331024, 121.0781921),
        GeoPoint(14.6331595, 121.0783354),
        GeoPoint(14.6333002, 121.0787821),
        GeoPoint(14.6336149, 121.0795619),
        GeoPoint(14.6339782, 121.0799374),
        GeoPoint(14.6345357, 121.0802379),
        GeoPoint(14.6346416, 121.0797189),
        GeoPoint(14.6355115, 121.0799697),
        GeoPoint(14.635823, 121.0803023),
        GeoPoint(14.6362589, 121.0806885),
        GeoPoint(14.6365807, 121.0806778),
        GeoPoint(14.6368195, 121.0808709),
        GeoPoint(14.636861, 121.0813323),
        GeoPoint(14.6369035, 121.0817386),
        GeoPoint(14.6373806, 121.0818386),
        GeoPoint(14.6379116, 121.0819219),
        GeoPoint(14.6383165, 121.0819852),
        GeoPoint(14.6383388, 121.0816883),
        GeoPoint(14.638401, 121.0811626),
        GeoPoint(14.6384576, 121.0807133),
        GeoPoint(14.638754, 121.0809909),
        GeoPoint(14.6391565, 121.0814591),
        GeoPoint(14.6395869, 121.0814819),
        GeoPoint(14.6400111, 121.0817834),
        GeoPoint(14.6401248, 121.0819886),
        GeoPoint(14.640833, 121.0823068),
        GeoPoint(14.6410846, 121.0823287),
        GeoPoint(14.6413518, 121.0824574),
        GeoPoint(14.6419772, 121.0822937),
        GeoPoint(14.6424372, 121.0823549),
        GeoPoint(14.6433858, 121.0831803),
        GeoPoint(14.6436992, 121.0831645),
        GeoPoint(14.6439884, 121.083191),
        GeoPoint(14.6439511, 121.0835988),
        GeoPoint(14.6436446, 121.084572),
        GeoPoint(14.6436375, 121.0847489),
        GeoPoint(14.6437206, 121.0853712),
        GeoPoint(14.6444918, 121.0855999),
        GeoPoint(14.6448987, 121.0876123),
        GeoPoint(14.6458583, 121.0874867),
        GeoPoint(14.6459452, 121.0881572),
        GeoPoint(14.6464517, 121.0889727),
        GeoPoint(14.6468726, 121.0896603),
        GeoPoint(14.6485394, 121.0877901),
        GeoPoint(14.6489835, 121.0877308),
        GeoPoint(14.6493282, 121.0868934),
        GeoPoint(14.6514982, 121.0865934),
        GeoPoint(14.6514588, 121.0867363),
        GeoPoint(14.651271, 121.0874186),
        GeoPoint(14.651506, 121.0874307),
        GeoPoint(14.652202, 121.0866746),
        GeoPoint(14.6527812, 121.0858927),
        GeoPoint(14.6529528, 121.0857761),
        GeoPoint(14.6532691, 121.0857806),
        GeoPoint(14.6545518, 121.0861472),
        GeoPoint(14.6547612, 121.0854564),
        GeoPoint(14.6554682, 121.0857081),
        GeoPoint(14.6562612, 121.0859908),
        GeoPoint(14.6557911, 121.0865123),
        GeoPoint(14.6566853, 121.0867891),
        GeoPoint(14.6573361, 121.0874608),
        GeoPoint(14.6566672, 121.0882081),
        GeoPoint(14.6596216, 121.0912009),
        GeoPoint(14.6605249, 121.0911456),
        GeoPoint(14.6609324, 121.0914765),
        GeoPoint(14.6617729, 121.0920319),
        GeoPoint(14.6634173, 121.0935248),
        GeoPoint(14.6639892, 121.0936321),
        GeoPoint(14.6643486, 121.0936995),
        GeoPoint(14.6645004, 121.0938826),
        GeoPoint(14.6646918, 121.0941136),
        GeoPoint(14.6649347, 121.0948585),
        GeoPoint(14.6652335, 121.0951488),
        GeoPoint(14.6652617, 121.095218),
        GeoPoint(14.6652695, 121.0952371),
        GeoPoint(14.6652424, 121.0956829),
        GeoPoint(14.6648805, 121.0961861),
        GeoPoint(14.664908, 121.0963356),
        GeoPoint(14.6648531, 121.0964764),
        GeoPoint(14.6646002, 121.096494),
        GeoPoint(14.6645363, 121.0965238),
        GeoPoint(14.6645002, 121.0966408),
        GeoPoint(14.6642299, 121.0967374),
        GeoPoint(14.6637413, 121.0979213),
        GeoPoint(14.6639866, 121.0980176),
        GeoPoint(14.6642649, 121.0981473),
        GeoPoint(14.664832, 121.0983915),
        GeoPoint(14.6651508, 121.0983993),
        GeoPoint(14.667012, 121.0987996),
        GeoPoint(14.6673511, 121.0986737),
        GeoPoint(14.6678005, 121.0987592),
        GeoPoint(14.66828, 121.0989231),
        GeoPoint(14.6692092, 121.0993176),
        GeoPoint(14.6700618, 121.1002379),
        GeoPoint(14.6723195, 121.103246),
        GeoPoint(14.6727604, 121.1036883),
        GeoPoint(14.6744874, 121.1050187),
        GeoPoint(14.6752513, 121.105877),
        GeoPoint(14.6757895, 121.1066178),
        GeoPoint(14.6772824, 121.1079596),
        GeoPoint(14.6787885, 121.1088846),
        GeoPoint(14.6808973, 121.1101685),
        GeoPoint(14.6834048, 121.1116706),
        GeoPoint(14.6844409, 121.1119916),
        GeoPoint(14.6846502, 121.1121169),
        GeoPoint(14.6852978, 121.1121855),
        GeoPoint(14.6892498, 121.1113444),
        GeoPoint(14.6894359, 121.1113484),
        GeoPoint(14.6912424, 121.1113873),
        GeoPoint(14.6930258, 121.1115295),
        GeoPoint(14.693783, 121.1115761),
        GeoPoint(14.6951533, 121.1114034),
        GeoPoint(14.6957288, 121.1114141),
        GeoPoint(14.6964194, 121.1121743),
        GeoPoint(14.696915, 121.1121494),
        GeoPoint(14.6973898, 121.112502),
        GeoPoint(14.6977012, 121.1129406),
        GeoPoint(14.6979009, 121.1134183),
        GeoPoint(14.6980488, 121.1139303),
        GeoPoint(14.7208067, 121.1171018),
        GeoPoint(14.7298888, 121.1183676),
        GeoPoint(14.7307439, 121.1184868),
        GeoPoint(14.7321399, 121.1184252),
        GeoPoint(14.7327323, 121.118638),
        GeoPoint(14.7327367, 121.1183484),
        GeoPoint(14.7332343, 121.1176351),
        GeoPoint(14.7340306, 121.1166812),
        GeoPoint(14.7343126, 121.1160177),
        GeoPoint(14.7344121, 121.1157523),
        GeoPoint(14.7346858, 121.1156528),
        GeoPoint(14.7346858, 121.1153542),
        GeoPoint(14.7350341, 121.1148897),
        GeoPoint(14.735565, 121.1144336),
        GeoPoint(14.7360875, 121.1141681),
        GeoPoint(14.7372321, 121.1137369),
        GeoPoint(14.737456, 121.1138032),
        GeoPoint(14.7376302, 121.1141598),
        GeoPoint(14.7377214, 121.1145497),
        GeoPoint(14.7379454, 121.1151634),
        GeoPoint(14.7385508, 121.1157523),
        GeoPoint(14.7396788, 121.1166398),
        GeoPoint(14.7398421, 121.1167681),
        GeoPoint(14.739857, 121.117859),
        GeoPoint(14.7406808, 121.1175255),
        GeoPoint(14.7413675, 121.117651),
        GeoPoint(14.7420636, 121.1178619),
        GeoPoint(14.7428784, 121.1180428),
        GeoPoint(14.7434952, 121.1183029),
        GeoPoint(14.74502, 121.1181852),
        GeoPoint(14.745882, 121.1176944),
        GeoPoint(14.746133, 121.1176619),
        GeoPoint(14.7462763, 121.1177004),
        GeoPoint(14.7464168, 121.1177821),
        GeoPoint(14.7475179, 121.1186965),
        GeoPoint(14.7495936, 121.1181479),
        GeoPoint(14.7509132, 121.1196186),
        GeoPoint(14.7520088, 121.1206314),
        GeoPoint(14.7527807, 121.1208202),
        GeoPoint(14.7539178, 121.1210519),
        GeoPoint(14.7550217, 121.1207944),
        GeoPoint(14.7559513, 121.1213609),
        GeoPoint(14.7568643, 121.1211807),
        GeoPoint(14.7578437, 121.1215498),
        GeoPoint(14.7579018, 121.123069),
        GeoPoint(14.7598938, 121.1235239),
        GeoPoint(14.7598523, 121.124262),
        GeoPoint(14.7608898, 121.1253091),
        GeoPoint(14.7610973, 121.1252233),
        GeoPoint(14.7626983, 121.125776),
        GeoPoint(14.7631133, 121.1251752),
        GeoPoint(14.764273, 121.1246215),
        GeoPoint(14.7645778, 121.1239254),
        GeoPoint(14.7653683, 121.1237838),
        GeoPoint(14.7658129, 121.1247996),
        GeoPoint(14.7668581, 121.1259981),
        GeoPoint(14.7681074, 121.1269178),
        GeoPoint(14.7687146, 121.1267174),
        GeoPoint(14.7693315, 121.1272269),
        GeoPoint(14.7691148, 121.127839),
        GeoPoint(14.7700103, 121.1278939),
        GeoPoint(14.7714835, 121.1290096),
        GeoPoint(14.7713221, 121.1297934),
        GeoPoint(14.7714603, 121.1308227),
        GeoPoint(14.771775, 121.1322758),
        GeoPoint(14.7720049, 121.132411),
        GeoPoint(14.7741422, 121.1327295),
        GeoPoint(14.7748, 121.13332),
        GeoPoint(14.7752992, 121.1337681),
        GeoPoint(14.7756687, 121.1331762),
        GeoPoint(14.7764137, 121.1332033),
        GeoPoint(14.7764085, 121.1317064),
        GeoPoint(14.7758509, 121.1311391),
        GeoPoint(14.7751283, 121.1309266),
        GeoPoint(14.7752879, 121.1298201),
        GeoPoint(14.7762065, 121.1289228),
        GeoPoint(14.7763691, 121.1282731),
        GeoPoint(14.7760592, 121.1272065),
        GeoPoint(14.7757419, 121.126301),
        GeoPoint(14.7758945, 121.1253473),
        GeoPoint(14.7733002, 121.123635),
        GeoPoint(14.7743387, 121.1227424),
        GeoPoint(14.774863, 121.1204059),
        GeoPoint(14.7740299, 121.1191841),
        GeoPoint(14.7723201, 121.1175027),
        GeoPoint(14.772087, 121.116914),
        GeoPoint(14.7712492, 121.1139187),
        GeoPoint(14.7693916, 121.1134127),
        GeoPoint(14.7679537, 121.112593),
        GeoPoint(14.7673232, 121.112048),
        GeoPoint(14.7665244, 121.1113289),
        GeoPoint(14.7651342, 121.1099963),
        GeoPoint(14.7646242, 121.1095933),
    )
}


