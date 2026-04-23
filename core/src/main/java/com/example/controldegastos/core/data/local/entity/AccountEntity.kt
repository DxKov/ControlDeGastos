package com.example.controldegastos.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val balance: String,
    val initialBalance: String,
    val currency: String,
    val color: Int,
    val icon: String,
    val createdAt: Long
)
