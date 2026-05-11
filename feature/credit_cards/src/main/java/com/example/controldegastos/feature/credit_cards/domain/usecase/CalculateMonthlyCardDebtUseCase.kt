package com.example.controldegastos.feature.credit_cards.domain.usecase

import com.example.controldegastos.core.domain.model.CreditCard
import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject

class CalculateMonthlyCardDebtUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(card: CreditCard): BigDecimal {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        // Transactions paid this month (used to subtract from monthly due)
        val allTransactionsThisMonth = transactionRepository
            .getTransactionsByDate(monthStart, monthEnd)
            .first()

        // --- Step 1: Fixed monthly installment from purchase transactions ---
        val purchaseTxs = transactionRepository
            .getTransactionsBySource(card.id, "CREDIT_CARD")
            .first()
            .filter { it.type == TransactionType.EXPENSE }

        val fixedMonthlyInstallment = purchaseTxs.fold(BigDecimal.ZERO) { acc, tx ->
            val months = tx.installmentMonths
            val contribution = if (months != null && months > 0) {
                tx.amount.divide(BigDecimal(months), 2, RoundingMode.CEILING)
            } else {
                tx.amount
            }
            acc.add(contribution)
        }.takeIf { purchaseTxs.isNotEmpty() } ?: card.usedBalance

        // --- Step 2: Payments already made THIS MONTH to this card ---
        val paymentsThisMonth = allTransactionsThisMonth
            .filter { tx ->
                tx.type == TransactionType.EXPENSE &&
                tx.sourceType == SourceType.ACCOUNT &&
                // Checking categoryId == 9L covers new payments. The note check is a fallback for older payments.
                (tx.categoryId == 9L || tx.note?.contains("Pago tarjeta: ${card.name}") == true)
            }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

        // --- Step 3: What's still due this month ---
        return (fixedMonthlyInstallment - paymentsThisMonth)
            .max(BigDecimal.ZERO)
            .min(card.usedBalance)
    }
}
