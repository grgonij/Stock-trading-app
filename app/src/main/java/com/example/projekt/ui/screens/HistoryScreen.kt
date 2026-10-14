package com.example.projekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

private enum class HistoryFilter { ALL, BUY, SELL }

// Eksplicitna lista umjesto .entries (kompatibilno sa starijim Kotlinom)
private val allFilters = listOf(HistoryFilter.ALL, HistoryFilter.BUY, HistoryFilter.SELL)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: PortfolioViewModel) {

    // Ispravan collect — allTransactions je Flow<List<Transaction>>
    val transactions by viewModel.allTransactions
        .collectAsStateWithLifecycle(initialValue = emptyList<Transaction>())

    var activeFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val filtered: List<Transaction> = when (activeFilter) {
        HistoryFilter.ALL  -> transactions
        HistoryFilter.BUY  -> transactions.filter { it.type == TransactionType.BUY }
        HistoryFilter.SELL -> transactions.filter { it.type == TransactionType.SELL }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.history_title),
                        color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Filter chips ──────────────────────────────────────────────────
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allFilters) { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick  = { activeFilter = filter },
                        label    = {
                            Text(
                                when (filter) {
                                    HistoryFilter.ALL  -> "Sve"
                                    HistoryFilter.BUY  -> stringResource(R.string.history_buy_label)
                                    HistoryFilter.SELL -> stringResource(R.string.history_sell_label)
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = GreenUp.copy(alpha = 0.15f),
                            selectedLabelColor       = GreenUp
                        )
                    )
                }
            }

            // ── Summary ───────────────────────────────────────────────────────
            if (filtered.isNotEmpty()) {
                SummaryRow(transactions = filtered)
            }

            // ── Lista / Empty state ───────────────────────────────────────────
            if (filtered.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.history_empty),
                            color = TextMuted, fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Eksplicitni tip — key lambda prima Transaction
                    items(
                        items = filtered,
                        key   = { transaction: Transaction -> transaction.id }
                    ) { transaction: Transaction ->
                        HistoryTransactionCard(
                            transaction = transaction,
                            onDelete    = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ─── Summary row ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryRow(transactions: List<Transaction>) {
    val totalBought = transactions
        .filter { it.type == TransactionType.BUY }
        .sumOf { it.totalAmount }
    val totalSold = transactions
        .filter { it.type == TransactionType.SELL }
        .sumOf { it.totalAmount }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip("Kupljeno", "$%,.2f".format(totalBought), GreenUp, Modifier.weight(1f))
        SummaryChip("Prodano",  "$%,.2f".format(totalSold),   RedDown, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        color    = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = color,     fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Transaction card ─────────────────────────────────────────────────────────

@Composable
private fun HistoryTransactionCard(
    transaction: Transaction,
    onDelete   : () -> Unit
) {
    val isPositive = transaction.type == TransactionType.BUY
    val color      = if (isPositive) GreenUp else RedDown
    val label      = if (isPositive)
        stringResource(R.string.history_buy_label)
    else
        stringResource(R.string.history_sell_label)

    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface1),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(0.12f)) {
                Text(
                    label,
                    color      = color,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.stockTicker,
                    color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
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
                    "%.4f".format(transaction.shares),
                    color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
                Text(
                    "$%,.2f".format(transaction.pricePerShare),
                    color = TextMuted, fontSize = 11.sp
                )
                Text(
                    "$%,.2f".format(transaction.totalAmount),
                    color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = { showConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.detail_delete),
                    tint               = TextMuted,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = Surface1,
            title = { Text("Obriši transakciju", color = TextPrimary) },
            text  = {
                Text(
                    "Jesi li siguran? Ovo će rekalkulirati poziciju dionice ${transaction.stockTicker}.",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showConfirm = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = RedDown)
                ) {
                    Text("Obriši", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.trade_cancel), color = TextMuted)
                }
            }
        )
    }
}