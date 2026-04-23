package com.example.controldegastos.feature.credit_cards.data.mapper

import com.example.controldegastos.core.domain.model.BillingCycle
import com.example.controldegastos.core.domain.model.BillingCycleStatus
import com.example.controldegastos.core.domain.model.CreditCard
import com.example.controldegastos.core.data.local.entity.BillingCycleEntity
import com.example.controldegastos.core.data.local.entity.CreditCardEntity
import java.math.BigDecimal
import java.time.LocalDate

fun CreditCardEntity.toDomain(): CreditCard {
    return CreditCard(
        id = id,
        name = name,
        bank = bank,
        last4Digits = last4Digits,
        creditLimit = BigDecimal(creditLimit),
        usedBalance = BigDecimal(usedBalance),
        cutoffDay = cutoffDay,
        paymentDueDay = paymentDueDay,
        color = color,
        isActive = isActive
    )
}

fun CreditCard.toEntity(): CreditCardEntity {
    return CreditCardEntity(
        id = id,
        name = name,
        bank = bank,
        last4Digits = last4Digits,
        creditLimit = creditLimit.toString(),
        usedBalance = usedBalance.toString(),
        cutoffDay = cutoffDay,
        paymentDueDay = paymentDueDay,
        color = color,
        isActive = isActive
    )
}

fun BillingCycleEntity.toDomain(): BillingCycle {
    return BillingCycle(
        id = id,
        cardId = cardId,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        totalDebt = BigDecimal(totalDebt),
        status = BillingCycleStatus.valueOf(status)
    )
}

fun BillingCycle.toEntity(): BillingCycleEntity {
    return BillingCycleEntity(
        id = id,
        cardId = cardId,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        totalDebt = totalDebt.toString(),
        status = status.name
    )
}

