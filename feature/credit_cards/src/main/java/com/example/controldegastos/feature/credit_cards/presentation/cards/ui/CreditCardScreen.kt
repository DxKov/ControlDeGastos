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

private val BgColor = Color(0xFF0F0F12)
private val CardBg = Color(0xFF1A1A24)
private val AccentPurple = Color(0xFF6C5CE7)
private val AccentGreen = Color(0xFF00B894)

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
        containerColor = BgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tarjetas de Crédito",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarjeta")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(padding)
        ) {
            when (val uiState = state) {
                is CreditCardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentPurple
                    )
                }

                is CreditCardUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Total debt hero card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFFE17055), Color(0xFFD63031))
                                                )
                                            )
                                            .padding(24.dp)
                                    ) {
                                        Column {
                                            Text(
                                                "Deuda Total",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                currencyFormatter.format(uiState.totalDebt),
                                                color = Color.White,
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "${uiState.cards.size} tarjeta(s) activa(s)",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 12.sp
                                            )
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
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Toca el botón + para agregar tu primera tarjeta",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            item {
                                Text(
                                    "Mis tarjetas  •  desliza para eliminar",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                            items(uiState.cards, key = { it.id }) { card ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.onEvent(CreditCardEvent.DeleteCard(card))
                                            true
                                        } else false
                                    }
                                )
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
                                                targetValue = if (isSwiping) Color(0xFFE17055) else Color.Transparent,
                                                label = "swipe_bg"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                                    .background(bgColor)
                                                    .padding(end = 28.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                if (isSwiping) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                        Text("Eliminar", color = Color.White, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        CreditCardWidget(card = card)
                                    }

                                    // Pay button attached below the card
                                    Surface(
                                        onClick = { payingCard = card },
                                        enabled = card.usedBalance > java.math.BigDecimal.ZERO,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (card.usedBalance > java.math.BigDecimal.ZERO)
                                            AccentGreen.copy(alpha = 0.15f)
                                        else
                                            Color.White.copy(alpha = 0.04f),
                                        shape = RoundedCornerShape(
                                            bottomStart = 20.dp, bottomEnd = 20.dp
                                        )
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
                                                    "💳  Pagar tarjeta  —  ${currencyFormatter.format(card.usedBalance)} de deuda"
                                                else
                                                    "✅  Sin deuda",
                                                color = if (card.usedBalance > java.math.BigDecimal.ZERO)
                                                    AccentGreen
                                                else
                                                    Color.Gray,
                                                fontSize = 13.sp,
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
                        Text(uiState.message, color = Color.Red)
                        Button(
                            onClick = { viewModel.onEvent(CreditCardEvent.Load) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                        ) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}
