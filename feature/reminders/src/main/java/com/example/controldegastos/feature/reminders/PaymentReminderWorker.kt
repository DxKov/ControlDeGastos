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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Worker responsible for checking if a credit card payment is due soon.
 * Uses Hilt EntryPoint to access repositories since it's instantiated by WorkManager.
 */
class PaymentReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderEntryPoint {
        fun creditCardRepository(): CreditCardRepository
    }

    override suspend fun doWork(): Result {
        val cardId = inputData.getLong("CARD_ID", -1L)
        if (cardId == -1L) return Result.failure()

        // Access the repository via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ReminderEntryPoint::class.java
        )
        val repository = entryPoint.creditCardRepository()
        
        val card = repository.getCardById(cardId) ?: return Result.failure()

        // Calculation logic
        val today = LocalDate.now()
        val dueDay = card.paymentDueDay
        
        // Find the next occurrence of the due day
        var dueDate = LocalDate.of(today.year, today.monthValue, 
            minOf(dueDay, today.lengthOfMonth()))
        
        if (dueDate.isBefore(today)) {
            dueDate = dueDate.plusMonths(1)
            dueDate = dueDate.withDayOfMonth(minOf(dueDay, dueDate.lengthOfMonth()))
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, dueDate)

        // Only notify if within the 3-day window
        if (daysRemaining <= 3) {
            showNotification(card.name, daysRemaining)
        }

        return Result.success()
    }

    private fun showNotification(cardName: String, daysRemaining: Long) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "payment_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Pago",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para vencimiento de tarjetas de crédito"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val message = when (daysRemaining) {
            0L -> "¡Hoy es el día de pago para tu tarjeta $cardName!"
            1L -> "Mañana es el día de pago para tu tarjeta $cardName."
            else -> "Faltan $daysRemaining días para el pago de tu tarjeta $cardName."
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Alerta de Pago de Tarjeta")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(cardName.hashCode(), notification)
    }
}
