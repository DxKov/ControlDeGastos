package com.example.controldegastos.feature.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

import com.example.controldegastos.feature.credit_cards.domain.usecase.CalculateMonthlyCardDebtUseCase

/**
 * Worker que se ejecuta diariamente a las 9 AM.
 * Verifica si hoy es el día de corte de alguna tarjeta de crédito
 * y envía una notificación con el monto adeudado en el ciclo actual.
 */
class CutoffDayWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CutoffEntryPoint {
        fun creditCardRepository(): CreditCardRepository
        fun calculateMonthlyCardDebtUseCase(): CalculateMonthlyCardDebtUseCase
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CutoffEntryPoint::class.java
        )
        val repository = entryPoint.creditCardRepository()
        val calculateMonthlyCardDebtUseCase = entryPoint.calculateMonthlyCardDebtUseCase()

        val today = LocalDate.now()
        val cards = repository.observeAll().first()

        cards.forEach { card ->
            if (card.cutoffDay == today.dayOfMonth) {
                // Fetch the current billing cycle to get the real amount due for the month
                val amountDue = calculateMonthlyCardDebtUseCase(card)
                showCutoffNotification(card.name, amountDue)
            }
        }

        return Result.success()
    }

    private fun showCutoffNotification(cardName: String, amountDue: BigDecimal) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cutoff_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Corte de Tarjeta",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerta del día de corte de tus tarjetas de crédito"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        val message = if (amountDue > BigDecimal.ZERO) {
            "Tu pago para no generar intereses es de ${formatter.format(amountDue)}"
        } else {
            "Hoy es tu fecha de corte y no tienes deuda pendiente para el mes. ¡Felicidades!"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("📅 Corte: $cardName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Use a unique ID per card to avoid overwriting other cards' notifications
        notificationManager.notify("cutoff_$cardName".hashCode(), notification)
    }
}
