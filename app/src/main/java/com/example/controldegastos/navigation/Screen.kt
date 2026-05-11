package com.example.controldegastos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class defining the available screens and their routes for the global navigation.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Dashboard)
    object Accounts : Screen("accounts", "Cuentas", Icons.Default.AccountBalance)
    object CreditCards : Screen("credit_cards", "Tarjetas", Icons.Default.CreditCard)
    object Analytics : Screen("analytics", "Análisis", Icons.Default.BarChart)
    
    object AddTransaction : Screen(
        route = "add_transaction?sourceId={sourceId}&type={type}",
        title = "Nuevo Movimiento"
    ) {
        /**
         * Helper function to build the route with optional parameters.
         */
        fun createRoute(sourceId: Long? = null, type: String? = null): String {
            return "add_transaction?sourceId=${sourceId ?: -1L}&type=${type ?: ""}"
        }
    }

    object CardDetail : Screen("card_detail/{cardId}", "Detalles de Tarjeta") {
        fun createRoute(cardId: Long) = "card_detail/$cardId"
    }
}
