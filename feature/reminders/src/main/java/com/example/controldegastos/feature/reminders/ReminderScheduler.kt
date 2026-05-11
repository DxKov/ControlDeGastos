package com.example.controldegastos.feature.reminders

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Class responsible for scheduling background workers for payment alerts.
 */
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
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

    /**
     * Schedules a daily reminder at 8:00 PM.
     *
     * Uses CANCEL_AND_REENQUEUE so the initial delay is always recalculated
     * from the current time, preventing drift that could cause the notification
     * to arrive in the early morning instead of 8 PM.
     */
    fun scheduleDailyExpenseReminder() {
        val now = LocalDateTime.now()
        var targetTime = now.withHour(20).withMinute(0).withSecond(0).withNano(0)
        
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, targetTime)

        val workRequest = PeriodicWorkRequestBuilder<DailyExpenseReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        // CANCEL_AND_REENQUEUE ensures the initial delay is recalculated on every app start,
        // fixing the bug where the reminder arrived in the early morning (stale delay from
        // a previous KEEP policy that never re-evaluated the target time).
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_expense_reminder",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    /**
     * Schedules a daily worker at 9:00 AM that checks if today is the cutoff day
     * for any credit card and sends a notification with the amount owed.
     */
    fun scheduleCutoffReminder() {
        val now = LocalDateTime.now()
        var targetTime = now.withHour(9).withMinute(0).withSecond(0).withNano(0)

        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, targetTime)

        val workRequest = PeriodicWorkRequestBuilder<CutoffDayWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "cutoff_day_reminder",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }
}

