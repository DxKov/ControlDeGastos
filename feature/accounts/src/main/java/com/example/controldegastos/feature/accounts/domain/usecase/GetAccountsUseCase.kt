package com.example.controldegastos.feature.accounts.domain.usecase

import com.example.controldegastos.core.domain.usecase.NoParamsUseCase
import com.example.controldegastos.core.domain.usecase.UseCase
import com.example.controldegastos.feature.accounts.domain.model.Account
import com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to observe all accounts.
 * Note: Assumes UseCase<P, R> exists in :core-domain
 */
class GetAccountsUseCase @Inject constructor(
    private val repo: AccountRepository
) : NoParamsUseCase<Flow<List<Account>>>() {
    
    override suspend fun execute(params: Unit): Result<Flow<List<Account>>> = runCatching {
        repo.observeAll()
    }
}
