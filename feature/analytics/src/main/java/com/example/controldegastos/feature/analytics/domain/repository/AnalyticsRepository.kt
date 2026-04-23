package com.example.controldegastos.feature.analytics.domain.repository

import com.example.controldegastos.feature.analytics.domain.model.MonthlyReport
import com.example.controldegastos.feature.analytics.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    /**
     * Returns a reactive flow of the financial report for a given year and month.
     */
    fun getMonthlyReport(year: Int, month: Int): Flow<MonthlyReport>

    /**
     * Returns a reactive flow of all active savings goals.
     */
    fun getSavingsGoals(): Flow<List<SavingsGoal>>
}
