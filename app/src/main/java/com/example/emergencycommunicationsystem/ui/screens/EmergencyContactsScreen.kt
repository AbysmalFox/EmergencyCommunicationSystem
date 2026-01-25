package com.example.emergencycommunicationsystem.ui.screens

import android.content.Intent
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
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

private data class Hotline(
    val name: String,
    val number: String,
    val description: String,
    val listIcon: ImageVector,
    val buttonIcon: ImageVector
)

@Composable
fun EmergencyContactsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current

    val hotlineGroups = remember {
        mapOf(
            "Quezon City Specific Hotlines" to listOf(
                Hotline("QC Helpline", "122", "Primary 24/7 contact center for all emergencies.", AppIcons.EmergencyCall, AppIcons.EmergencyCall),
                Hotline("QC DRRMO", "89275914", "Disaster Risk Reduction and Management.", AppIcons.Warning, AppIcons.Warning),
                Hotline("Quezon City Fire District", "83302344", "For fire hazards, rescues, and inspections.", AppIcons.LocalFireDepartment, AppIcons.LocalFireDepartment)
            ),
            "Nationwide Emergency Hotlines" to listOf(
                Hotline("National Emergency Hotline", "911", "National emergency hotline for police, fire, and medical.", AppIcons.Shield, AppIcons.Shield),
                Hotline("PNP", "117", "Philippine National Police connection.", AppIcons.LocalPolice, AppIcons.LocalPolice),
                Hotline("Philippine Red Cross", "143", "Medical and humanitarian aid.", AppIcons.LocalHospital, AppIcons.LocalHospital),
                Hotline("Bureau of Fire Protection", "84260219", "National fire protection and rescue.", AppIcons.LocalFireDepartment, AppIcons.LocalFireDepartment),
                Hotline("MMDA", "136", "Metropolitan Manila Development Authority.", AppIcons.Traffic, AppIcons.Traffic)
            )
        )
    }

    HotlineList(
        hotlineGroups = hotlineGroups,
        onItemClick = { hotline ->
            val intent = Intent(Intent.ACTION_DIAL, "tel:${hotline.number}".toUri())
            context.startActivity(intent)
        },
        onBackPressed = onBackPressed
    )
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
            // Custom reduced-height TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .height(56.dp) // Standard height for touch targets
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        "Emergency Hotlines",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
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