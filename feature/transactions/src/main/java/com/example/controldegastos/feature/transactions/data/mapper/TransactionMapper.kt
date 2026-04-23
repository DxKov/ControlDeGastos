package com.example.controldegastos.feature.transactions.data.mapper

import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.core.data.local.entity.TransactionEntity
import com.example.controldegastos.feature.transactions.domain.model.Transaction
import java.math.BigDecimal
import java.time.LocalDate

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = BigDecimal(amount),
        type = TransactionType.valueOf(type),
        date = LocalDate.parse(date),
        categoryId = categoryId,
        sourceType = SourceType.valueOf(sourceType),
        sourceId = sourceId,
        destinationId = destinationId,
        cycleId = cycleId,
        note = note,
        isRecurring = isRecurring
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount.toString(),
        type = type.name,
        date = date.toString(),
        categoryId = categoryId,
        sourceType = sourceType.name,
        sourceId = sourceId,
        destinationId = destinationId,
        cycleId = cycleId,
        note = note,
        isRecurring = isRecurring
    )
}

