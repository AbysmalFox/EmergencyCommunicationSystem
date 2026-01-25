package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager

import androidx.compose.foundation.clickable
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import java.util.Locale

// --- Custom Palette ---
private val ColorPaper = Color(0xFFF8FAFC)
private val ColorCharcoal = Color(0xFF1E293B)
private val ColorSlate = Color(0xFF64748B)
private val ColorSuccessGreen = Color(0xFF10B981)
private val ColorInactiveGray = Color(0xFFCBD5E1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreenContent(
    categories: List<SubscriptionCategory>,
    onSubscriptionChange: (Int, Boolean) -> Unit,
    onDeliveryMethodChange: (String, Boolean) -> Unit,
    onQuietHoursChange: (Boolean) -> Unit,
    isQuietHoursEnabled: Boolean,
    deliveryMethods: Map<String, Boolean>,
    quietHoursStart: Pair<Int, Int>,
    quietHoursEnd: Pair<Int, Int>,
    onStartTimeChange: (Int, Int) -> Unit,
    onEndTimeChange: (Int, Int) -> Unit
) {
    val isDark = ThemeManager.isDarkMode()
    val scrollState = rememberScrollState()
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onConfirm = { state ->
                onStartTimeChange(state.hour, state.minute)
                showStartTimePicker = false
            },
            initialHour = quietHoursStart.first,
            initialMinute = quietHoursStart.second,
            isDark = isDark
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onConfirm = { state ->
                onEndTimeChange(state.hour, state.minute)
                showEndTimePicker = false
            },
            initialHour = quietHoursEnd.first,
            initialMinute = quietHoursEnd.second,
            isDark = isDark
        )
    }
    
    // Adaptive Colors
    val backgroundColor = MaterialTheme.colorScheme.background // Keep scaffold bg
    val cardColor = if (isDark) ColorCharcoal else ColorPaper
    val headingColor = if (isDark) ColorPaper else ColorCharcoal
    val subtextColor = if (isDark) Color(0xFF94A3B8) else ColorSlate
    val iconColor = if (isDark) Color(0xFF94A3B8) else ColorSlate

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Text(
            text = "Notification Preferences",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
        )

        // 1. Alert Categories
        SectionHeader("Alert Categories", headingColor)
        
        ModernSettingsCard(cardColor) {
            categories.forEachIndexed { index, category ->
                val icon = getIconForCategory(category.name)
                val iconTint = if (category.isSubscribed == 1) ColorSuccessGreen else iconColor
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        // Category Icon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = iconTint.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = headingColor
                            )
                            Text(
                                text = "${category.name} Updates", // Simple subtext
                                style = MaterialTheme.typography.labelMedium,
                                color = subtextColor
                            )
                        }
                    }
                    
                    ModernSwitch(
                        checked = category.isSubscribed == 1,
                        onCheckedChange = { onSubscriptionChange(category.categoryId, it) }
                    )
                }
                
                if (index < categories.size - 1) {
                    Divider(color = subtextColor.copy(alpha = 0.1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Delivery Methods
        SectionHeader("Delivery Channels", headingColor)
        
        ModernSettingsCard(cardColor) {
            val methods = listOf(
                Triple("Push Notifications", Icons.Default.Notifications, "Instant alerts on device"),
                Triple("SMS", Icons.Default.Message, "Text alerts (Rates apply)"),
                Triple("Email", Icons.Default.Email, "Detailed summaries")
            )
            
            methods.forEachIndexed { index, (name, icon, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = headingColor
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelMedium,
                                color = subtextColor
                            )
                        }
                    }
                    
                    // Checkbox styled as a modern selection
                    Checkbox(
                        checked = deliveryMethods[name] ?: false,
                        onCheckedChange = { onDeliveryMethodChange(name, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ColorSuccessGreen,
                            uncheckedColor = ColorInactiveGray,
                            checkmarkColor = Color.White
                        )
                    )
                }
                if (index < methods.size - 1) {
                    Divider(color = subtextColor.copy(alpha = 0.1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Quiet Hours
        SectionHeader("Do Not Disturb", headingColor)
        
        ModernSettingsCard(cardColor) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.1f), // Indigo tint
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.MicOff,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Quiet Hours",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = headingColor
                            )
                            Text(
                                text = "Mute non-critical alerts",
                                style = MaterialTheme.typography.labelMedium,
                                color = subtextColor
                            )
                        }
                    }
                    ModernSwitch(
                        checked = isQuietHoursEnabled,
                        onCheckedChange = { onQuietHoursChange(it) }
                    )
                }
                
                if (isQuietHoursEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = subtextColor.copy(alpha = 0.1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeSelector(
                            label = "From",
                            time = quietHoursStart,
                            onClick = { showStartTimePicker = true },
                            color = headingColor,
                            subColor = subtextColor
                        )
                        TimeSelector(
                            label = "To",
                            time = quietHoursEnd,
                            onClick = { showEndTimePicker = true },
                            color = headingColor,
                            subColor = subtextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    label: String,
    time: Pair<Int, Int>,
    onClick: () -> Unit,
    color: Color,
    subColor: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = subColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", time.first, time.second),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (TimePickerState) -> Unit,
    initialHour: Int,
    initialMinute: Int,
    isDark: Boolean
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    
    val containerColor = if (isDark) ColorCharcoal else ColorPaper
    val contentColor = if (isDark) ColorPaper else ColorCharcoal

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = containerColor,
        titleContentColor = contentColor,
        textContentColor = contentColor,
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state) }) {
                Text("OK", color = ColorSuccessGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = ColorSlate)
            }
        }
    )
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color.copy(alpha = 0.8f),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
private fun ModernSettingsCard(
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        modifier = Modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            spotColor = Color.Black.copy(alpha = 0.05f),
            ambientColor = Color.Black.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
private fun ModernSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = ColorSuccessGreen, // Vibrant Green Active
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = ColorInactiveGray, // Neutral Inactive
            uncheckedBorderColor = Color.Transparent
        ),
        modifier = Modifier.scale(0.9f)
    )
}

private fun getIconForCategory(categoryName: String): ImageVector {
    return when {
        categoryName.contains("Fire", ignoreCase = true) -> AppIcons.Fire
        categoryName.contains("Weather", ignoreCase = true) || categoryName.contains("Typhoon", ignoreCase = true) -> AppIcons.Weather
        categoryName.contains("Earthquake", ignoreCase = true) -> AppIcons.Earthquake
        categoryName.contains("Flood", ignoreCase = true) -> AppIcons.Flood
        categoryName.contains("Medical", ignoreCase = true) || categoryName.contains("Health", ignoreCase = true) -> AppIcons.Health
        categoryName.contains("Traffic", ignoreCase = true) || categoryName.contains("Accident", ignoreCase = true) -> AppIcons.Traffic
        else -> AppIcons.Info
    }
}

// Helper for scaling switches
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.requiredSize(50.dp * scale, 30.dp * scale)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
)