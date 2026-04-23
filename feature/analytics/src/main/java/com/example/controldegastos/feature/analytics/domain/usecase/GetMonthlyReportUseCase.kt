package com.example.controldegastos.feature.analytics.domain.usecase

import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.core.domain.repository.TransactionRepository
import com.example.controldegastos.feature.analytics.domain.model.MonthlyReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject

data class PeriodReport(
    val report: MonthlyReport,
    val transactions: List<Transaction>
)

/**
 * Returns a reactive PeriodReport for a given date range, including individual transactions.
 */
class GetMonthlyReportUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val cardRepo: CreditCardRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<PeriodReport> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        return invoke(startDate, endDate)
    }

    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<PeriodReport> {
        return combine(
            transactionRepo.getTransactionsByDate(startDate, endDate),
            cardRepo.observeAll()
        ) { transactions, cards ->

            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

            val cashExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE && it.sourceType == SourceType.ACCOUNT }
                .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

            val creditExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE && it.sourceType == SourceType.CREDIT_CARD }
                .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

            val expensesByCategory = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .mapValues { entry -> entry.value.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) } }

            val totalCardDebt = cards.fold(BigDecimal.ZERO) { acc, card ->
                acc.add(card.usedBalance)
            }

            val totalExpenses = cashExpenses.add(creditExpenses)
            val savingsRate = if (totalIncome > BigDecimal.ZERO) {
                totalIncome.subtract(totalExpenses)
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .toFloat()
            } else 0f

            PeriodReport(
                report = MonthlyReport(
                    totalIncome = totalIncome,
                    cashExpenses = cashExpenses,
                    creditExpenses = creditExpenses,
                    totalCardDebt = totalCardDebt,
                    savingsRate = savingsRate,
                    expensesByCategory = expensesByCategory
                ),
                transactions = transactions.sortedByDescending { it.date }
            )
        }
    }
}
