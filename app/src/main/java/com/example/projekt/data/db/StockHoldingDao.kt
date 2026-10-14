package com.example.projekt.data.db
import androidx.room.*
import com.example.projekt.data.model.StockHolding
import com.example.projekt.data.model.StockWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface StockHoldingDao {

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Insert a new holding. Returns the generated row ID.
     * ABORT strategy: fails (throws) on duplicate ticker – handle in repo.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(holding: StockHolding): Long

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Observe all holdings ordered by total value descending.
     * Emits a new list every time any row changes – perfect for Compose.
     */
    @Query("SELECT * FROM stock_holdings ORDER BY (shares * currentPrice) DESC")
    fun getAllFlow(): Flow<List<StockHolding>>

    /** One-shot snapshot – use from repository when you don't need reactivity. */
    @Query("SELECT * FROM stock_holdings ORDER BY ticker ASC")
    suspend fun getAll(): List<StockHolding>

    @Query("SELECT * FROM stock_holdings WHERE id = :id")
    suspend fun getById(id: Int): StockHolding?

    @Query("SELECT * FROM stock_holdings WHERE ticker = :ticker")
    suspend fun getByTicker(ticker: String): StockHolding?

    /** Load holding + all its transactions in one go (uses @Relation). */
    @Transaction
    @Query("SELECT * FROM stock_holdings WHERE ticker = :ticker")
    suspend fun getWithTransactions(ticker: String): StockWithTransactions?

    /** All holdings with their transactions – for history screen. */
    @Transaction
    @Query("SELECT * FROM stock_holdings ORDER BY (shares * currentPrice) DESC")
    fun getAllWithTransactionsFlow(): Flow<List<StockWithTransactions>>

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Full row replace (shares, avgBuyPrice, currentPrice all at once). */
    @Update
    suspend fun update(holding: StockHolding)

    /** Partial update – only refreshes the current market price and timestamp. */
    @Query("""
        UPDATE stock_holdings
        SET currentPrice = :price, lastUpdated = :updatedAt
        WHERE ticker = :ticker
    """)
    suspend fun updatePrice(ticker: String, price: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * Recalculate shares and avgBuyPrice after a trade.
     * Called by the repository after inserting a Transaction.
     */
    @Query("""
        UPDATE stock_holdings
        SET shares       = :newShares,
            avgBuyPrice  = :newAvgPrice,
            lastUpdated  = :updatedAt
        WHERE ticker = :ticker
    """)
    suspend fun updatePosition(
        ticker      : String,
        newShares   : Double,
        newAvgPrice : Double,
        updatedAt   : Long = System.currentTimeMillis()
    )

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Delete
    suspend fun delete(holding: StockHolding)

    @Query("DELETE FROM stock_holdings WHERE ticker = :ticker")
    suspend fun deleteByTicker(ticker: String)

    @Query("DELETE FROM stock_holdings")
    suspend fun deleteAll()
}