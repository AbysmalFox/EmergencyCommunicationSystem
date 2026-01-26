package com.example.emergencycommunicationsystem.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.emergencycommunicationsystem.ui.icons.AppIcons

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    data object Home : Screen("home", "Home", AppIcons.Home)
    data object Alerts : Screen("alerts", "Alerts", AppIcons.Alerts)
    data object Profile : Screen("profile", "Profile", AppIcons.Profile)
    data object Map : Screen("map", "Map", AppIcons.Map)
    data object EmergencyContacts : Screen("emergency_contacts", "Emergency Contacts", null)
    data object ReportIncident : Screen("report_incident", "Report Incident", null)
    data object Login : Screen("login", "Login", null) // Added Login Screen
    data object SignUp : Screen("signup", "Sign Up", null) // Added SignUp Screen
    data object LanguageSettings : Screen("language_settings", "Language Settings", null)
    data object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", null)
    data object AboutApp : Screen("about_app", "About App", null) // Added AboutApp Screen
    data object Messaging : Screen("messaging", "Messaging", null) // Added Messaging Screen
    data object AutoReplyChat : Screen("auto_reply_chat", "Auto-Reply Chat", null)
    data object EmergencyGuides : Screen("emergency_guides", "Emergency Guides", null)
    data object EmergencyGuideDetail : Screen("emergency_guide_detail/{guideId}", "Emergency Guide Detail", null) {
        fun createRoute(guideId: String) = "emergency_guide_detail/$guideId"
    }
    data object MyReports : Screen("my_reports", "My Reports", null)

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
                "emergency_contacts" -> EmergencyContacts
                "emergency_guides" -> EmergencyGuides
                "my_reports" -> MyReports
                else -> {
                    // Handle dynamic route for EmergencyGuideDetail
                    if (route?.startsWith("emergency_guide_detail/") == true) {
                        EmergencyGuideDetail
                    } else {
                        Home // Default screen
                    }
                }
            }
        }
    }
}
