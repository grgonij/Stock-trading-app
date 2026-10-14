package com.example.projekt.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.projekt.data.model.StockHolding
import com.example.projekt.data.model.Transaction
import com.example.projekt.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class PortfolioUiState {
    object Loading : PortfolioUiState()
    object Empty   : PortfolioUiState()
    data class Success(val holdings: List<StockHolding>) : PortfolioUiState()
}

sealed class TradeUiState {
    object Idle    : TradeUiState()
    object Loading : TradeUiState()
    object Success : TradeUiState()
    data class Error(val message: String) : TradeUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class PortfolioViewModel(
    private val repository: StockRepository
) : ViewModel() {

    // ── Portfolio state ───────────────────────────────────────────────────────

    val portfolioState: StateFlow<PortfolioUiState> =
        repository.allHoldings
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
            .let { holdingsFlow ->
                val uiStateFlow = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Loading)
                viewModelScope.launch {
                    holdingsFlow.collect { holdings ->
                        uiStateFlow.value = if (holdings.isEmpty())
                            PortfolioUiState.Empty
                        else
                            PortfolioUiState.Success(holdings)
                    }
                }
                uiStateFlow.asStateFlow()
            }

    // ── Sve transakcije (za HistoryScreen) ───────────────────────────────────

    val allTransactions = repository.allTransactions

    // ── Trade state ───────────────────────────────────────────────────────────

    private val _tradeState = MutableStateFlow<TradeUiState>(TradeUiState.Idle)
    val tradeState: StateFlow<TradeUiState> = _tradeState.asStateFlow()

    // ── Portfolio summary ─────────────────────────────────────────────────────

    fun totalValue(holdings: List<StockHolding>): Double =
        holdings.sumOf { it.shares * it.currentPrice }

    fun totalChange(holdings: List<StockHolding>): Double =
        holdings.sumOf { it.shares * (it.currentPrice - it.avgBuyPrice) }

    fun totalChangePercent(holdings: List<StockHolding>): Double {
        val cost = holdings.sumOf { it.shares * it.avgBuyPrice }
        return if (cost != 0.0) totalChange(holdings) / cost * 100.0 else 0.0
    }

    // ── Trade actions ─────────────────────────────────────────────────────────

    fun buy(
        ticker       : String,
        companyName  : String,
        shares       : Double,
        pricePerShare: Double,
        currentPrice : Double,
        note         : String = ""
    ) {
        viewModelScope.launch {
            _tradeState.value = TradeUiState.Loading
            try {
                repository.executeBuy(ticker, companyName, shares, pricePerShare, currentPrice, note)
                _tradeState.value = TradeUiState.Success
            } catch (e: Exception) {
                _tradeState.value = TradeUiState.Error(e.message ?: "Greška pri kupnji")
            }
        }
    }

    fun sell(
        ticker       : String,
        shares       : Double,
        pricePerShare: Double,
        note         : String = ""
    ) {
        viewModelScope.launch {
            _tradeState.value = TradeUiState.Loading
            try {
                repository.executeSell(ticker, shares, pricePerShare, note)
                _tradeState.value = TradeUiState.Success
            } catch (e: Exception) {
                _tradeState.value = TradeUiState.Error(e.message ?: "Greška pri prodaji")
            }
        }
    }

    fun resetTradeState() { _tradeState.value = TradeUiState.Idle }

    // ── Holding actions ───────────────────────────────────────────────────────

    fun deleteHolding(holding: StockHolding) {
        viewModelScope.launch { repository.deleteHolding(holding) }
    }

    // ── Transaction actions ───────────────────────────────────────────────────

    fun getTransactionsForTicker(ticker: String) =
        repository.getTransactionsForTicker(ticker)

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(repository: StockRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PortfolioViewModel(repository) as T
            }
    }
}