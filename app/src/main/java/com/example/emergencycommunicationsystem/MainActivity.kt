package com.example.emergencycommunicationsystem

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import com.example.emergencycommunicationsystem.data.repository.SettingsRepository
import com.example.emergencycommunicationsystem.navigation.BottomNavigationBar
import com.example.emergencycommunicationsystem.navigation.Screen
import com.example.emergencycommunicationsystem.ui.screens.*
import com.example.emergencycommunicationsystem.ui.theme.EmergencyCommunicationSystemTheme
import com.example.emergencycommunicationsystem.util.LocationUpdater
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.viewmodel.ProfileViewModel
import com.example.emergencycommunicationsystem.viewmodel.ProfileViewModelFactory
import com.example.emergencycommunicationsystem.ui.screens.SignUpViewModel
import com.example.emergencycommunicationsystem.ui.screens.SignUpState
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required OSMDroid configuration
        Configuration.getInstance().userAgentValue = packageName

        ApiClient.initializeAndCheckConnection()
        AuthManager.initialize(applicationContext)
        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            val lang = UserPrefs.getLanguage(this@MainActivity).first()
            LocaleHelper.setAppLocale(this@MainActivity, lang)
        }

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

    val messagingRepository = remember { MessagingRepository() }
    val settingsRepository = remember { SettingsRepository() }

    val isLoggedIn by AuthManager.isLoggedInFlow.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(Screen.Home.route, Screen.Alerts.route, Screen.Map.route, Screen.Profile.route)
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")

    if (isLoggedIn) {
        LocationUpdater {
            latitude, longitude, accuracy ->
            coroutineScope.launch {
                try {
                    val userId = AuthManager.getUserId()
                    if (userId != -1) {
                        val address = LocationUtils.getAddressFromCoordinates(context, latitude, longitude)
                        settingsRepository.updateUserLocation(userId, latitude, longitude, address, accuracy)
                        Log.d("MainActivity", "Location updated successfully for user $userId")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to update location on server", e)
                }
            }
        }
    }

    fun navigateToMessaging(alertId: Int, alertTitle: String) {
        if (isLoggedIn) {
            if (alertId <= 0) {
                Toast.makeText(context, "Invalid Alert Data", Toast.LENGTH_SHORT).show()
                return
            }

            val userId = AuthManager.getUserId()
            val userName = AuthManager.getUsername() ?: "User"
            val encodedTitle = URLEncoder.encode(alertTitle, "UTF-8")

            navController.navigate(
                "${Screen.Messaging.route}?alertId=$alertId&alertTitle=$encodedTitle&userId=$userId&userName=$userName"
            )
        } else {
            Toast.makeText(context, "Please log in to send a message", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onEmergencyCallClick = { navController.navigate(Screen.EmergencyContacts.route) },
                        onReportIncidentClick = { navController.navigate(Screen.ReportIncident.route) },
                        onMessageClick = {
                            navigateToMessaging(alertId = 999, alertTitle = "General Inquiry")
                        },
                        weatherViewModel = weatherViewModel
                    )
                }
                composable(Screen.Alerts.route) {
                    AlertsScreen(
                        onMessageClick = { alertId, alertTitle ->
                            try { // <-- PROPER ERROR HANDLING
                                Log.d("Navigation", "Attempting to navigate for alertId: '''$alertId'''")
                                val userId = AuthManager.getUserId()
                                if (userId > 0) {
                                    val alertIdInt = alertId.toInt() // The risky operation
                                    val encodedTitle = URLEncoder.encode(alertTitle, "UTF-8")
                                    val userName = AuthManager.getUsername() ?: "User"
                                    navController.navigate("${Screen.Messaging.route}?alertId=$alertIdInt&alertTitle=$encodedTitle&userId=$userId&userName=$userName")
                                    Log.i("Navigation", "Successfully navigated to MessagingScreen for alertId: $alertIdInt.")
                                } else {
                                    Log.w("Navigation", "Navigation blocked: User is not logged in (userId: $userId).")
                                    Toast.makeText(context, "Please log in to send a message", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: NumberFormatException) {
                                // THIS IS THE LOGCAT MESSAGE YOU NEED
                                Log.e("NavigationError", "Failed to navigate. The alert ID '''$alertId''' is not a valid integer.", e)
                                // Also show a user-friendly message
                                Toast.makeText(context, "Error: Invalid alert data. Cannot open message.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                // Catch any other unexpected errors during navigation
                                Log.e("NavigationError", "An unexpected error occurred during navigation for alert: $alertTitle", e)
                                Toast.makeText(context, "An unexpected error occurred.", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
                composable(Screen.Map.route) {
                    MapScreen()
                }
                composable(Screen.Profile.route) {
                    val userId = AuthManager.getUserId()
                    val factory = remember(userId, settingsRepository) { ProfileViewModelFactory(userId, settingsRepository) }
                    val profileViewModel: ProfileViewModel = viewModel(key = "profile_$userId", factory = factory)

                    ProfileScreen(
                        isLoggedIn = isLoggedIn,
                        username = if (isLoggedIn) AuthManager.getUsername() else null,
                        email = if (isLoggedIn) AuthManager.getEmail() else null,
                        phone = if (isLoggedIn) AuthManager.getPhone() else null,
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                        onLogoutClick = {
                            coroutineScope.launch {
                                AuthManager.logout(context)
                                navController.navigate(Screen.Profile.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onLanguageSettingsClick = { navController.navigate(Screen.LanguageSettings.route) },
                        onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                        onAboutAppClick = { navController.navigate(Screen.AboutApp.route) },
                        profileViewModel = profileViewModel
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
                        onLoginSuccess = { userId, username, email, phone, token ->
                            AuthManager.saveLoginState(userId, username, email, phone, token)
                            navController.popBackStack()
                        },
                        onSignUpClick = { navController.navigate(Screen.SignUp.route) }
                    )
                }
                composable(Screen.SignUp.route) {
                    val viewModel: SignUpViewModel = viewModel()
                    val state by viewModel.signUpState.collectAsState()

                    SignUpScreen(
                        state = state,
                        onSignUpClick = { fullName, email, phone, password, confirmPassword, locationPermissionGranted, latitude, longitude, address ->
                            viewModel.signUp(fullName, email, phone, password, confirmPassword, locationPermissionGranted, latitude, longitude, address)
                        },
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        onBackPressed = { navController.popBackStack() },
                        onRegistrationSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
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
                composable(Screen.AboutApp.route) {
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
                    val userId = backStackEntry.arguments?.getInt("userId") ?: -1
                    val alertTitle = URLDecoder.decode(backStackEntry.arguments?.getString("alertTitle") ?: "Chat", "UTF-8")
                    val userName = backStackEntry.arguments?.getString("userName") ?: ""

                    if (alertId > 0 && userId > 0) {
                        val factory = MessagingViewModelFactory(alertId, userId, alertTitle, messagingRepository)
                        val messagingViewModel: MessagingViewModel = viewModel(key = "messaging_$alertId", factory = factory)

                        MessagingScreen(
                            viewModel = messagingViewModel,
                            alertId = alertId,
                            alertTitle = alertTitle,
                            userName = userName,
                            onBackPressed = { navController.popBackStack() },
                            onNavigateToPersistentChat = {
                                navigateToMessaging(alertId = 999, alertTitle = "General Inquiry")
                            },
                            onNavigateToEmergencyContacts = {
                                navController.navigate(Screen.EmergencyContacts.route)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            Toast.makeText(context, "Invalid chat session.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                }
            }

            if (currentRoute in mainScreens) {
                BottomNavigationBar(
                    navController = navController,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
