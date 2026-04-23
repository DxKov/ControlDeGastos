package com.example.controldegastos.feature.widgets

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickAddContent()
        }
    }

    @Composable
    private fun QuickAddContent() {
        val accentPurple = Color(0xFFBB86FC)
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0F0F12)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier
                    .size(64.dp)
                    .background(accentPurple)
                    .clickable(actionStartActivity(Intent().apply {
                        component = ComponentName("com.example.controldegastos", "com.example.controldegastos.MainActivity")
                    })),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
