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

// Dark Theme
private val AppDarkColorScheme = darkColorScheme(
    primary = Teal,                 
    onPrimary = DarkNavy,           
    secondary = Slate,              
    onSecondary = White,            
    background = DarkNavy,          
    onBackground = White,           
    surface = Slate,                
    onSurface = White,              
    surfaceVariant = DarkNavy,      
    onSurfaceVariant = LightGray,   
    error = Color(0xFFFF6B6B),      
    onError = Color.White
)

/**
 * Main Light Theme applying the requested "Dark Green BG / White Card" palette.
 * background: AlertaraBackground (Dark Greenish)
 * onBackground: White (For contrast on dark BG)
 * surface: White (Pure White for Cards to make them clearer)
 * onSurface: BrandDeepTeal (Dark Greenish text for readability on white cards)
 */
private val AlertaraLightColorScheme = lightColorScheme(
    primary = Color.White,      
    onPrimary = AlertaraBackground,
    secondary = BrandTealAccent,   
    onSecondary = White,
    background = AlertaraBackground, // Dark Greenish (0xFF34635D)
    onBackground = Color.White,      // Readable white text on dark background
    surface = Color.White,           // Pure White for cards in light mode
    onSurface = Color.Black,         // Black text for readability on white
    surfaceVariant = BrandTealLight, // Instruction Box remains light greenish
    onSurfaceVariant = BrandDeepTeal, 
    outline = Color(0xFFB2DFDB),     
    error = StatusDanger,
    onError = White
)

@Composable
fun EmergencyCommunicationSystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> AppDarkColorScheme
        else -> AlertaraLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // In light theme, if background is dark, we want light status bar icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
