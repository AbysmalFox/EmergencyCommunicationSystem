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
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val sliderHeight = 64.dp
    val thumbSize = 56.dp
    val thumbPadding = 4.dp
    
    val density = LocalDensity.current
    val sliderWidthPx = with(density) { sliderWidth.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val thumbPaddingPx = with(density) { thumbPadding.toPx() }
    val maxOffset = sliderWidthPx - thumbSizePx - (thumbPaddingPx * 2)
    val dragThresholdPx = maxOffset * 0.85f
    
    val progress = (offsetX / maxOffset).coerceIn(0f, 1f)

    // Colors
    val blueColor = Color(0xFF2196F3)
    val purpleColor = Color(0xFF9C27B0)
    val orangeColor = Color(0xFFFF9800)
    val pinkColor = Color(0xFFE91E63)
    val successColor = Color(0xFF4CAF50)

    val primaryGradient = Brush.linearGradient(
        colors = if (isInternetCall) listOf(blueColor, purpleColor) else listOf(orangeColor, pinkColor)
    )

    val backgroundColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    // Idle pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "idle")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Reset animation
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
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modern Segmented Selector
        Surface(
            modifier = Modifier
                .width(220.dp)
                .height(40.dp),
            shape = CircleShape,
            color = backgroundColor,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                listOf(true, false).forEach { isInternet ->
                    val selected = isInternetCall == isInternet
                    val label = if (isInternet) "Internet" else "Cellular"
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(if (selected) primaryGradient else SolidColor(Color.Transparent))
                            .clickable { 
                                isInternetCall = isInternet
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // The Slider
        Box(
            modifier = Modifier
                .width(sliderWidth)
                .height(sliderHeight)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = if (isDragging) blueColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .drawBehind {
                    // Path highlight
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
            // Placeholder Text
            androidx.compose.animation.AnimatedVisibility(
                visible = !showSuccess,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Slide to Call".uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = (1f - progress).coerceIn(0.2f, 1f)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = thumbSize)
                )
            }

            // Success State
            androidx.compose.animation.AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, tint = successColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECTING...",
                        color = successColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // The Thumb
            Box(
                modifier = Modifier
                    .padding(thumbPadding)
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .size(thumbSize)
                    .graphicsLayer {
                        scaleX = if (!isDragging && !showSuccess) idleScale else 1f
                        scaleY = if (!isDragging && !showSuccess) idleScale else 1f
                    }
                    .shadow(
                        elevation = if (isDragging) 12.dp else 4.dp,
                        shape = CircleShape,
                        ambientColor = if (isInternetCall) blueColor else orangeColor,
                        spotColor = if (isInternetCall) purpleColor else pinkColor
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
                                            animationSpec = spring()
                                        ) { value, _ -> offsetX = value }
                                        
                                        showSuccess = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCallInitiated()
                                        
                                        delay(3000)
                                        showSuccess = false
                                        animate(
                                            initialValue = maxOffset,
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        ) { value, _ -> offsetX = value }
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount.x
                            offsetX = newOffset.coerceIn(0f, maxOffset)
                            
                            // Subtle haptic during drag
                            if ((offsetX % 40).toInt() == 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = if (isInternetCall) blueColor else orangeColor,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = progress * 360f
                        }
                )
            }
        }
    }
}
