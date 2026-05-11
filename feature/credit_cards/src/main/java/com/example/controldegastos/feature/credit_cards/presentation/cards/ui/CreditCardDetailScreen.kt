package com.example.controldegastos.feature.credit_cards.presentation.cards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controldegastos.core.domain.model.Transaction
import com.example.controldegastos.feature.credit_cards.presentation.cards.CreditCardDetailUiState
import com.example.controldegastos.feature.credit_cards.presentation.cards.CreditCardDetailViewModel
import com.example.controldegastos.feature.credit_cards.presentation.cards.CreditCardEvent
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardDetailScreen(
    viewModel: CreditCardDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    var showPayDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Tarjeta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF13131A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (state is CreditCardDetailUiState.Success) {
                FloatingActionButton(
                    onClick = { showPayDialog = true },
                    containerColor = Color(0xFF00B894),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Payment, contentDescription = "Pagar")
                }
            }
        },
        containerColor = Color(0xFF13131A)
    ) { paddingValues ->

        if (showPayDialog && state is CreditCardDetailUiState.Success) {
            val successState = state as CreditCardDetailUiState.Success
            PayCreditCardDialog(
                card = successState.card,
                accounts = accounts,
                monthlyPayment = successState.monthlyDue,
                isProcessing = successState.isProcessingPayment,
                onDismiss = { showPayDialog = false },
                onPay = { accountId, amount, paymentType ->
                    viewModel.onEvent(
                        CreditCardEvent.PayCard(
                            cardId = successState.card.id,
                            sourceAccountId = accountId,
                            amount = amount,
                            paymentType = paymentType
                        )
                    )
                    showPayDialog = false
                }
            )
        }

        when (val uiState = state) {
            is CreditCardDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6C5CE7))
                }
            }
            is CreditCardDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = Color.Red)
                }
            }
            is CreditCardDetailUiState.Success -> {
                val card = uiState.card
                val msiTxs = uiState.msiTransactions
                val regularTxs = uiState.regularTransactions
                val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CardHeader(
                            cardName = card.name,
                            bankName = card.bank,
                            cardColor = Color(card.color),
                            usedBalance = card.usedBalance,
                            monthlyDue = uiState.monthlyDue,
                            formatter = formatter
                        )
                    }

                    if (msiTxs.isNotEmpty()) {
                        item {
                            Text(
                                "Compras a Meses sin Intereses",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(msiTxs) { tx ->
                            TransactionItem(tx = tx, isMsi = true, formatter = formatter)
                        }
                    }

                    if (regularTxs.isNotEmpty()) {
                        item {
                            Text(
                                "Compras Regulares",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(regularTxs) { tx ->
                            TransactionItem(tx = tx, isMsi = false, formatter = formatter)
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CardHeader(
    cardName: String,
    bankName: String,
    cardColor: Color,
    usedBalance: BigDecimal,
    monthlyDue: BigDecimal,
    formatter: NumberFormat
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardColor.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(cardName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(bankName, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Deuda Total", color = Color.Gray, fontSize = 12.sp)
                    Text(formatter.format(usedBalance), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pago Mensual", color = Color.Gray, fontSize = 12.sp)
                    Text(formatter.format(monthlyDue), color = cardColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction, isMsi: Boolean, formatter: NumberFormat) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1C1C24),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6C5CE7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF6C5CE7))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(tx.note ?: "Compra", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(tx.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "ES"))), color = Color.Gray, fontSize = 12.sp)
                    if (isMsi && tx.installmentMonths != null) {
                        Text(
                            "${tx.installmentMonths} meses",
                            color = Color(0xFF00B894),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Text(
                formatter.format(tx.amount),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
