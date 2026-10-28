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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppStrings
import com.example.domain.model.TransactionDetail
import com.example.ui.FinanceViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SoftCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme

@Composable
fun TransactionListScreen(
    viewModel: FinanceViewModel,
    listState: LazyListState,
    onOpenAddTransaction: (String) -> Unit = {},
    onScrollDirectionChange: (Boolean) -> Unit = {}
) {
    val dynamicTheme = LocalDynamicTheme.current
    val language by viewModel.settingsManager.language.collectAsState()

    val allTransactions by viewModel.transactions.collectAsState()
    val isAmountHidden by viewModel.isAmountHidden.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, EXPENSE, INCOME, TRANSFER
    var transactionToDelete by remember { mutableStateOf<TransactionDetail?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionDetail?>(null) }

    val filteredTransactions = remember(allTransactions, selectedTypeFilter, searchQuery) {
        if (selectedTypeFilter == "ALL" && searchQuery.isBlank()) {
            allTransactions
        } else {
            val query = searchQuery.trim()
            allTransactions.filter { tx ->
                val matchesType = when (selectedTypeFilter) {
                    "EXPENSE" -> tx.type == "EXPENSE"
                    "INCOME" -> tx.type == "INCOME"
                    "TRANSFER" -> tx.type == "TRANSFER"
                    else -> true
                }
                if (!matchesType) return@filter false
                if (query.isEmpty()) return@filter true
                tx.description.contains(query, ignoreCase = true) ||
                    (tx.category?.name?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header: Judul Riwayat (tanpa tombol catat di atas yang kejauhan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == "en") "Transactions" else "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (language == "en") "Search transactions..." else "Cari transaksi...",
                        color = dynamicTheme.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = dynamicTheme.textSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = dynamicTheme.textPrimary,
                    unfocusedTextColor = dynamicTheme.textPrimary,
                    focusedContainerColor = dynamicTheme.surface,
                    unfocusedContainerColor = dynamicTheme.surface,
                    focusedBorderColor = dynamicTheme.accent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = dynamicTheme.accent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "ALL" to AppStrings.all(language),
                    "EXPENSE" to AppStrings.expense(language),
                    "INCOME" to AppStrings.income(language),
                    "TRANSFER" to AppStrings.transfer(language)
                ).forEach { (typeKey, label) ->
                    item {
                        val isSelected = selectedTypeFilter == typeKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                                .clickable { selectedTypeFilter = typeKey }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) dynamicTheme.onAccentLight else dynamicTheme.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List Transaksi
            if (filteredTransactions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = AppStrings.noTransactions(language),
                    description = if (language == "en") "No transactions match your filter." else "Tidak ada transaksi yang cocok dengan filter yang dipilih."
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredTransactions,
                        key = { it.id },
                        contentType = { "tx_row" }
                    ) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            isAmountHidden = isAmountHidden,
                            onClick = { selectedTransactionForDetail = tx },
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }
    }

    // Modal Detail Transaksi & Gambar Struk
    selectedTransactionForDetail?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            viewModel = viewModel,
            onDismiss = { selectedTransactionForDetail = null }
        )
    }

    // Dialog Konfirmasi Hapus Transaksi
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(if (language == "en") "Delete Transaction?" else "Hapus Transaksi?") },
            text = { Text("Hapus transaksi '${tx.description}' (${formatRupiah(tx.amount)})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
                        transactionToDelete = null
                    }
                ) {
                    Text(AppStrings.delete(language), color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }
}
