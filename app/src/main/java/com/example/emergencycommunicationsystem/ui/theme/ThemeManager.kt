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
     * Checks if the app is currently in Dark Mode based on the MaterialTheme's background luminance.
     */
    @Composable
    fun isDarkMode(): Boolean {
        val bgColor = MaterialTheme.colorScheme.background
        // Simple luminance check: (R+G+B)/3
        return (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
    }

    /**
     * Returns a color that is optimized for the current theme mode.
     * Useful for elements that need custom colors but must stay readable.
     */
    @Composable
    fun getAdaptiveColor(lightColor: Color, darkColor: Color): Color {
        return if (isDarkMode()) darkColor else lightColor
    }

    /**
     * Ensures that surface containers (Cards/Boxes) have proper contrast.
     */
    @Composable
    fun getSurfaceColor(): Color {
        return MaterialTheme.colorScheme.surface
    }

    /**
     * Provides a standard "Clean" border color based on the current theme.
     */
    @Composable
    fun getBorderColor(alpha: Float = 0.1f): Color {
        return if (isDarkMode()) {
            Color.White.copy(alpha = alpha)
        } else {
            Color.Black.copy(alpha = alpha)
        }
    }

    /**
     * Helper to get high-contrast text color for the current background.
     */
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
