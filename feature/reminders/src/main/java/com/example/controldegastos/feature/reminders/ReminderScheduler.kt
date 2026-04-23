package com.example.controldegastos.feature.reminders

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Class responsible for scheduling background workers for payment alerts.
 */
class ReminderScheduler @Inject constructor(
    private val context: Context
) {
    /**
     * Schedules a one-time reminder worker with a delay based on the payment due date.
     */
    fun schedulePaymentReminder(cardId: Long, paymentDueDay: Int) {
        val now = LocalDateTime.now()
        
        // Target time is 9:00 AM on the payment due day
        var targetTime = LocalDateTime.of(now.year, now.monthValue, 
            minOf(paymentDueDay, now.toLocalDate().lengthOfMonth()), 9, 0)
        
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusMonths(1)
        }

        // We want to trigger the check 3 days before the due date
        val reminderTriggerTime = targetTime.minusDays(3)
        
        val initialDelay = if (reminderTriggerTime.isBefore(now)) {
            // If the 3-day window has already started, run almost immediately (1 minute delay)
            Duration.ofMinutes(1)
        } else {
            Duration.between(now, reminderTriggerTime)
        }

        val workRequest = OneTimeWorkRequestBuilder<PaymentReminderWorker>()
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .setInputData(workDataOf("CARD_ID" to cardId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "payment_reminder_$cardId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
