package com.example.controldegastos.core.domain.repository

import com.example.controldegastos.core.domain.model.Account
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface AccountRepository {
    fun observeAll(): Flow<List<Account>>
    
    suspend fun getAccountById(id: Long): Account?
    
    suspend fun insertAccount(account: Account)
    
    suspend fun updateAccount(account: Account)
    
    suspend fun deleteAccount(account: Account)
    
    suspend fun getTotalBalance(): BigDecimal
}
