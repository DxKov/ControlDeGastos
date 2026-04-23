package com.example.controldegastos.feature.accounts.presentation.accounts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controldegastos.core.domain.model.Account
import com.example.controldegastos.core.domain.model.AccountType
import java.math.BigDecimal

private val accountColors = listOf(
    0xFF6C5CE7.toInt(), // Purple
    0xFF00CEC9.toInt(), // Teal
    0xFF0984E3.toInt(), // Blue
    0xFFE17055.toInt(), // Coral
    0xFF00B894.toInt(), // Green
    0xFFFDCB6E.toInt(), // Yellow
    0xFFE84393.toInt(), // Pink
    0xFF636E72.toInt(), // Gray
)

private val accountIcons = listOf(
    "account_balance" to Icons.Default.AccountBalance,
    "savings" to Icons.Default.Savings,
    "wallet" to Icons.Default.AccountBalanceWallet,
    "credit_card" to Icons.Default.CreditCard,
    "attach_money" to Icons.Default.AttachMoney,
    "home" to Icons.Default.Home,
    "work" to Icons.Default.Work,
    "star" to Icons.Default.Star,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var selectedColor by remember { mutableStateOf(accountColors[0]) }
    var selectedIcon by remember { mutableStateOf("account_balance") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C24),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Nueva Cuenta",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Nombre de la cuenta") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C5CE7),
                        focusedLabelColor = Color(0xFF6C5CE7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6C5CE7)
                    ),
                    singleLine = true
                )

                // Initial Balance
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Saldo inicial") },
                    prefix = { Text("$", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C5CE7),
                        focusedLabelColor = Color(0xFF6C5CE7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6C5CE7)
                    ),
                    singleLine = true
                )

                // Account Type
                Text("Tipo de cuenta", color = Color.Gray, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        val label = if (type == AccountType.BANK) "Banco" else "Efectivo"
                        val icon = if (type == AccountType.BANK) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = { Text(label, color = if (isSelected) Color.White else Color.Gray) },
                            leadingIcon = { Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6C5CE7),
                                containerColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }
                }

                // Color selector
                Text("Color", color = Color.Gray, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accountColors.forEach { color ->
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

                // Icon selector
                Text("Ícono", color = Color.Gray, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accountIcons.forEach { (key, icon) ->
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(selectedColor).copy(alpha = 0.4f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .then(
                                    if (isSelected) Modifier.border(1.dp, Color(selectedColor), RoundedCornerShape(10.dp))
                                    else Modifier
                                )
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color(selectedColor) else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val balance = balanceText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    onSave(
                        Account(
                            id = 0L,
                            name = name.trim(),
                            type = selectedType,
                            balance = balance,
                            initialBalance = balance,
                            currency = "MXN",
                            color = selectedColor,
                            icon = selectedIcon,
                            createdAt = System.currentTimeMillis()
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
