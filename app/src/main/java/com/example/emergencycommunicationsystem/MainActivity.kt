package com.example.emergencycommunicationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.emergencycommunicationsystem.navigation.Screen
import com.example.emergencycommunicationsystem.ui.components.AppBottomNavigation
import com.example.emergencycommunicationsystem.ui.screens.AlertsScreen
import com.example.emergencycommunicationsystem.ui.screens.EmergencyContactsScreen
import com.example.emergencycommunicationsystem.ui.screens.HomeScreen
import com.example.emergencycommunicationsystem.ui.screens.ProfileScreen
import com.example.emergencycommunicationsystem.ui.screens.ReportIncidentScreen
import com.example.emergencycommunicationsystem.ui.screens.LoginScreen
import androidx.compose.runtime.Composable
import com.example.emergencycommunicationsystem.ui.screens.SignUpScreen
import com.example.emergencycommunicationsystem.ui.screens.SignUpViewModel
import com.example.emergencycommunicationsystem.ui.screens.SignUpState
import com.example.emergencycommunicationsystem.ui.theme.EmergencyCommunicationSystemTheme
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import com.example.emergencycommunicationsystem.AuthManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.initialize(applicationContext)
        enableEdgeToEdge()

        setContent {
            EmergencyCommunicationSystemTheme {
                EmergencyApp()
            }
        }
    }
}

@Composable
fun EmergencyApp() {
    val navController = rememberNavController()
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherState by weatherViewModel.weatherState.collectAsState()

    val isLoggedIn by AuthManager.isLoggedInFlow.collectAsState()

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
                    isLoggedIn = isLoggedIn,
                    username = if (isLoggedIn) AuthManager.getUsername() else null,
                    email = if (isLoggedIn) AuthManager.getEmail() else null,
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                    onLogoutClick = { AuthManager.logout() }
                )
            }
            composable(Screen.EmergencyContacts.route) {
                EmergencyContactsScreen(onBackPressed = { navController.popBackStack() })
            }
            composable(Screen.ReportIncident.route) {
                ReportIncidentScreen(weatherState = weatherState, onBackPressed = { navController.popBackStack() })
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onBackPressed = { navController.popBackStack() },
                    onLoginSuccess = { userId, username, email, token ->
                        AuthManager.saveLoginState(userId, username, email, token)
                        navController.popBackStack()
                    },
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) }
                )
            }
            composable(Screen.SignUp.route) {
                val viewModel: SignUpViewModel = viewModel()
                val state by viewModel.signUpState.collectAsState()

                LaunchedEffect(state) {
                    if (state is SignUpState.Success) {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                }

                SignUpScreen(
                    state = state,
                    onSignUpClick = { fullName, email, password, confirmPassword ->
                        viewModel.signUp(fullName, email, password, confirmPassword)
                    },
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}