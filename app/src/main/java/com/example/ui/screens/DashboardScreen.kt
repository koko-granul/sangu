package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.i18n.AppStrings
import com.example.domain.model.TransactionDetail
import com.example.ui.FinanceViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoundIconButton
import com.example.ui.components.SoftCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    listState: LazyListState,
    onOpenAddTransaction: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onScrollDirectionChange: (Boolean) -> Unit = {}
) {
    val dynamicTheme = LocalDynamicTheme.current

    val userName by viewModel.settingsManager.userName.collectAsState()
    val language by viewModel.settingsManager.language.collectAsState()
    val userProfileUri by viewModel.settingsManager.userProfileUri.collectAsState()

    val accounts by viewModel.accountsWithBalances.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val cashFlow by viewModel.cashFlowSummary.collectAsState()
    val isAmountHidden by viewModel.isAmountHidden.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = AppStrings.getGreeting(language, hour, userName)

    var selectedTransactionForDetail by remember { mutableStateOf<TransactionDetail?>(null) }
    val recentList = remember(transactions) { transactions.take(6) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Header Profil & Brand Sangu
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sangu",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = dynamicTheme.textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(dynamicTheme.accent)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = dynamicTheme.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Sembunyikan / Tampilkan Nominal
                    RoundIconButton(
                        icon = if (isAmountHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Privasi Saldo",
                        onClick = { viewModel.toggleAmountVisibility() },
                        size = 42.dp,
                        backgroundColor = dynamicTheme.surfaceSecondary,
                        iconTint = dynamicTheme.textPrimary
                    )

                    // Avatar Foto Profil (Klik untuk membuka Pengaturan / Profil)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(dynamicTheme.surfaceSecondary)
                            .clickable { onNavigateToTab(4) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfileUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfileUri,
                                contentDescription = "Foto Profil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto Profil",
                                tint = dynamicTheme.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Kartu Total Kekayaan
        item {
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 3.dp,
                backgroundColor = dynamicTheme.surface
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.totalBalance(language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = dynamicTheme.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isAmountHidden) "Rp ••••••••" else formatRupiah(cashFlow.totalNetWorth),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = dynamicTheme.textPrimary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rincian Akun Mini
                    if (accounts.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            accounts.take(3).forEach { acc ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(dynamicTheme.surfaceSecondary)
                                        .padding(vertical = 8.dp, horizontal = 10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = acc.account.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = dynamicTheme.textSecondary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (isAmountHidden) "Rp •••" else formatRupiah(acc.balance),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = dynamicTheme.textPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grafik Riwayat & Tren Saldo Riil (Otomatis Flat saat saldo 0 / belum ada transaksi)
                    DashboardBalanceTrendChart(
                        totalNetWorth = cashFlow.totalNetWorth,
                        transactions = transactions,
                        isAmountHidden = isAmountHidden,
                        language = language
                    )
                }
            }
        }

        // 3. Bar Aksi Cepat (Quick Actions)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = AppStrings.record(language),
                    onClick = { onOpenAddTransaction("EXPENSE") }
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = AppStrings.transfer(language),
                    onClick = { onOpenAddTransaction("TRANSFER") }
                )
                QuickActionButton(
                    icon = Icons.Default.Savings,
                    label = AppStrings.savingsPots(language),
                    onClick = { onNavigateToTab(2) }
                )
                QuickActionButton(
                    icon = Icons.Default.Handshake,
                    label = AppStrings.debts(language),
                    onClick = { onNavigateToTab(4) }
                )
            }
        }

        // 4. Kartu Arus Kas
        item {
            Spacer(modifier = Modifier.height(22.dp))
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                elevation = 2.dp,
                backgroundColor = dynamicTheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.monthlyCashflow(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (cashFlow.netSurplus >= 0) dynamicTheme.accentLight else Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (cashFlow.netSurplus >= 0) "Surplus" else "Defisit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cashFlow.netSurplus >= 0) dynamicTheme.accent else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pemasukan
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dynamicTheme.accent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.income(language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dynamicTheme.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAmountHidden) "Rp •••" else formatRupiah(cashFlow.totalIncome),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.accent
                            )
                        }

                        // Pengeluaran
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dynamicTheme.textPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.expense(language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dynamicTheme.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAmountHidden) "Rp •••" else formatRupiah(cashFlow.totalExpense),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // 5. Header Daftar Transaksi Terakhir
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.recentTransactions(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textPrimary
                )
                Text(
                    text = AppStrings.viewAll(language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = dynamicTheme.accent,
                    modifier = Modifier.clickable { onNavigateToTab(1) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 6. Daftar Transaksi Terakhir
        if (recentList.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Receipt,
                    title = AppStrings.noTransactions(language),
                    description = if (language == "en") "Start recording your daily expenses and income." else "Mulai mencatat pengeluaran atau pemasukan harian Anda."
                )
            }
        } else {
            items(
                items = recentList,
                key = { it.id },
                contentType = { "tx_row" }
            ) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    isAmountHidden = isAmountHidden,
                    onClick = { selectedTransactionForDetail = tx },
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    selectedTransactionForDetail?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            viewModel = viewModel,
            onDismiss = { selectedTransactionForDetail = null }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(dynamicTheme.surface)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(dynamicTheme.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = dynamicTheme.accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = dynamicTheme.textPrimary
        )
    }
}

private val transactionDateFormatThreadLocal = ThreadLocal.withInitial {
    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
}

fun formatDate(millis: Long): String {
    val sdf = transactionDateFormatThreadLocal.get() ?: SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun TransactionRowItem(
    transaction: TransactionDetail,
    isAmountHidden: Boolean,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val dynamicTheme = LocalDynamicTheme.current

    val formattedAmount = remember(transaction.amount, transaction.type, isAmountHidden) {
        if (isAmountHidden) {
            "Rp •••"
        } else {
            val prefix = when (transaction.type) {
                "INCOME" -> "+"
                "EXPENSE" -> "-"
                else -> ""
            }
            "$prefix${formatRupiah(transaction.amount)}"
        }
    }

    val formattedDate = remember(transaction.dateMillis) {
        formatDate(transaction.dateMillis)
    }

    val accountTag = remember(transaction.type, transaction.sourceAccount?.name, transaction.destinationAccount?.name) {
        when (transaction.type) {
            "TRANSFER" -> "${transaction.sourceAccount?.name ?: "?"} → ${transaction.destinationAccount?.name ?: "?"}"
            "INCOME" -> transaction.destinationAccount?.name ?: "Akun"
            else -> transaction.sourceAccount?.name ?: "Akun"
        }
    }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 1.dp,
        backgroundColor = dynamicTheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Jenis Transaksi
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(dynamicTheme.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        "INCOME" -> Icons.Default.Payments
                        "EXPENSE" -> Icons.Default.Receipt
                        else -> Icons.Default.SwapHoriz
                    },
                    contentDescription = null,
                    tint = if (transaction.type == "INCOME") dynamicTheme.accent else dynamicTheme.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Kolom Tengah: Deskripsi & Akun Sumber/Tujuan
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Baris 1: Judul Transaksi & Badge Foto Bukti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = dynamicTheme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Indikator ikon profesional jika ada lampiran foto struk
                    if (transaction.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(dynamicTheme.accentLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Lampiran Foto",
                                tint = dynamicTheme.onAccentLight,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Baris 2: Akun Tag
                Text(
                    text = accountTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = dynamicTheme.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Kolom Kanan: Nominal dan Tanggal & Jam (Rata Kanan Proporsional)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val isIncome = transaction.type == "INCOME"
                val amountColor = if (isIncome) dynamicTheme.accent else dynamicTheme.textPrimary

                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = dynamicTheme.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detail",
                        tint = dynamicTheme.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardBalanceTrendChart(
    totalNetWorth: Long,
    transactions: List<TransactionDetail>,
    isAmountHidden: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    val dynamicTheme = LocalDynamicTheme.current

    // Cepat & hemat memori: transaksi sudah terurut DESC di DB, cukup ambil 30 terakhir dan balikkan urutannya
    val validTx = remember(transactions) {
        transactions.asSequence()
            .filter { it.status == "CONFIRMED" }
            .take(30)
            .toList()
            .asReversed()
    }

    val (points, netDiff) = remember(validTx, totalNetWorth) {
        if (validTx.isEmpty() || totalNetWorth == 0L) {
            listOf(totalNetWorth, totalNetWorth, totalNetWorth) to 0L
        } else {
            val deltas = validTx.map { tx ->
                when (tx.type) {
                    "INCOME" -> tx.amount
                    "EXPENSE" -> -tx.amount
                    else -> 0L
                }
            }
            val totalDelta = deltas.sum()
            val startBalance = (totalNetWorth - totalDelta).coerceAtLeast(0L)
            val series = mutableListOf(startBalance)
            var current = startBalance
            for (delta in deltas) {
                current += delta
                series.add(current)
            }
            val samplePoints = if (series.size > 10) {
                val step = series.size / 10f
                (0..9).map { i -> series[(i * step).toInt().coerceIn(0, series.size - 1)] } + series.last()
            } else {
                series
            }
            samplePoints to (series.last() - series.first())
        }
    }

    val minVal = points.minOrNull() ?: 0L
    val maxVal = points.maxOrNull() ?: 0L
    val isFlat = (minVal == maxVal) || (totalNetWorth == 0L && validTx.isEmpty())

    val chartColor = when {
        isFlat -> dynamicTheme.textSecondary.copy(alpha = 0.6f)
        netDiff > 0L -> dynamicTheme.accent
        netDiff < 0L -> ExpenseRed
        else -> dynamicTheme.accent
    }

    val statusText = when {
        totalNetWorth == 0L && validTx.isEmpty() -> AppStrings.balanceTrendFlat(language)
        isFlat -> AppStrings.balanceTrendStable(language)
        netDiff > 0L -> AppStrings.balanceTrendUp(language, if (isAmountHidden) "•••" else formatRupiah(netDiff))
        netDiff < 0L -> AppStrings.balanceTrendDown(language, if (isAmountHidden) "•••" else formatRupiah(netDiff))
        else -> AppStrings.balanceTrendStable(language)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            val w = size.width
            val h = size.height

            if (isFlat) {
                drawLine(
                    color = chartColor,
                    start = Offset(0f, h * 0.5f),
                    end = Offset(w, h * 0.5f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = chartColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            } else {
                val range = (maxVal - minVal).toFloat().coerceAtLeast(1f)
                val stepX = w / (points.size - 1).coerceAtLeast(1)

                val path = Path()
                val fillPath = Path()

                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val norm = (value - minVal).toFloat() / range
                    val y = h * 0.85f - (norm * (h * 0.7f))

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevNorm = (points[index - 1] - minVal).toFloat() / range
                        val prevY = h * 0.85f - (prevNorm * (h * 0.7f))
                        val cx = (prevX + x) / 2f
                        path.cubicTo(cx, prevY, cx, y, x, y)
                        fillPath.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }

                fillPath.lineTo(w, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            chartColor.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )

                drawPath(
                    path = path,
                    color = chartColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                val lastNorm = (points.last() - minVal).toFloat() / range
                val lastY = h * 0.85f - (lastNorm * (h * 0.7f))
                drawCircle(
                    color = chartColor,
                    radius = 4.dp.toPx(),
                    center = Offset(w, lastY)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isFlat) dynamicTheme.textSecondary else chartColor,
                fontSize = 11.sp
            )
            if (!isFlat) {
                Text(
                    text = if (language == "en") "Trend" else "Arah Saldo",
                    style = MaterialTheme.typography.labelSmall,
                    color = dynamicTheme.textSecondary.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
