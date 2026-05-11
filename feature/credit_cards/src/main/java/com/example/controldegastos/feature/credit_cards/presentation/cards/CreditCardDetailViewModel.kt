package com.example.controldegastos.feature.credit_cards.presentation.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldegastos.core.domain.model.CreditCard
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.core.domain.repository.TransactionRepository
import com.example.controldegastos.feature.credit_cards.domain.usecase.CalculateMonthlyCardDebtUseCase
import com.example.controldegastos.feature.credit_cards.domain.usecase.PayCreditCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

sealed class CreditCardDetailUiState {
    object Loading : CreditCardDetailUiState()
    data class Success(
        val card: CreditCard,
        val monthlyDue: BigDecimal,
        val msiTransactions: List<Transaction>,
        val regularTransactions: List<Transaction>,
        val isProcessingPayment: Boolean = false,
        val paymentSuccess: Boolean = false
    ) : CreditCardDetailUiState()
    data class Error(val message: String) : CreditCardDetailUiState()
}

@HiltViewModel
class CreditCardDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val cardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val calculateMonthlyCardDebtUseCase: CalculateMonthlyCardDebtUseCase,
    private val payCreditCardUseCase: PayCreditCardUseCase
) : ViewModel() {

    private val cardId: Long = checkNotNull(savedStateHandle["cardId"])

    private val _state = MutableStateFlow<CreditCardDetailUiState>(CreditCardDetailUiState.Loading)
    val state: StateFlow<CreditCardDetailUiState> = _state.asStateFlow()

    val accounts = accountRepository.observeAll()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _state.value = CreditCardDetailUiState.Loading
            try {
                cardRepository.observeAll().collect { cards ->
                    val card = cards.find { it.id == cardId }
                    if (card != null) {
                        val monthlyDue = calculateMonthlyCardDebtUseCase(card)
                        
                        val allTransactions = transactionRepository
                            .getTransactionsBySource(card.id, "CREDIT_CARD")
                            .first()
                            .filter { it.type == TransactionType.EXPENSE }
                            
                        val msiTxs = allTransactions.filter { 
                            val m = it.installmentMonths
                            m != null && m > 0 
                        }
                        val regularTxs = allTransactions.filter { 
                            val m = it.installmentMonths
                            m == null || m == 0 
                        }

                        val current = _state.value
                        _state.value = CreditCardDetailUiState.Success(
                            card = card,
                            monthlyDue = monthlyDue,
                            msiTransactions = msiTxs,
                            regularTransactions = regularTxs,
                            isProcessingPayment = (current as? CreditCardDetailUiState.Success)?.isProcessingPayment ?: false,
                            paymentSuccess = (current as? CreditCardDetailUiState.Success)?.paymentSuccess ?: false
                        )
                    } else {
                        _state.value = CreditCardDetailUiState.Error("Tarjeta no encontrada")
                    }
                }
            } catch (e: Exception) {
                _state.value = CreditCardDetailUiState.Error(e.message ?: "Error al cargar detalles")
            }
        }
    }

    fun onEvent(event: CreditCardEvent) {
        when (event) {
            is CreditCardEvent.PayCard -> {
                viewModelScope.launch {
                    val current = _state.value as? CreditCardDetailUiState.Success ?: return@launch
                    _state.value = current.copy(isProcessingPayment = true, paymentSuccess = false)
                    payCreditCardUseCase(
                        cardId = event.cardId,
                        sourceAccountId = event.sourceAccountId,
                        amount = event.amount,
                        paymentType = event.paymentType
                    ).onSuccess {
                        _state.value = current.copy(isProcessingPayment = false, paymentSuccess = true)
                    }.onFailure {
                        _state.value = CreditCardDetailUiState.Error(it.message ?: "Error al pagar")
                    }
                }
            }
            else -> {}
        }
    }
}
