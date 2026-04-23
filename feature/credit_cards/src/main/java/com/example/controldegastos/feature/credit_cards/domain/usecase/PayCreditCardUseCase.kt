package com.example.controldegastos.feature.credit_cards.domain.usecase

import com.example.controldegastos.core.domain.model.CardPayment
import com.example.controldegastos.core.domain.model.PaymentType
import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase to pay a credit card from a bank account.
 *
 * Effects:
 *  1. Deducts [amount] from the source bank account balance
 *  2. Reduces the credit card's [usedBalance] (debt)
 *  3. Updates the active billing cycle's [totalDebt]
 *  4. Records a Transaction so it appears in Analytics
 *
 * Validates that:
 *  - The account exists and has enough balance
 *  - The amount is positive
 *  - The amount does not exceed the current card debt
 */
class PayCreditCardUseCase @Inject constructor(
    private val cardRepo: CreditCardRepository,
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository
) {
    suspend operator fun invoke(
        cardId: Long,
        sourceAccountId: Long,
        amount: BigDecimal,
        paymentType: PaymentType = PaymentType.PARTIAL,
        date: LocalDate = LocalDate.now()
    ): Result<Unit> = runCatching {

        require(amount > BigDecimal.ZERO) { "El monto del pago debe ser mayor a cero" }

        val card = cardRepo.getCardById(cardId)
            ?: throw Exception("Tarjeta no encontrada")

        val account = accountRepo.getAccountById(sourceAccountId)
            ?: throw Exception("Cuenta de origen no encontrada")

        require(account.balance >= amount) {
            "Saldo insuficiente en la cuenta. Disponible: ${account.balance}"
        }

        val finalPaymentAmount = amount.min(card.usedBalance) // Never reduce debt below zero

        // 1. Debit the bank account
        accountRepo.updateAccount(
            account.copy(balance = account.balance.subtract(amount))
        )

        // 2. Reduce card debt (usedBalance)
        val newUsedBalance = (card.usedBalance.subtract(finalPaymentAmount)).max(BigDecimal.ZERO)
        cardRepo.updateCard(card.copy(usedBalance = newUsedBalance))

        // 3. Update billing cycle debt if active cycle exists
        val cycle = cardRepo.getCurrentCycle(cardId).first()
        if (cycle != null) {
            val newCycleDebt = (cycle.totalDebt.subtract(finalPaymentAmount)).max(BigDecimal.ZERO)
            cardRepo.updateBillingCycle(cycle.copy(totalDebt = newCycleDebt))
        }

        // 4. Register through the repository's existing payment mechanism (for audit trail)
        if (cycle != null) {
            cardRepo.registerPayment(
                CardPayment(
                    cardId = cardId,
                    cycleId = cycle.id,
                    amount = finalPaymentAmount,
                    type = paymentType,
                    date = date
                )
            )
        }

        // 5. Save a Transaction record so it appears in Analytics
        transactionRepo.addTransaction(
            Transaction(
                amount = amount,
                type = TransactionType.EXPENSE,       // Money leaving the account
                date = date,
                categoryId = 8L,                      // "Otros" category
                sourceType = SourceType.ACCOUNT,
                sourceId = sourceAccountId,
                note = "Pago tarjeta: ${card.name}"
            )
        )
    }.map { Unit }
}
