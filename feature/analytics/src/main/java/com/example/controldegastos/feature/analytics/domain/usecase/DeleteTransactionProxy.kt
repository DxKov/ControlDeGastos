package com.example.controldegastos.feature.analytics.domain.usecase

import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.core.domain.repository.TransactionRepository
import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Proxy class in the analytics module that handles transaction deletion with balance reversal.
 * Mirrors DeleteTransactionUseCase to avoid cross-module dependency on feature:transactions.
 */
class DeleteTransactionProxy @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val cardRepo: CreditCardRepository
) {
    suspend fun delete(transaction: Transaction): Result<Unit> = runCatching {
        // Reverse balance effects
        when (transaction.sourceType) {
            SourceType.ACCOUNT -> {
                val account = accountRepo.getAccountById(transaction.sourceId)
                if (account != null) {
                    val reverted = when (transaction.type) {
                        TransactionType.INCOME -> account.balance.subtract(transaction.amount)
                        TransactionType.EXPENSE -> account.balance.add(transaction.amount)
                        TransactionType.TRANSFER -> account.balance.add(transaction.amount)
                    }
                    accountRepo.updateAccount(account.copy(balance = reverted))
                }
                val destId = transaction.destinationId
                if (transaction.type == TransactionType.TRANSFER && destId != null) {
                    val dest = accountRepo.getAccountById(destId)
                    if (dest != null) {
                        accountRepo.updateAccount(
                            dest.copy(balance = dest.balance.subtract(transaction.amount))
                        )
                    }
                }
            }
            SourceType.CREDIT_CARD -> {
                val card = cardRepo.getCardById(transaction.sourceId)
                if (card != null) {
                    val reverted = when (transaction.type) {
                        TransactionType.EXPENSE -> card.usedBalance.subtract(transaction.amount)
                        TransactionType.INCOME -> card.usedBalance.add(transaction.amount)
                        TransactionType.TRANSFER -> card.usedBalance.subtract(transaction.amount)
                    }.let { if (it < java.math.BigDecimal.ZERO) java.math.BigDecimal.ZERO else it }
                    cardRepo.updateCard(card.copy(usedBalance = reverted))
                }
            }
        }
        transactionRepo.deleteTransaction(transaction)
    }.map { Unit }
}
