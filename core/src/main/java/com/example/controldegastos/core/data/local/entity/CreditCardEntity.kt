package com.example.controldegastos.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bank: String,
    val last4Digits: String,
    val creditLimit: String,
    val usedBalance: String,
    val cutoffDay: Int,
    val paymentDueDay: Int,
    val color: Int,
    val isActive: Boolean
)
