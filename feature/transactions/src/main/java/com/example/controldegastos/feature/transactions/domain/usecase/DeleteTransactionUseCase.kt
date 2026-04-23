package com.example.controldegastos.feature.transactions.domain.usecase

import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.feature.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase to delete a transaction and reverse its effect on balances/debt.
 * - INCOME from ACCOUNT → balance was increased → subtract amount back
 * - EXPENSE from ACCOUNT → balance was decreased → add amount back
 * - TRANSFER from ACCOUNT → balance was decreased from source → add back; destination was increased → subtract back
 * - EXPENSE from CREDIT_CARD → usedBalance was increased → subtract back
 */
class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val cardRepo: CreditCardRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> = runCatching {
        // 1. Reverse the balance effect
        when (transaction.sourceType) {
            SourceType.ACCOUNT -> {
                val account = accountRepo.getAccountById(transaction.sourceId)
                if (account != null) {
                    val revertedBalance = when (transaction.type) {
                        TransactionType.INCOME -> account.balance.subtract(transaction.amount)
                        TransactionType.EXPENSE -> account.balance.add(transaction.amount)
                        TransactionType.TRANSFER -> account.balance.add(transaction.amount)
                    }
                    accountRepo.updateAccount(account.copy(balance = revertedBalance))
                }

                // For transfers, also revert the destination account
                if (transaction.type == TransactionType.TRANSFER) {
                    val destId = transaction.destinationId
                    if (destId != null) {
                        val dest = accountRepo.getAccountById(destId)
                        if (dest != null) {
                            accountRepo.updateAccount(
                                dest.copy(balance = dest.balance.subtract(transaction.amount))
                            )
                        }
                    }
                }
            }
            SourceType.CREDIT_CARD -> {
                val card = cardRepo.getCardById(transaction.sourceId)
                if (card != null) {
                    val revertedUsed = when (transaction.type) {
                        TransactionType.EXPENSE -> card.usedBalance.subtract(transaction.amount)
                        TransactionType.INCOME -> card.usedBalance.add(transaction.amount)
                        TransactionType.TRANSFER -> card.usedBalance.subtract(transaction.amount)
                    }
                    cardRepo.updateCard(card.copy(usedBalance = revertedUsed.coerceAtLeast(java.math.BigDecimal.ZERO)))

                    // Also revert the billing cycle debt
                    val cycle = cardRepo.getCurrentCycle(transaction.sourceId).first()
                    if (cycle != null) {
                        val revertedDebt = when (transaction.type) {
                            TransactionType.EXPENSE -> cycle.totalDebt.subtract(transaction.amount)
                            TransactionType.INCOME -> cycle.totalDebt.add(transaction.amount)
                            TransactionType.TRANSFER -> cycle.totalDebt.subtract(transaction.amount)
                        }
                        cardRepo.updateBillingCycle(cycle.copy(totalDebt = revertedDebt.coerceAtLeast(java.math.BigDecimal.ZERO)))
                    }
                }
            }
        }

        // 2. Delete the transaction record
        transactionRepo.deleteTransaction(transaction)
    }.map { Unit }

    private fun java.math.BigDecimal.coerceAtLeast(min: java.math.BigDecimal): java.math.BigDecimal =
        if (this < min) min else this
}
