package com.example.controldegastos.feature.accounts.presentation.accounts.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controldegastos.feature.accounts.domain.model.Account
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountItem(
    account: Account,
    modifier: Modifier = Modifier,
    onDelete: (Long) -> Unit = {},
    onToggleIncludeInTotal: (Account) -> Unit = {}
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
                false // Don't dismiss yet, wait for confirmation
            } else false
        }
    )

    // Reset swipe state if dialog is dismissed or cancelled
    LaunchedEffect(showDeleteConfirm) {
        if (!showDeleteConfirm && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1C1C24),
            title = { Text("¿Eliminar cuenta?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción no se puede deshacer. Todos los datos asociados a esta cuenta se perderán.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(account.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE17055))
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // Only show red bg when the user is actively swiping toward EndToStart
    val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val bgColor by animateColorAsState(
                targetValue = if (isSwiping) Color(0xFFE17055) else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                // Only render the icon when actively swiping to avoid bleed-through
                if (isSwiping) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(account.color).copy(alpha = 0.12f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(account.color).copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(account.color).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.name.take(1).uppercase(),
                            color = Color(account.color),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (account.type.name == "BANK") "Banco" else "Efectivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (!account.includeInTotal) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "• Excluida del total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFDCB6E)
                                )
                            }
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(account.balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (account.balance >= java.math.BigDecimal.ZERO)
                            Color(0xFF00CEC9)
                        else
                            Color(0xFFE17055),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Switch(
                        checked = account.includeInTotal,
                        onCheckedChange = { onToggleIncludeInTotal(account) },
                        modifier = Modifier.scale(0.7f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(account.color),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
