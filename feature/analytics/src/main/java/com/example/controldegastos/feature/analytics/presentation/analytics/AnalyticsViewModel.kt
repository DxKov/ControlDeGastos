package com.example.controldegastos.feature.analytics.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.feature.analytics.domain.model.MonthlyReport
import com.example.controldegastos.feature.analytics.domain.model.SavingsGoal
import com.example.controldegastos.feature.analytics.domain.usecase.DeleteTransactionProxy
import com.example.controldegastos.feature.analytics.domain.usecase.GetMonthlyReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class AnalyticsPeriod(val label: String) {
    DAILY("Hoy"),
    WEEKLY("Esta semana"),
    MONTHLY("Este mes")
}

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(
        val report: MonthlyReport,
        val transactions: List<Transaction> = emptyList(),
        val goals: List<SavingsGoal> = emptyList(),
        val period: AnalyticsPeriod = AnalyticsPeriod.MONTHLY
    ) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getMonthlyReportUseCase: GetMonthlyReportUseCase,
    private val deleteTransactionProxy: DeleteTransactionProxy
) : ViewModel() {

    private val _state = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    private val _period = MutableStateFlow(AnalyticsPeriod.MONTHLY)
    val period: StateFlow<AnalyticsPeriod> = _period.asStateFlow()

    init {
        loadForPeriod(AnalyticsPeriod.MONTHLY)
    }

    fun selectPeriod(p: AnalyticsPeriod) {
        _period.value = p
        loadForPeriod(p)
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionProxy.delete(transaction)
            // State will update automatically via the Flow re-emission
        }
    }

    private fun loadForPeriod(p: AnalyticsPeriod) {
        val today = LocalDate.now()
        val (start, end) = when (p) {
            AnalyticsPeriod.DAILY -> today to today
            AnalyticsPeriod.WEEKLY -> {
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                weekStart to today
            }
            AnalyticsPeriod.MONTHLY -> {
                today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
            }
        }

        viewModelScope.launch {
            _state.value = AnalyticsUiState.Loading
            getMonthlyReportUseCase(start, end)
                .catch { e ->
                    _state.value = AnalyticsUiState.Error(e.message ?: "Error al cargar el reporte")
                }
                .collect { periodReport ->
                    _state.value = AnalyticsUiState.Success(
                        report = periodReport.report,
                        transactions = periodReport.transactions,
                        period = p
                    )
                }
        }
    }
}
