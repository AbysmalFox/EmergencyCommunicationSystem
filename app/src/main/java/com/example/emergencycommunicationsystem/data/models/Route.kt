package com.example.emergencycommunicationsystem.data.models

/**
 * Route response from OSRM routing API
 */
data class RouteResponse(
    val code: String,
    val routes: List<Route>,
    val waypoints: List<Waypoint>
)

/**
 * OSRM Error response
 */
data class OSRMErrorResponse(
    val code: String,
    val message: String
)

data class Route(
    val geometry: Any?, // Can be String (encoded polyline) or Map (GeoJSON)
    val legs: List<RouteLeg>,
    val distance: Double, // in meters
    val duration: Double, // in seconds
    val weight: Double
)

data class RouteLeg(
    val steps: List<RouteStep>,
    val distance: Double,
    val duration: Double,
    val summary: String? = null
)

data class RouteStep(
    val distance: Double, // in meters
    val duration: Double, // in seconds
    val geometry: Any?, // Can be String (encoded polyline) or Map (GeoJSON)
    val maneuver: Maneuver,
    val name: String? = null,
    val mode: String? = null,
    val intersections: List<Intersection>? = null
)

data class Maneuver(
    val bearing_after: Int? = null,
    val bearing_before: Int? = null,
    val location: List<Double>, // [longitude, latitude]
    val modifier: String? = null, // left, right, straight, slight_left, etc.
    val type: String // turn, depart, arrive, etc.
)

data class Intersection(
    val location: List<Double>, // [longitude, latitude]
    val bearings: List<Int>? = null,
    val entry: List<Boolean>? = null,
    val in_: Int? = null,
    val out: Int? = null
)

data class Waypoint(
    val hint: String? = null,
    val distance: Double? = null,
    val name: String? = null,
    val location: List<Double> // [longitude, latitude]
)

/**
 * Navigation instruction for turn-by-turn directions
 */
data class NavigationInstruction(
    val stepIndex: Int,
    val instruction: String, // e.g., "Turn right onto Commonwealth Avenue"
    val distance: Double, // in meters
    val duration: Double, // in seconds
    val maneuverType: ManeuverType,
    val modifier: String? = null, // left, right, straight, etc.
    val streetName: String? = null
)

enum class ManeuverType {
    TURN,
    DEPART,
    ARRIVE,
    CONTINUE,
    ROUNDABOUT,
    FORK,
    MERGE,
    ON_RAMP,
    OFF_RAMP,
    UNKNOWN
}

/**
 * Current navigation state
 */
data class NavigationState(
    val isNavigating: Boolean = false,
    val isOffRoute: Boolean = false,
    val currentStepIndex: Int = 0,
    val instructions: List<NavigationInstruction> = emptyList(),
    val totalDistance: Double = 0.0, // in meters
    val totalDuration: Double = 0.0, // in seconds
    val remainingDistance: Double = 0.0, // in meters
    val remainingDuration: Double = 0.0, // in seconds
    val currentInstruction: NavigationInstruction? = null,
    val nextInstruction: NavigationInstruction? = null,
    val routeGeometry: List<org.osmdroid.util.GeoPoint> = emptyList()
)
