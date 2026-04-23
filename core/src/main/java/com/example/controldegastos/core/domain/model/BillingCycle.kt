package com.example.controldegastos.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class BillingCycle(
    val id: Long = 0,
    val cardId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDebt: BigDecimal,
    val status: BillingCycleStatus
)

enum class BillingCycleStatus {
    OPEN,
    CLOSED,
    PAID
}
