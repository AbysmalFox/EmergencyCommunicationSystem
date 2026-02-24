package com.example.emergencycommunicationsystem

import android.Manifest
import android.os.Bundle
import android.os.Build
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
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
import com.example.emergencycommunicationsystem.ui.BaseActivity
import com.example.emergencycommunicationsystem.ui.screens.*
import com.example.emergencycommunicationsystem.ui.theme.EmergencyCommunicationSystemTheme
import com.example.emergencycommunicationsystem.util.LocationUpdater
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.util.LocaleProvider
import com.example.emergencycommunicationsystem.util.LocaleManager
import com.example.emergencycommunicationsystem.util.GeminiWeatherService
import com.example.emergencycommunicationsystem.viewmodel.ProfileViewModel
import com.example.emergencycommunicationsystem.viewmodel.ProfileViewModelFactory
import com.example.emergencycommunicationsystem.ui.screens.SignUpViewModel
import com.example.emergencycommunicationsystem.ui.screens.SignUpState
import com.example.emergencycommunicationsystem.ui.screens.InternetCallScreen
import com.example.emergencycommunicationsystem.ui.screens.CallHistoryScreen
import com.example.emergencycommunicationsystem.ui.screens.MyReportsScreen
import com.example.emergencycommunicationsystem.data.repository.CallRepository
import com.example.emergencycommunicationsystem.viewmodel.WeatherViewModel
import com.example.emergencycommunicationsystem.viewmodel.InternetCallViewModel
import com.example.emergencycommunicationsystem.webrtc.SignalingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        
        enableEdgeToEdge()

        // Initialize system in background
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Initialize ApiClient first as others might depend on it
                ApiClient.initializeAndCheckConnection(applicationContext)
                
                // Subscribe to Global Emergency Topic
                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("emergency-room")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("MainActivity", "Subscribed to emergency-room topic")
                        }
                    }

                Log.i("MainActivity", "System initialization completed successfully.")
            } catch (e: Exception) {
                Log.e("MainActivity", "Initialization failed", e)
            }
        }
        
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            val context = LocalContext.current
            val currentTheme by UserPrefs.getTheme(context).collectAsState(initial = "light")
            val isDarkTheme = currentTheme == "dark"

            EmergencyCommunicationSystemTheme(darkTheme = isDarkTheme) {
                LocaleProvider {
                    EmergencyApp(
                        currentTheme = currentTheme,
                        onThemeChange = { newTheme ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                UserPrefs.saveTheme(context, newTheme)
                            }
                        },
                        onMagnifierToggle = { enabled ->
                            if (enabled) setupMagnifier() else removeMagnifier()
                        },
                        onLanguageChange = {
                            GeminiWeatherService.clearCache()
                            recreate()
                        }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATIONS_CODE
                )
            }
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS_CODE = 1001
    }
}

