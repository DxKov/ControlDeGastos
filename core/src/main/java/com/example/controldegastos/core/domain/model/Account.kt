package com.example.controldegastos.core.domain.model

import java.math.BigDecimal

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: BigDecimal,
    val currency: String = "MXN",
    val initialBalance: BigDecimal,
    val color: Int,
    val icon: String,
    val createdAt: Long
)
