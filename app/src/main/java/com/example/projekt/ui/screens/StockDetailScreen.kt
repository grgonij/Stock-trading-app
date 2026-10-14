package com.example.projekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projekt.R
import com.example.projekt.data.model.Transaction
import com.example.projekt.data.model.TransactionType
import com.example.projekt.ui.viewmodel.PortfolioUiState
import com.example.projekt.ui.viewmodel.PortfolioViewModel
import java.text.SimpleDateFormat
import java.util.*

private val BgDark      = Color(0xFF0D1117)
private val Surface1    = Color(0xFF161B22)
private val Surface2    = Color(0xFF21262D)
private val GreenUp     = Color(0xFF00C896)
private val RedDown     = Color(0xFFFF5252)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextMuted   = Color(0xFF8B949E)

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    ticker     : String,
    viewModel  : PortfolioViewModel,
    onBackClick: () -> Unit
) {
    val portfolioState by viewModel.portfolioState.collectAsStateWithLifecycle()
    val transactions   by viewModel.getTransactionsForTicker(ticker)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val holding = (portfolioState as? PortfolioUiState.Success)
        ?.holdings?.find { it.ticker == ticker }

    var showBuyDialog  by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(ticker, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->

        if (holding == null) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.detail_not_found), color = TextMuted)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding      = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Info kartica ──────────────────────────────────────────────────
            item {
                val isPositive  = holding.totalGain >= 0
                val changeColor = if (isPositive) GreenUp else RedDown
                val sign        = if (isPositive) "+" else ""

                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Surface1),
                    border    = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(holding.companyName, color = TextMuted, fontSize = 13.sp)
                        Text(
                            "$%,.2f".format(holding.currentPrice),
                            color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = changeColor.copy(0.12f)
                        ) {
                            Text(
                                "$sign${"%.2f".format(holding.totalGainPercent)}%  " +
                                        "$sign$${",.2f".format(holding.totalGain)}",
                                color      = changeColor,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        HorizontalDivider(
                            color    = Surface2,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoItem(stringResource(R.string.detail_shares),    "%.4f".format(holding.shares))
                            InfoItem(stringResource(R.string.detail_avg_price), "$%,.2f".format(holding.avgBuyPrice))
                            InfoItem(stringResource(R.string.detail_total),     "$%,.2f".format(holding.totalValue))
                        }
                    }
                }
            }

            // ── Kupi / Prodaj ─────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick  = { showBuyDialog = true },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = GreenUp)
                    ) {
                        Text(
                            stringResource(R.string.detail_buy),
                            color = Color.Black, fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick  = { showSellDialog = true },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = RedDown),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, RedDown)
                    ) {
                        Text(
                            stringResource(R.string.detail_sell),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Naslov transakcija ────────────────────────────────────────────
            item {
                Text(
                    stringResource(R.string.detail_history_title),
                    color         = TextMuted,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier      = Modifier.padding(horizontal = 4.dp)
                )
            }

            // ── Lista transakcija ─────────────────────────────────────────────
            if (transactions.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.detail_no_transactions),
                        color    = TextMuted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onDelete    = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    // ── Dijalozi ──────────────────────────────────────────────────────────────
    if (showBuyDialog && holding != null) {
        TradeDialog(
            title        = stringResource(R.string.trade_buy_title, holding.ticker),
            confirmText  = stringResource(R.string.trade_confirm_buy),
            confirmColor = GreenUp,
            onDismiss    = { showBuyDialog = false },
            onConfirm    = { shares, price, note ->
                viewModel.buy(
                    ticker        = holding.ticker,
                    companyName   = holding.companyName,
                    shares        = shares,
                    pricePerShare = price,
                    currentPrice  = price,
                    note          = note
                )
                showBuyDialog = false
            }
        )
    }

    if (showSellDialog && holding != null) {
        TradeDialog(
            title        = stringResource(R.string.trade_sell_title, holding.ticker),
            confirmText  = stringResource(R.string.trade_confirm_sell),
            confirmColor = RedDown,
            onDismiss    = { showSellDialog = false },
            onConfirm    = { shares, price, note ->
                viewModel.sell(holding.ticker, shares, price, note)
                showSellDialog = false
            }
        )
    }
}

// ─── Pomoćni Composableovi ────────────────────────────────────────────────────

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted,   fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, onDelete: () -> Unit) {
    val isPositive = transaction.type == TransactionType.BUY
    val color      = if (isPositive) GreenUp else RedDown
    val label      = if (isPositive)
        stringResource(R.string.history_buy_label)
    else
        stringResource(R.string.history_sell_label)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface1),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = color.copy(0.12f)) {
                Text(
                    label,
                    color      = color,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.detail_shares_format, transaction.shares),
                    color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
                Text(
                    dateFormat.format(Date(transaction.timestamp)),
                    color = TextMuted, fontSize = 11.sp
                )
                if (transaction.note.isNotBlank()) {
                    Text(transaction.note, color = TextMuted, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$%,.2f".format(transaction.pricePerShare),
                    color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$%,.2f".format(transaction.totalAmount),
                    color = TextMuted, fontSize = 11.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.detail_delete),
                    tint               = TextMuted,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TradeDialog(
    title       : String,
    confirmText : String,
    confirmColor: Color,
    onDismiss   : () -> Unit,
    onConfirm   : (shares: Double, price: Double, note: String) -> Unit
) {
    var sharesText by remember { mutableStateOf("") }
    var priceText  by remember { mutableStateOf("") }
    var note       by remember { mutableStateOf("") }
    var error      by remember { mutableStateOf("") }

    val errShares = stringResource(R.string.trade_error_shares)
    val errPrice  = stringResource(R.string.trade_error_price)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Surface1,
        title  = { Text(title, color = TextPrimary) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = sharesText,
                    onValueChange = { sharesText = it; error = "" },
                    label         = { Text(stringResource(R.string.trade_shares_label)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = priceText,
                    onValueChange = { priceText = it; error = "" },
                    label         = { Text(stringResource(R.string.trade_price_label)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text(stringResource(R.string.trade_note_label)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Text(error, color = RedDown, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                colors  = ButtonDefaults.buttonColors(containerColor = confirmColor),
                onClick = {
                    val shares = sharesText.toDoubleOrNull()
                    val price  = priceText.toDoubleOrNull()
                    when {
                        shares == null || shares <= 0 -> error = errShares
                        price  == null || price  <= 0 -> error = errPrice
                        else -> onConfirm(shares, price, note)
                    }
                }
            ) {
                Text(confirmText, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.trade_cancel), color = TextMuted)
            }
        }
    )
}