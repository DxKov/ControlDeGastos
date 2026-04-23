package com.example.controldegastos.feature.accounts.domain.usecase

import com.example.controldegastos.core.domain.usecase.NoParamsUseCase
import com.example.controldegastos.core.domain.usecase.UseCase
import com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
import java.math.BigDecimal
import javax.inject.Inject

/**
 * UseCase to get the total balance of all accounts.
 * Note: Assumes UseCase<P, R> exists in :core-domain
 */
class GetTotalBalanceUseCase @Inject constructor(
    private val repo: AccountRepository
) : NoParamsUseCase<BigDecimal>() {
    
    override suspend fun execute(params: Unit): Result<BigDecimal> = runCatching {
        repo.getTotalBalance()
    }
}
