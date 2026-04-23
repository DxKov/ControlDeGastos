package com.example.controldegastos.feature.transactions.presentation.transactions.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.controldegastos.feature.transactions.domain.model.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionHistoryList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    // Group transactions by date and sort dates descending
    val groupedTransactions = transactions.groupBy { it.date }
    val sortedDates = groupedTransactions.keys.sortedDescending()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        sortedDates.forEach { date ->
            // Sticky header for date grouping
            stickyHeader {
                DateHeader(date = date)
            }
            
            items(
                items = groupedTransactions[date] ?: emptyList(),
                key = { it.id }
            ) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    categoryName = "Categoría ${transaction.categoryId}" // Placeholder mapping
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("es", "MX"))
    val isToday = date == LocalDate.now()
    val isYesterday = date == LocalDate.now().minusDays(1)
    
    val headerText = when {
        isToday -> "Hoy"
        isYesterday -> "Ayer"
        else -> date.format(formatter).replaceFirstChar { it.uppercase() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F12))
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = headerText,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFBB86FC), // Accent color for dates
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
