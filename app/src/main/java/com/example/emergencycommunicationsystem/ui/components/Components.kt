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
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.emergencycommunicationsystem.util.WeatherIconUtils
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.localizedStringResource
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.WeatherState
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.navigation.Screen
import com.example.emergencycommunicationsystem.util.getIconForCategory
import com.example.emergencycommunicationsystem.util.getColorForCategory
import com.example.emergencycommunicationsystem.util.getCategoryDisplayName
import com.example.emergencycommunicationsystem.util.getSeverityColor
import com.example.emergencycommunicationsystem.util.getAlertSeverity
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
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    
    // Theme and colors
    // User requested: "slider track should be a solid dark charcoal gray"
    val trackBg = Color(0xFF2B2D30) 
    
    // Refined Palette for "Modern Minimal Emergency"
    val activeRed = Color(0xFFE02E2E) // Bright, urgent red
    
    // Swipe state
    val swipeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    // Completion state for visual feedback
    var isCompleted by remember { mutableStateOf(false) }
    
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val chevronAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chevron_alpha"
    )

    // Expand thumb on completion
    // Base thumb size increased to 82.dp to cover the track height (which is ~84dp available)
    // allowing a tiny margin but effectively hiding the background
    val thumbSize by animateDpAsState(
        targetValue = if (isCompleted) 200.dp else 82.dp, 
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "thumb_expansion"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(vertical = 6.dp)
    ) {
        val widthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val thumbWidth = 82.dp
        val thumbWidthPx = with(density) { thumbWidth.toPx() }
        // Padding inside the track - set to 0 to allow full travel and clean start
        val padding = 0.dp 
        val paddingPx = with(density) { padding.toPx() }
        
        // Max offset calculations
        val maxOffset = (widthPx - thumbWidthPx - (paddingPx * 2)).coerceAtLeast(0f)
        val progress = (swipeOffset.value / maxOffset).coerceIn(0f, 1f)
        
        val shape = RoundedCornerShape(100.dp) // Fully rounded capsule

        // 1. The Track Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 4.dp,
                    shape = shape,
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(shape)
                .background(trackBg)
        ) {
            
            // 2. The "Glowing Trail" (Active Background)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Draw the active red trail that follows the thumb
                        // Trail width is exactly up to the LEFT edge of the thumb (plus tiny overlap)
                        // This prevents the "red halo" around the thumb and ensures it's 0 width initially
                        val trailWidth = swipeOffset.value + paddingPx + (2 * density.density) // +2px overlap
                        if (trailWidth > 0) {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(activeRed.copy(alpha = 0.8f), activeRed),
                                    startX = 0f,
                                    endX = trailWidth
                                ),
                                size = Size(trailWidth, size.height)
                            )
                        }
                    }
            )

            // 3. Labels (Underneath the thumb)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // "Slide to Emergency Call" Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .offset(x = 24.dp) // Offset to clear the initial thumb position
                        .graphicsLayer {
                            // Fade out as thumb moves over it
                            alpha = (1f - (progress * 4f)).coerceIn(0f, 1f)
                        }
                ) {
                    Text(
                        text = localizedStringResource(R.string.slide_to_call),
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Animated Chevron Trail
                    Row {
                        repeat(3) { index ->
                            Text(
                                text = "›",
                                color = Color.White.copy(
                                    alpha = (chevronAlpha - (index * 0.2f)).coerceIn(0.1f, 1f)
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                
                // "Release to Cancel" hint (only visible when dragging but not complete)
                if (progress > 0.2f && !isCompleted) {
                    Text(
                        text = "RELEASE TO CANCEL",
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 32.dp)
                            .graphicsLayer {
                                alpha = (progress - 0.2f).coerceIn(0f, 1f)
                            }
                    )
                }
            }
            
            // 4. The Draggable Thumb
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(swipeOffset.value.roundToInt() + paddingPx.toInt(), 0) }
                    .size(thumbSize) // Animates size on completion
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (!isCompleted) {
                                coroutineScope.launch {
                                    val newOffset = (swipeOffset.value + delta).coerceIn(0f, maxOffset)
                                    swipeOffset.snapTo(newOffset)
                                }
                            }
                        },
                        onDragStopped = {
                            if (swipeOffset.value >= maxOffset * 0.85f) { // High threshold for safety
                                // TRIGGER ACTION
                                isCompleted = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                
                                // Snap to end
                                coroutineScope.launch {
                                    swipeOffset.animateTo(maxOffset, spring(stiffness = Spring.StiffnessMediumLow))
                                    delay(200) // Wait for visual confirmation
                                    onClick()
                                    // Reset state after action (if user comes back)
                                    delay(500)
                                    isCompleted = false
                                    swipeOffset.snapTo(0f)
                                }
                            } else {
                                // Snap back
                                coroutineScope.launch {
                                    swipeOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Thumb Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_tabler_phone),
                    contentDescription = null,
                    tint = activeRed,
                    modifier = Modifier
                        .size(32.dp) // Adjusted icon size
                        .graphicsLayer {
                            // Rotate icon slightly as you drag
                            rotationZ = progress * 30f
                            // Scale up on completion
                            val scale = if (isCompleted) 1.5f else 1f + (progress * 0.1f)
                            scaleX = scale
                            scaleY = scale
                        }
                )
                
                // Spinner/Loading indicator if processing (optional, adding for "state change" feel)
                if (isCompleted) {
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(activeRed.copy(alpha = 0.1f))
                    )
                }
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
        
        // Stacked actions
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionGridItem(
                title = localeContext.getString(R.string.report_incident),
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth(),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_file_alert,
                accentColor = StatusDanger,
                isFilled = true
            )
            ActionGridItem(
                title = localeContext.getString(R.string.i_am_safe),
                onClick = onSafeClick,
                modifier = Modifier.fillMaxWidth(),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_shield_check,
                accentColor = StatusSafe,
                isFilled = true
            )
            ActionGridItem(
                title = "Message Responder",
                onClick = onMessageClick,
                modifier = Modifier.fillMaxWidth(),
                useTablerIcon = true,
                tablerIconRes = R.drawable.ic_tabler_message_plus,
                accentColor = StatusInfo,
                isFilled = false, // Changed to false for variety or keep true? 
                // Keeping consistent with previous request which had it as false/outline? 
                // Previous code: isFilled = false.
                // Let's keep it consistent or maybe make it filled for uniformity?
                // User asked for "Stack buttons". Usually implies uniformity.
                // But let's stick to the previous style for Message Responder unless asked.
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
fun WeatherWidget(state: WeatherState, onRetry: () -> Unit = {}) {
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
            // Sage Green & Teal Gradient Palette
            val deepSage = Color(0xFF5D7A70)
            val teal = Color(0xFF4B8B8B)
            val gradient = Brush.verticalGradient(colors = listOf(deepSage, teal))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = shape, spotColor = teal)
                    .clip(shape)
                    .background(gradient)
                    .background(Color.White.copy(alpha = 0.05f)) // Glass overlay
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = shape
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Section: Icon & Temperature centered
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Floating 3D Cloud (No Background)
                        AsyncImage(
                            model = WeatherIconUtils.getWeatherAnimation(state.condition),
                            contentDescription = state.condition,
                            modifier = Modifier.size(110.dp),
                            contentScale = ContentScale.Fit
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // Large Temperature Typography
                        Text(
                            text = "${state.temperature.substringBefore(".")}°",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    }

                    // Condition & Location
                    Text(
                        text = state.condition,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Location,
                            contentDescription = "Location",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.location,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metrics Row (Will need to update this component to use White tint separately or pass a color)
                    WeatherDetailsRow(state, contentColor = Color.White)

                    Spacer(modifier = Modifier.height(24.dp))

                    // AI Insight Pill
                    WeatherAdvice(advice = state.advice)

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalForecastWidget(state.forecastData, useWhiteText = true)
                }
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weather Information Unavailable",
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onError),
                        modifier = Modifier.background(MaterialTheme.colorScheme.onError.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsRow(state: WeatherState.Success, contentColor: Color = MaterialTheme.colorScheme.onSurface) {
    val localeContext = getLocaleContext()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        WeatherDetailItem(
            icon = AppIcons.Thermostat,
            label = localeContext.getString(R.string.feels_like),
            value = state.feelsLike,
            contentColor = contentColor
        )
        WeatherDetailItem(
            icon = AppIcons.Humidity,
            label = localeContext.getString(R.string.humidity),
            value = state.humidity,
            contentColor = contentColor
        )
        WeatherDetailItem(
            icon = AppIcons.Wind,
            label = localeContext.getString(R.string.wind),
            value = state.windSpeed,
            contentColor = contentColor
        )
        WeatherDetailItem(
            icon = AppIcons.Visibility,
            label = localeContext.getString(R.string.visibility),
            value = state.visibility,
            contentColor = contentColor
        )
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, label: String, value: String, contentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = contentColor
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun WeatherAdvice(advice: String) {
    val context = LocalContext.current
    val currentLanguage by com.example.emergencycommunicationsystem.data.UserPrefs.getLanguage(context)
        .collectAsState(initial = "en")
    
    var translatedAdvice by remember { mutableStateOf(advice) }
    
    LaunchedEffect(advice, currentLanguage) {
        if (currentLanguage != "en" && advice.isNotBlank()) {
            translatedAdvice = com.example.emergencycommunicationsystem.util.TranslationService.translate(
                advice,
                currentLanguage
            )
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

    // UPDATED LAYOUT: AI Insight Pill
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Automatically animate height changes
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top // Keep badge at top for long text
    ) {
        // "AI Insight" Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "AI INSIGHT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color(0xFF5D7A70) // Deep Sage
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = displayedText.ifEmpty { "Analysing weather data..." },
            fontSize = 13.sp,
            color = Color.White,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f) // Ensure it takes available space
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

                val tempStr = "${String.format(Locale.US, "%.0f", item.main.temp)}°"
                val conditionMain = item.weather.firstOrNull()?.main ?: "Clear"

                ForecastDay(
                    dayName = dayName,
                    conditionMain = conditionMain,
                    temp = tempStr,
                    useWhiteText = useWhiteText
                )
            }
        }
    }
}

@Composable
fun ForecastDay(
    dayName: String,
    conditionMain: String,
    temp: String,
    useWhiteText: Boolean = false
) {
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

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = WeatherIconUtils.getWeatherAnimation(conditionMain),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit
            )
        }

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
    var translatedCategory by remember(alert.category) { mutableStateOf("") }
    
    LaunchedEffect(alert.title, alert.category, currentLanguage) {
        val title = alert.title ?: "No Title"
        val categoryBase = getCategoryDisplayName(alert)
        
        if (currentLanguage != "en") {
            launch {
                translatedTitle = TranslationService.translate(title, currentLanguage)
            }
            launch {
                translatedCategory = TranslationService.translate(categoryBase, currentLanguage)
            }
        } else {
            translatedTitle = title
            translatedCategory = categoryBase
        }
    }
    
    val displayTitle = if (translatedTitle.isEmpty()) localeContext.getString(R.string.no_title) else translatedTitle
    val displayCategory = if (translatedCategory.isEmpty()) localeContext.getString(R.string.general) else translatedCategory

    val severityColor = getSeverityColor(severity)
    val categoryColor = getColorForCategory(alert)
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
                tint = categoryColor
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
                        color = categoryColor
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
