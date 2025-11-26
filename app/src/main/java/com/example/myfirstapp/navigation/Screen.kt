package com.example.myfirstapp.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Alerts : Screen("alerts")
    data object Profile : Screen("profile")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Success : Screen("success")
    data object EmergencyContacts : Screen("emergency_contacts")
    data object ReportIncident : Screen("report_incident")
}
