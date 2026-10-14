package com.example.projekt.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projekt.R
import com.example.projekt.data.model.StockHolding
import com.example.projekt.ui.util.isLandscape
import com.example.projekt.ui.viewmodel.PortfolioUiState
import com.example.projekt.ui.viewmodel.PortfolioViewModel

private val BgDark      = Color(0xFF0D1117)
private val Surface1    = Color(0xFF161B22)
private val Surface2    = Color(0xFF21262D)
private val GreenUp     = Color(0xFF00C896)
private val RedDown     = Color(0xFFFF5252)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextMuted   = Color(0xFF8B949E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel   : PortfolioViewModel,
    onStockClick: (StockHolding) -> Unit = {},
    onAddClick  : () -> Unit = {}
) {
    val uiState  by viewModel.portfolioState.collectAsStateWithLifecycle()
    val landscape = isLandscape()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.nav_portfolio),
                        color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAddClick,
                containerColor = GreenUp,
                contentColor   = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.home_add_content_desc))
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is PortfolioUiState.Loading -> LoadingContent(innerPadding)
            is PortfolioUiState.Empty   -> EmptyContent(innerPadding, onAddClick)
            is PortfolioUiState.Success -> {
                if (landscape) {
                    // ── LANDSCAPE: summary lijevo, lista desno ────────────────
                    LandscapePortfolioContent(
                        holdings     = state.holdings,
                        viewModel    = viewModel,
                        onStockClick = onStockClick,
                        innerPadding = innerPadding
                    )
                } else {
                    // ── PORTRAIT: summary gore, lista ispod ───────────────────
                    PortraitPortfolioContent(
                        holdings     = state.holdings,
                        viewModel    = viewModel,
                        onStockClick = onStockClick,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

// ─── Portrait layout ──────────────────────────────────────────────────────────

@Composable
private fun PortraitPortfolioContent(
    holdings    : List<StockHolding>,
    viewModel   : PortfolioViewModel,
    onStockClick: (StockHolding) -> Unit,
    innerPadding: PaddingValues
) {
    val totalValue  = viewModel.totalValue(holdings)
    val totalChange = viewModel.totalChange(holdings)
    val changePct   = viewModel.totalChangePercent(holdings)
    val isPositive  = totalChange >= 0
    val sparkline   = buildPortfolioSparkline(holdings)

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PortfolioSummaryCard(totalValue, totalChange, changePct, isPositive, sparkline)
            Spacer(Modifier.height(8.dp))
        }
        item {
            Text(
                stringResource(R.string.home_my_holdings),
                color = TextMuted, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        items(holdings, key = { it.ticker }) { holding ->
            StockHoldingCard(holding = holding, onClick = { onStockClick(holding) })
        }
    }
}

// ─── Landscape layout ─────────────────────────────────────────────────────────

@Composable
private fun LandscapePortfolioContent(
    holdings    : List<StockHolding>,
    viewModel   : PortfolioViewModel,
    onStockClick: (StockHolding) -> Unit,
    innerPadding: PaddingValues
) {
    val totalValue  = viewModel.totalValue(holdings)
    val totalChange = viewModel.totalChange(holdings)
    val changePct   = viewModel.totalChangePercent(holdings)
    val isPositive  = totalChange >= 0
    val sparkline   = buildPortfolioSparkline(holdings)

    // Landscape: Row umjesto Column — summary panel lijevo, lista desno
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Lijeva kolona: summary (fiksna širina) ────────────────────────────
        Column(
            modifier            = Modifier
                .width(260.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top
        ) {
            PortfolioSummaryCard(totalValue, totalChange, changePct, isPositive, sparkline)
        }

        // ── Desna kolona: lista dionica (ostatak širine) ──────────────────────
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text(
                stringResource(R.string.home_my_holdings),
                color = TextMuted, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(holdings, key = { it.ticker }) { holding ->
                    StockHoldingCard(holding = holding, onClick = { onStockClick(holding) })
                }
            }
        }
    }
}

// ─── Loading / Empty ──────────────────────────────────────────────────────────

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
        CircularProgressIndicator(color = GreenUp)
    }
}

