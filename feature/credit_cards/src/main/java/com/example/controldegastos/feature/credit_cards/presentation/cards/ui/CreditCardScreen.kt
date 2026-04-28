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
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    var showAddDialog by remember { mutableStateOf(false) }
    var payingCard by remember { mutableStateOf<com.example.controldegastos.core.domain.model.CreditCard?>(null) }

    // Success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state) {
        if (state is CreditCardUiState.Success && (state as CreditCardUiState.Success).paymentSuccess) {
            snackbarHostState.showSnackbar("✅ Pago registrado exitosamente")
            viewModel.onEvent(CreditCardEvent.ClearPaymentSuccess)
        }
    }

    if (showAddDialog) {
        AddCreditCardDialog(
            onDismiss = { showAddDialog = false },
            onSave = { card ->
                viewModel.onEvent(CreditCardEvent.AddCard(card))
                showAddDialog = false
            }
        )
    }

    payingCard?.let { card ->
        PayCreditCardDialog(
            card = card,
            accounts = accounts,
            isProcessing = (state as? CreditCardUiState.Success)?.isProcessingPayment ?: false,
            onDismiss = { payingCard = null },
            onPay = { accountId, amount, paymentType ->
                viewModel.onEvent(
                    CreditCardEvent.PayCard(
                        cardId = card.id,
                        sourceAccountId = accountId,
                        amount = amount,
                        paymentType = paymentType
                    )
                )
                payingCard = null
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                                        CreditCardWidget(card = card)
                                    }

                                    // Minimalist Pay button attached below the card
                                    Surface(
                                        onClick = { payingCard = card },
                                        enabled = card.usedBalance > java.math.BigDecimal.ZERO,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(
                                            bottomStart = 20.dp, bottomEnd = 20.dp
                                        ),
                                        border = if (card.usedBalance > java.math.BigDecimal.ZERO) 
                                            androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        else null
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (card.usedBalance > java.math.BigDecimal.ZERO)
                                                    "Pagar — ${currencyFormatter.format(card.usedBalance)}"
                                                else
                                                    "Sin deuda",
                                                color = if (card.usedBalance > java.math.BigDecimal.ZERO)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
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
