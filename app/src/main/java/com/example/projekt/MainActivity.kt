package com.example.projekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekt.data.db.StockDatabase
import com.example.projekt.data.repository.StockRepository
import com.example.projekt.ui.navigation.AppNavigation
import com.example.projekt.ui.theme.ProjektTheme
import com.example.projekt.ui.viewmodel.PortfolioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database   = StockDatabase.getInstance(applicationContext)
        val repository = StockRepository(
            holdingDao     = database.stockHoldingDao(),
            transactionDao = database.transactionDao()
        )

        setContent {
            ProjektTheme {
                val viewModel: PortfolioViewModel = viewModel(
                    factory = PortfolioViewModel.factory(repository)
                )
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}