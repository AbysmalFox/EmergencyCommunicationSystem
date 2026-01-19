package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import com.example.emergencycommunicationsystem.data.UserPrefs
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
    currentTheme: String, 
    onThemeChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLanguageSettingsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onMagnifierToggle: (Boolean) -> Unit,
    profileViewModel: ProfileViewModel
) {
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val localeContext = getLocaleContext()

    val isMagnifierEnabled by UserPrefs.isMagnifierEnabled(context).collectAsState(initial = false)

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
            SettingsGroupHeader(text = "PREFERENCES")
            
            SettingsItem(
                text = localeContext.getString(R.string.receive_notifications) ?: "Push Notifications",
                trailingContent = {
                    Switch(
                        checked = true, 
                        onCheckedChange = { 
                             if (isLoggedIn) {
                                 showNotificationSettings = true 
                             } else {
                                 Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                             }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
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
                valueText = "English", 
                onClick = onLanguageSettingsClick
            )

            SettingsItem(
                text = "Appearance",
                valueText = currentTheme.replaceFirstChar { it.uppercase() },
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsGroupHeader(text = "ACCESSIBILITY")

            SettingsItem(
                text = "Floating Magnifier",
                trailingContent = {
                    Switch(
                        checked = isMagnifierEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                UserPrefs.saveMagnifierEnabled(context, enabled)
                                onMagnifierToggle(enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                },
                onClick = { /* Toggle via switch */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                onClick = onAboutAppClick 
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoggedIn) {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = localeContext.getString(R.string.logout) ?: "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showNotificationSettings) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSettings = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
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
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

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
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onEditClick() },
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.background)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = username,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Text(
            text = email,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onEditClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text("Edit Profile", fontWeight = FontWeight.Bold)
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
            color = Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.Person,
                    contentDescription = "Anonymous",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Anonymous User",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onLoginClick, 
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.height(48.dp).weight(1f).padding(start = 24.dp)
            ) {
                Text("Login", fontWeight = FontWeight.ExtraBold)
            }
            OutlinedButton(
                onClick = onSignUpClick, 
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(48.dp).weight(1f).padding(end = 24.dp)
            ) {
                Text("Sign Up", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White.copy(alpha = 0.6f),
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
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
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f)
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
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        androidx.compose.foundation.lazy.LazyColumn {
            items(categories.size) { index ->
                val category = categories[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                   Text(
                       text = category.name,
                       style = MaterialTheme.typography.bodyLarge,
                       color = MaterialTheme.colorScheme.onSurface,
                       fontWeight = FontWeight.Bold,
                       modifier = Modifier.weight(1f)
                   )
                   Switch(
                       checked = category.isSubscribed == 1,
                       onCheckedChange = { isEnabled ->
                           onSubscriptionChange(category.categoryId, isEnabled)
                       },
                       colors = SwitchDefaults.colors(
                           checkedThumbColor = Color.White,
                           checkedTrackColor = MaterialTheme.colorScheme.secondary
                       )
                   )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
