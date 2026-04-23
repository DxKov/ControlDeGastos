package com.example.controldegastos.feature.transactions.presentation.transactions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.controldegastos.feature.transactions.domain.model.Transaction
import com.example.controldegastos.feature.transactions.domain.model.TransactionType
import java.text.NumberFormat
import java.util.*

@Composable
fun TransactionItem(
    transaction: Transaction,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val isIncome = transaction.type == TransactionType.INCOME
    
    // Aesthetic colors
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color.White
    val amountPrefix = if (isIncome) "+" else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon Background
        Surface(
            modifier = Modifier.size(44.dp),
            shape = MaterialTheme.shapes.medium,
            color = Color.White.copy(alpha = 0.05f)
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Transaction Label and Note
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            transaction.note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        // Formatted Amount
        Text(
            text = "$amountPrefix${currencyFormatter.format(transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = amountColor,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
    }
}
