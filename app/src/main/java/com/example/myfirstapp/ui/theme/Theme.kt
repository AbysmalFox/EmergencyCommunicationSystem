package com.example.myfirstapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF7A00),      // SafetyOrange
    background = Color(0xFF1A1C2C),   // MidnightBlue
    surface = Color(0xFF2A2D40),      // DarkSurface
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF9E9E9E), // MutedGray
    error = Color(0xFFD32F2F)         // A brighter red for the call button
)
