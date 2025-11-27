package com.lgu.emergencycommunicationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myfirstapp.navigation.Screen
import com.example.myfirstapp.ui.components.AppBottomNavigation
import com.example.myfirstapp.ui.screens.AlertsScreen
import com.example.myfirstapp.ui.screens.EmergencyContactsScreen
import com.example.myfirstapp.ui.screens.HomeScreen
import com.example.myfirstapp.ui.screens.LoginScreen
import com.example.myfirstapp.ui.screens.ProfileScreen
import com.example.myfirstapp.ui.screens.ReportIncidentScreen
import com.example.myfirstapp.ui.screens.SignUpScreen
import com.example.myfirstapp.ui.screens.SuccessScreen
import com.example.myfirstapp.ui.theme.DarkColorScheme
import com.example.myfirstapp.viewmodel.AuthViewModel
import com.example.myfirstapp.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(colorScheme = DarkColorScheme) {
                EmergencyApp()
            }
        }
    }
}

@Composable
fun EmergencyApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherState by weatherViewModel.weatherState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(Screen.Home.route, Screen.Alerts.route, Screen.Profile.route)

    Scaffold(
        bottomBar = {
            if (currentRoute in mainScreens) {
                AppBottomNavigation(
                    selectedScreen = Screen.fromRoute(currentRoute),
                    onScreenSelected = {
                        screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onEmergencyCallClick = { navController.navigate(Screen.EmergencyContacts.route) },
                    onReportIncidentClick = { navController.navigate(Screen.ReportIncident.route) },
                    weatherViewModel = weatherViewModel
                )
            }
            composable(Screen.Alerts.route) {
                AlertsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(authViewModel = authViewModel, onLoginSuccess = { navController.navigate(Screen.Profile.route) { popUpTo(Screen.Login.route) { inclusive = true } } })
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(authViewModel = authViewModel, onSignUpSuccess = { navController.navigate(Screen.Success.route) { popUpTo(Screen.SignUp.route) { inclusive = true } } })
            }
            composable(Screen.Success.route) {
                SuccessScreen(onTimeout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Success.route) { inclusive = true } } })
            }
            composable(Screen.EmergencyContacts.route) {
                EmergencyContactsScreen(onBackPressed = { navController.popBackStack() })
            }
            composable(Screen.ReportIncident.route) {
                ReportIncidentScreen(weatherState = weatherState, onBackPressed = { navController.popBackStack() })
            }
        }
    }
}

val Screen.route: String
    get() = when (this) {
        is Screen.Home -> "home"
        is Screen.Alerts -> "alerts"
        is Screen.Profile -> "profile"
        is Screen.Login -> "login"
        is Screen.SignUp -> "signup"
        is Screen.Success -> "success"
        is Screen.EmergencyContacts -> "emergency_contacts"
        is Screen.ReportIncident -> "report_incident"
    }

// Helper to get Screen object from route string
fun Screen.Companion.fromRoute(route: String?): Screen {
    return when (route) {
        "home" -> Screen.Home
        "alerts" -> Screen.Alerts
        "profile" -> Screen.Profile
        else -> Screen.Home // Default screen
    }
}
