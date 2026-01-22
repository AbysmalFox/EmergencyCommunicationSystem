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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.sin
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.VibrantRed
import com.example.emergencycommunicationsystem.ui.theme.VibrantRedLight
import com.example.emergencycommunicationsystem.ui.theme.EmergencyRedMain
import com.example.emergencycommunicationsystem.ui.theme.EmergencyRedLight
import com.example.emergencycommunicationsystem.ui.theme.EmergencyRedDark
import com.example.emergencycommunicationsystem.ui.theme.EmergencyRedPulse
import com.example.emergencycommunicationsystem.ui.theme.DarkNavy
import com.example.emergencycommunicationsystem.ui.theme.Slate
import com.example.emergencycommunicationsystem.ui.theme.CardBorder
import com.example.emergencycommunicationsystem.ui.theme.SoftShadow
import com.example.emergencycommunicationsystem.ui.theme.StatusDanger
import com.example.emergencycommunicationsystem.ui.theme.StatusDangerLight
import com.example.emergencycommunicationsystem.ui.theme.StatusInfo
import com.example.emergencycommunicationsystem.ui.theme.StatusSafe
import com.example.emergencycommunicationsystem.ui.theme.StatusSafeLight
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
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.emergencycommunicationsystem.util.getIconForCategory
import com.example.emergencycommunicationsystem.util.getSeverityColor
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

/**
 * Helper function to draw an animated wave pattern (optimized for performance)
 */
private fun DrawScope.drawWavePattern(
    size: Size,
    waveOffset: Float,
    waveColor: Color,
    waveAmplitude: Float,
    waveFrequency: Float
) {
    val centerY = size.height / 2f
    val waveLength = size.width / waveFrequency
    
    // Optimized: Use fewer points for better performance (step 8 instead of 3)
    val stepSize = 8f // Larger step = fewer calculations = better performance
    
    // Simplified: Draw only one wave layer instead of multiple
    val path = Path()
    path.moveTo(0f, size.height)
    path.lineTo(0f, centerY)
    
    // Draw the wave curve with fewer points
    var x = 0f
    while (x <= size.width) {
        val y = centerY + waveAmplitude * sin((x / waveLength) * 2f * PI.toFloat() + waveOffset)
        path.lineTo(x, y)
        x += stepSize
    }
    
    // Complete the path to create a filled shape
    path.lineTo(size.width, size.height)
    path.close()
    
    // Draw single filled wave (simplified from multiple layers)
    drawPath(
        path = path,
        color = waveColor.copy(alpha = 0.15f) // Reduced opacity for subtlety
    )
}

