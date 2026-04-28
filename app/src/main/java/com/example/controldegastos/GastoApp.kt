package com.example.controldegastos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for GastoApp.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
import com.example.controldegastos.feature.reminders.ReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class GastoApp : Application() {
    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        reminderScheduler.scheduleDailyExpenseReminder()
    }
}
