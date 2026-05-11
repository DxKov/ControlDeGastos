package com.example.controldegastos.feature.transactions.presentation.transactions.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controldegastos.feature.transactions.domain.model.SourceType
import com.example.controldegastos.feature.transactions.domain.model.Transaction
import com.example.controldegastos.feature.transactions.domain.model.TransactionType
import com.example.controldegastos.feature.transactions.presentation.transactions.TransactionEvent
import com.example.controldegastos.feature.transactions.presentation.transactions.TransactionViewModel
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val typeLabels = mapOf(
    TransactionType.INCOME to "Ingreso",
    TransactionType.EXPENSE to "Gasto",
    TransactionType.TRANSFER to "Transferencia"
)

private val categoryNames = mapOf(
    1L to "Comida",
    2L to "Transporte",
    3L to "Ocio",
    4L to "Servicios",
    5L to "Salud",
    6L to "Educación",
    7L to "Compras",
    8L to "Otros"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onTransactionSaved: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedSourceId by remember { mutableStateOf<Long?>(null) }
    var selectedSourceType by remember { mutableStateOf(SourceType.ACCOUNT) }
    var selectedDestinationId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(1L) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    // Meses sin intereses — solo aplica cuando se elige tarjeta de crédito
    var selectedInstallmentMonths by remember { mutableStateOf<Int?>(null) }

    val backgroundColor = Color(0xFF0F0F12)
    val accentPurple = Color(0xFF6C5CE7)
    val cardBg = Color(0xFF1A1A24)

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onTransactionSaved()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Movimiento", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) amount = it },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$ ", color = accentPurple) },
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentPurple,
                    focusedLabelColor = accentPurple,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Type selector — Spanish labels
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            // Reset destination when changing type
                            if (type != TransactionType.TRANSFER) selectedDestinationId = null
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TransactionType.entries.size)
                    ) {
                        Text(typeLabels[type] ?: type.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Source Selector
            Text("Cuenta / Tarjeta de origen", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.accounts.forEach { account ->
                    val isSelected = selectedSourceId == account.id && selectedSourceType == SourceType.ACCOUNT
                    Surface(
                        onClick = {
                            selectedSourceId = account.id
                            selectedSourceType = SourceType.ACCOUNT
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isSelected) accentPurple.copy(alpha = 0.2f) else cardBg,
                        shape = MaterialTheme.shapes.medium,
                        border = if (isSelected) BorderStroke(1.dp, accentPurple) else null
                    ) {
                        Text(account.name, modifier = Modifier.padding(16.dp), color = Color.White)
                    }
                }
                if (selectedType != TransactionType.TRANSFER) {
                    state.cards.forEach { card ->
                        val isSelected = selectedSourceId == card.id && selectedSourceType == SourceType.CREDIT_CARD
                        Surface(
                            onClick = {
                                selectedSourceId = card.id
                                selectedSourceType = SourceType.CREDIT_CARD
                                viewModel.onEvent(TransactionEvent.CardSelected(card.id))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isSelected) accentPurple.copy(alpha = 0.2f) else cardBg,
                            shape = MaterialTheme.shapes.medium,
                            border = if (isSelected) BorderStroke(1.dp, accentPurple) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(card.name, color = Color.White)
                                Badge(containerColor = Color(0xFF3700B3)) {
                                    Text("CRÉDITO", modifier = Modifier.padding(2.dp), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Meses sin intereses (solo visible cuando la fuente es tarjeta de crédito)
            if (selectedSourceType == SourceType.CREDIT_CARD && selectedType == TransactionType.EXPENSE) {
                val installmentOptions = listOf(null, 3, 6, 9, 12)
                Text("¿Meses sin intereses?", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    installmentOptions.forEach { months ->
                        val isSelected = selectedInstallmentMonths == months
                        val label = if (months == null) "Sin meses" else "$months MSI"
                        Surface(
                            onClick = { selectedInstallmentMonths = months },
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) accentPurple.copy(alpha = 0.25f) else cardBg,
                            border = if (isSelected)
                                BorderStroke(1.5.dp, accentPurple)
                            else
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = if (isSelected) accentPurple else Color.Gray,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Destination selector (only for TRANSFER)
            if (selectedType == TransactionType.TRANSFER) {
                Text("Cuenta de destino", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts
                        .filter { it.id != selectedSourceId } // Exclude source account
                        .forEach { account ->
                            val isSelected = selectedDestinationId == account.id
                            Surface(
                                onClick = { selectedDestinationId = account.id },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (isSelected) Color(0xFF00CEC9).copy(alpha = 0.2f) else cardBg,
                                shape = MaterialTheme.shapes.medium,
                                border = if (isSelected) BorderStroke(1.dp, Color(0xFF00CEC9)) else null
                            ) {
                                Text(account.name, modifier = Modifier.padding(16.dp), color = Color.White)
                            }
                        }
                    if (state.accounts.size < 2) {
                        Text(
                            "Necesitas al menos 2 cuentas para hacer una transferencia.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Category (not needed for transfers, but shown anyway)
            if (selectedType != TransactionType.TRANSFER) {
                Text("Categoría", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                CategoryPicker(
                    categories = categoryNames.map { (id, name) ->
                        CategoryUiModel(id, name, "")
                    },
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it }
                )
            }

            // Date Picker Trigger
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = accentPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy")),
                        color = Color.White
                    )
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    val finalAmount = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val isTransfer = selectedType == TransactionType.TRANSFER
                    val canSave = selectedSourceId != null &&
                        (isTransfer || selectedCategoryId != null) &&
                        (!isTransfer || selectedDestinationId != null)

                    if (canSave) {
                        viewModel.onEvent(TransactionEvent.SaveTransaction(
                            Transaction(
                                amount = finalAmount,
                                type = selectedType,
                                date = selectedDate,
                                categoryId = selectedCategoryId ?: 8L,
                                sourceType = selectedSourceType,
                                sourceId = selectedSourceId!!,
                                destinationId = if (isTransfer) selectedDestinationId else null,
                                note = note.ifEmpty { null },
                                installmentMonths = if (selectedSourceType == SourceType.CREDIT_CARD) selectedInstallmentMonths else null
                            )
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentPurple),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                else Text("Guardar Movimiento", fontWeight = FontWeight.Bold, color = Color.Black)
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
