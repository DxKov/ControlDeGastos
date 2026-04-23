package com.example.controldegastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.controldegastos.navigation.MainScaffold
import com.example.controldegastos.ui.theme.GastoAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the application UI.
 * Handles the initial setup of the theme and navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display and transparent status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            GastoAppTheme {
                // Main container with the app's background color
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0F12)
                ) {
                    MainScaffold()
                }
            }
        }
    }
}