@Composable
fun EmergencyCallButton(onClick: () -> Unit) {
    val localeContext = getLocaleContext()
    // Unique shape: Cut corners for a more urgent, technical look
    val shape = CutCornerShape(
        topStart = 8.dp, 
        topEnd = 24.dp, 
        bottomStart = 24.dp, 
        bottomEnd = 8.dp
    )
    // Use MaterialTheme to detect dark mode (respects user's theme preference)
    // Check if background is dark by comparing luminance
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
    
    // Use refined emergency palette
    val baseRed = if (isDarkMode) VibrantRedLight else EmergencyRedMain
    val lightRed = if (isDarkMode) EmergencyRedPulse else EmergencyRedLight
    val darkRed = if (isDarkMode) EmergencyRedMain else EmergencyRedDark
    
    // Pulsing animation - makes the red lighter periodically (more noticeable)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing), // Slightly faster
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_progress"
    )
    
    // Wave animation - horizontal wave movement (slower for better performance)
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing), // Slower = fewer updates = better performance
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    // Incoming call animation for the phone icon - subtle horizontal shake like phone ringing
    val phoneShake by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing), // Slower, more chill
            repeatMode = RepeatMode.Reverse
        ),
        label = "phone_shake"
    )
    
    // Get density for dp to px conversion
    val density = LocalDensity.current
    
    // Calculate horizontal shake (left to right like phone vibrating)
    // More subtle: moves between -2 and +2 dp
    val shakeOffsetDp = (phoneShake * 4f) - 2f // Moves between -2 and +2 dp
    val shakeOffsetX = with(density) { shakeOffsetDp.dp.toPx() }
    
    // Very subtle vertical movement (barely noticeable)
    val subtleBounceDp = (phoneShake * 1f) - 0.5f // Moves between -0.5 and +0.5 dp
    val shakeOffsetY = with(density) { subtleBounceDp.dp.toPx() }
    
    // Very subtle rotation (gentle sway)
    val rotationAngle = (phoneShake - 0.5f) * 2f // Rotates between -1 and +1 degrees (very subtle)
    
    // Very subtle scale (gentle pulse)
    val iconScale = 1f + (phoneShake * 0.03f) // Scales between 1.0 and 1.03 (very subtle)
    
    // Interpolate between lighter and base colors - make animation more noticeable
    // Pulse between 30% lighter and 100% base for more visible effect
    val lightnessFactor = 0.3f + (pulseProgress * 0.7f) // Oscillates between 0.3 and 1.0
    
    // Optimized inline color interpolation (no function call overhead)
    val animatedLightRed = Color(
        red = lightRed.red + (baseRed.red - lightRed.red) * lightnessFactor,
        green = lightRed.green + (baseRed.green - lightRed.green) * lightnessFactor,
        blue = lightRed.blue + (baseRed.blue - lightRed.blue) * lightnessFactor,
        alpha = lightRed.alpha + (baseRed.alpha - lightRed.alpha) * lightnessFactor
    )
    
    val factor = (1f - lightnessFactor) * 0.3f
    val animatedBaseRed = Color(
        red = baseRed.red + (lightRed.red - baseRed.red) * factor,
        green = baseRed.green + (lightRed.green - baseRed.green) * factor,
        blue = baseRed.blue + (lightRed.blue - baseRed.blue) * factor,
        alpha = baseRed.alpha + (lightRed.alpha - baseRed.alpha) * factor
    )
    
    // Gradient: Top-light to Bottom-dark Red with animation
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            animatedLightRed,
            animatedBaseRed,
            darkRed
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp) // Slightly increased height for better presence
            .shadow(
                elevation = 20.dp, // Increased elevation for a "floating" look
                shape = shape,
                spotColor = baseRed.copy(alpha = 0.8f),
                ambientColor = baseRed.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(gradientBrush)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = shape
            )
            .drawBehind {
                // Draw animated wave pattern (optimized for performance)
                // Only draw if size is valid to avoid unnecessary calculations
                if (size.width > 0 && size.height > 0) {
                    drawWavePattern(
                        size = size,
                        waveOffset = waveOffset,
                        waveColor = Color.White.copy(alpha = 0.25f), // Slightly more visible
                        waveAmplitude = size.height * 0.25f, 
                        waveFrequency = 1.0f 
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Thicker icon stroke simulated by using a larger size and potentially a different icon if available
            // Since we can't easily change the vector path here, we'll keep the icon but ensure it's prominent
            // Add incoming call animation - bounce, rotate, and scale
            Icon(
                painter = painterResource(id = R.drawable.ic_tabler_phone),
                contentDescription = localeContext.getString(R.string.emergency_call_label),
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp) // Increased size
                    .graphicsLayer(
                        translationX = shakeOffsetX, // Horizontal shake (left to right)
                        translationY = shakeOffsetY, // Very subtle vertical movement
                        rotationZ = rotationAngle, // Gentle sway
                        scaleX = iconScale,
                        scaleY = iconScale
                    )
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = localeContext.getString(R.string.emergency_call_label).uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Black, // Heavier weight
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = localeContext.getString(R.string.call_button),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold, // Extra bold
                    fontSize = 28.sp,
                    letterSpacing = 0.5.sp
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
    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        EmergencyCallButton(onClick = onEmergencyCallClick)
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionGridItem(
                title = localeContext.getString(R.string.report_incident),
                onClick = onReportClick,
                modifier = Modifier.weight(1f),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_file_alert,
                accentColor = StatusDanger,
                isFilled = true
            )
            ActionGridItem(
                title = localeContext.getString(R.string.i_am_safe),
                onClick = onSafeClick,
                modifier = Modifier.weight(1f),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_shield_check,
                accentColor = StatusSafe,
                isFilled = true
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionGridItem(
                title = "Message Responder",
                onClick = onMessageClick,
                modifier = Modifier.weight(1f),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_message_plus,
                accentColor = StatusInfo,
                isFilled = false,
                isCentered = true
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
    @androidx.annotation.DrawableRes tablerIconRes: Int? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isFilled: Boolean = false,
    isCentered: Boolean = false
) {
    val shape = RoundedCornerShape(24.dp)
    // Use MaterialTheme to detect dark mode (respects user's theme preference)
    // Check if background is dark by comparing luminance
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
    
    // Use lighter colors in dark mode for red and green - significantly lighter
    val adjustedAccentColor = when {
        isDarkMode && accentColor == StatusDanger -> StatusDangerLight
        isDarkMode && accentColor == StatusSafe -> StatusSafeLight
        else -> accentColor
    }
    
    // Filled style uses solid accent color with white icon
    // Non-filled style uses subtle tint with colored icon
    val circleBackgroundColor = if (isFilled) adjustedAccentColor else adjustedAccentColor.copy(alpha = 0.1f)
    val iconTintColor = if (isFilled) Color.White else adjustedAccentColor
    
    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surface
            } else {
                Color.White
            }
        ),
        modifier = modifier
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = shape,
                spotColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.08f),
                ambientColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.05f)
            )
            .height(84.dp),
        border = BorderStroke(
            width = if (isDarkMode) 1.dp else 0.5.dp,
            color = if (isDarkMode) {
                Color.Black.copy(alpha = 0.1f)
            } else {
                adjustedAccentColor.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isCentered) Arrangement.Center else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(circleBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (useTablerIcon && tablerIconRes != null) {
                    Icon(
                        painter = painterResource(id = tablerIconRes),
                        contentDescription = title,
                        tint = iconTintColor,
                        modifier = Modifier.size(28.dp)
                    )
                } else if (icon != null) {
                    Icon(icon, contentDescription = title, tint = iconTintColor, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 16.sp
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
            val bgColor = MaterialTheme.colorScheme.background
            val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkMode) 8.dp else 6.dp,
                        shape = shape,
                        spotColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.1f),
                        ambientColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.06f)
                    )
                    .clip(shape)
                    .background(
                        if (isDarkMode) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.White
                        }
                    )
                    .border(
                        width = if (isDarkMode) 1.dp else 0.5.dp,
                        color = if (isDarkMode) {
                            Color.Black.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        },
                        shape = shape
                    )
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
    
    // Detect dark mode to adjust colors
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp), // removed bottom padding so Scaffold won't reserve extra space
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp, // Increased elevation for more prominence
                    shape = RoundedCornerShape(50),
                    spotColor = Color.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.3f)
                )
                .border(
                    width = 1.5.dp,
                    color = if (isDarkMode) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(50)
                ),
            shape = RoundedCornerShape(50),
            // Use a more distinct color to stand out from content
            color = if (isDarkMode) {
                // Dark mode: use a lighter, more distinct surface color
                Slate.copy(alpha = 0.95f) // Use Slate color which is lighter than DarkNavy
            } else {
                // Light mode: use a slightly darker, more distinct surface
                Color.White.copy(alpha = 0.98f) // Almost white but distinct
            },
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
fun HorizontalForecastWidget(
    forecastItems: List<com.example.emergencycommunicationsystem.data.models.ForecastItem>,
    useWhiteText: Boolean = false
) {
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
    val labelColor = if (useWhiteText) Color.White else MaterialTheme.colorScheme.onSurface
    
    Column {
        Text(
            text = translatedForecastLabel,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = labelColor,
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

                ForecastDay(dayName = dayName, iconUrl = iconUrl, temp = tempStr, useWhiteText = useWhiteText)
            }
        }
    }
}

