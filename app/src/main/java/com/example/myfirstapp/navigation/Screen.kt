package com.example.myfirstapp.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Alerts : Screen("alerts")
    data object Profile : Screen("profile")
    data object EmergencyContacts : Screen("emergency_contacts")
    data object ReportIncident : Screen("report_incident")

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                "home" -> Home
                "alerts" -> Alerts
                "profile" -> Profile
                else -> Home // Default screen
            }
        }
    }
}
