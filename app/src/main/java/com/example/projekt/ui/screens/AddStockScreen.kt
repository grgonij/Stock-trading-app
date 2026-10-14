package com.example.projekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projekt.R
import com.example.projekt.ui.util.isLandscape
import com.example.projekt.ui.viewmodel.PortfolioViewModel
import com.example.projekt.ui.viewmodel.TradeUiState

private val BgDark      = Color(0xFF0D1117)
private val Surface2    = Color(0xFF21262D)
private val GreenUp     = Color(0xFF00C896)
private val RedDown     = Color(0xFFFF5252)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextMuted   = Color(0xFF8B949E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(
    viewModel  : PortfolioViewModel,
    onBackClick: () -> Unit
) {
    var ticker       by remember { mutableStateOf("") }
    var companyName  by remember { mutableStateOf("") }
    var shares       by remember { mutableStateOf("") }
    var buyPrice     by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var note         by remember { mutableStateOf("") }
    var errorMsg     by remember { mutableStateOf("") }

    val tradeState  by viewModel.tradeState.collectAsStateWithLifecycle()
    val landscape    = isLandscape()

    val errTicker       = stringResource(R.string.add_error_ticker)
    val errCompany      = stringResource(R.string.add_error_company)
    val errShares       = stringResource(R.string.add_error_shares)
    val errBuyPrice     = stringResource(R.string.add_error_buy_price)
    val errCurrentPrice = stringResource(R.string.add_error_current_price)

    LaunchedEffect(tradeState) {
        if (tradeState is TradeUiState.Success) {
            viewModel.resetTradeState()
            onBackClick()
        }
    }

    val onConfirm: () -> Unit = {
        errorMsg = ""
        val sharesVal  = shares.toDoubleOrNull()
        val buyVal     = buyPrice.toDoubleOrNull()
        val currentVal = currentPrice.toDoubleOrNull()
        when {
            ticker.isBlank()                      -> errorMsg = errTicker
            companyName.isBlank()                 -> errorMsg = errCompany
            sharesVal == null || sharesVal <= 0   -> errorMsg = errShares
            buyVal    == null || buyVal    <= 0   -> errorMsg = errBuyPrice
            currentVal == null || currentVal <= 0 -> errorMsg = errCurrentPrice
            else -> viewModel.buy(
                ticker        = ticker.trim(),
                companyName   = companyName.trim(),
                shares        = sharesVal,
                pricePerShare = buyVal,
                currentPrice  = currentVal,
                note          = note.trim()
            )
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.add_title),
                        color = TextPrimary, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                            tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->
        if (landscape) {
            // ── LANDSCAPE: dva stupca ─────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lijeva kolona — ticker, naziv, bilješka
                Column(
                    modifier            = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.add_subtitle),
                        color = TextMuted, fontSize = 13.sp)
                    StyledTextField(ticker,      { ticker = it.uppercase().take(5) }, stringResource(R.string.add_ticker))
                    StyledTextField(companyName, { companyName = it },                stringResource(R.string.add_company_name))
                    StyledTextField(note,        { note = it },                       stringResource(R.string.add_note))
                }

                // Desna kolona — dionice, cijene, gumb
                Column(
                    modifier            = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StyledTextField(shares,       { shares = it },       stringResource(R.string.add_shares))
                    StyledTextField(buyPrice,     { buyPrice = it },     stringResource(R.string.add_buy_price))
                    StyledTextField(currentPrice, { currentPrice = it }, stringResource(R.string.add_current_price))

                    if (errorMsg.isNotBlank()) {
                        Text(errorMsg, color = RedDown, fontSize = 12.sp)
                    }
                    if (tradeState is TradeUiState.Error) {
                        Text((tradeState as TradeUiState.Error).message, color = RedDown, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = GreenUp),
                        onClick  = onConfirm
                    ) {
                        if (tradeState is TradeUiState.Loading) {
                            CircularProgressIndicator(color = Color.Black,
                                modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.add_confirm),
                                color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        } else {
            // ── PORTRAIT: jedan stupac ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.add_subtitle),
                    color = TextMuted, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp))

                StyledTextField(ticker,       { ticker = it.uppercase().take(5) }, stringResource(R.string.add_ticker))
                StyledTextField(companyName,  { companyName = it },                stringResource(R.string.add_company_name))
                StyledTextField(shares,       { shares = it },                     stringResource(R.string.add_shares))
                StyledTextField(buyPrice,     { buyPrice = it },                   stringResource(R.string.add_buy_price))
                StyledTextField(currentPrice, { currentPrice = it },               stringResource(R.string.add_current_price))
                StyledTextField(note,         { note = it },                       stringResource(R.string.add_note))

                if (errorMsg.isNotBlank()) {
                    Text(errorMsg, color = RedDown, fontSize = 12.sp)
                }
                if (tradeState is TradeUiState.Error) {
                    Text((tradeState as TradeUiState.Error).message, color = RedDown, fontSize = 12.sp)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenUp),
                    onClick  = onConfirm
                ) {
                    if (tradeState is TradeUiState.Loading) {
                        CircularProgressIndicator(color = Color.Black,
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.add_confirm),
                            color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StyledTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, color = TextMuted) },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color(0xFF1AAB8B),
            unfocusedBorderColor = Surface2,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = Color(0xFF1AAB8B)
        )
    )
}