package com.example.controldegastos.feature.credit_cards.presentation.cards

import com.example.controldegastos.feature.credit_cards.domain.model.CardPayment
import com.example.controldegastos.feature.credit_cards.domain.model.CreditCard
import java.math.BigDecimal

sealed class CreditCardUiState {
    object Loading : CreditCardUiState()
    data class Success(
        val cards: List<CreditCard>,
        val totalDebt: BigDecimal,
        /** Monthly installment payment per card: amount/months for MSI, full amount otherwise. */
        val monthlyDebtPerCard: Map<Long, BigDecimal> = emptyMap(),
        val paymentSuccess: Boolean = false,
        val isProcessingPayment: Boolean = false
    ) : CreditCardUiState()
    data class Error(val message: String) : CreditCardUiState()
}

sealed class CreditCardEvent {
    object Load : CreditCardEvent()
    data class AddCard(val card: com.example.controldegastos.core.domain.model.CreditCard) : CreditCardEvent()
    data class DeleteCard(val card: com.example.controldegastos.core.domain.model.CreditCard) : CreditCardEvent()
    data class CloseCycle(val cardId: Long) : CreditCardEvent()
    data class RegisterPayment(val payment: CardPayment) : CreditCardEvent()
    data class PayCard(
        val cardId: Long,
        val sourceAccountId: Long,
        val amount: java.math.BigDecimal,
        val paymentType: com.example.controldegastos.core.domain.model.PaymentType =
            com.example.controldegastos.core.domain.model.PaymentType.PARTIAL
    ) : CreditCardEvent()
    object ClearPaymentSuccess : CreditCardEvent()
}
