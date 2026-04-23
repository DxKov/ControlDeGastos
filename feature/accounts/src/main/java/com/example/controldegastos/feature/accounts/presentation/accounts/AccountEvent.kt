package com.example.controldegastos.feature.accounts.presentation.accounts

import com.example.controldegastos.feature.accounts.domain.model.Account

sealed class AccountEvent {
    object LoadAccounts : AccountEvent()
    data class SaveAccount(val account: Account) : AccountEvent()
    data class DeleteAccount(val id: Long) : AccountEvent()
}
