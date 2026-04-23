package com.example.controldegastos.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class CardPayment(
    val id: Long = 0,
    val cardId: Long,
    val cycleId: Long,
    val amount: BigDecimal,
    val type: PaymentType,
    val date: LocalDate
)

enum class PaymentType {
    MINIMUM,
    FULL,
    PARTIAL
}
