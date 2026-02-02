package com.example.emergencycommunicationsystem.data.network

import com.example.emergencycommunicationsystem.util.LogFilter
import com.example.emergencycommunicationsystem.data.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

/**
 * Service for fetching routes from OSRM (Open Source Routing Machine)
 * OSRM is free and doesn't require an API key
 */
object RoutingService {
    private const val TAG = "RoutingService"
    
    // Error categories for better error handling
    enum class ErrorCategory {
        NETWORK,
        INVALID_INPUT,
        PARSING,
        ROUTING_SERVICE,
        UNKNOWN
    }
    
    data class RoutingError(
        val category: ErrorCategory,
        val message: String,
        val userMessage: String,
        val throwable: Throwable? = null
    )
    
    // Using public OSRM demo server (you can also host your own)
    private const val OSRM_BASE_URL = "https://router.project-osrm.org"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()

    /**
     * Get route from origin to destination
     * @param originLat Origin latitude
     * @param originLon Origin longitude
     * @param destLat Destination latitude
     * @param destLon Destination longitude
     * @return RouteResponse with route details
     */
    suspend fun getRoute(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double
    ): Result<RouteResponse> = withContext(Dispatchers.IO) {
        try {
            LogFilter.d(TAG, "Requesting route from ($originLat, $originLon) to ($destLat, $destLon)")
            
            // Validate coordinates
            if (!isValidCoordinate(originLat, originLon) || !isValidCoordinate(destLat, destLon)) {
                val error = RoutingError(
                    category = ErrorCategory.INVALID_INPUT,
                    message = "Invalid coordinates: origin=($originLat, $originLon), dest=($destLat, $destLon)",
                    userMessage = "Invalid location coordinates. Please check your location."
                )
                LogFilter.e(TAG, error.message)
                return@withContext Result.failure(IllegalArgumentException(error.message))
            }
            
            // Additional validation: Check if coordinates are reasonable for Quezon City area
            // Quezon City is approximately: lat 14.4-14.8, lon 120.9-121.2
            val qcLatMin = 14.0
            val qcLatMax = 15.0
            val qcLonMin = 120.5
            val qcLonMax = 121.5
            
            val originInQC = originLat in qcLatMin..qcLatMax && originLon in qcLonMin..qcLonMax
            val destInQC = destLat in qcLatMin..qcLatMax && destLon in qcLonMin..qcLonMax
            
            if (!originInQC || !destInQC) {
                val error = RoutingError(
                    category = ErrorCategory.INVALID_INPUT,
                    message = "Coordinates outside expected region: origin=($originLat, $originLon), dest=($destLat, $destLon). Expected Quezon City area (lat: 14.0-15.0, lon: 120.5-121.5)",
                    userMessage = "Location appears to be outside Quezon City. Please ensure GPS is enabled and location services are working correctly."
                )
                LogFilter.w(TAG, error.message)
                // Don't fail here - let OSRM try, but log a warning
            }
            
            // OSRM route API format: /route/v1/{profile}/{coordinates}?{options}
            // profile: driving, walking, cycling
            // coordinates: lon1,lat1;lon2,lat2
            val coordinates = "$originLon,$originLat;$destLon,$destLat"
            val url = "$OSRM_BASE_URL/route/v1/driving/$coordinates?overview=full&geometries=geojson&steps=true"
            
            LogFilter.d(TAG, "OSRM API URL: $url")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            
            LogFilter.d(TAG, "OSRM API response: code=${response.code}, duration=${duration}ms")
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "No error body"
                val routingError = try {
                    val errorResponse = gson.fromJson(errorBody, OSRMErrorResponse::class.java)
                    when (errorResponse.code) {
                        "NoRoute" -> RoutingError(
                            category = ErrorCategory.ROUTING_SERVICE,
                            message = "OSRM: No route found between points",
                            userMessage = "No road route available. Points may be too far apart or not connected by roads."
                        )
                        "InvalidInput" -> RoutingError(
                            category = ErrorCategory.INVALID_INPUT,
                            message = "OSRM: Invalid input coordinates",
                            userMessage = "Invalid location coordinates."
                        )
                        "NoSegment" -> RoutingError(
                            category = ErrorCategory.ROUTING_SERVICE,
                            message = "OSRM: No segment found near coordinates",
                            userMessage = "No roads found near the specified locations."
                        )
                        else -> RoutingError(
                            category = ErrorCategory.ROUTING_SERVICE,
                            message = "OSRM error code: ${errorResponse.code}, message: ${errorResponse.message}",
                            userMessage = errorResponse.message.takeIf { it.isNotBlank() } 
                                ?: "Routing service error: ${errorResponse.code}"
                        )
                    }
                } catch (e: Exception) {
                    // If we can't parse the error, use generic message
                    RoutingError(
                        category = ErrorCategory.ROUTING_SERVICE,
                        message = "HTTP ${response.code} ${response.message}. Body: $errorBody",
                        userMessage = when (response.code) {
                            400 -> "Invalid route request. Please check the locations."
                            404 -> "Routing service not found."
                            429 -> "Too many requests. Please try again later."
                            500, 502, 503 -> "Routing service temporarily unavailable. Please try again later."
                            else -> "Failed to get route: HTTP ${response.code}"
                        },
                        throwable = e
                    )
                }
                
                LogFilter.e(TAG, "OSRM API error: ${routingError.message}", routingError.throwable)
                return@withContext Result.failure(Exception(routingError.userMessage))
            }
            
            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                val error = RoutingError(
                    category = ErrorCategory.NETWORK,
                    message = "Empty response body from OSRM API",
                    userMessage = "No data received from routing service."
                )
                LogFilter.e(TAG, error.message)
                return@withContext Result.failure(Exception(error.userMessage))
            }
            
