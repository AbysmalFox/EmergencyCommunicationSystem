package com.example.emergencycommunicationsystem.ui.screens

import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.viewmodel.ProfileViewModel
import com.example.emergencycommunicationsystem.ui.screens.NotificationSettingsScreenContent
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
    profilePic: String? = null,
    currentTheme: String, 
    onThemeChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLanguageSettingsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onMagnifierToggle: (Boolean) -> Unit,
    onMyReportsClick: () -> Unit,
    onCallHistoryClick: () -> Unit,
    profileViewModel: ProfileViewModel
) {
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    // Notification Settings State (Local for now, move to VM later)
    var deliveryMethods by remember { mutableStateOf(mapOf("Push Notifications" to true, "SMS" to false, "Email" to false)) }
    var isQuietHoursEnabled by remember { mutableStateOf(false) }
    var quietHoursStart by remember { mutableStateOf(22 to 0) } // 22:00
    var quietHoursEnd by remember { mutableStateOf(6 to 0) }   // 06:00
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
    
    // Change Password result observer
    val changePasswordResult by profileViewModel.changePasswordResult.collectAsState()
    LaunchedEffect(changePasswordResult) {
        changePasswordResult?.onSuccess {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showChangePasswordDialog = false
            profileViewModel.clearChangePasswordResult()
        }?.onFailure {
            Toast.makeText(context, it.message ?: "Change password failed", Toast.LENGTH_SHORT).show()
            profileViewModel.clearChangePasswordResult()
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(localeContext.getString(R.string.logout)) },
            text = { Text(localeContext.getString(R.string.logout_message)) },
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
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { current, new ->
                profileViewModel.changePassword(current, new)
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
                    profilePic = profilePic,
                    onEditClick = {
                        showEditProfile = true
                    }
                )
            } else {
                AnonymousHeader(onLoginClick, onSignUpClick)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // B. Settings List (Grouped)
            SettingsGroupHeader(text = localeContext.getString(R.string.preferences))
            
            SettingsItem(
                text = localeContext.getString(R.string.receive_notifications),
                icon = painterResource(id = R.drawable.ic_tabler_bell_ringing),
                trailingContent = {
                    Switch(
                        checked = true, 
                        onCheckedChange = { 
                             if (isLoggedIn) {
                                 showNotificationSettings = true 
                             } else {
                                 Toast.makeText(context, localeContext.getString(R.string.please_login_to_send_message), Toast.LENGTH_SHORT).show()
                             }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                },
                onClick = { 
                    if (isLoggedIn) showNotificationSettings = true 
                    else Toast.makeText(context, localeContext.getString(R.string.please_login_to_send_message), Toast.LENGTH_SHORT).show()
                }
            )

            SettingsItem(
                text = localeContext.getString(R.string.language_preference),
                icon = rememberVectorPainter(AppIcons.Language),
                valueText = when(currentLanguage) {
                    "fil", "tl" -> localeContext.getString(R.string.language_filipino)
                    "es" -> localeContext.getString(R.string.language_spanish)
                    "ceb" -> localeContext.getString(R.string.language_cebuano)
                    "war" -> localeContext.getString(R.string.language_waray)
                    "ilo" -> localeContext.getString(R.string.language_ilocano)
                    "bcl" -> localeContext.getString(R.string.language_bicolano)
                    else -> localeContext.getString(R.string.language_english)
                }, 
                onClick = onLanguageSettingsClick
            )

            // Embedded Theme Selector
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localeContext.getString(R.string.appearance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
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
                icon = painterResource(id = R.drawable.ic_magnifier),
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
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                },
                onClick = { /* Toggle via switch */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoggedIn) {
                SettingsGroupHeader(text = localeContext.getString(R.string.history_title))
                SettingsItem(
                    text = localeContext.getString(R.string.report_history_label),
                    icon = painterResource(id = R.drawable.ic_tabler_file_alert),
                    onClick = onMyReportsClick
                )
                SettingsItem(
                    text = localeContext.getString(R.string.call_history_label),
                    icon = painterResource(id = R.drawable.ic_tabler_phone),
                    onClick = onCallHistoryClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            SettingsGroupHeader(text = localeContext.getString(R.string.security))

            SettingsItem(
                text = localeContext.getString(R.string.change_password),
                icon = painterResource(id = R.drawable.ic_tabler_shield_check),
                onClick = { 
                    if (isLoggedIn) showChangePasswordDialog = true
                    else Toast.makeText(context, localeContext.getString(R.string.please_login_to_send_message), Toast.LENGTH_SHORT).show()
                }
            )

            SettingsItem(
                text = localeContext.getString(R.string.privacy_policy),
                icon = rememberVectorPainter(AppIcons.Security),
                onClick = onPrivacyPolicyClick
            )

            SettingsItem(
                text = localeContext.getString(R.string.terms_of_service),
                icon = rememberVectorPainter(AppIcons.Info),
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
                NotificationSettingsScreenContent(
                    categories = settingsState!!,
                    onSubscriptionChange = { categoryId, isEnabled ->
                        profileViewModel.onSubscriptionChange(categoryId, isEnabled)
                    },
                    onDeliveryMethodChange = { method, isEnabled ->
                        deliveryMethods = deliveryMethods.toMutableMap().apply { this[method] = isEnabled }
                    },
                    onQuietHoursChange = { isQuietHoursEnabled = it },
                    isQuietHoursEnabled = isQuietHoursEnabled,
                    deliveryMethods = deliveryMethods,
                    quietHoursStart = quietHoursStart,
                    quietHoursEnd = quietHoursEnd,
                    onStartTimeChange = { h, m -> quietHoursStart = h to m },
                    onEndTimeChange = { h, m -> quietHoursEnd = h to m }
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
                currentProfilePic = profilePic,
                onSave = { name, email, phone, picUri ->
                    profileViewModel.updateProfile(context, name, email, phone, picUri)
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
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val localeContext = getLocaleContext()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localeContext.getString(R.string.change_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (newPassword == confirmPassword && newPassword.isNotEmpty() && currentPassword.isNotEmpty()) {
                        onConfirm(currentPassword, newPassword)
                    }
                },
                enabled = newPassword == confirmPassword && newPassword.isNotEmpty() && currentPassword.isNotEmpty()
            ) {
                Text(localeContext.getString(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localeContext.getString(R.string.cancel))
            }
        }
    )
}

@Composable
fun EditProfileSheet(
    currentName: String,
    currentEmail: String,
    currentPhone: String,
    currentProfilePic: String?,
    onSave: (String, String, String, String?) -> Unit,
    onCancel: () -> Unit
) {
    val localeContext = getLocaleContext()
    var name by remember(currentName) { mutableStateOf(currentName) }
    var email by remember(currentEmail) { mutableStateOf(currentEmail) }
    var phone by remember(currentPhone) { mutableStateOf(currentPhone) }
    var profilePicUri by remember(currentProfilePic) { mutableStateOf<Uri?>(if (currentProfilePic != null) Uri.parse(currentProfilePic) else null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                profilePicUri = uri
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = localeContext.getString(R.string.edit_profile),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Profile Picture Edit
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                if (profilePicUri != null) {
                    AsyncImage(
                        model = profilePicUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Photo",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(localeContext.getString(R.string.full_name)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(localeContext.getString(R.string.email_address)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone field with fixed prefix logic
        OutlinedTextField(
            value = if (phone.startsWith("+63")) phone.removePrefix("+63") else phone,
            onValueChange = { newNumber ->
                val digits = newNumber.filter { it.isDigit() }
                if (digits.length <= 10) {
                    phone = "+63$digits"
                }
            },
            label = { Text(localeContext.getString(R.string.phone_number)) },
            leadingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                    Text("+63", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                }
            },
            placeholder = { Text("9123456789") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(localeContext.getString(R.string.cancel))
            }
            
            val saveBtnColor = if (MaterialTheme.colorScheme.primary == Color.White) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            val saveBtnContentColor = if (MaterialTheme.colorScheme.primary == Color.White) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary

            Button(
                onClick = { onSave(name, email, phone, profilePicUri?.toString()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = saveBtnColor,
                    contentColor = saveBtnContentColor
                )
            ) {
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
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

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
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun ProfileHeader(
    username: String,
    email: String,
    profilePic: String?,
    onEditClick: () -> Unit
) {
    val localeContext = getLocaleContext()
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
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (profilePic != null) {
                    AsyncImage(
                        model = profilePic,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = username,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = email,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onEditClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = localeContext.getString(R.string.edit_profile), 
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun AnonymousHeader(onLoginClick: () -> Unit, onSignUpClick: () -> Unit) {
    val localeContext = getLocaleContext()
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
            text = localeContext.getString(R.string.anonymous_user),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onLoginClick, 
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.height(48.dp).weight(1f).padding(start = 24.dp)
            ) {
                Text(localeContext.getString(R.string.login), fontWeight = FontWeight.ExtraBold)
            }
            OutlinedButton(
                onClick = onSignUpClick, 
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
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
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
    icon: androidx.compose.ui.graphics.painter.Painter,
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
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
        
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


