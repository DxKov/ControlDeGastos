package com.example.controldegastos.feature.accounts.data.repository

import com.example.controldegastos.core.data.local.dao.AccountDao
import com.example.controldegastos.feature.accounts.data.mapper.toDomain
import com.example.controldegastos.feature.accounts.data.mapper.toEntity
import com.example.controldegastos.feature.accounts.domain.model.Account
import com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao
) : AccountRepository {

    override fun observeAll(): Flow<List<Account>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAccountById(id: Long): Account? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun insertAccount(account: Account) {
        dao.insert(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        dao.update(account.toEntity())
    }

    override suspend fun deleteAccount(account: Account) {
        dao.delete(account.toEntity())
    }

    override suspend fun getTotalBalance(): BigDecimal {
        val accounts = dao.observeAll().first()
        return accounts.fold(BigDecimal.ZERO) { acc, entity ->
            acc.add(BigDecimal(entity.balance))
        }
    }
}

