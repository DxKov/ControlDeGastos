package com.example.controldegastos.feature.accounts.domain.usecase

import com.example.controldegastos.feature.accounts.domain.model.Account
import com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repo: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Unit> = runCatching {
        repo.deleteAccount(account)
    }
}
