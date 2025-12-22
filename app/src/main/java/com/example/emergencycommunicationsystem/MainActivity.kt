package com.example.emergencycommunicationsystem

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.navigation.Screen
import com.example.emergencycommunicationsystem.ui.components.AppBottomNavigation
import com.example.emergencycommunicationsystem.ui.screens.AboutAppScreen
import com.example.emergencycommunicationsystem.ui.screens.AlertsScreen
import com.example.emergencycommunicationsystem.ui.screens.EmergencyContactsScreen
import com.example.emergencycommunicationsystem.ui.screens.HomeScreen
import com.example.emergencycommunicationsystem.ui.screens.LanguageSettingsScreen
import com.example.emergencycommunicationsystem.ui.screens.LoginScreen
import com.example.emergencycommunicationsystem.ui.screens.MessagingScreen
import com.example.emergencycommunicationsystem.ui.screens.PrivacyPolicyScreen
import com.example.emergencycommunicationsystem.ui.screens.ProfileScreen
import com.example.emergencycommunicationsystem.ui.screens.ReportIncidentScreen
import com.example.emergencycommunicationsystem.ui.screens.SignUpScreen
import com.example.emergencycommunicationsystem.ui.screens.SignUpState
import com.example.emergencycommunicationsystem.ui.screens.SignUpViewModel
import com.example.emergencycommunicationsystem.ui.theme.EmergencyCommunicationSystemTheme
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        runBlocking {
            val lang = UserPrefs.getLanguage(newBase).first()
            super.attachBaseContext(LocaleHelper.setAppLocale(newBase, lang))
        }
    }

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
    val context = LocalContext.current
    val activity = (LocalContext.current as? ComponentActivity)
    val coroutineScope = rememberCoroutineScope()

    val isLoggedIn by AuthManager.isLoggedInFlow.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(Screen.Home.route, Screen.Alerts.route, Screen.Profile.route)
    val currentLanguage = runBlocking { UserPrefs.getLanguage(context).first() }

    Scaffold(
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    AlertsScreen(
                        onMessageClick = { alert ->
                            val userId = AuthManager.getUserId()
                            val userName = AuthManager.getUsername() ?: "User"
                            navController.navigate(
                                "${Screen.Messaging.route}?alertId=${alert.id.toInt()}&alertTitle=${alert.title}&userId=$userId&userName=$userName"
                            )
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        isLoggedIn = isLoggedIn, // <-- Pass the collected state here
                        username = if (isLoggedIn) AuthManager.getUsername() else null, // <-- Get username only if logged in
                        email = if (isLoggedIn) AuthManager.getEmail() else null,       // <-- Get email only if logged in
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                        onLogoutClick = {
                            AuthManager.logout()
                            // Navigate back to the profile screen and clear the back stack
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onLanguageSettingsClick = { navController.navigate(Screen.LanguageSettings.route) },
                        onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                        onAboutAppClick = { navController.navigate(Screen.AboutApp.route) } // Added this line
                    )
                }
                composable(Screen.EmergencyContacts.route) {
                    EmergencyContactsScreen(
                        onBackPressed = { navController.popBackStack() }
                    )
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
                composable(Screen.LanguageSettings.route) {
                    LanguageSettingsScreen(
                        currentLanguage = currentLanguage,
                        onConfirm = {
                            lang ->
                            coroutineScope.launch {
                                UserPrefs.saveLanguage(context, lang)
                                activity?.recreate()
                            }
                        },
                        onBackPressed = { navController.popBackStack() }
                    )
                }
                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(onBackPressed = { navController.popBackStack() })
                }
                composable(Screen.AboutApp.route) { // Added this block
                    AboutAppScreen(onBackPressed = { navController.popBackStack() })
                }
                composable(
                    "${Screen.Messaging.route}?alertId={alertId}&alertTitle={alertTitle}&userId={userId}&userName={userName}",
                    arguments = listOf(
                        navArgument("alertId") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("alertTitle") { type = NavType.StringType; defaultValue = "" },
                        navArgument("userId") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("userName") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val alertId = backStackEntry.arguments?.getInt("alertId") ?: -1
                    val alertTitle = backStackEntry.arguments?.getString("alertTitle") ?: ""
                    val userId = backStackEntry.arguments?.getInt("userId") ?: -1
                    val userName = backStackEntry.arguments?.getString("userName") ?: ""

                    if (alertId != -1 && userId != -1) {
                        MessagingScreen(
                            alertId = alertId,
                            alertTitle = alertTitle,
                            userId = userId,
                            userName = userName,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                }
            }

            // Overlay the oval bottom nav so it does NOT reserve Scaffold padding
            if (currentRoute in mainScreens) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding() // respect system nav/gesture area
                        .padding(bottom = 20.dp), // lift the oval a bit higher
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AppBottomNavigation(
                        selectedScreen = Screen.fromRoute(currentRoute),
                        onScreenSelected = { screen ->
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
        }
    }
}
