package com.example.emergencycommunicationsystem.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import java.util.Locale
import kotlinx.coroutines.delay

private data class Hotline(
    val name: String,
    val number: String,
    val description: String,
    val listIcon: ImageVector,
    val buttonIcon: ImageVector
)

@Suppress("UNUSED_VALUE")
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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        AnimatedContent(
            modifier = Modifier.padding(padding),
            targetState = activeCall,
            transitionSpec = { fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400)) },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            // add extra bottom padding so content and footer are not overlapped by the global bottom nav
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
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

            // Footer placed as a list item so it appears above the global bottom nav
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Footer()
            }
        }
    }
}

@Composable
private fun ListHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun HotlineCard(hotline: Hotline, onClick: (Hotline) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(hotline) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(hotline.listIcon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(hotline.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(hotline.number, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 32.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(hotline.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, lineHeight = 18.sp, modifier = Modifier.padding(start = 32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(hotline.buttonIcon, "Call ${hotline.name}", tint = MaterialTheme.colorScheme.onPrimary)
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
                Box(modifier = Modifier.size(150.dp).scale(scale).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(hotline.buttonIcon, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(60.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Calling...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(hotline.name, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val minutes = timerSeconds / 60
            val seconds = timerSeconds % 60
            Text(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(text, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EndCallButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            Icons.Default.CallEnd,
            contentDescription = "End Call",
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun Footer() {
    Text(
        text = "Location: Quezon City. Disclaimer: Use only for emergencies.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
