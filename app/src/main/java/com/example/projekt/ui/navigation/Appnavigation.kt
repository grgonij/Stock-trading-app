package com.example.projekt.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projekt.ui.screens.*
import com.example.projekt.ui.viewmodel.PortfolioViewModel

@Composable
fun AppNavigation(viewModel: PortfolioViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color(0xFF0D1117),
        bottomBar      = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel    = viewModel,
                    onStockClick = { holding ->
                        navController.navigate(Screen.StockDetail.createRoute(holding.ticker))
                    },
                    onAddClick = {
                        navController.navigate(Screen.AddStock.route)
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route     = Screen.StockDetail.route,
                arguments = listOf(navArgument("ticker") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticker = backStackEntry.arguments?.getString("ticker") ?: return@composable
                StockDetailScreen(
                    ticker      = ticker,
                    viewModel   = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AddStock.route) {
                AddStockScreen(
                    viewModel   = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}