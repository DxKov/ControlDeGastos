package com.example.controldegastos.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controldegastos.feature.accounts.presentation.accounts.AccountUiState
import com.example.controldegastos.feature.accounts.presentation.accounts.AccountViewModel
import java.text.NumberFormat
import java.util.*

private val BgColor = Color(0xFF0F0F12)
private val CardBg = Color(0xFF1A1A24)
private val AccentPurple = Color(0xFF6C5CE7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    val accountState by accountViewModel.state.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    val total = when (val s = accountState) {
        is AccountUiState.Success -> s.total
        else -> java.math.BigDecimal.ZERO
    }
    val accounts = when (val s = accountState) {
        is AccountUiState.Success -> s.accounts
        else -> emptyList()
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GastoApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(
                            buildString {
                                val cal = Calendar.getInstance()
                                append(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "MX"))?.replaceFirstChar { it.uppercase() } ?: "")
                                append(" ${cal.get(Calendar.YEAR)}")
                            },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.Gray)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = AccentPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Movimiento")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero balance card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF6C5CE7), Color(0xFF9B59B6), Color(0xFF2C2C54))
                                    )
                                )
                                .padding(28.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Patrimonio Total", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = currencyFormatter.format(total),
                                    color = Color.White,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    BalanceStat(
                                        icon = Icons.Default.TrendingUp,
                                        label = "Cuentas",
                                        value = "${accounts.size}",
                                        iconTint = Color(0xFF00CEC9)
                                    )
                                    BalanceStat(
                                        icon = Icons.Default.AttachMoney,
                                        label = "Moneda",
                                        value = "MXN",
                                        iconTint = Color(0xFFFDCB6E)
                                    )
                                    BalanceStat(
                                        icon = Icons.Default.CreditCard,
                                        label = "Tarjetas",
                                        value = "—",
                                        iconTint = Color(0xFFE17055)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick actions row
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Acciones Rápidas", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            icon = Icons.Default.Add,
                            label = "Movimiento",
                            color = AccentPurple,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAddTransaction
                        )
                        QuickActionCard(
                            icon = Icons.Default.AccountBalance,
                            label = "Cuentas",
                            color = Color(0xFF00CEC9),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAccounts
                        )
                        QuickActionCard(
                            icon = Icons.Default.BarChart,
                            label = "Análisis",
                            color = Color(0xFFE17055),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAnalytics
                        )
                    }
                }
            }

            // Accounts summary
            if (accounts.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mis Cuentas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = onNavigateToAccounts) {
                            Text("Ver todas", color = AccentPurple, fontSize = 13.sp)
                        }
                    }
                }
                items(accounts.take(3)) { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(account.color).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (account.type.name == "BANK") Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(account.color),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(account.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        if (account.type.name == "BANK") "Banco" else "Efectivo",
                                        color = Color.Gray, fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                currencyFormatter.format(account.balance),
                                color = if (account.balance >= java.math.BigDecimal.ZERO) Color(0xFF00CEC9) else Color(0xFFE17055),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                // Empty state call-to-action
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🚀", fontSize = 40.sp)
                            Text("¡Bienvenido a GastoApp!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(
                                "Empieza agregando tus cuentas para llevar el control de tus finanzas.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = onNavigateToAccounts,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Agregar Cuenta")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceStat(icon: ImageVector, label: String, value: String, iconTint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
