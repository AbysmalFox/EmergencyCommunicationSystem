package com.example.emergencycommunicationsystem.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun ProfileItem(icon: ImageVector, text: String, hasSwitch: Boolean = false, onClick: () -> Unit = {}) {
    var isChecked by remember { mutableStateOf(true) }
    Card(
        onClick = { if (!hasSwitch) onClick() },
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
            if (hasSwitch) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
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
fun SectionTitle(title: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = color, modifier = Modifier.padding(bottom = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonRow(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEach { label ->
            SegmentedButton(
                shape = RoundedCornerShape(50),
                onClick = { onOptionSelected(label) },
                selected = (label == selectedOption),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun EmergencyCallButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC93F3F) // Red color from image
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp, // Reduced elevation
                shape = shape,
                spotColor = Color.Red,
                ambientColor = Color.Red.copy(alpha = 0.4f)
            )
            .height(80.dp) // Reduced height
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = stringResource(R.string.emergency_call_label),
                tint = Color.White,
                modifier = Modifier.size(32.dp) // Reduced icon size
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.emergency_call_label),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, // Reduced font size
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = stringResource(R.string.call_button),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp, // Reduced font size
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}


@Composable
fun ActionGrid(onEmergencyCallClick: () -> Unit, onReportClick: () -> Unit, onSafeClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) { // Reduced spacing
        EmergencyCallButton(onClick = onEmergencyCallClick)
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionGridItem(stringResource(R.string.report_incident), Icons.Filled.Warning, onClick = onReportClick, modifier = Modifier.weight(1f))
            ActionGridItem(stringResource(R.string.i_am_safe), Icons.Filled.CheckCircle, onClick = onSafeClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ActionGridItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.3f))
            .height(80.dp), // Reduced height
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp)) // Reduced icon size
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) // Reduced font size
        }
    }
}


@Composable
fun WeatherWidget(state: WeatherState) {
    val shape = RoundedCornerShape(24.dp)
    when (state) {
        is WeatherState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is WeatherState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.3f))
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = state.iconUrl,
                        contentDescription = state.condition,
                        modifier = Modifier.size(90.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row {
                            Text(
                                text = state.temperature.substringBefore("."),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = ".${state.temperature.substringAfter(".").substringBefore("°")}°C",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                        Text(
                            text = state.condition,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.location,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                WeatherDetailsRow(state)

                Spacer(modifier = Modifier.height(24.dp))

                WeatherAdvice(advice = state.advice)
            }
        }
        is WeatherState.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.gps_signal_lost),
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsRow(state: WeatherState.Success) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        WeatherDetailItem(
            icon = Icons.Default.Thermostat,
            label = stringResource(R.string.feels_like),
            value = state.feelsLike
        )
        WeatherDetailItem(
            icon = Icons.Default.WaterDrop,
            label = stringResource(R.string.humidity),
            value = state.humidity
        )
        WeatherDetailItem(
            icon = Icons.Default.Air,
            label = stringResource(R.string.wind),
            value = state.windSpeed
        )
        WeatherDetailItem(
            icon = Icons.Default.Visibility,
            label = stringResource(R.string.visibility),
            value = state.visibility
        )
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeatherAdvice(advice: String) {
    var displayedText by remember(advice) { mutableStateOf("") }

    LaunchedEffect(advice) {
        displayedText = ""
        delay(200)
        advice.forEachIndexed { index, _ ->
            displayedText = advice.substring(0, index + 1)
            delay(30)
        }
    }

    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Weather Advice",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = 12.dp, top = 4.dp)
                .size(24.dp)
        )
        Text(
            text = stringResource(R.string.weather_widget_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HotlineItem(name: String, number: String) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            Button(onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Filled.Call, contentDescription = "Call $name", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun AppBottomNavigation(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    val items = listOf(Screen.Home, Screen.Alerts, Screen.Profile)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 65.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50),
                    clip = true
                ),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Increased height to accommodate text
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    BottomNavItem(
                        screen = screen,
                        isSelected = selectedScreen == screen,
                        onSelected = { onScreenSelected(screen) }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(screen: Screen, isSelected: Boolean, onSelected: () -> Unit) {
    val icon = when (screen) {
        Screen.Home -> Icons.Filled.Home
        Screen.Alerts -> Icons.Filled.Notifications
        Screen.Profile -> Icons.Filled.Person
        else -> Icons.Filled.Error // Should not happen
    }
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "icon color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "text color"
    )


    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(24.dp)) // Use rounded corner shape for better click feedback
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelected
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = screen.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val title = when (screen) {
            Screen.Home -> stringResource(R.string.home)
            Screen.Alerts -> stringResource(R.string.alerts)
            Screen.Profile -> stringResource(R.string.profile)
            else -> ""
        }
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