            LogFilter.d(TAG, "Response body length: ${responseBody.length} characters")
            
            try {
                val routeResponse = gson.fromJson(responseBody, RouteResponse::class.java)
                
                if (routeResponse.code != "Ok") {
                    val error = RoutingError(
                        category = ErrorCategory.ROUTING_SERVICE,
                        message = "OSRM routing failed with code: ${routeResponse.code}",
                        userMessage = "Routing failed: ${routeResponse.code}"
                    )
                    LogFilter.e(TAG, error.message)
                    return@withContext Result.failure(Exception(error.userMessage))
                }
                
                if (routeResponse.routes.isEmpty()) {
                    val error = RoutingError(
                        category = ErrorCategory.ROUTING_SERVICE,
                        message = "OSRM returned empty routes list",
                        userMessage = "No routes found for the specified locations."
                    )
                    LogFilter.e(TAG, error.message)
                    return@withContext Result.failure(Exception(error.userMessage))
                }
                
                val route = routeResponse.routes[0]
                LogFilter.d(TAG, "Route found: distance=${route.distance}m, duration=${route.duration}s, legs=${route.legs.size}")
                
                Result.success(routeResponse)
            } catch (e: com.google.gson.JsonSyntaxException) {
                val error = RoutingError(
                    category = ErrorCategory.PARSING,
                    message = "Failed to parse OSRM JSON response: ${e.message}",
                    userMessage = "Invalid route data format received.",
                    throwable = e
                )
                LogFilter.e(TAG, error.message, e)
                Result.failure(Exception(error.userMessage))
            }
        } catch (e: java.net.UnknownHostException) {
            val error = RoutingError(
                category = ErrorCategory.NETWORK,
                message = "Network error: Cannot reach OSRM server",
                userMessage = "No internet connection. Please check your network.",
                throwable = e
            )
            LogFilter.e(TAG, error.message, e)
            Result.failure(Exception(error.userMessage))
        } catch (e: java.net.SocketTimeoutException) {
            val error = RoutingError(
                category = ErrorCategory.NETWORK,
                message = "Network timeout: OSRM server took too long to respond",
                userMessage = "Request timeout. Please try again.",
                throwable = e
            )
            LogFilter.e(TAG, error.message, e)
            Result.failure(Exception(error.userMessage))
        } catch (e: java.io.IOException) {
            val error = RoutingError(
                category = ErrorCategory.NETWORK,
                message = "IO error while fetching route: ${e.message}",
                userMessage = "Network error. Please check your connection.",
                throwable = e
            )
            LogFilter.e(TAG, error.message, e)
            Result.failure(Exception(error.userMessage))
        } catch (e: Exception) {
            val error = RoutingError(
                category = ErrorCategory.UNKNOWN,
                message = "Unexpected error while fetching route: ${e.message}",
                userMessage = "An unexpected error occurred. Please try again.",
                throwable = e
            )
            LogFilter.e(TAG, error.message, e)
            Result.failure(Exception(error.userMessage))
        }
    }
    
    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }

    /**
     * Decode polyline geometry from OSRM response
     * OSRM returns geometry as a string (encoded polyline) or GeoJSON object
     * When using geometries=geojson, it returns: {"type":"LineString","coordinates":[[lon,lat],...]}
     */
    fun decodeGeometry(geometryString: String): List<org.osmdroid.util.GeoPoint> {
        try {
            if (geometryString.isBlank()) {
                LogFilter.w(TAG, "Empty geometry string provided")
                return emptyList()
            }
            
            // Try to parse as JSON first (GeoJSON format)
            if (geometryString.trimStart().startsWith("{")) {
                val jsonObject = gson.fromJson(geometryString, Map::class.java) as? Map<*, *>
                val type = jsonObject?.get("type") as? String
                
                if (type != "LineString") {
                    LogFilter.w(TAG, "Unexpected GeoJSON type: $type (expected LineString)")
                }
                
                val coordinates = jsonObject?.get("coordinates") as? List<*>
                
                if (coordinates == null) {
                    LogFilter.w(TAG, "No coordinates found in GeoJSON geometry")
                    return emptyList()
                }
                
                val points = coordinates.mapNotNull { coord ->
                    val coordList = coord as? List<*>
                    if (coordList != null && coordList.size >= 2) {
                        try {
                            val lon = (coordList[0] as? Number)?.toDouble()
                            val lat = (coordList[1] as? Number)?.toDouble()
                            
                            if (lon != null && lat != null && isValidCoordinate(lat, lon)) {
                                org.osmdroid.util.GeoPoint(lat, lon)
                            } else {
                                // Only log first few invalid coordinates to avoid spam
                                null
                            }
                        } catch (e: Exception) {
                            // Suppress individual coordinate parsing errors
                            null
                        }
                    } else {
                        null
                    }
                }
                
                if (points.isEmpty()) {
                    LogFilter.w(TAG, "No valid coordinates decoded from geometry")
                } else {
                    LogFilter.d(TAG, "Decoded ${points.size} points from geometry")
                }
                return points
            } else {
                // If it's an encoded polyline string, return empty (would need polyline decoder)
                LogFilter.w(TAG, "Encoded polyline format not supported. Geometry string doesn't start with '{'")
                return emptyList()
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            LogFilter.e(TAG, "JSON syntax error while decoding geometry: ${e.message}", e)
            return emptyList()
        } catch (e: Exception) {
            LogFilter.e(TAG, "Unexpected error while decoding geometry: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Convert OSRM route to navigation instructions
     */
    fun convertToNavigationInstructions(route: Route): List<NavigationInstruction> {
        val instructions = mutableListOf<NavigationInstruction>()
        
        route.legs.forEachIndexed { legIndex, leg ->
            leg.steps.forEachIndexed { stepIndex, step ->
                val maneuverType = when (step.maneuver.type) {
                    "turn" -> ManeuverType.TURN
                    "depart" -> ManeuverType.DEPART
                    "arrive" -> ManeuverType.ARRIVE
                    "continue" -> ManeuverType.CONTINUE
                    "roundabout" -> ManeuverType.ROUNDABOUT
                    "fork" -> ManeuverType.FORK
                    "merge" -> ManeuverType.MERGE
                    "on ramp" -> ManeuverType.ON_RAMP
                    "off ramp" -> ManeuverType.OFF_RAMP
                    else -> ManeuverType.UNKNOWN
                }
                
                val instructionText = buildInstructionText(step, maneuverType)
                
                instructions.add(
                    NavigationInstruction(
                        stepIndex = instructions.size,
                        instruction = instructionText,
                        distance = step.distance,
                        duration = step.duration,
                        maneuverType = maneuverType,
                        modifier = step.maneuver.modifier,
                        streetName = step.name
                    )
                )
            }
        }
        
        return instructions
    }

    private fun buildInstructionText(step: RouteStep, maneuverType: ManeuverType): String {
        val modifier = step.maneuver.modifier ?: ""
        val streetName = step.name ?: "the road"
        
        return when (maneuverType) {
            ManeuverType.DEPART -> "Start navigation to $streetName"
            ManeuverType.ARRIVE -> "Arrive at destination"
            ManeuverType.TURN -> {
                when (modifier) {
                    "left" -> "Turn left onto $streetName"
                    "right" -> "Turn right onto $streetName"
                    "slight left" -> "Slight left onto $streetName"
                    "slight right" -> "Slight right onto $streetName"
                    "sharp left" -> "Sharp left onto $streetName"
                    "sharp right" -> "Sharp right onto $streetName"
                    "straight" -> "Continue straight on $streetName"
                    else -> "Turn onto $streetName"
                }
            }
            ManeuverType.CONTINUE -> "Continue on $streetName"
            ManeuverType.ROUNDABOUT -> "Enter roundabout and take exit onto $streetName"
            ManeuverType.FORK -> "At fork, take $modifier onto $streetName"
            ManeuverType.MERGE -> "Merge onto $streetName"
            ManeuverType.ON_RAMP -> "Take ramp onto $streetName"
            ManeuverType.OFF_RAMP -> "Take exit ramp onto $streetName"
            ManeuverType.UNKNOWN -> "Follow $streetName"
        }
    }
}
