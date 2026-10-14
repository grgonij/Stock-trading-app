package com.example.projekt.data.repository

import com.example.projekt.data.db.StockHoldingDao
import com.example.projekt.data.db.TransactionDao
import com.example.projekt.data.model.StockHolding
import com.example.projekt.data.model.StockWithTransactions
import com.example.projekt.data.model.Transaction
import com.example.projekt.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for all stock and transaction data.
 *
 * ViewModels talk only to this class – never to DAOs directly.
 * All suspend functions must be called from a coroutine (e.g. viewModelScope).
 */
class StockRepository(
    private val holdingDao    : StockHoldingDao,
    private val transactionDao: TransactionDao
) {

    // ── Flows (reactive – for Compose) ────────────────────────────────────────

    /** Emits the full portfolio list whenever any holding changes. */
    val allHoldings: Flow<List<StockHolding>> = holdingDao.getAllFlow()

    /** All transactions, newest first. */
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllFlow()

    /** Holdings with their full transaction history. */
    val allHoldingsWithTransactions: Flow<List<StockWithTransactions>> =
        holdingDao.getAllWithTransactionsFlow()

    // ── Holdings CRUD ─────────────────────────────────────────────────────────

    suspend fun getHoldingByTicker(ticker: String): StockHolding? =
        holdingDao.getByTicker(ticker)

    suspend fun getHoldingWithTransactions(ticker: String): StockWithTransactions? =
        holdingDao.getWithTransactions(ticker)

    suspend fun updateCurrentPrice(ticker: String, price: Double) =
        holdingDao.updatePrice(ticker, price)

    suspend fun deleteHolding(holding: StockHolding) =
        holdingDao.delete(holding)        // cascade deletes its transactions too

    // ── Trade execution ───────────────────────────────────────────────────────

    /**
     * Execute a BUY trade:
     * 1. Insert the Transaction row.
     * 2. If the holding doesn't exist yet → create it.
     *    If it already exists → recalculate shares and weighted avg price.
     *
     * @throws IllegalArgumentException if shares or price are ≤ 0.
     */
    suspend fun executeBuy(
        ticker       : String,
        companyName  : String,
        shares       : Double,
        pricePerShare: Double,
        currentPrice : Double,
        note         : String = ""
    ) {
        require(shares > 0)        { "Shares must be positive" }
        require(pricePerShare > 0) { "Price must be positive" }

        val existing = holdingDao.getByTicker(ticker)

        if (existing == null) {
            holdingDao.insert(
                StockHolding(
                    ticker       = ticker,
                    companyName  = companyName,
                    shares       = shares,
                    avgBuyPrice  = pricePerShare,
                    currentPrice = currentPrice
                )
            )
            transactionDao.insert(
                Transaction(
                    stockTicker   = ticker,
                    type          = TransactionType.BUY,
                    shares        = shares,
                    pricePerShare = pricePerShare,
                    note          = note
                )
            )
        } else {
            // Holding već postoji — transakcija može ići odmah
            transactionDao.insert(
                Transaction(
                    stockTicker   = ticker,
                    type          = TransactionType.BUY,
                    shares        = shares,
                    pricePerShare = pricePerShare,
                    note          = note
                )
            )
            val newShares   = existing.shares + shares
            val newAvgPrice = transactionDao.weightedAvgBuyPrice(ticker)
            holdingDao.updatePosition(ticker, newShares, newAvgPrice)
        }
    }

    /**
     * Execute a SELL trade:
     * 1. Validate the user owns enough shares.
     * 2. Insert the Transaction row.
     * 3. Reduce the holding's share count.
     *    If shares reach 0 → delete the holding entirely.
     *
     * @throws IllegalStateException if the user doesn't have enough shares.
     */
    suspend fun executeSell(
        ticker       : String,
        shares       : Double,
        pricePerShare: Double,
        note         : String = ""
    ) {
        require(shares > 0)        { "Shares must be positive" }
        require(pricePerShare > 0) { "Price must be positive" }

        val existing = holdingDao.getByTicker(ticker)
            ?: throw IllegalStateException("No holding found for $ticker")

        check(existing.shares >= shares) {
            "Cannot sell $shares shares – only ${existing.shares} held"
        }

        // 1. Record the transaction
        transactionDao.insert(
            Transaction(
                stockTicker   = ticker,
                type          = TransactionType.SELL,
                shares        = shares,
                pricePerShare = pricePerShare,
                note          = note
            )
        )

        // 2. Update or remove the holding
        val remaining = existing.shares - shares
        if (remaining <= 0.0) {
            holdingDao.deleteByTicker(ticker)           // also cascades transactions
        } else {
            val newAvgPrice = transactionDao.weightedAvgBuyPrice(ticker)
            holdingDao.updatePosition(ticker, remaining, newAvgPrice)
        }
    }

    // ── Transaction CRUD ──────────────────────────────────────────────────────

    fun getTransactionsForTicker(ticker: String): Flow<List<Transaction>> =
        transactionDao.getByTickerFlow(ticker)

    suspend fun updateTransactionNote(id: Int, note: String) =
        transactionDao.updateNote(id, note)

    /**
     * Delete a transaction and recompute the parent holding's position.
     * Use carefully – deleting a trade rewrites the cost basis.
     */
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)

        val newShares   = transactionDao.totalSharesBought(transaction.stockTicker) -
                transactionDao.totalSharesSold(transaction.stockTicker)
        val newAvgPrice = transactionDao.weightedAvgBuyPrice(transaction.stockTicker)

        if (newShares <= 0.0) {
            holdingDao.deleteByTicker(transaction.stockTicker)
        } else {
            holdingDao.updatePosition(transaction.stockTicker, newShares, newAvgPrice)
        }
    }
}