package com.example.controldegastos.feature.analytics.domain.model

import java.math.BigDecimal

data class MonthlyReport(
    val totalIncome: BigDecimal,
    val cashExpenses: BigDecimal,
    val creditExpenses: BigDecimal,
    val totalCardDebt: BigDecimal,
    val savingsRate: Float,
    val expensesByCategory: Map<Long, BigDecimal>
)
