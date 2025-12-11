package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emergencycommunicationsystem.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    username: String?,
    email: String?,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLanguageSettingsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAboutAppClick: () -> Unit
) {
    var showNotificationSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.profile_and_settings),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                if (isLoggedIn) {
                    LoggedInUserCard(username = username, email = email, onLogoutClick = onLogoutClick)
                } else {
                    AnonymousUserCard(onLoginClick = onLoginClick, onSignUpClick = onSignUpClick)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            item {
                ProfileItem(
                    icon = Icons.Default.Notifications,
                    text = stringResource(R.string.receive_notifications),
                    onClick = { showNotificationSettings = true }
                )
            }
            item {
                ProfileItem(
                    icon = Icons.Default.Language,
                    text = stringResource(R.string.language_preference),
                    onClick = onLanguageSettingsClick
                )
            }
            item {
                ProfileItem(
                    icon = Icons.Default.Security,
                    text = stringResource(R.string.privacy_policy),
                    onClick = onPrivacyPolicyClick
                )
            }
            item {
                ProfileItem(
                    icon = Icons.Default.Info,
                    text = stringResource(R.string.about_app),
                    onClick = onAboutAppClick
                )
            }
        }

        if (showNotificationSettings) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationSettings = false },
                sheetState = sheetState
            ) {
                NotificationSettingsSheet(onDoneClick = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showNotificationSettings = false
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun NotificationSettingsSheet(onDoneClick: () -> Unit) {
    var receiveNotifications by remember { mutableStateOf(true) }
    var crimeAlerts by remember { mutableStateOf(true) }
    var disasterWarnings by remember { mutableStateOf(true) }
    var fireAlerts by remember { mutableStateOf(true) }
    var weatherAdvisories by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        Text(
            "Notification Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ProfileItem(
            icon = Icons.Default.Notifications,
            text = "Receive Notifications",
            checked = receiveNotifications,
            onCheckedChange = { receiveNotifications = it }
        )

        AnimatedVisibility(
            visible = receiveNotifications,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                ProfileItem(
                    icon = Icons.Default.LocalPolice, 
                    text = "Crime Alerts",
                    checked = crimeAlerts,
                    onCheckedChange = { crimeAlerts = it }
                )
                ProfileItem(
                    icon = Icons.Default.Warning,
                    text = "Disaster Warnings",
                    checked = disasterWarnings,
                    onCheckedChange = { disasterWarnings = it }
                )
                ProfileItem(
                    icon = Icons.Default.LocalFireDepartment,
                    text = "Fire Alerts",
                    checked = fireAlerts,
                    onCheckedChange = { fireAlerts = it }
                )
                ProfileItem(
                    icon = Icons.Default.Air,
                    text = "Weather Advisories",
                    checked = weatherAdvisories,
                    onCheckedChange = { weatherAdvisories = it }
                )
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

@Composable
private fun AnonymousUserCard(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLoginClick, modifier = Modifier.weight(1f)) {
                    Text("Login")
                }
                OutlinedButton(onClick = onSignUpClick, modifier = Modifier.weight(1f)) {
                    Text("Sign Up")
                }
            }
        }
    }
}

@Composable
private fun LoggedInUserCard(
    username: String?,
    email: String?,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth() 
                .padding(vertical = 24.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User avatar",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = username ?: "User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLogoutClick) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileItem(
    icon: ImageVector,
    text: String,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val isSwitchItem = checked != null && onCheckedChange != null

    Card(
        onClick = { 
            if (onClick != null) onClick() 
            else if (isSwitchItem) onCheckedChange?.invoke(checked.not()) 
        },
        modifier = Modifier.fillMaxWidth(),
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
                    checked = checked ?: false,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    )
                )
            } else if (onClick != null) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
