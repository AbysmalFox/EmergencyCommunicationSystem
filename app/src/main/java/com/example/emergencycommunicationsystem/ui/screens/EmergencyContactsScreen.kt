package com.example.emergencycommunicationsystem.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import java.util.Locale
import kotlinx.coroutines.delay

// 1. Define your Colors explicitly
private val DarkBg = Color(0xFF1A1E29)
private val SafetyOrange = Color(0xFFFF7F00)
private val TextWhite = Color.White
private val SubtleGray = Color(0xFF2A2E3D)
private val CallEndRed = Color(0xFFE63946)

private data class Hotline(
    val name: String,
    val number: String,
    val description: String,
    val listIcon: ImageVector,
    val buttonIcon: ImageVector
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EmergencyContactsScreen(onBackPressed: () -> Unit) {
    var activeCall by remember { mutableStateOf<Hotline?>(null) }

    val hotlineGroups = remember {
        mapOf(
            "Quezon City Specific Hotlines" to listOf(
                Hotline("QC Helpline", "122", "Primary 24/7 contact center for all emergencies.", Icons.Default.Call, Icons.Default.Call),
                Hotline("QC DRRMO", "89275914", "Disaster Risk Reduction and Management.", Icons.Default.Warning, Icons.Default.Warning),
                Hotline("Quezon City Fire District", "83302344", "For fire hazards, rescues, and inspections.", Icons.Default.LocalFireDepartment, Icons.Default.LocalFireDepartment)
            ),
            "Nationwide Emergency Hotlines" to listOf(
                Hotline("National Emergency Hotline", "911", "National emergency hotline for police, fire, and medical.", Icons.Default.Shield, Icons.Default.Shield),
                Hotline("PNP", "117", "Philippine National Police connection.", Icons.Default.LocalPolice, Icons.Default.LocalPolice),
                Hotline("Philippine Red Cross", "143", "Medical and humanitarian aid.", Icons.Default.LocalHospital, Icons.Default.LocalHospital),
                Hotline("Bureau of Fire Protection", "84260219", "National fire protection and rescue.", Icons.Default.LocalFireDepartment, Icons.Default.LocalFireDepartment),
                Hotline("MMDA", "136", "Metropolitan Manila Development Authority.", Icons.Default.Traffic, Icons.Default.Traffic)
            )
        )
    }

    Scaffold(containerColor = DarkBg) { padding ->
        AnimatedContent(
            modifier = Modifier.padding(padding),
            targetState = activeCall,
            transitionSpec = { fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400)) },
            label = "ScreenSwitch"
        ) { call ->
            if (call == null) {
                HotlineList(
                    hotlineGroups = hotlineGroups,
                    onItemClick = { hotline -> activeCall = hotline },
                    onBackPressed = onBackPressed
                )
            } else {
                SimulatedCallInterface(
                    hotline = call,
                    onEndCall = { activeCall = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HotlineList(
    hotlineGroups: Map<String, List<Hotline>>,
    onItemClick: (Hotline) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Hotlines") },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg, titleContentColor = TextWhite, navigationIconContentColor = TextWhite)
            )
        },
        bottomBar = { Footer() },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            hotlineGroups.forEach { (header, hotlines) ->
                stickyHeader {
                    ListHeader(title = header)
                }
                items(hotlines, key = { it.number }) {
                    HotlineCard(hotline = it, onClick = onItemClick)
                }
            }
        }
    }
}

@Composable
private fun ListHeader(title: String) {
    Text(
        text = title,
        color = SafetyOrange,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(vertical = 8.dp, horizontal = 16.dp) // Added horizontal padding to align with cards
    )
}

@Composable
private fun HotlineCard(hotline: Hotline, onClick: (Hotline) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(hotline) }.padding(horizontal = 16.dp), // Added padding here
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SubtleGray)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(hotline.listIcon, null, tint = TextWhite.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(hotline.name, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(hotline.number, color = TextWhite.copy(alpha = 0.9f), fontSize = 16.sp, modifier = Modifier.padding(start = 32.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(hotline.description, color = TextWhite.copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 18.sp, modifier = Modifier.padding(start = 32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SafetyOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(hotline.buttonIcon, "Call ${hotline.name}", tint = TextWhite)
            }
        }
    }
}

@Composable
private fun SimulatedCallInterface(hotline: Hotline, onEndCall: () -> Unit) {
    var timerSeconds by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_DIAL, "tel:${hotline.number}".toUri())
        context.startActivity(intent)

        while (true) {
            delay(1000)
            timerSeconds++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse), label = "Scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(150.dp).scale(scale).clip(CircleShape).background(SafetyOrange.copy(alpha = 0.3f)))
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(SafetyOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(hotline.buttonIcon, null, tint = TextWhite, modifier = Modifier.size(60.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Calling...", color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(hotline.name, color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val minutes = timerSeconds / 60
            val seconds = timerSeconds % 60
            Text(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds), color = TextWhite, fontSize = 20.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(icon = Icons.Default.MicOff, text = "Mute", onClick = {})
            EndCallButton(onEndCall)
            ActionButton(icon = Icons.Default.Dialpad, text = "Keypad", onClick = {})
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(64.dp),
            border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, contentDescription = text, tint = TextWhite, modifier = Modifier.size(28.dp))
        }
        Text(text, color = TextWhite, fontSize = 12.sp)
    }
}

@Composable
private fun EndCallButton(onEndCall: () -> Unit) {
    Button(
        onClick = onEndCall,
        shape = CircleShape,
        modifier = Modifier.size(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CallEndRed),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(Icons.Default.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun Footer() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Location: Quezon City. Disclaimer: Use only for emergencies.",
            color = TextWhite.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
