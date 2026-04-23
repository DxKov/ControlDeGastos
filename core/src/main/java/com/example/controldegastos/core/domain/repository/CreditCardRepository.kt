package com.example.controldegastos.core.domain.repository

import com.example.controldegastos.core.domain.model.BillingCycle
import com.example.controldegastos.core.domain.model.CardPayment
import com.example.controldegastos.core.domain.model.CreditCard
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun observeAll(): Flow<List<CreditCard>>
    
    fun getCurrentCycle(cardId: Long): Flow<BillingCycle?>
    
    suspend fun getCardById(cardId: Long): CreditCard?
    
    suspend fun insertCard(card: CreditCard): Result<Unit>
    
    suspend fun deleteCard(card: CreditCard): Result<Unit>
    
    suspend fun updateCard(card: CreditCard): Result<Unit>
    
    suspend fun registerPayment(payment: CardPayment): Result<Unit>
    
    suspend fun saveBillingCycle(cycle: BillingCycle): Result<Unit>
    
    suspend fun updateBillingCycle(cycle: BillingCycle): Result<Unit>
}
