package com.example.emergencycommunicationsystem.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.util.localizedStringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Alertara Green-ish Theme Colors
private val AlertaraTeal = Color(0xFF508684)
private val AlertaraTealDark = Color(0xFF34635D)
private val AlertaraTealLight = Color(0xFF669997)
private val AlertaraTealAccent = Color(0xFFB2DFDB)
private val AlertaraSuccess = Color(0xFF43A047)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternetCallSlider(
    onCallInitiated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isDarkMode = ThemeManager.isDarkMode()
    
    var isDragging by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Call type: true for Internet, false for Data (Cellular)
    var isInternetCall by remember { mutableStateOf(true) }
    
    val sliderWidth = 320.dp
    val sliderHeight = 60.dp 
    val thumbSize = 52.dp 
    val thumbPadding = 4.dp
    
    val density = LocalDensity.current
    val sliderWidthPx = with(density) { sliderWidth.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val thumbPaddingPx = with(density) { thumbPadding.toPx() }
    val maxOffset = sliderWidthPx - thumbSizePx - (thumbPaddingPx * 2)
    val dragThresholdPx = maxOffset * 0.85f
    
    val progress = (offsetX / maxOffset).coerceIn(0f, 1f)

    // Adjusted gradient for dark mode to provide better visibility and vibrance
    val primaryGradient = if (isDarkMode) {
        if (isInternetCall) {
            Brush.linearGradient(listOf(AlertaraTealLight, AlertaraTeal))
        } else {
            Brush.linearGradient(listOf(AlertaraTealAccent, AlertaraTealLight))
        }
    } else {
        if (isInternetCall) {
            Brush.linearGradient(listOf(AlertaraTeal, AlertaraTealDark))
        } else {
            Brush.linearGradient(listOf(AlertaraTealLight, AlertaraTeal))
        }
    }

    val backgroundColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    } else {
        Color(0xFFF1F5F4) 
    }

    LaunchedEffect(isDragging) {
        if (!isDragging && offsetX < maxOffset) {
            animate(
                initialValue = offsetX,
                targetValue = 0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) { value, _ -> offsetX = value }
        }
    }

    Column(
        modifier = modifier
            .width(sliderWidth + 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkMode) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.5f))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Muted Segmented Selector with clearer Divider
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            border = BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.1f) else AlertaraTeal.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option 1: Internet
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isInternetCall) primaryGradient else SolidColor(Color.Transparent))
                        .clickable { 
                            isInternetCall = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isInternetCall) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = localizedStringResource(R.string.internet_call),
                            fontSize = 11.sp,
                            fontWeight = if (isInternetCall) FontWeight.Bold else FontWeight.Medium,
                            color = if (isInternetCall) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Greenish divider for light mode, white for dark mode
                val dividerColor = if (isDarkMode) Color.White.copy(alpha = 0.4f) else AlertaraTeal.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(dividerColor)
                )

                // Option 2: Cellular
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (!isInternetCall) primaryGradient else SolidColor(Color.Transparent))
                        .clickable { 
                            isInternetCall = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (!isInternetCall) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = localizedStringResource(R.string.cellular_call),
                            fontSize = 11.sp,
                            fontWeight = if (!isInternetCall) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isInternetCall) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // The Slider Track
        Box(
            modifier = Modifier
                .width(sliderWidth)
                .height(sliderHeight)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.2f) else AlertaraTeal.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .drawBehind {
                    if (progress > 0.01f) {
                        drawRoundRect(
                            brush = primaryGradient,
                            size = size.copy(width = (offsetX + thumbSizePx + thumbPaddingPx * 2)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                        )
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !showSuccess,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localizedStringResource(R.string.slide_to_call_short).uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = (1f - progress).coerceIn(0.2f, 0.6f)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = thumbSize)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, tint = AlertaraSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedStringResource(R.string.connecting),
                        color = AlertaraSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = (offsetX + thumbPaddingPx).roundToInt(), y = 0) }
                    .align(Alignment.CenterStart)
                    .size(thumbSize)
                    .shadow(
                        elevation = if (isDragging) 8.dp else 2.dp,
                        shape = CircleShape,
                        ambientColor = AlertaraTeal,
                        spotColor = AlertaraTealDark
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .pointerInput(isInternetCall) {
                        detectDragGestures(
                            onDragStart = { 
                                isDragging = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragEnd = {
                                isDragging = false
                                if (offsetX >= dragThresholdPx) {
                                    scope.launch {
                                        animate(
                                            initialValue = offsetX,
                                            targetValue = maxOffset,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                        ) { value, _ -> offsetX = value }
                                        
                                        showSuccess = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCallInitiated()
                                        
                                        delay(3000)
                                        showSuccess = false
                                        animate(
                                            initialValue = maxOffset,
                                            targetValue = 0f,
                                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                                        ) { value, _ -> offsetX = value }
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount.x
                            offsetX = newOffset.coerceIn(0f, maxOffset)
                            
                            if ((offsetX % 50).toInt() == 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = if (isDarkMode) AlertaraTealDark else AlertaraTeal,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            rotationZ = progress * 360f
                        }
                )
            }
        }
    }
}
