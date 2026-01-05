package com.example.emergencycommunicationsystem.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    data object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    data object Map : Screen("map", "Map", Icons.Default.Map)
    data object EmergencyContacts : Screen("emergency_contacts", "Emergency Contacts", null)
    data object ReportIncident : Screen("report_incident", "Report Incident", null)
    data object Login : Screen("login", "Login", null) // Added Login Screen
    data object SignUp : Screen("signup", "Sign Up", null) // Added SignUp Screen
    data object LanguageSettings : Screen("language_settings", "Language Settings", null)
    data object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", null)
    data object AboutApp : Screen("about_app", "About App", null) // Added AboutApp Screen
    data object Messaging : Screen("messaging", "Messaging", null) // Added Messaging Screen
    data object AutoReplyChat : Screen("auto_reply_chat", "Auto-Reply Chat", null)


    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                "home" -> Home
                "alerts" -> Alerts
                "profile" -> Profile
                "map" -> Map
                "login" -> Login // Added Login route handling
                "signup" -> SignUp // Added SignUp route handling
                "language_settings" -> LanguageSettings
                "privacy_policy" -> PrivacyPolicy
                "about_app" -> AboutApp // Added AboutApp route handling
                "messaging" -> Messaging // Added Messaging route handling
                "auto_reply_chat" -> AutoReplyChat
                else -> Home // Default screen
            }
        }
    }
}
