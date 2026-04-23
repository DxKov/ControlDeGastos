package com.example.controldegastos.feature.credit_cards.presentation.cards.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controldegastos.core.domain.model.Account
import com.example.controldegastos.core.domain.model.CreditCard
import com.example.controldegastos.core.domain.model.PaymentType
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

private val DialogBg = Color(0xFF1C1C24)
private val AccentPurple = Color(0xFF6C5CE7)
private val AccentGreen = Color(0xFF00B894)
private val AccentCoral = Color(0xFFE17055)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayCreditCardDialog(
    card: CreditCard,
    accounts: List<Account>,
    isProcessing: Boolean = false,
    onDismiss: () -> Unit,
    onPay: (sourceAccountId: Long, amount: BigDecimal, paymentType: PaymentType) -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val minimumPayment = card.usedBalance.multiply(BigDecimal("0.10")) // 10% minimum
    val fullBalance = card.usedBalance

    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var amountText by remember { mutableStateOf("") }
    var selectedPaymentType by remember { mutableStateOf(PaymentType.PARTIAL) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // Auto-fill amount based on payment type selection
    LaunchedEffect(selectedPaymentType) {
        when (selectedPaymentType) {
            PaymentType.MINIMUM -> amountText = minimumPayment.setScale(2, java.math.RoundingMode.CEILING).toString()
            PaymentType.FULL    -> amountText = fullBalance.setScale(2, java.math.RoundingMode.HALF_UP).toString()
            PaymentType.PARTIAL -> { /* user types manually */ }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(card.color), modifier = Modifier.size(24.dp))
                Column {
                    Text("Pagar Tarjeta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(card.name, color = Color.Gray, fontSize = 13.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Debt summary
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentCoral.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Deuda actual", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                currencyFormatter.format(fullBalance),
                                color = AccentCoral,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pago mínimo", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                currencyFormatter.format(minimumPayment),
                                color = Color(0xFFFDCB6E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Payment type selector
                Text("Tipo de pago", color = Color.Gray, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentTypeOption(
                        label = "Pago parcial",
                        subtitle = "Ingresa el monto manualmente",
                        isSelected = selectedPaymentType == PaymentType.PARTIAL,
                        color = AccentPurple,
                        onClick = { selectedPaymentType = PaymentType.PARTIAL }
                    )
                    PaymentTypeOption(
                        label = "Pago mínimo",
                        subtitle = currencyFormatter.format(minimumPayment),
                        isSelected = selectedPaymentType == PaymentType.MINIMUM,
                        color = Color(0xFFFDCB6E),
                        onClick = { selectedPaymentType = PaymentType.MINIMUM }
                    )
                    PaymentTypeOption(
                        label = "Liquidar deuda",
                        subtitle = currencyFormatter.format(fullBalance),
                        isSelected = selectedPaymentType == PaymentType.FULL,
                        color = AccentGreen,
                        onClick = { selectedPaymentType = PaymentType.FULL }
                    )
                }

                // Amount input (editable in PARTIAL mode, read-only otherwise)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v ->
                        if (selectedPaymentType == PaymentType.PARTIAL) {
                            amountText = v
                            amountError = null
                        }
                    },
                    label = { Text("Monto a pagar") },
                    prefix = { Text("$  ", color = AccentPurple) },
                    readOnly = selectedPaymentType != PaymentType.PARTIAL,
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it, color = AccentCoral) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        focusedLabelColor = AccentPurple,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White
                    )
                )

                // Account selector
                if (accounts.isEmpty()) {
                    Surface(shape = RoundedCornerShape(12.dp), color = AccentCoral.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
                            Text("Agrega una cuenta bancaria para realizar el pago.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    Text("Pagar desde", color = Color.Gray, fontSize = 13.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.forEach { account ->
                            val isSelected = selectedAccountId == account.id
                            Surface(
                                onClick = { selectedAccountId = account.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentPurple.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                                border = if (isSelected) BorderStroke(1.dp, AccentPurple) else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(account.color).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(account.color), modifier = Modifier.size(18.dp))
                                        }
                                        Column {
                                            Text(account.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text(
                                                NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(account.balance),
                                                color = Color(0xFF00CEC9),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toBigDecimalOrNull()
                    val accountId = selectedAccountId
                    when {
                        amount == null || amount <= BigDecimal.ZERO ->
                            amountError = "Ingresa un monto válido"
                        amount > fullBalance ->
                            amountError = "El monto supera la deuda actual"
                        accountId == null ->
                            amountError = "Selecciona una cuenta"
                        else -> onPay(accountId, amount, selectedPaymentType)
                    }
                },
                enabled = !isProcessing && accounts.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Procesando...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Confirmar Pago", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun PaymentTypeOption(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = if (isSelected) BorderStroke(1.5.dp, color) else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color, unselectedColor = Color.Gray)
            )
        }
    }
}
