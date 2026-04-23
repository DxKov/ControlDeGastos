package com.example.controldegastos.feature.transactions.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldegastos.core.domain.repository.AccountRepository
import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.feature.transactions.domain.model.SourceType
import com.example.controldegastos.feature.transactions.domain.model.Transaction
import com.example.controldegastos.feature.transactions.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val accountRepo: AccountRepository,
    private val cardRepo: CreditCardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionUiState())
    val state: StateFlow<TransactionUiState> = _state.asStateFlow()

    init {
        onEvent(TransactionEvent.LoadData)
    }

    fun onEvent(event: TransactionEvent) {
        viewModelScope.launch {
            when (event) {
                is TransactionEvent.LoadData -> loadData()
                is TransactionEvent.CardSelected -> loadCardCycle(event.cardId)
                is TransactionEvent.SaveTransaction -> saveTransaction(event.transaction)
            }
        }
    }

    private suspend fun loadData() {
        _state.update { it.copy(isLoading = true) }
        
        // Parallel observation of accounts and cards
        viewModelScope.launch {
            accountRepo.observeAll().collectLatest { accounts ->
                _state.update { it.copy(accounts = accounts) }
            }
        }
        
        viewModelScope.launch {
            cardRepo.observeAll().collectLatest { cards ->
                _state.update { it.copy(cards = cards, isLoading = false) }
            }
        }
    }

    private suspend fun loadCardCycle(cardId: Long) {
        // When a card is selected in the UI, we find its active billing cycle
        cardRepo.getCurrentCycle(cardId).collectLatest { cycle ->
            _state.update { it.copy(selectedCycle = cycle) }
        }
    }

    private suspend fun saveTransaction(transaction: Transaction) {
        // Validation: Amount must be positive
        if (transaction.amount <= BigDecimal.ZERO) {
            _state.update { it.copy(error = "El monto debe ser mayor a cero") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        
        // Enrich transaction with the active cycle ID if source is a credit card
        val transactionWithCycle = if (transaction.sourceType == SourceType.CREDIT_CARD) {
            transaction.copy(cycleId = _state.value.selectedCycle?.id)
        } else {
            transaction
        }

        addTransactionUseCase(transactionWithCycle).onSuccess {
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }.onFailure { e ->
            _state.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar la transacción") }
        }
    }
}
