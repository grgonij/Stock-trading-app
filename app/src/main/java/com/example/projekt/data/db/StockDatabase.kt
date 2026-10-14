package com.example.projekt.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projekt.data.model.StockHolding
import com.example.projekt.data.model.Transaction
import com.example.projekt.data.model.TransactionType

// ─── Type Converters ──────────────────────────────────────────────────────────

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities  = [StockHolding::class, Transaction::class],
    version   = 1,
    exportSchema = true                 // keeps schema.json for migration diffs
)
@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {

    abstract fun stockHoldingDao(): StockHoldingDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getInstance(context: Context): StockDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                StockDatabase::class.java,
                "stock_trading.db"
            )
                .build()
    }
}