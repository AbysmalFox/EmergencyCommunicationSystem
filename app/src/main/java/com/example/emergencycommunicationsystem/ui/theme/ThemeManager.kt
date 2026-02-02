package com.example.emergencycommunicationsystem.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ThemeManager: Centralized utility to ensure UI components strictly adhere 
 * to the current theme (Light or Dark mode).
 */
object ThemeManager {

    /**
     * Checks if the app is currently in Dark Mode.
     * Updated: Now checks Surface luminance instead of Background, because our 
     * light theme uses a dark greenish background but light cards (surfaces).
     */
    @Composable
    fun isDarkMode(): Boolean {
        val surfaceColor = MaterialTheme.colorScheme.surface
        // Luminance check: (R+G+B)/3
        return (surfaceColor.red + surfaceColor.green + surfaceColor.blue) / 3f < 0.5f
    }

    /**
     * Returns a color that is optimized for the current theme mode.
     */
    @Composable
    fun getAdaptiveColor(lightColor: Color, darkColor: Color): Color {
        return if (isDarkMode()) darkColor else lightColor
    }

    @Composable
    fun getSurfaceColor(): Color {
        return MaterialTheme.colorScheme.surface
    }

    @Composable
    fun getBorderColor(alpha: Float = 0.1f): Color {
        return if (isDarkMode()) {
            Color.White.copy(alpha = alpha)
        } else {
            Color.Black.copy(alpha = alpha)
        }
    }

    @Composable
    fun getOnBackgroundColor(): Color {
        return MaterialTheme.colorScheme.onBackground
    }
}

/**
 * Extension to apply theme-safe styling to any Modifier.
 */
@Composable
fun Modifier.themeShadow(
    elevation: Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp)
): Modifier {
    val isDark = ThemeManager.isDarkMode()
    return this.shadow(
        elevation = elevation,
        shape = shape,
        spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.1f),
        ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.05f)
    )
}
