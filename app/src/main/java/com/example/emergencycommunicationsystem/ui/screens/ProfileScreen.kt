package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Re-using the ProfileItem from earlier discussions, assuming it's in a Components.kt or similar
@Composable
fun ProfileItem(icon: ImageVector, text: String, checked: Boolean? = null, onCheckedChange: ((Boolean) -> Unit)? = null, onClick: (() -> Unit)? = null) {
    val isSwitchItem = checked != null && onCheckedChange != null
    var internalChecked by remember(checked) { mutableStateOf(checked ?: false) }

    Card(
        onClick = { if (!isSwitchItem && onClick != null) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (isSwitchItem) {
                Switch(
                    checked = internalChecked,
                    onCheckedChange = {
                        internalChecked = it
                        onCheckedChange?.invoke(it) // Propagate change upwards
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    )
                )
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit, // This will be used when we implement login
    onSignUpClick: () -> Unit, // This is the one we'll connect
    // Add other callbacks for other settings items if needed
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    "Profile & Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Anonymous User Card (or logged-in user card)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User avatar",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Anonymous User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Login Button (still disabled for now, but will use onLoginClick later)
                            Button(onClick = onLoginClick, modifier = Modifier.weight(1f), enabled = false) {
                                Text("Login")
                            }
                            // Sign Up Button - NOW ENABLED!
                            OutlinedButton(
                                onClick = onSignUpClick, // This will navigate to SignUpScreen
                                modifier = Modifier.weight(1f),
                                enabled = true // <--- CHANGED TO TRUE
                            ) {
                                Text("Sign Up")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Example Settings Items (you might have different ones)
            item {
                var receiveNotifications by remember { mutableStateOf(true) } // Example state
                ProfileItem(
                    icon = Icons.Default.Notifications,
                    text = "Receive Notifications",
                    checked = receiveNotifications,
                    onCheckedChange = { receiveNotifications = it }
                )
            }
            item {
                ProfileItem(
                    icon = Icons.Default.Language,
                    text = "Language Preference",
                    onClick = { /* Handle language settings click */ }
                )
            }
            item {
                ProfileItem(
                    icon = Icons.Default.Security,
                    text = "Privacy Policy",
                    onClick = { /* Handle privacy policy click */ }
                )
            }
            item {
                ProfileItem(
                    icon = Icons.Default.Info,
                    text = "About App",
                    onClick = { /* Handle about app click */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Logout Button (currently removed as no auth, but would go here)
                // If a user is logged in, you'd show a logout button instead of login/signup
                // Button(onClick = { /* Handle logout */ }, modifier = Modifier.fillMaxWidth()) {
                //     Text("Logout")
                // }
            }
        }
    }
}