package com.example.controldegastos.feature.credit_cards.domain.usecase

import com.example.controldegastos.feature.credit_cards.domain.model.BillingCycle
import com.example.controldegastos.feature.credit_cards.domain.model.BillingCycleStatus
import com.example.controldegastos.feature.credit_cards.domain.repository.CreditCardRepository
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

/**
 * UseCase to close the current billing cycle and automatically open the next one.
 * Note: Assumes UseCase<P, R> exists in :core-domain
 */
class CloseBillingCycleUseCase @Inject constructor(
    private val repo: CreditCardRepository
) {
    suspend operator fun invoke(cycle: BillingCycle): Result<Unit> = runCatching {
        // 1. Close current cycle
        val closedCycle = cycle.copy(status = BillingCycleStatus.CLOSED)
        repo.updateBillingCycle(closedCycle)
        
        // 2. Retrieve card details to handle cutoff days correctly
        val card = repo.getCardById(cycle.cardId) ?: throw Exception("Card with ID ${cycle.cardId} not found")
        
        // 3. Calculate next cycle dates
        val nextStartDate = cycle.endDate.plusDays(1)
        
        // The end date should be roughly one month after the start, 
        // honoring the cutoff day but adjusting for shorter months.
        val nextCycleMonth = YearMonth.from(nextStartDate).plusMonths(1)
        val lastDayOfNextMonth = nextCycleMonth.lengthOfMonth()
        val nextCutoffDay = minOf(card.cutoffDay, lastDayOfNextMonth)
        
        val nextEndDate = nextCycleMonth.atDay(nextCutoffDay)
        
        val nextCycle = BillingCycle(
            cardId = cycle.cardId,
            startDate = nextStartDate,
            endDate = nextEndDate,
            totalDebt = BigDecimal.ZERO,
            status = BillingCycleStatus.OPEN
        )
        
        // 4. Save the new open cycle
        repo.saveBillingCycle(nextCycle)
    }.map { Unit }
}
