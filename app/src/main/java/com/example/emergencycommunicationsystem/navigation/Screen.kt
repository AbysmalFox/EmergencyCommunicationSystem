package com.example.emergencycommunicationsystem.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Alerts : Screen("alerts")
    data object Profile : Screen("profile")
    data object EmergencyContacts : Screen("emergency_contacts")
    data object ReportIncident : Screen("report_incident")
    data object Login : Screen("login") // Added Login Screen
    data object SignUp : Screen("signup") // Added SignUp Screen

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                "home" -> Home
                "alerts" -> Alerts
                "profile" -> Profile
                "login" -> Login // Added Login route handling
                "signup" -> SignUp // Added SignUp route handling
                else -> Home // Default screen
            }
        }
    }
}
