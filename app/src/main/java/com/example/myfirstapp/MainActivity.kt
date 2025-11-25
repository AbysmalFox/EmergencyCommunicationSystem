package com.example.myfirstapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import com.example.myfirstapp.navigation.Screen
import com.example.myfirstapp.ui.components.AppBottomNavigation
import com.example.myfirstapp.ui.screens.AlertsScreen
import com.example.myfirstapp.ui.screens.EmergencyContactsScreen
import com.example.myfirstapp.ui.screens.HomeScreen
import com.example.myfirstapp.ui.screens.LoginScreen
import com.example.myfirstapp.ui.screens.ProfileScreen
import com.example.myfirstapp.ui.screens.ReportIncidentScreen
import com.example.myfirstapp.ui.screens.SignUpScreen
import com.example.myfirstapp.ui.theme.DarkColorScheme
import com.example.myfirstapp.viewmodel.WeatherViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = Firebase.auth

        // Your test sign-in
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseTest", "Anonymous sign-in SUCCESSFUL!")
                } else {
                    Log.e("FirebaseTest", "Anonymous sign-in FAILED", task.exception)
                }
            }

        setContent {
            MaterialTheme(colorScheme = DarkColorScheme) { 
                EmergencyApp()
            }
        }
    }
}

@Composable
fun EmergencyApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val weatherViewModel: WeatherViewModel = viewModel()
    
    // --- SOLUTION ---
    // 1. Collect the state ONCE at the top level.
    val weatherState by weatherViewModel.weatherState.collectAsState()
    
    var isLoggedIn by remember { mutableStateOf(false) } // Keep this for auth state

    when (val screen = currentScreen) {
        is Screen.Login -> LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
                currentScreen = Screen.Profile // Go to Profile after login
            },
            onSignUpClick = { currentScreen = Screen.SignUp },
            onBackPressed = { currentScreen = Screen.Home }
        )
        is Screen.SignUp -> SignUpScreen(
             onSignUpSuccess = {
                isLoggedIn = true
                currentScreen = Screen.Profile
             },
             onLoginClick = { currentScreen = Screen.Login },
             onBackPressed = { currentScreen = Screen.Home }
        )
        is Screen.EmergencyContacts -> EmergencyContactsScreen(onBackPressed = { currentScreen = Screen.Home })
        is Screen.ReportIncident -> ReportIncidentScreen(
            // 2. Pass the already-collected state down.
            weatherState = weatherState, 
            onBackPressed = { currentScreen = Screen.Home }
        )
        else -> { // Handles Home, Alerts, Profile
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    AppBottomNavigation(
                        selectedScreen = screen,
                        onScreenSelected = { newScreen -> currentScreen = newScreen }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (screen) {
                        is Screen.Home -> HomeScreen(
                            onEmergencyCallClick = { currentScreen = Screen.EmergencyContacts },
                            onReportIncidentClick = { currentScreen = Screen.ReportIncident },
                            // 3. Pass the ViewModel down to screens that need to trigger events.
                            weatherViewModel = weatherViewModel 
                        )
                        is Screen.Alerts -> AlertsScreen()
                        is Screen.Profile -> ProfileScreen(
                            isLoggedIn = isLoggedIn,
                            onLogin = { currentScreen = Screen.Login },
                            onSignUp = { currentScreen = Screen.SignUp },
                            onLogout = { isLoggedIn = false }
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}
