package com.example.projekt.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_holdings",
    indices = [Index(value = ["ticker"], unique = true)]
)

data class StockHolding(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val ticker : String,
    val companyName : String,
    val shares : Double,
    val avgBuyPrice : Double,
    val currentPrice : Double,
    val lastUpdated : Long = System.currentTimeMillis()
){
    val totalValue: Double get() = shares * currentPrice

    val totalGain: Double get() = shares * (currentPrice - avgBuyPrice)

    val totalGainPercent: Double
        get() = if (avgBuyPrice != 0.0)
            (currentPrice - avgBuyPrice) / avgBuyPrice * 100.0
        else 0.0
}