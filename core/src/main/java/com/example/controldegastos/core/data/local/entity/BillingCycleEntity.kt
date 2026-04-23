package com.example.controldegastos.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "billing_cycles")
data class BillingCycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val startDate: String,
    val endDate: String,
    val totalDebt: String,
    val status: String
)
