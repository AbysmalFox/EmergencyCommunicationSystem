package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ChevronRight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    username: String?,
    email: String?,
    phone: String?,
    currentTheme: String, // "system", "light", "dark"
    onThemeChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLanguageSettingsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAboutAppClick: () -> Unit, // Using this for Help/Terms
    profileViewModel: ProfileViewModel
) {
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val localeContext = getLocaleContext()

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(localeContext.getString(R.string.logout) ?: "Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(localeContext.getString(R.string.logout) ?: "Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Appearance") },
            text = {
                Column {
                    ThemeOption(
                        text = "System Default",
                        selected = currentTheme == "system",
                        onClick = { onThemeChange("system"); showThemeDialog = false }
                    )
                    ThemeOption(
                        text = "Light",
                        selected = currentTheme == "light",
                        onClick = { onThemeChange("light"); showThemeDialog = false }
                    )
                    ThemeOption(
                        text = "Dark",
                        selected = currentTheme == "dark",
                        onClick = { onThemeChange("dark"); showThemeDialog = false }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // A. Profile Header
            if (isLoggedIn) {
                ProfileHeader(
                    username = username ?: "User",
                    email = email ?: "",
                    onEditClick = {
                        Toast.makeText(context, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                AnonymousHeader(onLoginClick, onSignUpClick)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // B. Settings List (Grouped)
            
            // PREFERENCES Group
            SettingsGroupHeader(text = "PREFERENCES")
            
            SettingsItem(
                text = localeContext.getString(R.string.receive_notifications) ?: "Push Notifications",
                trailingContent = {
                    Switch(
                        checked = true, // Placeholder state. Real app would check ViewModel state
                        onCheckedChange = { 
                             if (isLoggedIn) {
                                 showNotificationSettings = true 
                             } else {
                                 Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                             }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                },
                onClick = { 
                    if (isLoggedIn) showNotificationSettings = true 
                    else Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                }
            )

            SettingsItem(
                text = localeContext.getString(R.string.language_preference) ?: "Language",
                valueText = "English", // In real app, bind to current language
                onClick = onLanguageSettingsClick
            )

            SettingsItem(
                text = "Appearance",
                valueText = currentTheme.replaceFirstChar { it.uppercase() },
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECURITY Group
            SettingsGroupHeader(text = "SECURITY")

            SettingsItem(
                text = "Change Password",
                onClick = { Toast.makeText(context, "Change Password clicked", Toast.LENGTH_SHORT).show() }
            )

            SettingsItem(
                text = localeContext.getString(R.string.privacy_policy) ?: "Privacy Policy",
                onClick = onPrivacyPolicyClick
            )

            SettingsItem(
                text = "Terms of Service",
                onClick = onAboutAppClick // Reusing about app for now
            )

            Spacer(modifier = Modifier.height(40.dp))

            // C. Logout
            if (isLoggedIn) {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = localeContext.getString(R.string.logout) ?: "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for nav bar
        }
    }

    // Existing Bottom Sheet Logic
    if (showNotificationSettings) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSettings = false },
            sheetState = sheetState
        ) {
            val settingsState by profileViewModel.uiState.collectAsState()
            if (settingsState != null) {
                NotificationSettingsSheet(
                    categories = settingsState!!,
                    onSubscriptionChange = { categoryId, isEnabled ->
                        profileViewModel.onSubscriptionChange(categoryId, isEnabled)
                    },
                    onDoneClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showNotificationSettings = false
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handled by Row click
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

// --- Components ---

@Composable
fun ProfileHeader(
    username: String,
    email: String,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with Badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            // Edit Badge
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onEditClick() },
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.background)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = username,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Email
        Text(
            text = email,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB0BEC5) // Light grey as requested
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Edit Profile Pill Button
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { onEditClick() }
        ) {
            Text(
                text = "Edit Profile",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnonymousHeader(onLoginClick: () -> Unit, onSignUpClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.Person,
                    contentDescription = "Anonymous",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Anonymous User",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onLoginClick, shape = RoundedCornerShape(50)) {
                Text("Login")
            }
            OutlinedButton(onClick = onSignUpClick, shape = RoundedCornerShape(50)) {
                Text("Sign Up")
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary, // Or grey
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    text: String,
    valueText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        if (trailingContent != null) {
            trailingContent()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (valueText != null) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingsSheet(
    categories: List<SubscriptionCategory>,
    onSubscriptionChange: (Int, Boolean) -> Unit,
    onDoneClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        Text(
            "Notification Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        androidx.compose.foundation.lazy.LazyColumn {
            items(categories.size) { index ->
                val category = categories[index]
                // Reuse SettingsItem-like row or simplified row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                   Text(
                       text = category.name,
                       style = MaterialTheme.typography.bodyLarge,
                       modifier = Modifier.weight(1f)
                   )
                   Switch(
                       checked = category.isSubscribed == 1,
                       onCheckedChange = { isEnabled ->
                           onSubscriptionChange(category.categoryId, isEnabled)
                       }
                   )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Done")
        }
    }
}
