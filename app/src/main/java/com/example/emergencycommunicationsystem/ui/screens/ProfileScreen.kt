package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var showEditProfile by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()
    val editSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val localeContext = getLocaleContext()

    val isMagnifierEnabled by UserPrefs.isMagnifierEnabled(context).collectAsState(initial = false)
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")

    // Update Profile result observer
    val updateResult by profileViewModel.updateProfileResult.collectAsState()
    LaunchedEffect(updateResult) {
        updateResult?.onSuccess {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showEditProfile = false
            profileViewModel.clearUpdateProfileResult()
        }?.onFailure {
            Toast.makeText(context, it.message ?: "Update failed", Toast.LENGTH_SHORT).show()
            profileViewModel.clearUpdateProfileResult()
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(localeContext.getString(R.string.logout)) },
            text = { Text(localeContext.getString(R.string.logout_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(localeContext.getString(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(localeContext.getString(R.string.cancel))
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
                        showEditProfile = true
                    },
                    localeContext = localeContext
                )
            } else {
                AnonymousHeader(onLoginClick, onSignUpClick, localeContext)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // B. Settings List (Grouped)
            SettingsGroupHeader(text = localeContext.getString(R.string.preferences))
            
            SettingsItem(
                text = localeContext.getString(R.string.receive_notifications),
                icon = AppIcons.Notifications,
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
                text = localeContext.getString(R.string.language_preference),
                icon = AppIcons.Language,
                valueText = when(currentLanguage) {
                    "fil", "tl" -> "Tagalog"
                    "es" -> "Español" 
                    "ceb" -> "Cebuano"
                    "war" -> "Waray"
                    "ilo" -> "Ilocano"
                    "bcl" -> "Bicolano"
                    else -> "English"
                }, 
                onClick = onLanguageSettingsClick
            )

            // Embedded Theme Selector
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localeContext.getString(R.string.appearance),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeSelectionCard(
                    text = localeContext.getString(R.string.system_theme),
                    selected = currentTheme == "system",
                    icon = AppIcons.Settings,
                    onClick = { onThemeChange("system") },
                    modifier = Modifier.weight(1f)
                )
                ThemeSelectionCard(
                    text = localeContext.getString(R.string.light_theme),
                    selected = currentTheme == "light",
                    icon = AppIcons.LightMode, 
                    onClick = { onThemeChange("light") },
                    modifier = Modifier.weight(1f)
                )
                ThemeSelectionCard(
                    text = localeContext.getString(R.string.dark_theme),
                    selected = currentTheme == "dark",
                    icon = AppIcons.DarkMode,
                    onClick = { onThemeChange("dark") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(24.dp))

            SettingsGroupHeader(text = localeContext.getString(R.string.accessibility))

            SettingsItem(
                text = localeContext.getString(R.string.floating_magnifier),
                icon = AppIcons.Search,
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

            SettingsGroupHeader(text = localeContext.getString(R.string.security))

            SettingsItem(
                text = localeContext.getString(R.string.privacy_policy),
                icon = AppIcons.Shield,
                onClick = onPrivacyPolicyClick
            )

            SettingsItem(
                text = "Terms of Service",
                icon = AppIcons.Info,
                onClick = onAboutAppClick 
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoggedIn) {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_tabler_logout),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localeContext.getString(R.string.logout),
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

    if (showEditProfile) {
        ModalBottomSheet(
            onDismissRequest = { showEditProfile = false },
            sheetState = editSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            EditProfileSheet(
                currentName = username ?: "",
                currentEmail = email ?: "",
                currentPhone = phone ?: "",
                onSave = { name, email, phone ->
                    profileViewModel.updateProfile(name, email, phone)
                },
                onCancel = {
                    scope.launch { editSheetState.hide() }.invokeOnCompletion {
                        if (!editSheetState.isVisible) showEditProfile = false
                    }
                }
            )
        }
    }
}

@Composable
fun EditProfileSheet(
    currentName: String,
    currentEmail: String,
    currentPhone: String,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val localeContext = getLocaleContext()
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var phone by remember { mutableStateOf(currentPhone) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = localeContext.getString(R.string.edit_profile),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                val localeContext = getLocaleContext()
                Text(localeContext.getString(R.string.cancel))
            }
            Button(
                onClick = { onSave(name, email, phone) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                val localeContext = getLocaleContext()
                Text(localeContext.getString(R.string.save_changes))
            }
        }
    }
}

@Composable
fun ThemeSelectionCard(
    text: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f)
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    
    // For inline display on the dark profile screen, unselected content must be White
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else Color.White

    Card(
        onClick = onClick,
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
fun ProfileHeader(
    username: String,
    email: String,
    onEditClick: () -> Unit,
    localeContext: android.content.Context
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
            Text(localeContext.getString(R.string.edit_profile), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnonymousHeader(onLoginClick: () -> Unit, onSignUpClick: () -> Unit, localeContext: android.content.Context) {
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
            text = localeContext.getString(R.string.anonymous_user),
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
                Text(localeContext.getString(R.string.login), fontWeight = FontWeight.ExtraBold)
            }
            OutlinedButton(
                onClick = onSignUpClick, 
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(48.dp).weight(1f).padding(end = 24.dp)
            ) {
                Text(localeContext.getString(R.string.signup), fontWeight = FontWeight.ExtraBold)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NotificationSettingsSheet(
    categories: List<SubscriptionCategory>,
    onSubscriptionChange: (Int, Boolean) -> Unit,
    onDoneClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Push Notifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDoneClick) {
                Text("Done", color = MaterialTheme.colorScheme.secondary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Receive alerts for ${category.name.lowercase()} events",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = category.isSubscribed == 1,
                    onCheckedChange = { onSubscriptionChange(category.categoryId, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}
