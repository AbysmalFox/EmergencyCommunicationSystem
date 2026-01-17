package com.example.emergencycommunicationsystem.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.CatEarthquake
import com.example.emergencycommunicationsystem.ui.theme.CatFire
import com.example.emergencycommunicationsystem.ui.theme.CatGeneral
import com.example.emergencycommunicationsystem.ui.theme.CatHealth
import com.example.emergencycommunicationsystem.ui.theme.CatSecurity
import com.example.emergencycommunicationsystem.ui.theme.CatWeather
import com.example.emergencycommunicationsystem.ui.theme.SafetyOrange
import com.example.emergencycommunicationsystem.ui.theme.StatusWarning
import java.util.Locale

fun getIconForCategory(alert: Alert): ImageVector {
    val categoryId = try { alert.category?.toIntOrNull() ?: 0 } catch (_: Exception) { 0 }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        categoryId == 1 || "weather" in categoryStr || "typhoon" in title || "storm" in title -> AppIcons.Weather
        categoryId == 2 || "earthquake" in categoryStr || "tremor" in title -> AppIcons.Earthquake
        categoryId == 4 || "fire" in categoryStr -> AppIcons.Fire
        "security" in categoryStr -> AppIcons.Security
        "water" in categoryStr || "flood" in title -> AppIcons.Flood
        else -> AppIcons.Info
    }
}

fun getColorForCategory(alert: Alert): Color {
    val categoryId = try { alert.category?.toIntOrNull() ?: 0 } catch (_: Exception) { 0 }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        categoryId == 1 || "weather" in categoryStr || "flood" in title -> CatWeather
        categoryId == 2 || "earthquake" in categoryStr -> CatEarthquake
        categoryId == 4 || "fire" in categoryStr -> CatFire
        categoryId == 3 || "health" in categoryStr -> CatHealth
        "security" in categoryStr -> CatSecurity
        else -> CatGeneral
    }
}

fun getAlertSeverity(alert: Alert): String {
    val categoryId = try { alert.category?.toIntOrNull() ?: 0 } catch (_: Exception) { 0 }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val content = alert.content?.lowercase(Locale.getDefault()) ?: ""
    val allText = "$title $content"
    
    val highKeywords = listOf("fire", "flood", "earthquake", "tsunami", "typhoon", "evacuate", "urgent", "emergency")
    
    return when {
        categoryId == 2 || categoryId == 4 -> "High"
        highKeywords.any { it in allText } -> "High"
        categoryId == 1 -> "Medium"
        else -> "Low"
    }
}

@Composable
fun getSeverityColor(severity: String): Color {
    return when (severity) {
        "High" -> MaterialTheme.colorScheme.error
        "Medium" -> SafetyOrange
        "Low" -> StatusWarning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
