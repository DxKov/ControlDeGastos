package com.example.controldegastos.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class Transaction(
    val id: Long = 0,
    val amount: BigDecimal,
    val type: TransactionType,
    val date: LocalDate,
    val categoryId: Long,
    val sourceType: SourceType,
    val sourceId: Long,
    val destinationId: Long? = null, // For TRANSFER: the receiving account ID
    val cycleId: Long? = null,
    val note: String? = null,
    val isRecurring: Boolean = false
)
