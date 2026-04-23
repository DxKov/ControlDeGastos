package com.example.controldegastos.feature.analytics.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class SavingsGoal(
    val id: Long = 0,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val deadline: LocalDate
) {
    /**
     * Calculates the completion percentage of the goal.
     */
    val progressPercent: Float
        get() = if (targetAmount > BigDecimal.ZERO) {
            currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toFloat()
        } else 0f
}
