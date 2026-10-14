package com.example.projekt.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class StockWithTransactions(
    @Embedded
    val holding: StockHolding,

    @Relation(
        parentColumn = "ticker",
        entityColumn = "stockTicker"
    )

    val transactions: List<Transaction>
)

