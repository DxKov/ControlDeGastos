package com.example.controldegastos.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Dark color palette for GastoApp.
 * Uses #0F0F12 as the primary background color for a premium dark mode feel.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),      // Purple Accent
    secondary = Color(0xFF00E5FF),    // Electric Blue
    tertiary = Color(0xFF3700B3),     // Dark Blue
    background = Color(0xFF0F0F12),   // Main Background
    surface = Color(0xFF0F0F12),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFCF6679)
)

@Composable
fun GastoAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
