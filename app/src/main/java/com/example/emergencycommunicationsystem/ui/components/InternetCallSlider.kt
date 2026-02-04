package com.example.emergencycommunicationsystem.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.util.getLocaleContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun InternetCallSlider(
    onCallInitiated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localeContext = getLocaleContext()
    val scope = rememberCoroutineScope()
    
    var isDragging by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var isCallActive by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    val sliderWidth = 300.dp
    val sliderHeight = 80.dp
    
    // Convert Dp threshold to Px for float comparison
    val density = LocalDensity.current
    val dragThresholdPx = with(density) { (sliderWidth * 0.7f).toPx() }
    
    // Animation for success state
    val successAnimation by animateFloatAsState(
        targetValue = if (showSuccess) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "successAnimation"
    )
    
    // Reset animation when not dragging
    LaunchedEffect(isDragging) {
        if (!isDragging && offsetX < dragThresholdPx) {
            delay(100)
            offsetX = 0f
        }
    }
    
    Box(
        modifier = modifier
            .width(sliderWidth)
            .height(sliderHeight)
            .clip(RoundedCornerShape(40.dp))
            .background(
                if (isCallActive) 
                    Color(0xFF4CAF50) // Green when active
                else 
                    Color(0xFF2196F3) // Blue when inactive
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(40.dp)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(40.dp),
                spotColor = if (isCallActive) Color.Green else Color.Blue
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background text
        if (!showSuccess) {
            Text(
                text = localeContext.getString(R.string.slide_to_call_internet),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 16.dp)
            )
        }
        
        // Success message
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + slideInHorizontally(),
            exit = slideOutHorizontally()
        ) {
            Text(
                text = "CONNECTING...",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        // Draggable button
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(60.dp)
                .padding(start = 10.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            if (offsetX >= dragThresholdPx) {
                                // Trigger call
                                isCallActive = true
                                showSuccess = true
                                onCallInitiated()
                                
                                // Reset after delay using local scope
                                scope.launch {
                                    delay(2000)
                                    offsetX = 0f
                                    isCallActive = false
                                    showSuccess = false
                                }
                            } else {
                                // Snap back
                                offsetX = 0f
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val newOffset = offsetX + dragAmount.x
                        val maxOffset = with(density) { (sliderWidth - 70.dp).toPx() }
                        offsetX = newOffset.coerceIn(0f, maxOffset)
                    }
                }
                .graphicsLayer(
                    scaleX = 1f + (successAnimation * 0.2f),
                    scaleY = 1f + (successAnimation * 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call via Internet",
                tint = if (isCallActive) Color(0xFF4CAF50) else Color(0xFF2196F3),
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Animated pulse effect when dragging
        if (isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(40.dp)
                    )
            )
        }
    }
}