@Composable
fun EmergencyApp(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    onMagnifierToggle: (Boolean) -> Unit,
    onLanguageChange: () -> Unit
) {
    val navController = rememberNavController()
    val weatherViewModel: WeatherViewModel = viewModel()
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val messagingRepository = remember { MessagingRepository() }
    val settingsRepository = remember { SettingsRepository() }

    val isLoggedIn by AuthManager.isLoggedInFlow.collectAsState()
    val username by AuthManager.usernameFlow.collectAsState()
    val email by AuthManager.emailFlow.collectAsState()
    val phone by AuthManager.phoneFlow.collectAsState()
    val profilePic by AuthManager.profilePicFlow.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(
        Screen.Home.route,
        Screen.Alerts.route,
        Screen.Alerts.routeWithArgs,
        Screen.Map.route,
        Screen.Profile.route
    )

    if (isLoggedIn) {
        LocationUpdater {
            latitude, longitude, accuracy ->
            val userId = AuthManager.getUserId()
            if (userId != -1) {
                coroutineScope.launch {
                    try {
                        val address = LocationUtils.getAddressFromCoordinates(context, latitude, longitude)
                        settingsRepository.updateUserLocation(userId, latitude, longitude, address, accuracy)
                        Log.d("MainActivity", "Location updated successfully for user $userId")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to update location on server: ${e.message}")
                    }
                }
            }
        }
    }

    fun navigateToMessaging(alertId: Int, alertTitle: String) {
        if (alertId <= 0) {
            Toast.makeText(context, "Invalid Alert Data", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = if (isLoggedIn) AuthManager.getUserId().toString() else "guest_${System.currentTimeMillis()}"
        val userName = if (isLoggedIn) (username ?: "User") else "Guest"
        val userEmail = if (isLoggedIn) (email ?: "") else ""
        val userPhone = if (isLoggedIn) (phone ?: "") else ""
        val encodedTitle = URLEncoder.encode(alertTitle, "UTF-8")
        val encodedName = URLEncoder.encode(userName, "UTF-8")
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val encodedPhone = URLEncoder.encode(userPhone, "UTF-8")

        navController.navigate(
            "${Screen.Messaging.route}?alertId=$alertId&alertTitle=$encodedTitle&userId=$userId&userName=$encodedName&userEmail=$encodedEmail&userPhone=$encodedPhone"
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
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
                        onInternetCallClick = { callType ->
                            navController.navigate(Screen.InternetCall.createRoute(callType))
                        },
                        onReportIncidentClick = { navController.navigate(Screen.ReportIncident.route) },
                        onMessageClick = {
                            navigateToMessaging(alertId = 999, alertTitle = "General Inquiry")
                        },
                        onAlertClick = { alertId ->
                            navController.navigate(Screen.Alerts.createRoute(alertId)) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onEmergencyGuidesClick = {
                            navController.navigate(Screen.EmergencyGuides.route)
                        },
                        weatherViewModel = weatherViewModel
                    )
                }
                composable(
                    route = Screen.Alerts.routeWithArgs,
                    arguments = listOf(
                        navArgument("alertId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val alertId = backStackEntry.arguments?.getString("alertId")
                    AlertsScreen(
                        weatherViewModel = weatherViewModel,
                        alertId = alertId,
                        onMessageClick = { alertId, alertTitle ->
                            try {
                                val userId = if (isLoggedIn) AuthManager.getUserId().toString() else "guest_${System.currentTimeMillis()}"
                                val alertIdInt = alertId.toInt()
                                val encodedTitle = URLEncoder.encode(alertTitle, "UTF-8")
                                val userName = if (isLoggedIn) (username ?: "User") else "Guest"
                                val userEmail = if (isLoggedIn) (email ?: "") else ""
                                val userPhone = if (isLoggedIn) (phone ?: "") else ""
                                
                                val encodedName = URLEncoder.encode(userName, "UTF-8")
                                val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
                                val encodedPhone = URLEncoder.encode(userPhone, "UTF-8")
                                
                                navController.navigate("${Screen.Messaging.route}?alertId=$alertIdInt&alertTitle=$encodedTitle&userId=$userId&userName=$encodedName&userEmail=$encodedEmail&userPhone=$encodedPhone")
                            } catch (e: Exception) {
                                Log.e("NavigationError", "Error navigating to messaging", e)
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
                    
                    // Observe user data changes
                    val userDataUpdate by AuthManager.userDataFlow.collectAsState(initial = Unit)
                    
                    ProfileScreen(
                        isLoggedIn = isLoggedIn,
                        username = if (isLoggedIn) username else null,
                        email = if (isLoggedIn) email else null,
                        phone = if (isLoggedIn) phone else null,
                        profilePic = if (isLoggedIn) profilePic else null,
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
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
                        onMagnifierToggle = onMagnifierToggle,
                        onMyReportsClick = { navController.navigate(Screen.MyReports.route) },
                        onCallHistoryClick = { navController.navigate(Screen.CallHistory.route) },
                        profileViewModel = profileViewModel
                    )
                }
                composable(Screen.EmergencyContacts.route) {
                    EmergencyContactsScreen(
                        onBackPressed = { navController.popBackStack() },
                        onMyReportsClick = { navController.navigate(Screen.MyReports.route) }
                    )
                }
                composable(Screen.ReportIncident.route) {
                    ReportIncidentScreen(weatherState = weatherState, onBackPressed = { navController.popBackStack() })
                }
                composable(Screen.CallHistory.route) {
                    CallHistoryScreen(onBackPressed = { navController.popBackStack() })
                }
                composable(Screen.EmergencyGuides.route) {
                    EmergencyGuidesScreen(
                        onBackPressed = { navController.popBackStack() },
                        onGuideClick = { guideId ->
                            navController.navigate(Screen.EmergencyGuideDetail.createRoute(guideId))
                        }
                    )
                }
                composable(
                    route = Screen.EmergencyGuideDetail.route,
                    arguments = listOf(navArgument("guideId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val guideId = backStackEntry.arguments?.getString("guideId") ?: ""
                    EmergencyGuideDetailScreen(guideId = guideId, onBackPressed = { navController.popBackStack() })
                }
                composable(Screen.MyReports.route) {
                    val userId = AuthManager.getUserId()
                    if (userId != -1) {
                        MyReportsScreen(userId = userId, onBackPressed = { navController.popBackStack() })
                    } else {
                        // Handle case where user is not logged in but somehow reached this screen
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
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
                    val otpState by viewModel.otpState.collectAsState()

                    SignUpScreen(
                        state = state,
                        otpState = otpState,
                        onSignUpClick = { fullName, email, phone, password, confirmPassword, locationPermissionGranted, latitude, longitude, address ->
                            viewModel.signUp(fullName, email, phone, password, confirmPassword, locationPermissionGranted, latitude, longitude, address)
                        },
                        onEmailChanged = { email -> viewModel.onEmailChanged(email) },
                        onSendOtpClick = { email -> viewModel.sendOtp(email) },
                        onVerifyOtpClick = { email, otp -> viewModel.verifyOtp(email, otp) },
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
                        currentLanguage = UserPrefs.getLanguage(context).collectAsState(initial = "en").value,
                        onConfirm = { lang ->
                            coroutineScope.launch(Dispatchers.IO) {
                                UserPrefs.saveLanguage(context, lang)
                                withContext(Dispatchers.Main) {
                                    onLanguageChange()
                                }
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
                    "${Screen.Messaging.route}?alertId={alertId}&alertTitle={alertTitle}&userId={userId}&userName={userName}&userEmail={userEmail}&userPhone={userPhone}",
                    arguments = listOf(
                        navArgument("alertId") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("alertTitle") { type = NavType.StringType; defaultValue = "" },
                        navArgument("userId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("userName") { type = NavType.StringType; defaultValue = "" },
                        navArgument("userEmail") { type = NavType.StringType; defaultValue = "" },
                        navArgument("userPhone") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val alertId = backStackEntry.arguments?.getInt("alertId") ?: -1
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    val alertTitle = URLDecoder.decode(backStackEntry.arguments?.getString("alertTitle") ?: "Chat", "UTF-8")
                    val userName = URLDecoder.decode(backStackEntry.arguments?.getString("userName") ?: "Guest", "UTF-8")
                    val userEmail = URLDecoder.decode(backStackEntry.arguments?.getString("userEmail") ?: "", "UTF-8")
                    val userPhone = URLDecoder.decode(backStackEntry.arguments?.getString("userPhone") ?: "", "UTF-8")
                    
                    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")

                    if (alertId > 0) {
                        val factory = MessagingViewModelFactory(
                            alertId = alertId,
                            userId = userId,
                            userName = userName,
                            userEmail = userEmail.ifEmpty { null },
                            userPhone = userPhone.ifEmpty { null },
                            alertTitle = alertTitle,
                            repository = messagingRepository,
                            currentLanguage = currentLanguage
                        )
                        val messagingViewModel: MessagingViewModel = viewModel(key = "messaging_${alertId}_$currentLanguage", factory = factory)

                        if (alertId == 999) {
                            ResponderChatScreen(
                                viewModel = messagingViewModel,
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
                            ChatbotScreen(
                                viewModel = messagingViewModel,
                                alertTitle = alertTitle,
                                onBackPressed = { navController.popBackStack() },
                                onNavigateToPersistentChat = {
                                    navigateToMessaging(alertId = 999, alertTitle = "General Inquiry")
                                },
                                onNavigateToEmergencyContacts = {
                                    navController.navigate(Screen.EmergencyContacts.route)
                                }
                            )
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            Toast.makeText(context, "Invalid chat session.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                }
                composable(
                    route = Screen.InternetCall.route,
                    arguments = listOf(
                        navArgument("callType") {
                            type = NavType.StringType
                            defaultValue = "internet"
                        }
                    )
                ) { backStackEntry ->
                    val callTypeArg = backStackEntry.arguments?.getString("callType") ?: "internet"
                    val callRepository = remember { CallRepository(context) }
                    val signalingManager = remember { SignalingManager(context.applicationContext) }
                    val viewModel: InternetCallViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return InternetCallViewModel(callRepository, signalingManager) as T
                            }
                        }
                    )
                    InternetCallScreen(
                        onEndCall = { navController.popBackStack() },
                        viewModel = viewModel,
                        callType = callTypeArg
                    )
                }
            }

            if (currentRoute in mainScreens) {
                BottomNavigationBar(
                    navController = navController,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
