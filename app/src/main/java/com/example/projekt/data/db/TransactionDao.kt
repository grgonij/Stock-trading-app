package com.example.projekt.data.db

import androidx.room.*
import com.example.projekt.data.model.Transaction
import com.example.projekt.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: Transaction): Long

    // ── READ ──────────────────────────────────────────────────────────────────

    /** All transactions, newest first – good for a history feed. */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<Transaction>>

    /** All transactions for a specific stock. */
    @Query("""
        SELECT * FROM transactions
        WHERE stockTicker = :ticker
        ORDER BY timestamp DESC
    """)
    fun getByTickerFlow(ticker: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): Transaction?

    /** Sum of shares bought for a ticker (used by repo to recompute position). */
    @Query("""
        SELECT COALESCE(SUM(shares), 0.0) FROM transactions
        WHERE stockTicker = :ticker AND type = 'BUY'
    """)
    suspend fun totalSharesBought(ticker: String): Double

    /** Sum of shares sold for a ticker. */
    @Query("""
        SELECT COALESCE(SUM(shares), 0.0) FROM transactions
        WHERE stockTicker = :ticker AND type = 'SELL'
    """)
    suspend fun totalSharesSold(ticker: String): Double

    /**
     * Weighted average buy price = Σ(shares × price) / Σ(shares) for BUY rows.
     * Returns 0.0 if no buys exist.
     */
    @Query("""
        SELECT COALESCE(SUM(shares * pricePerShare) / NULLIF(SUM(shares), 0), 0.0)
        FROM transactions
        WHERE stockTicker = :ticker AND type = 'BUY'
    """)
    suspend fun weightedAvgBuyPrice(ticker: String): Double

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Edit the note on an existing transaction (only field user can change). */
    @Query("UPDATE transactions SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Int, note: String)

    @Update
    suspend fun update(transaction: Transaction)

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** Cascades automatically when parent StockHolding is deleted (FK CASCADE). */
    @Query("DELETE FROM transactions WHERE stockTicker = :ticker")
    suspend fun deleteByTicker(ticker: String)
}