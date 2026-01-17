package com.example.emergencycommunicationsystem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.ui.theme.themeShadow
import com.example.emergencycommunicationsystem.util.getLocaleContext
import com.example.emergencycommunicationsystem.util.getColorForCategory
import com.example.emergencycommunicationsystem.util.getIconForCategory
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import kotlinx.coroutines.launch

/**
 * AlertNotifCard: A standardized component for alert notifications
 * fully integrated with the Main Theme (Greenish for Light Mode).
 */
@Composable
fun AlertNotifCard(
    alert: Alert,
    onMessageClick: (id: String, title: String) -> Unit
) {
    val isDark = ThemeManager.isDarkMode()
    val categoryColor = getColorForCategory(alert)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .themeShadow(
                elevation = if (isDark) 12.dp else 6.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Clean White in Light Mode
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) // Soft Emerald border
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- Header Section ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForCategory(alert),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = (alert.category ?: "Alert").uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = categoryColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = alert.title ?: "Untitled Alert",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = alert.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 10.dp, bottom = 16.dp),
                lineHeight = 20.sp
            )

            // --- The "What To Do" Widget ---
            InstructionWidget(alert = alert)

            Spacer(modifier = Modifier.height(20.dp))

            // --- Footer Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = alert.source ?: "Unknown Source",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = alert.timestamp ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = { onMessageClick(alert.id.toString(), alert.title ?: "Chat") },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary, // Rich Teal
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(AppIcons.Message, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    val localeContext = getLocaleContext()
                    Text(localeContext.getString(R.string.message), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InstructionWidget(alert: Alert) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = ThemeManager.isDarkMode()
    
    // Determine type for instructions
    val alertType = when {
        alert.category?.contains("weather", true) == true || alert.title?.contains("typhoon", true) == true -> "flood"
        alert.category?.contains("fire", true) == true -> "fire"
        alert.category?.contains("earthquake", true) == true -> "earthquake"
        else -> "general"
    }
    
    val stepsEn = when (alertType) {
        "flood" -> listOf("Avoid floodwaters", "Go to highest floor", "Turn off electricity if safe")
        "fire" -> listOf("Use stairs only", "Stay low to avoid smoke", "Feel doors before opening")
        "earthquake" -> listOf("Drop to hands and knees", "Cover head and neck", "Hold on to sturdy furniture")
        else -> listOf("Follow local guidance", "Stay tuned for updates")
    }

    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    var translatedSteps by remember { mutableStateOf(stepsEn) }
    var translatedLabel by remember { mutableStateOf("What To Do:") }
    
    LaunchedEffect(alertType, currentLanguage) {
        if (currentLanguage != "en") {
            coroutineScope.launch {
                translatedLabel = TranslationService.translate("What To Do:", currentLanguage)
                translatedSteps = TranslationService.translateBatch(stepsEn, currentLanguage)
            }
        } else {
            translatedLabel = "What To Do:"
            translatedSteps = stepsEn
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant, // Crystal Clear Mint in Light Mode
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = translatedLabel,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
            
            Spacer(Modifier.height(10.dp))
            
            translatedSteps.forEach { step ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        "• ", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Darker Teal in Light Mode
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
