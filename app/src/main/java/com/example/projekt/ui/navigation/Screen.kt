package com.example.projekt.ui.navigation

/**
 * Sve rute u aplikaciji
 */
sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object History  : Screen("history")
    object Settings : Screen("settings")

    // Ekrani s argumentima
    object StockDetail : Screen("stock_detail/{ticker}") {
        fun createRoute(ticker: String) = "stock_detail/$ticker"
    }
    object AddStock : Screen("add_stock")
}