@Composable
fun ForecastDay(dayName: String, iconUrl: String, temp: String, useWhiteText: Boolean = false) {
    val textColor = if (useWhiteText) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (useWhiteText) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val bgAlpha = if (useWhiteText) 0.2f else 0.5f
    
    Column(
        modifier = Modifier
            .background(
                color = if (useWhiteText) Color.White.copy(alpha = bgAlpha) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = bgAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = dayName,
            fontSize = 12.sp,
            color = subTextColor,
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
            color = textColor
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
    val context = LocalContext.current
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    
    // Dynamic translation
    var translatedTitle by remember(alert.title) { mutableStateOf(alert.title ?: "") }
    var translatedCategory by remember(alert.category) { mutableStateOf(alert.category ?: "") }
    
    LaunchedEffect(alert.title, alert.category, currentLanguage) {
        val title = alert.title ?: "No Title"
        val category = alert.category ?: "General"
        
        if (currentLanguage != "en") {
            launch {
                translatedTitle = TranslationService.translate(title, currentLanguage)
            }
            launch {
                translatedCategory = TranslationService.translate(category, currentLanguage)
            }
        } else {
            translatedTitle = title
            translatedCategory = category
        }
    }
    
    val displayTitle = if (translatedTitle.isEmpty()) localeContext.getString(R.string.no_title) else translatedTitle
    val displayCategory = if (translatedCategory.isEmpty()) localeContext.getString(R.string.general) else translatedCategory

    val severityColor = getSeverityColor(severity)
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.08f),
                ambientColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.05f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surface
            } else {
                Color.White // Pure white in light mode for better contrast
            }
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isDarkMode) 1.dp else 0.5.dp,
            color = if (isDarkMode) {
                Color.Black.copy(alpha = 0.1f)
            } else {
                severityColor.copy(alpha = 0.2f) // Subtle colored border in light mode
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Severity indicator bar - enhanced for light mode
            Box(
                modifier = Modifier
                    .width(if (isDarkMode) 6.dp else 5.dp)
                    .height(64.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                severityColor,
                                severityColor.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(if (isDarkMode) 3.dp else 2.5.dp)
                    )
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
                        text = displayCategory.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                    // Semi-transparent badge styling
                    Box(
                        modifier = Modifier
                            .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = severity,
                            fontSize = 10.sp, // Slightly larger
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Title
                Text(
                    text = displayTitle,
                    fontSize = 15.sp, // Increased size
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Distance and location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (distanceKm != null) {
                        Icon(
                            imageVector = AppIcons.Location,
                            contentDescription = "Distance",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray // Medium gray
                        )
                        Text(
                            text = com.example.emergencycommunicationsystem.util.LocationUtils.formatDistance(distanceKm),
                            fontSize = 13.sp, // Increased font size
                            color = Color.Gray, // Medium gray
                            fontWeight = FontWeight.Medium
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
    
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .shadow(
                elevation = if (isDarkMode) 8.dp else 6.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.1f),
                ambientColor = if (isDarkMode) SoftShadow else Color.Black.copy(alpha = 0.06f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surface
            } else {
                Color.White
            }
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isDarkMode) 1.dp else 0.5.dp,
            color = if (isDarkMode) {
                Color.Black.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            }
        )
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
