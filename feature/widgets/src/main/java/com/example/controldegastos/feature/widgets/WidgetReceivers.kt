package com.example.controldegastos.feature.widgets

import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the Balance desktop widget.
 */
class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BalanceWidget()
}

/**
 * Receiver for the Quick Add desktop widget.
 */
class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QuickAddWidget()
}
