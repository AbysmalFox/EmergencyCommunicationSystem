package com.example.emergencycommunicationsystem.ui.components

/**
 * UI Components for the Emergency Communication System
 * 
 * Note: All Tabler icons are local vector drawables stored in res/drawable/
 * They are bundled with the app and work offline - no network connection required.
 */

import android.content.Intent
import androidx.core.net.toUri
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
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import com.example.emergencycommunicationsystem.R
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.navigation.Screen
import com.example.emergencycommunicationsystem.ui.screens.getIconForCategory
import kotlinx.coroutines.delay
import java.util.Locale

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
                Icon(AppIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val localeContext = getLocaleContext()
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
                painter = painterResource(id = R.drawable.ic_tabler_phone),
                contentDescription = localeContext.getString(R.string.emergency_call_label),
                tint = Color.White,
                modifier = Modifier.size(32.dp) // Reduced icon size
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = localeContext.getString(R.string.emergency_call_label),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, // Reduced font size
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = localeContext.getString(R.string.call_button),
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
fun ActionGrid(
    onEmergencyCallClick: () -> Unit,
    onReportClick: () -> Unit,
    onSafeClick: () -> Unit,
    onMessageClick: () -> Unit = {}
) {
    val localeContext = getLocaleContext()
    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) { // Reduced spacing
        EmergencyCallButton(onClick = onEmergencyCallClick)
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionGridItem(
                title = localeContext.getString(R.string.report_incident),
                onClick = onReportClick,
                modifier = Modifier.weight(1f),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_file_alert
            )
            ActionGridItem(
                title = localeContext.getString(R.string.i_am_safe),
                onClick = onSafeClick,
                modifier = Modifier.weight(1f),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_shield_check
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionGridItem(
                title = "Message Responder",
                onClick = onMessageClick,
                modifier = Modifier.weight(1f),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_message_plus
            )
        }
    }
}

@Composable
fun ActionGridItem(
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useTablerIcon: Boolean = false,
    @androidx.annotation.DrawableRes tablerIconRes: Int? = null
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (useTablerIcon && tablerIconRes != null) {
                Icon(
                    painter = painterResource(id = tablerIconRes),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(37.dp)
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
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
                                imageVector = AppIcons.Location,
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

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalForecastWidget(state.forecastData)
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
                    val localeContext = getLocaleContext()
                    Icon(AppIcons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        localeContext.getString(R.string.gps_signal_lost),
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsRow(state: WeatherState.Success) {
    val localeContext = getLocaleContext()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        WeatherDetailItem(
            icon = AppIcons.Thermostat,
            label = localeContext.getString(R.string.feels_like),
            value = state.feelsLike
        )
        WeatherDetailItem(
            icon = AppIcons.Humidity,
            label = localeContext.getString(R.string.humidity),
            value = state.humidity
        )
        WeatherDetailItem(
            icon = AppIcons.Wind,
            label = localeContext.getString(R.string.wind),
            value = state.windSpeed
        )
        WeatherDetailItem(
            icon = AppIcons.Visibility,
            label = localeContext.getString(R.string.visibility),
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by com.example.emergencycommunicationsystem.data.UserPrefs.getLanguage(context)
        .collectAsState(initial = "en")
    
    // Translate the advice text
    var translatedAdvice by remember { mutableStateOf(advice) }
    
    LaunchedEffect(advice, currentLanguage) {
        if (currentLanguage != "en" && advice.isNotBlank()) {
            coroutineScope.launch {
                translatedAdvice = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                    advice,
                    currentLanguage
                )
            }
        } else {
            translatedAdvice = advice
        }
    }
    
    var displayedText by remember(translatedAdvice) { mutableStateOf("") }

    LaunchedEffect(translatedAdvice) {
        displayedText = ""
        delay(200)
        translatedAdvice.forEachIndexed { index, _ ->
            displayedText = translatedAdvice.substring(0, index + 1)
            delay(30)
        }
    }

    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = AppIcons.Chat,
            contentDescription = "Weather Advice",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = 12.dp, top = 4.dp)
                .size(24.dp)
        )
        val localeContext = getLocaleContext()
        Text(
            text = displayedText.ifEmpty { localeContext.getString(R.string.weather_widget_message) },
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
                val intent = Intent(Intent.ACTION_DIAL, ("tel:$number").toUri())
                context.startActivity(intent)
            }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tabler_phone),
                    contentDescription = "Call $name",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
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
            .padding(start = 24.dp, end = 24.dp), // removed bottom padding so Scaffold won't reserve extra space
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50),
                ),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .height(74.dp), // reduced height to be compact
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
        Screen.Home -> AppIcons.Home
        Screen.Alerts -> AppIcons.Alerts
        Screen.Profile -> AppIcons.Profile
        else -> AppIcons.Error // Should not happen
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
        val localeContext = getLocaleContext()
        val title = when (screen) {
            Screen.Home -> localeContext.getString(R.string.home)
            Screen.Alerts -> localeContext.getString(R.string.alerts)
            Screen.Profile -> localeContext.getString(R.string.profile)
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

@Composable
fun HorizontalForecastWidget(forecastItems: List<com.example.emergencycommunicationsystem.data.models.ForecastItem>) {
    if (forecastItems.isEmpty()) return

    // Use legacy date APIs (Calendar/SimpleDateFormat) to support older Android API levels
    val dateKeyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    dateKeyFormat.timeZone = java.util.TimeZone.getDefault()

    // Group forecast items by local date string (e.g. 2025-12-16)
    val itemsByDate = forecastItems.groupBy { item ->
        val d = java.util.Date(item.dt * 1000)
        dateKeyFormat.format(d)
    }

    // Build pairs (dateString -> representative item) for the next 6 days (tomorrow..+6)
    val dayPairs = mutableListOf<Pair<String, com.example.emergencycommunicationsystem.data.models.ForecastItem>>()
    val cal = java.util.Calendar.getInstance()
    for (d in 1..6) {
        val targetCal = java.util.Calendar.getInstance()
        targetCal.add(java.util.Calendar.DAY_OF_YEAR, d)
        val key = dateKeyFormat.format(targetCal.time)
        val listForDate = itemsByDate[key]
        if (!listForDate.isNullOrEmpty()) {
            // choose the item closest to 12:00 local time (midday) as representative
            val chosen = listForDate.minByOrNull { item ->
                cal.time = java.util.Date(item.dt * 1000)
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                kotlin.math.abs(hour - 12)
            } ?: listForDate.first()
            dayPairs.add(key to chosen)
        }
    }

    if (dayPairs.isEmpty()) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    var translatedForecastLabel by remember { mutableStateOf("6-Day Forecast") }
    
    // Translate forecast label
    LaunchedEffect(currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedForecastLabel = TranslationService.translate("6-Day Forecast", currentLanguage)
            }
        } else {
            translatedForecastLabel = "6-Day Forecast"
        }
    }
    
    val dayNameFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
    Column {
        Text(
            text = translatedForecastLabel,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(count = dayPairs.size, key = { index -> dayPairs[index].second.dt }) { index ->
                val item = dayPairs[index].second
                // Use the chosen item's timestamp to derive the day short name
                val repDate = java.util.Date(item.dt * 1000)
                val dayName = dayNameFormat.format(repDate) // e.g. "Tue"

                val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
                val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                val tempStr = "${String.format(Locale.US, "%.0f", item.main.temp)}°"

                ForecastDay(dayName = dayName, iconUrl = iconUrl, temp = tempStr)
            }
        }
    }
}

