package com.example.controldegastos.core.domain.model

import java.math.BigDecimal

data class CreditCard(
    val id: Long = 0,
    val name: String,
    val bank: String,
    val last4Digits: String,
    val creditLimit: BigDecimal,
    val usedBalance: BigDecimal,
    val cutoffDay: Int,
    val paymentDueDay: Int,
    val color: Int,
    val isActive: Boolean = true
) {
    val availableBalance: BigDecimal
        get() = creditLimit.subtract(usedBalance)
}
