package com.example.myfirstapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapp.ui.components.ProfileItem
import com.example.myfirstapp.ui.components.SectionTitle
import com.example.myfirstapp.viewmodel.AuthViewModel
import com.example.myfirstapp.viewmodel.AuthState

@Composable
fun AuthPromptScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGuestModeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Create an account or log in", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "You\'ll be able to save your details and preferences.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onGuestModeClick) {
            Text("Continue as Guest", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (authState) {
                is AuthState.Authenticated -> {
                    // Screen for logged-in users
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Header
                        item {
                            Text(
                                "Profile & Settings",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }

                        // Profile Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "User avatar",
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(authViewModel.currentUser?.email ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = { /* TODO */ },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Edit Profile", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Settings Items
                        item { SectionTitle("Settings") }
                        item {
                            ProfileItem(icon = Icons.Default.Notifications, text = "Notifications", hasSwitch = true)
                        }
                        item {
                            ProfileItem(icon = Icons.Default.Security, text = "Privacy & Security")
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // Legal Items
                        item { SectionTitle("Legal") }
                        item {
                            ProfileItem(icon = Icons.Default.Description, text = "Terms of Service")
                        }
                        item {
                            ProfileItem(icon = Icons.Default.Info, text = "About this App")
                        }

                        // Logout Button
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { authViewModel.logout() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Logout")
                            }
                        }
                    }
                }
                is AuthState.Anonymous -> {
                    // Screen for guest users
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item { Text("Profile & Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 24.dp)) }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = "User avatar", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Anonymous User", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = onLoginClick, modifier = Modifier.weight(1f)) { Text("Login") }
                                        OutlinedButton(onClick = onSignUpClick, modifier = Modifier.weight(1f)) { Text("Sign Up") }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        item { SectionTitle("Settings") }
                        item { ProfileItem(icon = Icons.Default.Notifications, text = "Notifications", hasSwitch = true) }
                        item { ProfileItem(icon = Icons.Default.Security, text = "Privacy & Security") }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { SectionTitle("Legal") }
                        item { ProfileItem(icon = Icons.Default.Description, text = "Terms of Service") }
                        item { ProfileItem(icon = Icons.Default.Info, text = "About this App") }
                    }
                }
                is AuthState.Unauthenticated -> {
                    AuthPromptScreen(
                        onLoginClick = onLoginClick,
                        onSignUpClick = onSignUpClick,
                        onGuestModeClick = { authViewModel.signInAnonymously() }
                    )
                }
                else -> {
                    // Loading or Error states can be handled here
                }
            }
        }
    }
}
