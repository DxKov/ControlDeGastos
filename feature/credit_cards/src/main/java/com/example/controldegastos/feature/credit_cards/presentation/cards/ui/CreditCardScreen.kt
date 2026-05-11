package com.example.controldegastos.feature.credit_cards.presentation.cards.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controldegastos.core.domain.model.Account
import com.example.controldegastos.feature.credit_cards.presentation.cards.CreditCardEvent
import com.example.controldegastos.feature.credit_cards.presentation.cards.CreditCardUiState
import com.example.controldegastos.feature.credit_cards.presentation.cards.CreditCardViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardScreen(
    viewModel: CreditCardViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddCreditCardDialog(
            onDismiss = { showAddDialog = false },
            onSave = { card ->
                viewModel.onEvent(CreditCardEvent.AddCard(card))
                showAddDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tarjetas de Crédito",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarjeta")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val uiState = state) {
                is CreditCardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is CreditCardUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Minimalist Debt Section
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Deuda Consolidada",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = currencyFormatter.format(uiState.totalDebt),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.totalDebt > java.math.BigDecimal.ZERO) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${uiState.cards.size} tarjeta(s) activa(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        // Per-card monthly debt breakdown — only shown when cards have debt
                        val cardsWithDebt = uiState.cards.filter { it.usedBalance > java.math.BigDecimal.ZERO }
                        if (cardsWithDebt.isNotEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            "Deuda por Tarjeta",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        cardsWithDebt.forEachIndexed { index, card ->
                                            val monthlyAmount = uiState.monthlyDebtPerCard[card.id] ?: card.usedBalance
                                            val fraction = if (uiState.totalDebt > java.math.BigDecimal.ZERO)
                                                card.usedBalance.divide(uiState.totalDebt, 4, java.math.RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
                                            else 0f
                                            val cardColor = Color(card.color)

                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(cardColor)
                                                        )
                                                        Column {
                                                            Text(
                                                                card.name,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                card.bank,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                                            )
                                                        }
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            "Cuota mensual",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                                        )
                                                        Text(
                                                            currencyFormatter.format(monthlyAmount),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                        if (monthlyAmount < card.usedBalance) {
                                                            Text(
                                                                "Total: ${currencyFormatter.format(card.usedBalance)}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(6.dp))
                                                // Proportional progress bar
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction)
                                                            .fillMaxHeight()
                                                            .background(
                                                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                                    colors = listOf(
                                                                        cardColor.copy(alpha = 0.7f),
                                                                        cardColor
                                                                    )
                                                                )
                                                            )
                                                    )
                                                }
                                                if (index < cardsWithDebt.lastIndex) {
                                                    Spacer(Modifier.height(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.cards.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("💳", fontSize = 48.sp)
                                    Text(
                                        "Sin tarjetas aún",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Toca el botón + para agregar tu primera tarjeta",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            item {
                                Text(
                                    "Mis tarjetas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                            items(uiState.cards, key = { it.id }) { card ->
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
                                        title = { Text("¿Eliminar tarjeta?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                                        text = { Text("Se perderá todo el historial de esta tarjeta.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.onEvent(CreditCardEvent.DeleteCard(card))
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

                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
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
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(bgColor)
                                                    .padding(end = 28.dp),
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
                                        Surface(
                                            onClick = { onNavigateToDetail(card.id) },
                                            color = Color.Transparent,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CreditCardWidget(card = card)
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }

                is CreditCardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Text(uiState.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = { viewModel.onEvent(CreditCardEvent.Load) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}
