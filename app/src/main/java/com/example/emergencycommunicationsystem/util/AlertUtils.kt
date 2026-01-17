package com.example.emergencycommunicationsystem.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
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

/**
 * Returns a theme-aware color for alert categories.
 * In Light Mode, it shifts towards the "Satisfying Green/Teal" identity.
 */
@Composable
fun getColorForCategory(alert: Alert): Color {
    val isDark = ThemeManager.isDarkMode()
    val categoryId = try { alert.category?.toIntOrNull() ?: 0 } catch (_: Exception) { 0 }
    val title = alert.title?.lowercase(Locale.getDefault()) ?: ""
    val categoryStr = alert.category?.lowercase(Locale.getDefault()) ?: ""
    
    return when {
        // Weather - Move from Blue to Teal for Light Mode
        categoryId == 1 || "weather" in categoryStr || "flood" in title -> {
            if (isDark) Color(0xFF80CBC4) else Color(0xFF00ACC1)
        }
        // Fire - Use a richer, more professional Red/Orange
        categoryId == 4 || "fire" in categoryStr -> {
            if (isDark) Color(0xFFFF8A80) else Color(0xFFE53935)
        }
        // Earthquake - Professional Brown/Gray
        categoryId == 2 || "earthquake" in categoryStr -> {
            if (isDark) Color(0xFFA1887F) else Color(0xFF795548)
        }
        // Health / Security / General - Use the Primary Brand Teal
        else -> {
            if (isDark) Color(0xFF4DB6AC) else Color(0xFF00897B)
        }
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
