package com.example.controldegastos.core.domain.repository

import com.example.controldegastos.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    suspend fun addTransaction(transaction: Transaction): Result<Unit>

    suspend fun getTransactionById(id: Long): Transaction?

    fun getTransactionsByDate(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    fun getTransactionsByCycleId(cycleId: Long): Flow<List<Transaction>>

    fun getTransactionsBySource(sourceId: Long, sourceType: String): Flow<List<Transaction>>

    suspend fun deleteTransaction(transaction: Transaction): Result<Unit>
}
