package com.example.controldegastos.feature.analytics.presentation.analytics.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Analíticas", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Minimalist Period selector
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
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, 
                            selected = isSelected,
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            when (val uiState = state) {
                is AnalyticsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is AnalyticsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(uiState.message, color = MaterialTheme.colorScheme.error)
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
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Registra ingresos y gastos para ver tus analíticas aquí.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Summary Section (Minimalist, like Dashboard)
                            if (hasData) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                when (uiState.period) {
                                                    AnalyticsPeriod.DAILY -> "Resumen de Hoy"
                                                    AnalyticsPeriod.WEEKLY -> "Resumen Semanal"
                                                    AnalyticsPeriod.MONTHLY -> "Resumen del Mes"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Spacer(Modifier.height(16.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                SummaryStatMinimal("Ingresos", currencyFormatter.format(monthlyReport.totalIncome))
                                                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                                SummaryStatMinimal("Gastos", currencyFormatter.format(totalExpenses))
                                                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                                SummaryStatMinimal("Ahorro", currencyFormatter.format(savings))
                                            }
                                            
                                            if (uiState.period != AnalyticsPeriod.DAILY && monthlyReport.totalIncome > BigDecimal.ZERO) {
                                                Spacer(Modifier.height(24.dp))
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                Spacer(Modifier.height(16.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Tasa de ahorro", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                    Text("${monthlyReport.savingsRate.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                LinearProgressIndicator(
                                                    progress = { (monthlyReport.savingsRate / 100f).coerceIn(0f, 1f) },
                                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    strokeCap = StrokeCap.Round
                                                )
                                            }
                                        }
                                    }
                                }

                                // Breakdown
                                item {
                                    Text(
                                        "Desglose", 
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                                    )
                                }
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                            BreakdownRowMinimal("Efectivo / Débito", monthlyReport.cashExpenses, totalExpenses, currencyFormatter)
                                            BreakdownRowMinimal("Tarjeta de crédito", monthlyReport.creditExpenses, totalExpenses, currencyFormatter)
                                            if (monthlyReport.totalCardDebt > BigDecimal.ZERO) {
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                BreakdownRowMinimal("Deuda Acumulada", monthlyReport.totalCardDebt, monthlyReport.totalCardDebt, currencyFormatter)
                                            }
                                        }
                                    }
                                }

                                // Category chart
                                if (monthlyReport.expensesByCategory.isNotEmpty()) {
                                    item { 
                                        Text(
                                            "Gastos por Categoría", 
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                                        ) 
                                    }
                                    item { ExpenseBarChartMinimal(expenses = monthlyReport.expensesByCategory) }
                                }
                            }

                            // Transaction history
                            if (uiState.transactions.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Movimientos", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "${uiState.transactions.size} ops",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                                items(uiState.transactions, key = { it.id }) { transaction ->
                                    val showDeleteConfirm = remember { mutableStateOf(false) }
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { value ->
                                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                                showDeleteConfirm.value = true
                                                false
                                            } else false
                                        }
                                    )

                                    LaunchedEffect(showDeleteConfirm.value) {
                                        if (!showDeleteConfirm.value && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                            dismissState.reset()
                                        }
                                    }

                                    if (showDeleteConfirm.value) {
                                        AlertDialog(
                                            onDismissRequest = { showDeleteConfirm.value = false },
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            title = { Text("¿Eliminar movimiento?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                                            text = { Text("Esta acción ajustará automáticamente el balance de tu cuenta.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                            confirmButton = {
                                                Button(
                                                    onClick = {
                                                        viewModel.deleteTransaction(transaction)
                                                        showDeleteConfirm.value = false
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text("Eliminar", fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showDeleteConfirm.value = false }) {
                                                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                }
                                            }
                                        )
                                    }

                                    val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            val bgColor by animateColorAsState(
                                                targetValue = if (isSwiping) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else Color.Transparent,
                                                label = "swipe_bg"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(bgColor)
                                                    .padding(end = 24.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                if (isSwiping) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        TransactionItemMinimal(transaction = transaction, formatter = currencyFormatter)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatMinimal(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun BreakdownRowMinimal(label: String, amount: BigDecimal, total: BigDecimal, formatter: NumberFormat) {
    val fraction = if (total > BigDecimal.ZERO) (amount.toDouble() / total.toDouble()).toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(formatter.format(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun TransactionItemMinimal(transaction: Transaction, formatter: NumberFormat) {
    val typeColor = when (transaction.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(typeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (transaction.type == TransactionType.INCOME) Icons.Default.Add else Icons.Default.Delete,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.note ?: "Sin nota",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = categoryNames[transaction.categoryId] ?: "Otros",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            Text(
                text = (if (transaction.type == TransactionType.EXPENSE) "-" else "+") + formatter.format(transaction.amount.abs()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )
        }
    }
}

@Composable
fun ExpenseBarChartMinimal(expenses: Map<Long, BigDecimal>) {
    val maxExpense = expenses.values.maxOfOrNull { it.toDouble() } ?: 1.0
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            expenses.entries.sortedByDescending { it.value }.take(5).forEach { (categoryId, amount) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(categoryNames[categoryId] ?: "Categoría", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(amount),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (amount.toDouble() / maxExpense).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