@Composable
fun ForecastDay(dayName: String, iconUrl: String, temp: String) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = dayName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = temp,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Compact Alert Card for Dashboard
 */
@Composable
fun CompactAlertCard(
    alert: Alert,
    distanceKm: Double?,
    severity: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localeContext = getLocaleContext()
    val severityColor = com.example.emergencycommunicationsystem.ui.screens.getSeverityColor(severity)
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Severity indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(severityColor, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Icon
            Icon(
                imageVector = getIconForCategory(alert),
                contentDescription = alert.category,
                modifier = Modifier.size(32.dp),
                tint = severityColor
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Category and Severity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = (alert.category ?: localeContext.getString(R.string.general)).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                    Box(
                        modifier = Modifier
                            .background(severityColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = severity,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Title
                Text(
                    text = alert.title ?: localeContext.getString(R.string.no_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Distance and location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (distanceKm != null) {
                        Icon(
                            imageVector = AppIcons.Location,
                            contentDescription = "Distance",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = com.example.emergencycommunicationsystem.util.LocationUtils.formatDistance(distanceKm),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Emergency Instructions Component - Context-aware guidance
 * Shows instructions based on alert type, user location, and time of day
 */
@Composable
fun EmergencyInstructions(
    alerts: List<Alert>,
    userLat: Double?,
    userLon: Double?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by com.example.emergencycommunicationsystem.data.UserPrefs.getLanguage(context)
        .collectAsState(initial = "en")
    
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val timeOfDay = when (currentHour) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        in 18..22 -> "evening"
        else -> "night"
    }
    
    // Determine primary alert type from active alerts
    val primaryAlertType = getPrimaryAlertType(alerts)
    val instructionsEn = getEmergencyInstructions(primaryAlertType, timeOfDay, userLat != null && userLon != null)
    
    // Translated strings
    var translatedHeader by remember { mutableStateOf("🧠 Emergency Instructions") }
    var translatedMain by remember { mutableStateOf(instructionsEn.main) }
    var translatedSteps by remember { mutableStateOf(instructionsEn.steps) }
    var translatedContextNote by remember { mutableStateOf(instructionsEn.contextNote) }
    
    // Translate all instruction text
    LaunchedEffect(primaryAlertType, timeOfDay, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedHeader = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                    "🧠 Emergency Instructions",
                    currentLanguage
                )
                translatedMain = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                    instructionsEn.main,
                    currentLanguage
                )
                translatedSteps = com.example.emergencycommunicationsystem.util.TranslationService.translateBatch(
                    instructionsEn.steps,
                    currentLanguage
                )
                if (instructionsEn.contextNote.isNotEmpty()) {
                    translatedContextNote = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                        instructionsEn.contextNote,
                        currentLanguage
                    )
                } else {
                    translatedContextNote = ""
                }
            }
        } else {
            translatedHeader = "🧠 Emergency Instructions"
            translatedMain = instructionsEn.main
            translatedSteps = instructionsEn.steps
            translatedContextNote = instructionsEn.contextNote
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = AppIcons.Info,
                    contentDescription = "Emergency Instructions",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = translatedHeader,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (onClick != null) {
                    Icon(
                        imageVector = AppIcons.ChevronRight,
                        contentDescription = "View More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main instruction
            Text(
                text = translatedMain,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Additional steps
            translatedSteps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}. ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = step,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Context note
            if (translatedContextNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = translatedContextNote,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Data class for emergency instructions
 */
private data class EmergencyInstruction(
    val main: String,
    val steps: List<String>,
    val contextNote: String = ""
)

/**
 * Determine primary alert type from active alerts
 */
private fun getPrimaryAlertType(alerts: List<Alert>): String {
    if (alerts.isEmpty()) return "general"
    
    // Count alert types
    val typeCounts = mutableMapOf<String, Int>()
    alerts.forEach { alert ->
        val categoryId = try {
            alert.category?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            0
        }
        
        val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
        val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
        
        when {
            categoryId == 2 || "earthquake" in categoryStr || "earthquake" in title || "tremor" in title -> {
                typeCounts["earthquake"] = typeCounts.getOrDefault("earthquake", 0) + 1
            }
            categoryId == 4 || "fire" in categoryStr || "fire" in title || "wildfire" in title -> {
                typeCounts["fire"] = typeCounts.getOrDefault("fire", 0) + 1
            }
            categoryId == 1 || "weather" in categoryStr || "flood" in title || "typhoon" in title || "storm" in title || "rain" in title -> {
                typeCounts["flood"] = typeCounts.getOrDefault("flood", 0) + 1
            }
            else -> {
                typeCounts["general"] = typeCounts.getOrDefault("general", 0) + 1
            }
        }
    }
    
    // Return the most common type
    return typeCounts.maxByOrNull { it.value }?.key ?: "general"
}

/**
 * Get emergency instructions based on alert type, time of day, and location availability
 */
private fun getEmergencyInstructions(
    alertType: String,
    timeOfDay: String,
    hasLocation: Boolean
): EmergencyInstruction {
    return when (alertType) {
        "earthquake" -> EmergencyInstruction(
            main = "During Earthquake: Duck, Cover, Hold",
            steps = listOf(
                "Drop to your hands and knees",
                "Cover your head and neck with your arms",
                "Hold on to any sturdy furniture",
                "Stay away from windows and heavy objects",
                "If outdoors, move to an open area away from buildings",
                "After shaking stops, check for injuries and hazards"
            ),
            contextNote = if (hasLocation) "Stay alert for aftershocks. Check your location for nearby safe zones." else "Stay alert for aftershocks."
        )
        "fire" -> EmergencyInstruction(
            main = "During Fire: Do not use elevators",
            steps = listOf(
                "Alert others and activate fire alarm if available",
                "Use stairs, never elevators",
                "Stay low to avoid smoke inhalation",
                "Feel doors before opening - if hot, use another exit",
                "If trapped, seal the room and signal for help",
                "Once outside, move to a safe distance and call emergency services"
            ),
            contextNote = if (timeOfDay == "night") "Visibility may be limited. Use a flashlight if available." else "Evacuate immediately and do not return until authorities say it's safe."
        )
        "flood" -> EmergencyInstruction(
            main = "During Flood: Move to higher ground",
            steps = listOf(
                "Move to higher ground immediately",
                "Avoid walking or driving through floodwaters",
                "Stay away from bridges over fast-moving water",
                "If trapped in a building, go to the highest floor",
                "Turn off electricity at the main breaker if safe to do so",
                "Listen to emergency broadcasts for updates"
            ),
            contextNote = if (hasLocation) "Check your location and identify nearest evacuation centers on the map." else "Monitor water levels and be ready to evacuate."
        )
        else -> EmergencyInstruction(
            main = "General Emergency: Stay Calm and Follow Instructions",
            steps = listOf(
                "Stay calm and assess the situation",
                "Follow instructions from authorities",
                "Keep emergency contacts accessible",
                "Have an emergency kit ready",
                "Stay informed through official channels",
                "Help others if it's safe to do so"
            ),
            contextNote = if (hasLocation) "Your location is being tracked for better assistance." else "Enable location services for location-specific guidance."
        )
    }
}
