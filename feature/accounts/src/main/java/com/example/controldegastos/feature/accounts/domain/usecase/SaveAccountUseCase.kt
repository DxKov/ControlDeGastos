package com.example.controldegastos.feature.accounts.domain.usecase

import com.example.controldegastos.core.domain.usecase.UseCase
import com.example.controldegastos.feature.accounts.domain.model.Account
import com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * UseCase to save or update an account.
 * Note: Assumes UseCase<P, R> exists in :core-domain
 */
class SaveAccountUseCase @Inject constructor(
    private val repo: AccountRepository
) : UseCase<Account, Unit>() {
    
    override suspend fun execute(params: Account): Result<Unit> = runCatching {
        if (params.id == 0L) {
            repo.insertAccount(params)
        } else {
            repo.updateAccount(params)
        }
    }
}