@Composable
private fun EmptyContent(padding: PaddingValues, onAddClick: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📈", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.home_empty_title),
            color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.home_empty_subtitle), color = TextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors  = ButtonDefaults.buttonColors(containerColor = GreenUp)
        ) {
            Text(stringResource(R.string.home_add_stock), color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Summary card ─────────────────────────────────────────────────────────────

@Composable
private fun PortfolioSummaryCard(
    totalValue : Double,
    totalChange: Double,
    changePct  : Double,
    isPositive : Boolean,
    sparkline  : List<Float>
) {
    val changeColor = if (isPositive) GreenUp else RedDown
    val sign        = if (isPositive) "+" else ""

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface1),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.home_total_value), color = TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "$%,.2f".format(totalValue),
                    color = TextPrimary, fontSize = 28.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = changeColor.copy(alpha = 0.12f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$sign${"%.2f".format(changePct)}%",
                                color = changeColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                            )
                            Text("·", color = changeColor.copy(0.5f), fontSize = 12.sp)
                            Text(
                                sign + "$" + "%,.2f".format(totalChange),
                                color = changeColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.home_today), color = TextMuted, fontSize = 12.sp)
                }
            }
            if (sparkline.size >= 2) {
                PortfolioSparkline(
                    data     = sparkline,
                    color    = changeColor,
                    modifier = Modifier.fillMaxWidth().height(70.dp)
                )
            }
        }
    }
}

// ─── Stock card ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockHoldingCard(holding: StockHolding, onClick: () -> Unit) {
    val isPositive  = holding.totalGain >= 0
    val changeColor = if (isPositive) GreenUp else RedDown
    val sign        = if (isPositive) "+" else ""

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface1),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(changeColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(holding.ticker.take(2), color = changeColor,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(holding.ticker, color = TextPrimary,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(holding.companyName, color = TextMuted,
                    fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            MiniSparkline(isPositive = isPositive, modifier = Modifier.width(52.dp).height(30.dp))
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("$%,.2f".format(holding.currentPrice),
                    color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("$sign${"%.2f".format(holding.totalGainPercent)}%",
                    color = changeColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── Sparkline helpers ────────────────────────────────────────────────────────

private fun buildPortfolioSparkline(holdings: List<StockHolding>): List<Float> {
    if (holdings.isEmpty()) return emptyList()
    val steps = 10
    return (0 until steps).map { i ->
        holdings.sumOf { h ->
            val progress = i.toDouble() / (steps - 1)
            (h.avgBuyPrice + (h.currentPrice - h.avgBuyPrice) * progress) * h.shares
        }.toFloat()
    }
}

@Composable
private fun PortfolioSparkline(data: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val minV   = data.min()
        val maxV   = data.max()
        val range  = (maxV - minV).coerceAtLeast(0.01f)
        val topPad = size.height * 0.08f
        val step   = size.width / (data.size - 1)
        val pts    = data.mapIndexed { i, v ->
            Offset(i * step, topPad + (1f - (v - minV) / range) * (size.height - topPad))
        }
        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fillPath = Path().apply {
            addPath(linePath); lineTo(pts.last().x, size.height); lineTo(0f, size.height); close()
        }
        drawPath(fillPath, brush = Brush.verticalGradient(
            listOf(color.copy(alpha = 0.25f), Color.Transparent)))
        drawPath(linePath, color = color,
            style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, 4.dp.toPx(), pts.last())
        drawCircle(color.copy(0.25f), 8.dp.toPx(), pts.last())
    }
}

@Composable
private fun MiniSparkline(isPositive: Boolean, modifier: Modifier = Modifier) {
    val color = if (isPositive) GreenUp else RedDown
    val data  = if (isPositive)
        listOf(0.3f, 0.4f, 0.35f, 0.5f, 0.6f, 0.55f, 0.7f, 0.8f, 0.75f, 0.9f)
    else
        listOf(0.9f, 0.8f, 0.85f, 0.7f, 0.6f, 0.65f, 0.5f, 0.4f, 0.45f, 0.3f)
    Canvas(modifier = modifier) {
        val step = size.width / (data.size - 1)
        val pts  = data.mapIndexed { i, v -> Offset(i * step, size.height - v * size.height) }
        val path = Path().apply {
            moveTo(pts.first().x, pts.first().y); pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color = color,
            style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}