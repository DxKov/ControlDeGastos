package com.example.controldegastos.feature.transactions.data.repository

import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.repository.TransactionRepository
import com.example.controldegastos.core.data.local.dao.TransactionDao
import com.example.controldegastos.feature.transactions.data.mapper.toDomain
import com.example.controldegastos.feature.transactions.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> = runCatching {
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return dao.getTransactionById(id)?.toDomain()
    }

    override fun getTransactionsByDate(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange(startDate.toString(), endDate.toString())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun deleteTransaction(transaction: Transaction): Result<Unit> = runCatching {
        dao.deleteTransaction(transaction.toEntity())
    }
}

