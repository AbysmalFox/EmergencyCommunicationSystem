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

// Improved Light Scheme - Cleaner, more professional, and easier to read
private val AlertaraLightColorScheme = lightColorScheme(
    primary = EmergencyRedMain,      // Striking Emergency Red
    onPrimary = White,
    secondary = Color(0xFF00838F),   // Deep teal for better contrast in light mode
    onSecondary = White,
    background = Color(0xFFF0F2F5),  // Very light gray background for depth
    onBackground = Color(0xFF1C1E21), // Near-black for text
    surface = White,                 // Pure white cards
    onSurface = Color(0xFF1C1E21),
    surfaceVariant = Color(0xFFE4E6EB),
    onSurfaceVariant = Color(0xFF65676B),
    primaryContainer = Color(0xFFFEEBEC), // Very light red tint for boxes
    onPrimaryContainer = EmergencyRedMain,
    secondaryContainer = Color(0xFFE0F7FA), // Very light teal tint
    onSecondaryContainer = Color(0xFF006064),
    outline = Color(0xFFDDDFE2),     // Subtle borders
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
            window.statusBarColor = colorScheme.background.toArgb()
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
