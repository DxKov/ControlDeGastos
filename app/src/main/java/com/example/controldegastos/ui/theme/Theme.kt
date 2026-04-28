package com.example.controldegastos.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Dark color palette for GastoApp.
 * Uses #0F0F12 as the primary background color for a premium dark mode feel.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),      // Indigo suave
    secondary = Color(0xFF94A3B8),    // Slate/Gris azulado
    tertiary = Color(0xFF475569),     // Slate oscuro
    background = Color(0xFF0A0A0A),   // Fondo casi negro
    surface = Color(0xFF161616),      // Superficie oscura
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0), // Texto claro suave
    onSurface = Color(0xFFE2E8F0),
    error = Color(0xFFEF4444)
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
