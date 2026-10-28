package com.example.ui.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppStrings
import com.example.domain.model.CategoryDistribution
import com.example.ui.FinanceViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SoftCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme

@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    listState: LazyListState = rememberLazyListState()
) {
    val dynamicTheme = LocalDynamicTheme.current
    val language by viewModel.settingsManager.language.collectAsState()

    val periodIndex by viewModel.analyticsPeriodIndex.collectAsState()
    val cashFlow by viewModel.cashFlowSummary.collectAsState()
    val categoryDistribution by viewModel.categoryDistribution.collectAsState()
    val isAmountHidden by viewModel.isAmountHidden.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        item {
            Text(
                text = AppStrings.tabAnalytics(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = dynamicTheme.textPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Periode Selector (Bulan Ini, Minggu Ini, Semua)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(dynamicTheme.surfaceSecondary)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    0 to (if (language == "en") "This Month" else "Bulan Ini"),
                    1 to (if (language == "en") "This Week" else "Minggu Ini"),
                    2 to AppStrings.all(language)
                ).forEach { (idx, label) ->
                    val isSelected = periodIndex == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) dynamicTheme.surface else Color.Transparent)
                            .clickable { viewModel.setAnalyticsPeriod(idx) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) dynamicTheme.accent else dynamicTheme.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Kartu Ringkasan Arus Kas
        item {
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                elevation = 2.dp,
                backgroundColor = dynamicTheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (language == "en") "Cash Flow Summary" else "Ringkasan Arus Kas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Pemasukan
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = dynamicTheme.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.income(language), style = MaterialTheme.typography.bodySmall, color = dynamicTheme.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAmountHidden) "Rp •••" else formatRupiah(cashFlow.totalIncome),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.accent
                            )
                        }

                        // Total Pengeluaran
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.expense(language), style = MaterialTheme.typography.bodySmall, color = dynamicTheme.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAmountHidden) "Rp •••" else formatRupiah(cashFlow.totalExpense),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(dynamicTheme.surfaceSecondary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (language == "en") "Net Surplus" else "Surplus Bersih", style = MaterialTheme.typography.bodySmall, color = dynamicTheme.textSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAmountHidden) "Rp •••" else formatRupiah(cashFlow.netSurplus),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cashFlow.netSurplus >= 0) dynamicTheme.accent else ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (language == "en") "Daily Average" else "Rata-rata / Hari", style = MaterialTheme.typography.bodySmall, color = dynamicTheme.textSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAmountHidden) "Rp •••" else "${formatRupiah(cashFlow.dailyAverageExpense)}/hari",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Header Kategori Pengeluaran
        item {
            Text(
                text = if (language == "en") "Expense by Category" else "Distribusi Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = dynamicTheme.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (categoryDistribution.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.PieChart,
                    title = if (language == "en") "No Expenses" else "Belum Ada Pengeluaran",
                    description = if (language == "en") "Record expenses to see category distribution." else "Catat pengeluaran Anda untuk melihat analisis distribusi kategori."
                )
            }
        } else {
            items(categoryDistribution, key = { "dist_${it.categoryName}" }) { dist ->
                CategoryDistributionRow(distribution = dist, isAmountHidden = isAmountHidden)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
fun CategoryDistributionRow(
    distribution: CategoryDistribution,
    isAmountHidden: Boolean
) {
    val dynamicTheme = LocalDynamicTheme.current

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 1.dp,
        backgroundColor = dynamicTheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(dynamicTheme.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = distribution.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = dynamicTheme.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", distribution.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.accent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAmountHidden) "Rp •••" else formatRupiah(distribution.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (distribution.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = dynamicTheme.accent,
                trackColor = dynamicTheme.surfaceSecondary
            )
        }
    }
}
