package com.example.emergencycommunicationsystem.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Fullscreen overlay that shows a loading spinner for ~2s then crossfades to a check icon with a bounce
 * animation and a message. Calls onDismiss() when finished.
 */
@Composable
fun SafeOverlay(
    visible: Boolean,
    message: String = "Always stay safe",
    onDismiss: () -> Unit
) {
    if (!visible) return

    var success by remember { mutableStateOf(false) }

    val overlayAlpha = remember { Animatable(0f) }
    val checkScale = remember { Animatable(0f) }
    val checkAlpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            // fade in overlay
            overlayAlpha.animateTo(1f, animationSpec = tween(durationMillis = 250))
            // show loading for 2s
            delay(2000)
            // switch to success
            success = true
            checkAlpha.snapTo(0f)
            checkScale.snapTo(0f)
            checkAlpha.animateTo(1f, animationSpec = tween(200))
            checkScale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            checkScale.animateTo(1f, animationSpec = tween(200))
            // keep success visible briefly then dismiss
            delay(1400)
            overlayAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
            onDismiss()
            // reset internal state
            success = false
            checkScale.snapTo(0f)
            checkAlpha.snapTo(0f)
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * overlayAlpha.value)),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = success) { isSuccess ->
            if (!isSuccess) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(24.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sending status...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(24.dp)
                ) {
                    val iconSize = with(LocalDensity.current) { 96.dp }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(iconSize)
                            .scale(checkScale.value)
                            .alpha(checkAlpha.value)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
