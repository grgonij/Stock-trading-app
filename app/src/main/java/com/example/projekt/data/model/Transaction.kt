package com.example.projekt.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

enum class TransactionType { BUY, SELL }

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity        = StockHolding::class,
            parentColumns = ["ticker"],
            childColumns  = ["stockTicker"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stockTicker")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id            : Int             = 0,
    val stockTicker   : String,
    val type          : TransactionType,
    val shares        : Double,
    val pricePerShare : Double,
    val timestamp     : Long = System.currentTimeMillis(),
    val note          : String = ""
) {
    val totalAmount: Double get() = shares * pricePerShare
}
