package com.example.controldegastos.feature.credit_cards.presentation.cards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controldegastos.core.domain.model.CreditCard
import java.math.BigDecimal

private val cardColors = listOf(
    0xFF6C5CE7.toInt(),
    0xFF0984E3.toInt(),
    0xFF00CEC9.toInt(),
    0xFFE17055.toInt(),
    0xFFE84393.toInt(),
    0xFF2C2C54.toInt(),
    0xFF1E272E.toInt(),
    0xFF6D4C41.toInt(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }
    var cutoffDay by remember { mutableStateOf("1") }
    var paymentDueDay by remember { mutableStateOf("20") }
    var selectedColor by remember { mutableStateOf(cardColors[0]) }
    var nameError by remember { mutableStateOf(false) }
    var bankError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C24),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Nueva Tarjeta de Crédito",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C5CE7),
                    focusedLabelColor = Color(0xFF6C5CE7),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF6C5CE7)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Nombre de la tarjeta") },
                    placeholder = { Text("Ej: Visa Oro", color = Color.Gray) },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it; bankError = false },
                    label = { Text("Banco emisor") },
                    placeholder = { Text("Ej: BBVA, Santander", color = Color.Gray) },
                    isError = bankError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = last4,
                    onValueChange = { if (it.length <= 4) last4 = it.filter { c -> c.isDigit() } },
                    label = { Text("Últimos 4 dígitos") },
                    placeholder = { Text("1234", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Límite de crédito") },
                    prefix = { Text("$", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cutoffDay,
                        onValueChange = { cutoffDay = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Día de corte") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = fieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paymentDueDay,
                        onValueChange = { paymentDueDay = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Día de pago") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = fieldColors,
                        singleLine = true
                    )
                }

                Text("Color de la tarjeta", color = Color.Gray, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    cardColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (selectedColor == color) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    if (bank.isBlank()) { bankError = true; return@Button }
                    onSave(
                        CreditCard(
                            id = 0L,
                            name = name.trim(),
                            bank = bank.trim(),
                            last4Digits = last4.ifBlank { "0000" },
                            creditLimit = limitText.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                            usedBalance = BigDecimal.ZERO,
                            cutoffDay = cutoffDay.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            paymentDueDay = paymentDueDay.toIntOrNull()?.coerceIn(1, 31) ?: 20,
                            color = selectedColor,
                            isActive = true
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
