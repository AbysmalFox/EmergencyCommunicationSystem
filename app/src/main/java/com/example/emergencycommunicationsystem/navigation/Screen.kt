package com.example.emergencycommunicationsystem.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Alerts : Screen("alerts", "Alerts")
    data object Profile : Screen("profile", "Profile")
    data object EmergencyContacts : Screen("emergency_contacts", "Emergency Contacts")
    data object ReportIncident : Screen("report_incident", "Report Incident")
    data object Login : Screen("login", "Login") // Added Login Screen
    data object SignUp : Screen("signup", "Sign Up") // Added SignUp Screen
    data object LanguageSettings : Screen("language_settings", "Language Settings")
    data object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy")
    data object AboutApp : Screen("about_app", "About App") // Added AboutApp Screen


    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                "home" -> Home
                "alerts" -> Alerts
                "profile" -> Profile
                "login" -> Login // Added Login route handling
                "signup" -> SignUp // Added SignUp route handling
                "language_settings" -> LanguageSettings
                "privacy_policy" -> PrivacyPolicy
                "about_app" -> AboutApp // Added AboutApp route handling
                else -> Home // Default screen
            }
        }
    }
}
