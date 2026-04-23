package com.example.controldegastos.feature.accounts.presentation.accounts

import com.example.controldegastos.feature.accounts.domain.model.Account
import java.math.BigDecimal

sealed class AccountUiState {
    object Loading : AccountUiState()
    data class Success(
        val accounts: List<Account>,
        val total: BigDecimal
    ) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}
