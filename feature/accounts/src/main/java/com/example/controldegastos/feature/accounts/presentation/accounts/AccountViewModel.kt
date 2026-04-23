package com.example.controldegastos.feature.accounts.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldegastos.feature.accounts.domain.usecase.DeleteAccountUseCase
import com.example.controldegastos.feature.accounts.domain.usecase.GetAccountsUseCase
import com.example.controldegastos.feature.accounts.domain.usecase.GetTotalBalanceUseCase
import com.example.controldegastos.feature.accounts.domain.usecase.SaveAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val getTotalBalanceUseCase: GetTotalBalanceUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        onEvent(AccountEvent.LoadAccounts)
    }

    fun onEvent(event: AccountEvent) {
        viewModelScope.launch {
            when (event) {
                is AccountEvent.LoadAccounts -> loadAccounts()
                is AccountEvent.SaveAccount -> saveAccount(event.account)
                is AccountEvent.DeleteAccount -> deleteAccount(event.id)
                is AccountEvent.ToggleIncludeInTotal -> toggleIncludeInTotal(event.account)
            }
        }
    }

    private suspend fun toggleIncludeInTotal(account: com.example.controldegastos.feature.accounts.domain.model.Account) {
        val updated = account.copy(includeInTotal = !account.includeInTotal)
        saveAccountUseCase(updated).onFailure {
            _state.value = AccountUiState.Error(it.message ?: "Error al actualizar la cuenta")
        }
    }

    private suspend fun loadAccounts() {
        getAccountsUseCase().onSuccess { flow ->
            flow.collectLatest { accounts ->
                getTotalBalanceUseCase().onSuccess { total ->
                    _state.value = AccountUiState.Success(accounts, total)
                }.onFailure {
                    _state.value = AccountUiState.Error(it.message ?: "Error calculating total balance")
                }
            }
        }.onFailure {
            _state.value = AccountUiState.Error(it.message ?: "Error loading accounts")
        }
    }

    private suspend fun saveAccount(account: com.example.controldegastos.feature.accounts.domain.model.Account) {
        saveAccountUseCase(account).onFailure {
            _state.value = AccountUiState.Error(it.message ?: "Error saving account")
        }
    }

    private suspend fun deleteAccount(id: Long) {
        val current = (_state.value as? AccountUiState.Success)?.accounts ?: return
        val toDelete = current.find { it.id == id } ?: return
        deleteAccountUseCase(toDelete).onFailure {
            _state.value = AccountUiState.Error(it.message ?: "Error al eliminar la cuenta")
        }
    }
}
