package com.example.emergencycommunicationsystem.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define the new Dark Color Scheme using the colors from Color.kt
private val AppDarkColorScheme = darkColorScheme(
    primary = Teal,                 // Main accent color for buttons, icons, switches
    onPrimary = DarkNavy,           // Color of text/icons on top of the primary color (e.g., text on a Teal button)
    secondary = Slate,              // Secondary accent color
    onSecondary = White,            // Text/icons on top of the secondary color
    background = DarkNavy,          // Main screen background
    onBackground = White,           // Main text color on the background
    surface = Slate,                // Color of cards and other surfaces on top of the background
    onSurface = White,              // Main text color on surfaces (like cards)
    surfaceVariant = DarkNavy,      // Subtle variation for surfaces
    onSurfaceVariant = LightGray,   // Text color for less important elements on surfaces
    error = Color(0xFFFF6B6B),      // A standard error color
    onError = Color.White
)

// Alertara Light Scheme (matching the provided image)
private val AlertaraLightColorScheme = lightColorScheme(
    primary = AlertaraPrimary,      // Emergency Red for buttons/actions
    onPrimary = White,
    secondary = AlertaraSecondary,
    onSecondary = AlertaraBackground,
    background = AlertaraBackground, // The deep teal-green background
    onBackground = White,
    surface = AlertaraSurface,      // The lighter teal-green for cards/sections
    onSurface = White,
    surfaceVariant = AlertaraSurface.copy(alpha = 0.7f),
    onSurfaceVariant = White.copy(alpha = 0.8f),
    primaryContainer = AlertaraPrimary,
    onPrimaryContainer = White,
    outline = AlertaraSurface,
    error = StatusDanger,
    onError = White
)

@Composable
fun EmergencyCommunicationSystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but we will disable it to enforce our custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // We use Alertara theme for light mode as requested
        darkTheme -> AppDarkColorScheme
        else -> AlertaraLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // With edge-to-edge, we make the status bar transparent to let the app's background show.
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            // This controls whether the status bar icons (clock, battery) are light or dark.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
