package com.example.controldegastos.feature.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.controldegastos.core.domain.repository.AccountRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class BalanceWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BalanceEntryPoint {
        fun accountRepository(): AccountRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BalanceEntryPoint::class.java
        )
        val repository = entryPoint.accountRepository()
        val totalBalance = repository.getTotalBalance()

        provideContent {
            BalanceWidgetContent(totalBalance)
        }
    }

    @Composable
    private fun BalanceWidgetContent(balance: BigDecimal) {
        val electricBlue = ColorProvider(Color(0xFF00E5FF))
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0F0F12))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SALDO TOTAL",
                style = TextStyle(
                    color = ColorProvider(Color.Gray),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = currencyFormatter.format(balance),
                style = TextStyle(
                    color = electricBlue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
