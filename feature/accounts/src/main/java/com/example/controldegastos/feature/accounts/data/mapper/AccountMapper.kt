package com.example.controldegastos.feature.accounts.data.mapper

import com.example.controldegastos.core.data.local.entity.AccountEntity
import com.example.controldegastos.feature.accounts.domain.model.Account
import com.example.controldegastos.feature.accounts.domain.model.AccountType
import java.math.BigDecimal

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountType.valueOf(type),
        balance = BigDecimal(balance),
        currency = currency,
        initialBalance = BigDecimal(initialBalance),
        color = color,
        icon = icon,
        createdAt = createdAt,
        includeInTotal = includeInTotal
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type.name,
        balance = balance.toPlainString(),
        currency = currency,
        initialBalance = initialBalance.toPlainString(),
        color = color,
        icon = icon,
        createdAt = createdAt,
        includeInTotal = includeInTotal
    )
}

