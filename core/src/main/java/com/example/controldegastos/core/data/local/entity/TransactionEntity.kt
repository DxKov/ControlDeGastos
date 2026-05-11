package com.example.controldegastos.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("categoryId"), Index("sourceId"), Index("cycleId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: String,
    val type: String,
    val date: String, // Stored as ISO-8601 String
    val categoryId: Long,
    val sourceType: String,
    val sourceId: Long,
    val destinationId: Long? = null, // For TRANSFER: the receiving account ID
    val cycleId: Long? = null,
    val note: String? = null,
    val isRecurring: Boolean = false,
    val installmentMonths: Int? = null  // null = pago normal; 3/6/9/12 = meses sin intereses
)
