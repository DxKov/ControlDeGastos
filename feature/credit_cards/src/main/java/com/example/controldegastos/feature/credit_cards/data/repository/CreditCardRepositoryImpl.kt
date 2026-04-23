package com.example.controldegastos.feature.credit_cards.data.repository

import com.example.controldegastos.core.domain.model.BillingCycle
import com.example.controldegastos.core.domain.model.CardPayment
import com.example.controldegastos.core.domain.model.CreditCard
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.core.data.local.dao.CreditCardDao
import com.example.controldegastos.feature.credit_cards.data.mapper.toDomain
import com.example.controldegastos.feature.credit_cards.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val dao: CreditCardDao
) : CreditCardRepository {

    override fun observeAll(): Flow<List<CreditCard>> {
        return dao.observeAllCards().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getCardById(cardId: Long): CreditCard? {
        return dao.getCardById(cardId)?.toDomain()
    }

    override suspend fun insertCard(card: CreditCard): Result<Unit> = runCatching {
        dao.insertCard(card.toEntity())
    }

    override suspend fun deleteCard(card: CreditCard): Result<Unit> = runCatching {
        dao.deleteCard(card.toEntity())
    }

    override suspend fun updateCard(card: CreditCard): Result<Unit> = runCatching {
        dao.insertCard(card.toEntity()) // Using insert with REPLACE for update
    }

    override fun getCurrentCycle(cardId: Long): Flow<BillingCycle?> {
        return dao.observeCurrentCycle(cardId).map { it?.toDomain() }
    }

    override suspend fun registerPayment(payment: CardPayment): Result<Unit> = runCatching {
        val card = dao.getCardById(payment.cardId) ?: throw Exception("Card not found")
        val cycle = dao.getCurrentCycle(payment.cardId) ?: throw Exception("No active billing cycle")
        
        // Update card's used balance
        val newUsedBalance = card.toDomain().usedBalance.subtract(payment.amount)
        dao.insertCard(card.copy(usedBalance = newUsedBalance.toString()))
        
        // Update cycle's total debt
        val newTotalDebt = cycle.toDomain().totalDebt.subtract(payment.amount)
        dao.updateCycle(cycle.copy(totalDebt = newTotalDebt.toString()))
    }

    override suspend fun saveBillingCycle(cycle: BillingCycle): Result<Unit> = runCatching {
        dao.insertCycle(cycle.toEntity())
    }

    override suspend fun updateBillingCycle(cycle: BillingCycle): Result<Unit> = runCatching {
        dao.insertCycle(cycle.toEntity()) // REPLACE strategy handles both insert and update
    }
}

