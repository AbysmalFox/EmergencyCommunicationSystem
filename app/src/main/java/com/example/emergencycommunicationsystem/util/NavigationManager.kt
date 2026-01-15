package com.example.emergencycommunicationsystem.util

import com.example.emergencycommunicationsystem.data.models.NavigationInstruction
import com.example.emergencycommunicationsystem.data.models.NavigationState
import com.example.emergencycommunicationsystem.data.models.Route
import com.example.emergencycommunicationsystem.data.network.RoutingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.math.*

/**
 * Manages real-time navigation state and updates
 */
class NavigationManager(
    private val coroutineScope: CoroutineScope
) {
    private val TAG = "NavigationManager"
    
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private var currentRoute: Route? = null
    private var routeGeometry: List<GeoPoint> = emptyList()

    /**
     * Start navigation to destination
     */
    fun startNavigation(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double,
        onSuccess: (List<GeoPoint>) -> Unit,
        onError: (String) -> Unit
    ) {
        LogFilter.d(TAG, "Starting navigation from ($originLat, $originLon) to ($destLat, $destLon)")
        
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val result = RoutingService.getRoute(originLat, originLon, destLat, destLon)
                
                result.onSuccess { routeResponse ->
                    if (routeResponse.routes.isNotEmpty()) {
                        val route = routeResponse.routes[0]
                        currentRoute = route
                        
                        LogFilter.d(TAG, "Route received: ${route.legs.size} legs, ${route.distance}m total distance")
                        
                        // Decode geometry from route or steps
                        routeGeometry = try {
                            val routeGeom = when (val geom = route.geometry) {
                                is String -> {
                                    RoutingService.decodeGeometry(geom)
                                }
                                is Map<*, *> -> {
                                    val gson = com.google.gson.Gson()
                                    RoutingService.decodeGeometry(gson.toJson(geom))
                                }
                                else -> {
                                    LogFilter.w(TAG, "Route geometry is null or unknown type, using step geometries")
                                    emptyList()
                                }
                            }
                            
                            if (routeGeom.isNotEmpty()) {
                                LogFilter.d(TAG, "Using route-level geometry: ${routeGeom.size} points")
                                routeGeom
                            } else {
                                // Fallback: decode from step geometries
                                LogFilter.d(TAG, "Decoding geometry from ${route.legs.size} legs")
                                val stepGeom = route.legs.flatMapIndexed { legIndex, leg ->
                                    leg.steps.flatMapIndexed { stepIndex, step ->
                                        try {
                                            val stepGeomStr = when (val geom = step.geometry) {
                                                is String -> geom
                                                is Map<*, *> -> {
                                                    val gson = com.google.gson.Gson()
                                                    gson.toJson(geom)
                                                }
                                                else -> null
                                            }
                                            
                                            if (stepGeomStr != null) {
                                                RoutingService.decodeGeometry(stepGeomStr)
                                            } else {
                                                emptyList()
                                            }
                                        } catch (e: Exception) {
                                            // Only log first error per leg to avoid spam
                                            if (stepIndex == 0) {
                                                LogFilter.e(TAG, "Error decoding step geometry (leg $legIndex): ${e.message}", e)
                                            }
                                            emptyList()
                                        }
                                    }
                                }
                                LogFilter.d(TAG, "Decoded ${stepGeom.size} points from step geometries")
                                stepGeom
                            }
                        } catch (e: Exception) {
                            LogFilter.e(TAG, "Error decoding route geometry: ${e.message}", e)
                            emptyList()
                        }
                        
                        if (routeGeometry.isEmpty()) {
                            val error = "Failed to decode route geometry"
                            LogFilter.e(TAG, error)
                            onError(error)
                            return@launch
                        }
                        
                        // Convert to navigation instructions
                        val instructions = try {
                            RoutingService.convertToNavigationInstructions(route)
                        } catch (e: Exception) {
                            LogFilter.e(TAG, "Error converting route to instructions: ${e.message}", e)
                            emptyList()
                        }
                        
                        LogFilter.d(TAG, "Generated ${instructions.size} navigation instructions")
                        
                        val newState = NavigationState(
                            isNavigating = true,
                            currentStepIndex = 0,
                            instructions = instructions,
                            totalDistance = route.distance,
                            totalDuration = route.duration,
                            remainingDistance = route.distance,
                            remainingDuration = route.duration,
                            currentInstruction = instructions.firstOrNull(),
                            nextInstruction = instructions.getOrNull(1),
                            routeGeometry = routeGeometry
                        )
                        
                        _navigationState.value = newState
                        LogFilter.d(TAG, "Navigation started successfully")
                        onSuccess(routeGeometry)
                    } else {
                        val error = "OSRM returned empty routes list"
                        LogFilter.e(TAG, error)
                        onError(error)
                    }
                }.onFailure { error ->
                    val errorMsg = error.message ?: "Unknown error"
                    LogFilter.e(TAG, "Failed to start navigation: $errorMsg", error)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Unexpected error starting navigation: ${e.message}"
                LogFilter.e(TAG, errorMsg, e)
                onError(errorMsg)
            }
        }
    }

    /**
     * Update navigation based on current user location
     */
    fun updateLocation(currentLat: Double, currentLon: Double) {
        val state = _navigationState.value
        if (!state.isNavigating) {
            // Suppress this log - it's called frequently when not navigating
            return
        }
        
        if (routeGeometry.isEmpty()) {
            LogFilter.w(TAG, "updateLocation called but route geometry is empty")
            return
        }

        try {
            // Find closest point on route
            val currentPoint = GeoPoint(currentLat, currentLon)
            val closestRoutePoint = routeGeometry.minByOrNull { point ->
                calculateDistance(currentPoint, point)
            }
            
            if (closestRoutePoint == null) {
                LogFilter.w(TAG, "Could not find closest route point")
                return
            }

            // Find current step based on closest route point
            val routePointIndex = routeGeometry.indexOf(closestRoutePoint)
            if (routePointIndex < 0) {
                LogFilter.w(TAG, "Route point index not found")
                return
            }
            
            val progress = routePointIndex.toDouble() / routeGeometry.size
            val distanceToRoute = calculateDistance(currentPoint, closestRoutePoint)
            
            // Log if user is far from route (might be off-route)
            if (distanceToRoute > 150) {
                LogFilter.w(TAG, "User is ${distanceToRoute.toInt()}m away from route (might be off-route)")
            }

            // Calculate remaining distance more accurately from route geometry
            // Calculate distance from current position to destination along the route
            var remainingDistance = 0.0
            var remainingDuration = 0.0
            var currentStepIndex = 0
            
            // Calculate remaining distance along the route geometry from current point to end
            if (routePointIndex < routeGeometry.size - 1) {
                // Sum distances between remaining route points
                for (i in routePointIndex until routeGeometry.size - 1) {
                    remainingDistance += calculateDistance(
                        routeGeometry[i],
                        routeGeometry[i + 1]
                    )
                }
                
                // Also calculate remaining duration based on progress
                val remainingProgress = 1.0 - progress
                remainingDuration = state.totalDuration * remainingProgress
                
                // Find current step index based on progress
                var accumulatedDistance = 0.0
                currentRoute?.legs?.forEach { leg ->
                    leg.steps.forEachIndexed { stepIndex, step ->
                        if (accumulatedDistance < state.totalDistance * progress) {
                            accumulatedDistance += step.distance
                            currentStepIndex = stepIndex
                        }
                    }
                }
            } else {
                // Fallback: use step-based calculation
                var accumulatedDistance = 0.0
                var accumulatedDuration = 0.0

                currentRoute?.legs?.forEach { leg ->
                    leg.steps.forEachIndexed { stepIndex, step ->
                        val stepDistance = step.distance
                        val stepDuration = step.duration
                        
                        if (accumulatedDistance < state.totalDistance * progress) {
                            accumulatedDistance += stepDistance
                            accumulatedDuration += stepDuration
                            currentStepIndex = stepIndex
                        } else {
                            remainingDistance += stepDistance
                            remainingDuration += stepDuration
                        }
                    }
                }
            }

            // Update current instruction
            val currentInstruction = state.instructions.getOrNull(currentStepIndex)
            val nextInstruction = state.instructions.getOrNull(currentStepIndex + 1)
            
            // Only log step changes to reduce verbosity
            if (currentStepIndex != state.currentStepIndex) {
                LogFilter.d(TAG, "Step changed: ${state.currentStepIndex} -> $currentStepIndex")
            }

            _navigationState.value = state.copy(
                currentStepIndex = currentStepIndex,
                remainingDistance = remainingDistance,
                remainingDuration = remainingDuration,
                currentInstruction = currentInstruction,
                nextInstruction = nextInstruction
            )
        } catch (e: Exception) {
            LogFilter.e(TAG, "Error updating location: ${e.message}", e)
        }
    }

    /**
     * Stop navigation
     */
    fun stopNavigation() {
        LogFilter.d(TAG, "Stopping navigation")
        _navigationState.value = NavigationState()
        currentRoute = null
        routeGeometry = emptyList()
        LogFilter.d(TAG, "Navigation stopped")
    }

    /**
     * Calculate distance between two GeoPoints in meters (Haversine formula)
     */
    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return 6371000 * c // Earth radius in meters
    }

    /**
     * Format duration as ETA string
     */
    fun formatETA(seconds: Double): String {
        val totalMinutes = (seconds / 60).toInt()
        if (totalMinutes < 60) {
            return "$totalMinutes min"
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes > 0) {
            "$hours h $minutes min"
        } else {
            "$hours h"
        }
    }
}
