package com.example.controldegastos.feature.transactions.presentation.transactions

import com.example.controldegastos.core.domain.model.Account
import com.example.controldegastos.core.domain.model.BillingCycle
import com.example.controldegastos.core.domain.model.CreditCard
import com.example.controldegastos.feature.transactions.domain.model.Transaction

data class TransactionUiState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val cards: List<CreditCard> = emptyList(),
    val selectedCycle: BillingCycle? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed class TransactionEvent {
    object LoadData : TransactionEvent()
    data class CardSelected(val cardId: Long) : TransactionEvent()
    data class SaveTransaction(val transaction: Transaction) : TransactionEvent()
}
