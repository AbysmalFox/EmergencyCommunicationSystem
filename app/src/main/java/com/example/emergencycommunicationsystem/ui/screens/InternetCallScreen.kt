package com.example.emergencycommunicationsystem.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.models.CallMessage
import com.example.emergencycommunicationsystem.ui.components.MessageBubble
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.viewmodel.InternetCallViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternetCallScreen(
    onEndCall: () -> Unit,
    viewModel: InternetCallViewModel,
    callType: String = "internet"
) {
    val context = LocalContext.current
    val localeContext = getLocaleContext()
    val scope = rememberCoroutineScope()
    
    val callState by viewModel.callState.collectAsState()
    val messageInput by viewModel.messageInput
    val isSending by viewModel.isSending
    
    var hasAudioPermission by remember { mutableStateOf(false) }
    val normalizedCallType = remember(callType) {
        if (callType.equals("cellular", ignoreCase = true)) "cellular" else "internet"
    }
    
    // Permission launcher for audio recording
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            scope.launch {
                val userId = AuthManager.getUserId()
                val roomName = "emergency-room-${System.currentTimeMillis()}"
                val location = resolveCallLocation(context)
                viewModel.startCall(
                    userId = userId,
                    roomName = roomName,
                    callType = normalizedCallType,
                    latitude = location?.first,
                    longitude = location?.second,
                    locationLabel = location?.third
                )
            }
        }
    }
    
    // Check audio permission on launch
    LaunchedEffect(Unit) {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasAudioPermission) {
            // Start call with current user
            val userId = AuthManager.getUserId()
            val roomName = "emergency-room-${System.currentTimeMillis()}"
            val location = resolveCallLocation(context)
            viewModel.startCall(
                userId = userId,
                roomName = roomName,
                callType = normalizedCallType,
                latitude = location?.first,
                longitude = location?.second,
                locationLabel = location?.third
            )
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    // Update call duration is now handled by ViewModel
    
    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 60.dp)
            ) {
                Text(
                    text = if (callState.isConnecting) localeContext.getString(com.example.emergencycommunicationsystem.R.string.connecting)
                    else if (callState.isActive) localeContext.getString(com.example.emergencycommunicationsystem.R.string.emergency_call_title)
                    else if (normalizedCallType == "cellular") localeContext.getString(com.example.emergencycommunicationsystem.R.string.cellular_call)
                    else localeContext.getString(com.example.emergencycommunicationsystem.R.string.internet_call),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // User profile section
                callState.remoteUser?.let { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Avatar",
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.username,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (callState.isActive) localeContext.getString(com.example.emergencycommunicationsystem.R.string.connected_status)
                                else if (normalizedCallType == "cellular") localeContext.getString(com.example.emergencycommunicationsystem.R.string.cellular_call)
                                else localeContext.getString(com.example.emergencycommunicationsystem.R.string.via_internet),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } ?: Text(
                    text = if (callState.isActive) localeContext.getString(com.example.emergencycommunicationsystem.R.string.connected_emergency_services)
                    else if (normalizedCallType == "cellular") localeContext.getString(com.example.emergencycommunicationsystem.R.string.cellular_call)
                    else localeContext.getString(com.example.emergencycommunicationsystem.R.string.via_internet),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                
                if (callState.isActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = formatDuration(callState.duration),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Middle section - Visual indicator
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (callState.isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(100.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                } else if (callState.isActive) {
                    // Animated pulse effect for active call
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Active Call",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneDisabled,
                            contentDescription = "Not Connected",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
            
            // Bottom section - Controls and Messages
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                // Call controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    // Mute button
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (callState.isMuted) Color.Red.copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.2f)
                            )
                    ) {
                        Icon(
                            imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Cancel/End call button
                    IconButton(
                        onClick = { 
                            if (callState.isActive) {
                                viewModel.endCall()
                                onEndCall()
                            } else {
                                viewModel.cancelCall()
                                onEndCall()
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = if (callState.isActive) "End Call" else "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Speaker button
                    IconButton(
                        onClick = { viewModel.toggleSpeaker() },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (callState.isSpeakerOn) Color(0xFF4CAF50).copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.2f)
                            )
                    ) {
                        Icon(
                            imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Speaker",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Message conversation area
                if (callState.isActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Messages area
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(callState.messages) { message ->
                                    MessageBubble(message = message)
                                }
                            }
                            
                            // Message input
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = messageInput,
                                    onValueChange = { viewModel.updateMessageInput(it) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            localeContext.getString(com.example.emergencycommunicationsystem.R.string.type_message),
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedTextColor = Color.White,
                                        focusedTextColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                        focusedBorderColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = { viewModel.sendMessage() }
                                    ),
                                    singleLine = true
                                )
                                
                                IconButton(
                                    onClick = { viewModel.sendMessage() },
                                    enabled = messageInput.isNotBlank() && !isSending,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (messageInput.isNotBlank() && !isSending)
                                                Color(0xFF4CAF50)
                                            else
                                                Color.Gray.copy(alpha = 0.5f)
                                        )
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (!hasAudioPermission) {
                    Text(
                        text = localeContext.getString(com.example.emergencycommunicationsystem.R.string.microphone_permission_required),
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun resolveCallLocation(context: android.content.Context): Triple<Double, Double, String>? {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return null

    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val lastLoc = suspendCancellableCoroutine<android.location.Location?> { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        }

        val location = if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 600000) {
            lastLoc
        } else {
            suspendCancellableCoroutine<android.location.Location?> { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resume(null) }
                    .addOnCanceledListener { continuation.resume(null) }
            }
        }

        location?.let {
            Triple(it.latitude, it.longitude, "${it.latitude},${it.longitude}")
        }
    } catch (_: Exception) {
        null
    }
}
