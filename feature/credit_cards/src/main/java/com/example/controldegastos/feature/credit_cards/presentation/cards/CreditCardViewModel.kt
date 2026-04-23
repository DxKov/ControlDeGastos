package com.example.controldegastos.feature.credit_cards.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.feature.credit_cards.domain.model.CardPayment
import com.example.controldegastos.feature.credit_cards.domain.repository.CreditCardRepository
import com.example.controldegastos.feature.credit_cards.domain.usecase.CloseBillingCycleUseCase
import com.example.controldegastos.feature.credit_cards.domain.usecase.PayCreditCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val repository: CreditCardRepository,
    private val accountRepository: AccountRepository,
    private val closeBillingCycleUseCase: CloseBillingCycleUseCase,
    private val payCreditCardUseCase: PayCreditCardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreditCardUiState>(CreditCardUiState.Loading)
    val state: StateFlow<CreditCardUiState> = _state.asStateFlow()

    /** Accounts available for payment source — exposed for the payment dialog */
    val accounts = accountRepository.observeAll()

    init {
        onEvent(CreditCardEvent.Load)
    }

    fun onEvent(event: CreditCardEvent) {
        viewModelScope.launch {
            when (event) {
                is CreditCardEvent.Load -> loadCards()
                is CreditCardEvent.AddCard -> addCard(event.card)
                is CreditCardEvent.DeleteCard -> deleteCard(event.card)
                is CreditCardEvent.CloseCycle -> closeCycle(event.cardId)
                is CreditCardEvent.RegisterPayment -> registerPayment(event.payment)
                is CreditCardEvent.PayCard -> payCard(
                    event.cardId, event.sourceAccountId, event.amount, event.paymentType
                )
                is CreditCardEvent.ClearPaymentSuccess -> clearPaymentSuccess()
            }
        }
    }

    private suspend fun addCard(card: com.example.controldegastos.core.domain.model.CreditCard) {
        repository.insertCard(card).onFailure {
            _state.value = CreditCardUiState.Error(it.message ?: "Error al crear la tarjeta")
        }
    }

    private suspend fun deleteCard(card: com.example.controldegastos.core.domain.model.CreditCard) {
        repository.deleteCard(card).onFailure {
            _state.value = CreditCardUiState.Error(it.message ?: "Error al eliminar la tarjeta")
        }
    }

    private suspend fun loadCards() {
        _state.value = CreditCardUiState.Loading
        repository.observeAll().collectLatest { cards ->
            val totalDebt = cards.fold(BigDecimal.ZERO) { acc, card ->
                acc.add(card.usedBalance)
            }
            val current = _state.value
            _state.value = CreditCardUiState.Success(
                cards = cards,
                totalDebt = totalDebt,
                paymentSuccess = (current as? CreditCardUiState.Success)?.paymentSuccess ?: false
            )
        }
    }

    private suspend fun closeCycle(cardId: Long) {
        val cycle = repository.getCurrentCycle(cardId).first()
        if (cycle != null) {
            closeBillingCycleUseCase(cycle).onFailure {
                _state.value = CreditCardUiState.Error(it.message ?: "Error al cerrar ciclo")
            }
        } else {
            _state.value = CreditCardUiState.Error("No hay ciclo de facturación activo")
        }
    }

    private suspend fun registerPayment(payment: CardPayment) {
        repository.registerPayment(payment).onFailure {
            _state.value = CreditCardUiState.Error(it.message ?: "Error al registrar pago")
        }
    }

    private suspend fun payCard(
        cardId: Long,
        sourceAccountId: Long,
        amount: BigDecimal,
        paymentType: com.example.controldegastos.core.domain.model.PaymentType
    ) {
        val current = _state.value as? CreditCardUiState.Success ?: return
        _state.value = current.copy(isProcessingPayment = true)

        payCreditCardUseCase(cardId, sourceAccountId, amount, paymentType)
            .onSuccess {
                // loadCards() will be triggered by the repository Flow collecting
                val updated = _state.value as? CreditCardUiState.Success
                _state.value = (updated ?: current).copy(
                    isProcessingPayment = false,
                    paymentSuccess = true
                )
            }
            .onFailure { e ->
                _state.value = current.copy(
                    isProcessingPayment = false,
                    paymentSuccess = false
                )
                _state.value = CreditCardUiState.Error(e.message ?: "Error al realizar el pago")
            }
    }

    private fun clearPaymentSuccess() {
        val current = _state.value as? CreditCardUiState.Success ?: return
        _state.value = current.copy(paymentSuccess = false)
    }
}
