package com.example.controldegastos.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.controldegastos.feature.accounts.presentation.accounts.ui.AccountScreen
import com.example.controldegastos.feature.credit_cards.presentation.cards.ui.CreditCardScreen
import com.example.controldegastos.feature.transactions.presentation.transactions.ui.AddTransactionScreen
import com.example.controldegastos.feature.analytics.presentation.analytics.ui.AnalyticsScreen

/**
 * Navigate to a tab destination with a clean backstack (same behaviour as bottom nav).
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Dashboard.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAccounts = { navController.navigateToTab(Screen.Accounts.route) },
                onNavigateToAnalytics = { navController.navigateToTab(Screen.Analytics.route) },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.createRoute())
                }
            )
        }

        composable(Screen.Accounts.route) {
            AccountScreen()
        }

        composable(Screen.CreditCards.route) {
            CreditCardScreen()
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("sourceId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("type") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            AddTransactionScreen(
                onTransactionSaved = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.Dashboard,
        Screen.Accounts,
        Screen.CreditCards,
        Screen.Analytics
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.AddTransaction.route) {
                NavigationBar(
                    containerColor = Color(0xFF0F0F12),
                    contentColor = Color.White
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon!!, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = { navController.navigateToTab(item.route) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF6C5CE7),
                                selectedTextColor = Color(0xFF6C5CE7),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF6C5CE7).copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
