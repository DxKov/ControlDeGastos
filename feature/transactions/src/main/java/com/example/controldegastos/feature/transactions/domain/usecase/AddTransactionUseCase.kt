package com.example.controldegastos.feature.transactions.domain.usecase

import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.feature.transactions.domain.model.SourceType
import com.example.controldegastos.feature.transactions.domain.model.Transaction
import com.example.controldegastos.feature.transactions.domain.model.TransactionType
import com.example.controldegastos.feature.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase to add a transaction and update the corresponding balance or debt.
 * Coordinates between transactions, bank accounts, and credit cards.
 */
class AddTransactionUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val cardRepo: CreditCardRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> = runCatching {
        // Business logic to affect balances based on source type
        when (transaction.sourceType) {
            SourceType.ACCOUNT -> {
                val account = accountRepo.getAccountById(transaction.sourceId)
                    ?: throw Exception("Account with ID ${transaction.sourceId} not found")
                
                val newBalance = when (transaction.type) {
                    TransactionType.INCOME -> account.balance.add(transaction.amount)
                    TransactionType.EXPENSE -> account.balance.subtract(transaction.amount)
                    TransactionType.TRANSFER -> account.balance.subtract(transaction.amount)
                }
                accountRepo.updateAccount(account.copy(balance = newBalance))

                // For TRANSFER, also credit the destination account
                val destId = transaction.destinationId
                if (transaction.type == TransactionType.TRANSFER && destId != null) {
                    val dest = accountRepo.getAccountById(destId)
                        ?: throw Exception("Destination account $destId not found")
                    accountRepo.updateAccount(dest.copy(balance = dest.balance.add(transaction.amount)))
                }
            }
            SourceType.CREDIT_CARD -> {
                val card = cardRepo.getCardById(transaction.sourceId)
                    ?: throw Exception("Credit card with ID ${transaction.sourceId} not found")
                
                // 1. Update card's used balance (debt)
                val newUsedBalance = when (transaction.type) {
                    TransactionType.EXPENSE -> card.usedBalance.add(transaction.amount)
                    TransactionType.INCOME -> card.usedBalance.subtract(transaction.amount)
                    TransactionType.TRANSFER -> card.usedBalance.add(transaction.amount)
                }
                cardRepo.updateCard(card.copy(usedBalance = newUsedBalance))
                
                // 2. Get or auto-create current billing cycle
                val existingCycle = cardRepo.getCurrentCycle(transaction.sourceId).first()
                val cycle = existingCycle ?: run {
                    // Auto-create an open billing cycle based on card's cut-off day
                    val today = java.time.LocalDate.now()
                    val cutoffDay = card.cutoffDay.coerceIn(1, 28)
                    val startDate = if (today.dayOfMonth <= cutoffDay) {
                        today.withDayOfMonth(1)
                    } else {
                        today.withDayOfMonth(1)
                    }
                    val endDate = today.withDayOfMonth(
                        minOf(cutoffDay, today.lengthOfMonth())
                    ).let {
                        if (it.isBefore(today)) it.plusMonths(1) else it
                    }
                    val newCycle = com.example.controldegastos.core.domain.model.BillingCycle(
                        id = 0,
                        cardId = transaction.sourceId,
                        startDate = startDate,
                        endDate = endDate,
                        totalDebt = java.math.BigDecimal.ZERO,
                        status = com.example.controldegastos.core.domain.model.BillingCycleStatus.OPEN
                    )
                    cardRepo.saveBillingCycle(newCycle).getOrThrow()
                    // Re-fetch after creation to get the DB-assigned id
                    cardRepo.getCurrentCycle(transaction.sourceId).first()
                        ?: newCycle // Fallback to the in-memory object if re-fetch is null
                }

                val newTotalDebt = when (transaction.type) {
                    TransactionType.EXPENSE -> cycle.totalDebt.add(transaction.amount)
                    TransactionType.INCOME -> cycle.totalDebt.subtract(transaction.amount)
                    TransactionType.TRANSFER -> cycle.totalDebt.add(transaction.amount)
                }
                cardRepo.updateBillingCycle(cycle.copy(totalDebt = newTotalDebt))
            }
        }
        
        // 3. Persist the transaction movement
        transactionRepo.addTransaction(transaction)
    }.map { Unit }
}
