package com.example.controldegastos.feature.analytics.presentation.analytics.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controldegastos.core.domain.model.SourceType
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.core.domain.model.TransactionType
import com.example.controldegastos.feature.analytics.domain.model.MonthlyReport
import com.example.controldegastos.feature.analytics.domain.model.SavingsGoal
import com.example.controldegastos.feature.analytics.presentation.analytics.AnalyticsPeriod
import com.example.controldegastos.feature.analytics.presentation.analytics.AnalyticsUiState
import com.example.controldegastos.feature.analytics.presentation.analytics.AnalyticsViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

private val BgColor = Color(0xFF0F0F12)
private val CardBg = Color(0xFF1A1A24)
private val AccentPurple = Color(0xFF6C5CE7)
private val AccentBlue = Color(0xFF00CEC9)
private val AccentCoral = Color(0xFFE17055)

private val categoryNames = mapOf(
    1L to "Comida", 2L to "Transporte", 3L to "Ocio", 4L to "Servicios",
    5L to "Salud", 6L to "Educación", 7L to "Compras", 8L to "Otros"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    report: MonthlyReport? = null,
    goals: List<SavingsGoal> = emptyList(),
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentPeriod by viewModel.period.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Analíticas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(padding)
        ) {
            // Period selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsPeriod.entries.forEach { period ->
                    val isSelected = currentPeriod == period
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectPeriod(period) },
                        label = {
                            Text(
                                period.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPurple,
                            containerColor = Color.White.copy(alpha = 0.06f),
                            selectedLabelColor = Color.White,
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = isSelected,
                            selectedBorderColor = AccentPurple,
                            borderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            when (val uiState = state) {
                is AnalyticsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
                is AnalyticsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(uiState.message, color = Color.Red)
                        }
                    }
                }
                is AnalyticsUiState.Success -> {
                    val monthlyReport = uiState.report
                    val totalExpenses = monthlyReport.cashExpenses.add(monthlyReport.creditExpenses)
                    val savings = monthlyReport.totalIncome.subtract(totalExpenses)
                    val hasData = monthlyReport.totalIncome > BigDecimal.ZERO || totalExpenses > BigDecimal.ZERO

                    if (!hasData && uiState.transactions.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📊", fontSize = 56.sp)
                                Text(
                                    when (uiState.period) {
                                        AnalyticsPeriod.DAILY -> "Sin movimientos hoy"
                                        AnalyticsPeriod.WEEKLY -> "Sin movimientos esta semana"
                                        AnalyticsPeriod.MONTHLY -> "Sin movimientos este mes"
                                    },
                                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Registra ingresos y gastos para ver tus analíticas aquí.",
                                    color = Color.Gray, fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Summary hero card
                            if (hasData) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(listOf(AccentPurple, Color(0xFF9B59B6))),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                when (uiState.period) {
                                                    AnalyticsPeriod.DAILY -> "Resumen de Hoy"
                                                    AnalyticsPeriod.WEEKLY -> "Resumen Semanal"
                                                    AnalyticsPeriod.MONTHLY -> "Resumen del Mes"
                                                },
                                                color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                SummaryStat("Ingresos", currencyFormatter.format(monthlyReport.totalIncome), AccentBlue)
                                                SummaryStat("Gastos", currencyFormatter.format(totalExpenses), AccentCoral)
                                                SummaryStat(
                                                    "Ahorro", currencyFormatter.format(savings),
                                                    if (savings >= BigDecimal.ZERO) AccentBlue else AccentCoral
                                                )
                                            }
                                            if (uiState.period != AnalyticsPeriod.DAILY && monthlyReport.totalIncome > BigDecimal.ZERO) {
                                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Tasa de ahorro", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                                    Text("${monthlyReport.savingsRate.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                }
                                                LinearProgressIndicator(
                                                    progress = { (monthlyReport.savingsRate / 100f).coerceIn(0f, 1f) },
                                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                                    color = AccentBlue,
                                                    trackColor = Color.White.copy(alpha = 0.2f),
                                                    strokeCap = StrokeCap.Round
                                                )
                                            }
                                        }
                                    }
                                }

                                // Breakdown
                                item {
                                    Text("Desglose de Gastos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardBg)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            BreakdownRow("Efectivo / Débito", monthlyReport.cashExpenses, totalExpenses, AccentCoral, currencyFormatter)
                                            BreakdownRow("Tarjeta de crédito", monthlyReport.creditExpenses, totalExpenses, Color(0xFFE84393), currencyFormatter)
                                            if (monthlyReport.totalCardDebt > BigDecimal.ZERO) {
                                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                                BreakdownRow("Deuda en tarjetas", monthlyReport.totalCardDebt, monthlyReport.totalCardDebt, Color(0xFFFDCB6E), currencyFormatter)
                                            }
                                        }
                                    }
                                }

                                // Category chart
                                if (monthlyReport.expensesByCategory.isNotEmpty()) {
                                    item { Text("Gastos por Categoría", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                                    item { ExpenseBarChart(expenses = monthlyReport.expensesByCategory) }
                                }
                            }

                            // Transaction history
                            if (uiState.transactions.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Operaciones", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${uiState.transactions.size} movimiento(s)",
                                            color = Color.Gray, fontSize = 12.sp
                                        )
                                    }
                                }
                                item {
                                    Text(
                                        "← Desliza para eliminar",
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                                items(uiState.transactions, key = { it.id }) { transaction ->
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { value ->
                                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                                viewModel.deleteTransaction(transaction)
                                                true
                                            } else false
                                        }
                                    )
                                    val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            val bg by animateColorAsState(
                                                targetValue = if (isSwiping) AccentCoral else Color.Transparent,
                                                label = "tx_swipe_bg"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(bg)
                                                    .padding(end = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                if (isSwiping) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                                                        Text("Deshacer", color = Color.White, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        TransactionHistoryItem(transaction = transaction, formatter = currencyFormatter)
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionHistoryItem(transaction: Transaction, formatter: NumberFormat) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "MX"))
    val typeColor = when (transaction.type) {
        TransactionType.INCOME -> AccentBlue
        TransactionType.EXPENSE -> AccentCoral
        TransactionType.TRANSFER -> Color(0xFFFDCB6E)
    }
    val typeLabel = when (transaction.type) {
        TransactionType.INCOME -> "Ingreso"
        TransactionType.EXPENSE -> "Gasto"
        TransactionType.TRANSFER -> "Transferencia"
    }
    val amountPrefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> "↔"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when (transaction.type) {
                            TransactionType.INCOME -> "↑"
                            TransactionType.EXPENSE -> "↓"
                            TransactionType.TRANSFER -> "↔"
                        },
                        color = typeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        categoryNames[transaction.categoryId] ?: typeLabel,
                        color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            transaction.date.format(dateFormatter),
                            color = Color.Gray, fontSize = 12.sp
                        )
                        if (transaction.sourceType == SourceType.CREDIT_CARD) {
                            Text("• Tarjeta", color = Color(0xFFE84393), fontSize = 12.sp)
                        }
                        transaction.note?.let {
                            Text("• $it", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
            Text(
                "$amountPrefix${formatter.format(transaction.amount)}",
                color = typeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun BreakdownRow(label: String, amount: BigDecimal, total: BigDecimal, color: Color, formatter: NumberFormat) {
    val fraction = if (total > BigDecimal.ZERO) (amount.toDouble() / total.toDouble()).toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(formatter.format(amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun ExpenseBarChart(expenses: Map<Long, BigDecimal>) {
    val maxExpense = expenses.values.maxOfOrNull { it.toDouble() } ?: 1.0
    val colors = listOf(AccentPurple, AccentBlue, AccentCoral, Color(0xFFFDCB6E), Color(0xFFE84393))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            expenses.entries.sortedByDescending { it.value }.forEachIndexed { idx, (categoryId, amount) ->
                val barColor = colors[idx % colors.size]
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(categoryNames[categoryId] ?: "Categoría $categoryId", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(amount),
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (amount.toDouble() / maxExpense).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = barColor,
                        trackColor = Color.White.copy(alpha = 0.06f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
