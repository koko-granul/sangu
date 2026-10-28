package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.core.i18n.AppStrings
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.FundPotEntity
import com.example.domain.model.AccountWithBalance
import com.example.domain.model.SavingsForecast
import com.example.ui.FinanceViewModel
import com.example.ui.components.SoftCard
import com.example.ui.components.ThousandsSeparatorVisualTransformation
import com.example.ui.components.formatRupiah
import com.example.ui.components.themedTextFieldColors
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme
import kotlinx.coroutines.launch

@Composable
fun AccountsAndPotsScreen(
    viewModel: FinanceViewModel,
    listState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dynamicTheme = LocalDynamicTheme.current

    val language by viewModel.settingsManager.language.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val savingsForecasts by viewModel.savingsForecasts.collectAsState()
    val isAmountHidden by viewModel.isAmountHidden.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var accountToDelete by remember { mutableStateOf<AccountWithBalance?>(null) }

    var showAddPotDialog by remember { mutableStateOf(false) }
    var potToEdit by remember { mutableStateOf<SavingsForecast?>(null) }
    var potToAllocate by remember { mutableStateOf<SavingsForecast?>(null) }
    var potToWithdraw by remember { mutableStateOf<SavingsForecast?>(null) }
    var potToDelete by remember { mutableStateOf<SavingsForecast?>(null) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Bagian Rekening & Dompet
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.walletsAndAccounts(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(dynamicTheme.accentLight)
                        .clickable { showAddAccountDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = dynamicTheme.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.addWallet(language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.accent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // List Rekening
        if (accounts.isEmpty()) {
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = dynamicTheme.surface
                ) {
                    Text(
                        text = if (language == "en") "No wallets added yet. Tap Add Wallet to create one." else "Belum ada dompet. Ketuk Tambah Dompet untuk membuat akun baru.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = dynamicTheme.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(accounts, key = { "account_card_${it.account.id}" }) { accWithBal ->
                AccountCardItem(
                    accountWithBalance = accWithBal,
                    isAmountHidden = isAmountHidden,
                    onEdit = { accountToEdit = accWithBal.account },
                    onDelete = { accountToDelete = accWithBal }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 2. Bagian Pos Tabungan
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.savingsPots(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(dynamicTheme.surfaceSecondary)
                        .clickable { showAddPotDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = dynamicTheme.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.addPot(language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.textPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (savingsForecasts.isEmpty()) {
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = dynamicTheme.surface
                ) {
                    Text(
                        text = if (language == "en") "No savings pots yet. Create one if you want to track targeted funds." else "Belum ada pos tabungan. Buat pos baru untuk rencana dana atau impian Anda (opsional).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = dynamicTheme.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(savingsForecasts, key = { "pot_item_${it.potId}" }) { forecast ->
                FundPotCardItem(
                    forecast = forecast,
                    viewModel = viewModel,
                    language = language,
                    isAmountHidden = isAmountHidden,
                    onEdit = { potToEdit = forecast },
                    onAllocate = { potToAllocate = forecast },
                    onWithdraw = { potToWithdraw = forecast },
                    onDelete = { potToDelete = forecast }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // Dialog Tambah Akun / Dompet
    if (showAddAccountDialog) {
        var accountName by remember { mutableStateOf("") }
        var accountType by remember { mutableStateOf("BANK") }
        var initialBalanceText by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(AppStrings.addWallet(language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text(if (language == "en") "Wallet Name (e.g. BCA, Mandiri, Cash)" else "Nama Dompet (misal: BCA, Mandiri, Tunai)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "BANK" to if (language == "en") "Bank" else "Bank",
                            "CASH" to if (language == "en") "Cash" else "Tunai",
                            "EWALLET" to "E-Wallet"
                        ).forEach { (typeKey, label) ->
                            val isSel = accountType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                                    .clickable { accountType = typeKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) dynamicTheme.onAccentLight else dynamicTheme.textPrimary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = initialBalanceText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) initialBalanceText = it },
                        label = { Text(if (language == "en") "Initial Balance (Rp)" else "Saldo Awal (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (accountName.isNotBlank()) {
                            viewModel.createAccount(
                                name = accountName.trim(),
                                type = accountType,
                                colorHex = "#10B981",
                                iconName = if (accountType == "CASH") "payments" else "account_balance",
                                initialBalance = initialBalanceText.toLongOrNull() ?: 0L,
                                onSuccess = {
                                    showAddAccountDialog = false
                                    Toast.makeText(context, "$accountName tersimpan", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Edit Akun
    accountToEdit?.let { acc ->
        var editName by remember { mutableStateOf(acc.name) }
        var editType by remember { mutableStateOf(acc.type) }

        AlertDialog(
            onDismissRequest = { accountToEdit = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(AppStrings.editWallet(language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (language == "en") "Wallet Name" else "Nama Dompet") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "BANK" to if (language == "en") "Bank" else "Bank",
                            "CASH" to if (language == "en") "Cash" else "Tunai",
                            "EWALLET" to "E-Wallet"
                        ).forEach { (typeKey, label) ->
                            val isSel = editType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                                    .clickable { editType = typeKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) dynamicTheme.onAccentLight else dynamicTheme.textPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateAccount(
                                account = acc.copy(
                                    name = editName.trim(),
                                    type = editType,
                                    iconName = if (editType == "CASH") "payments" else "account_balance"
                                ),
                                onSuccess = {
                                    accountToEdit = null
                                    Toast.makeText(context, "$editName diperbarui", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToEdit = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Akun dengan Opsi Penyelamatan Saldo
    accountToDelete?.let { accWithBal ->
        val otherAccounts = accounts.filter { it.account.id != accWithBal.account.id }
        var actionOption by remember { mutableStateOf(if (otherAccounts.isNotEmpty() && accWithBal.balance > 0L) "TRANSFER" else "DELETE_DIRECT") }
        var targetAccountId by remember { mutableStateOf(otherAccounts.firstOrNull()?.account?.id) }

        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(if (language == "en") "Delete ${accWithBal.account.name}?" else "Hapus Dompet ${accWithBal.account.name}?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == "en")
                            "Current balance: ${formatRupiah(accWithBal.balance)}"
                        else
                            "Saldo saat ini: ${formatRupiah(accWithBal.balance)}",
                        fontWeight = FontWeight.SemiBold,
                        color = dynamicTheme.textPrimary
                    )

                    if (accWithBal.balance > 0L && otherAccounts.isNotEmpty()) {
                        Text(
                            text = if (language == "en") "Choose action for remaining balance:" else "Pilih tindakan untuk sisa saldo:",
                            style = MaterialTheme.typography.bodySmall,
                            color = dynamicTheme.textSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { actionOption = "TRANSFER" }
                        ) {
                            RadioButton(
                                selected = actionOption == "TRANSFER",
                                onClick = { actionOption = "TRANSFER" },
                                colors = RadioButtonDefaults.colors(selectedColor = dynamicTheme.accent)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == "en") "Transfer balance to another wallet" else "Pindahkan saldo ke dompet lain",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (actionOption == "TRANSFER") {
                            Text(
                                text = if (language == "en") "Destination Wallet:" else "Dompet Tujuan:",
                                style = MaterialTheme.typography.labelSmall,
                                color = dynamicTheme.textSecondary
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(otherAccounts, key = { "del_target_${it.account.id}" }) { other ->
                                    val isSelected = targetAccountId == other.account.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                                            .clickable { targetAccountId = other.account.id }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = other.account.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) dynamicTheme.accent else dynamicTheme.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { actionOption = "DELETE_DIRECT" }
                        ) {
                            RadioButton(
                                selected = actionOption == "DELETE_DIRECT",
                                onClick = { actionOption = "DELETE_DIRECT" },
                                colors = RadioButtonDefaults.colors(selectedColor = ExpenseRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == "en") "Delete wallet and its balance directly" else "Hapus langsung beserta saldonya",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed
                            )
                        }
                    } else {
                        Text(
                            text = if (language == "en")
                                "This wallet will be deleted permanently."
                            else
                                "Dompet ini akan dihapus secara permanen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = dynamicTheme.textSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actionOption == "TRANSFER" && targetAccountId != null && accWithBal.balance > 0L) {
                            viewModel.deleteAccountAndTransfer(
                                sourceAccountId = accWithBal.account.id,
                                targetAccountId = targetAccountId!!,
                                balance = accWithBal.balance,
                                onSuccess = {
                                    accountToDelete = null
                                    Toast.makeText(context, "Saldo dipindahkan & akun dihapus", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            viewModel.deleteAccount(
                                accountId = accWithBal.account.id,
                                onSuccess = {
                                    accountToDelete = null
                                    Toast.makeText(context, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                ) {
                    Text(AppStrings.delete(language), color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Tambah Pos Tabungan
    if (showAddPotDialog) {
        var potTitle by remember { mutableStateOf("") }
        var targetAmountText by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var attachedImagePath by remember { mutableStateOf<String?>(null) }
        var isProcessingImage by remember { mutableStateOf(false) }

        val potImageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    isProcessingImage = true
                    try {
                        val att = viewModel.processImageAttachment(uri, "SAVINGS_POT")
                        attachedImagePath = att.relativePath
                    } catch (e: Exception) {
                        Toast.makeText(context, "${if (language == "en") "Failed to load image" else "Gagal memproses gambar"}: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isProcessingImage = false
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddPotDialog = false },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(AppStrings.addPot(language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = potTitle,
                        onValueChange = { potTitle = it },
                        label = { Text(if (language == "en") "Pot Name (e.g. Vacation, Gadget, Motorcycle)" else "Nama Pos (misal: Liburan, Gadget, Motor Baru)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) targetAmountText = it },
                        label = { Text(if (language == "en") "Target Amount (Rp)" else "Target Nominal (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (language == "en") "Notes (Optional)" else "Catatan (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Attachment Foto Target / Impian Celengan
                    Text(
                        text = AppStrings.potGoalImage(language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = dynamicTheme.textSecondary
                    )

                    if (attachedImagePath != null) {
                        val file = viewModel.mediaManager.getFileFromRelativePath(attachedImagePath!!)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(dynamicTheme.surfaceSecondary)
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = AppStrings.potGoalImage(language),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .clickable { attachedImagePath = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = AppStrings.delete(language),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { potImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = dynamicTheme.surfaceSecondary,
                                contentColor = dynamicTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessingImage) {
                                CircularProgressIndicator(
                                    color = dynamicTheme.textPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("...", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = dynamicTheme.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.addGoalPhoto(language),
                                    color = dynamicTheme.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetVal = targetAmountText.toLongOrNull() ?: 0L
                        if (potTitle.isNotBlank() && targetVal > 0) {
                            viewModel.addFundPot(
                                title = potTitle.trim(),
                                targetAmount = targetVal,
                                colorHex = "#10B981",
                                iconName = "savings",
                                notes = notes.ifBlank { null },
                                imagePath = attachedImagePath,
                                onSuccess = {
                                    showAddPotDialog = false
                                    Toast.makeText(context, if (language == "en") "Savings pot created" else "Pos tabungan tersimpan", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPotDialog = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Edit Pos Tabungan (bisa ganti nama, target, catatan, dan foto target)
    potToEdit?.let { forecast ->
        var editTitle by remember { mutableStateOf(forecast.title) }
        var editTargetText by remember { mutableStateOf(forecast.targetAmount.toString()) }
        var editImagePath by remember { mutableStateOf(forecast.imagePath) }
        var isProcessingImage by remember { mutableStateOf(false) }

        val editImageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    isProcessingImage = true
                    try {
                        val att = viewModel.processImageAttachment(uri, "SAVINGS_POT")
                        editImagePath = att.relativePath
                    } catch (e: Exception) {
                        Toast.makeText(context, "${if (language == "en") "Failed to load image" else "Gagal memproses gambar"}: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isProcessingImage = false
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { potToEdit = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(if (language == "en") "Edit Pot" else "Ubah Pos Tabungan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text(if (language == "en") "Pot Name" else "Nama Pos Tabungan") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editTargetText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) editTargetText = it },
                        label = { Text(if (language == "en") "Target Amount (Rp)" else "Target Nominal (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Attachment Foto Target
                    Text(
                        text = AppStrings.potGoalImage(language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = dynamicTheme.textSecondary
                    )

                    if (editImagePath != null) {
                        val file = viewModel.mediaManager.getFileFromRelativePath(editImagePath!!)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(dynamicTheme.surfaceSecondary)
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = AppStrings.potGoalImage(language),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .clickable { editImagePath = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = AppStrings.delete(language),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { editImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = dynamicTheme.surfaceSecondary,
                                contentColor = dynamicTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessingImage) {
                                CircularProgressIndicator(
                                    color = dynamicTheme.textPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("...", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = dynamicTheme.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.addGoalPhoto(language),
                                    color = dynamicTheme.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTarget = editTargetText.toLongOrNull() ?: forecast.targetAmount
                        if (editTitle.isNotBlank()) {
                            viewModel.updateFundPot(
                                pot = FundPotEntity(
                                    id = forecast.potId,
                                    title = editTitle.trim(),
                                    targetAmount = newTarget,
                                    currentAmount = forecast.currentAmount,
                                    colorHex = "#10B981",
                                    iconName = "savings",
                                    imagePath = editImagePath
                                ),
                                onSuccess = {
                                    potToEdit = null
                                    Toast.makeText(context, if (language == "en") "Pot updated" else "Pos diperbarui", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { potToEdit = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Tambah Tabungan ke Pos
    potToAllocate?.let { forecast ->
        var allocAmountText by remember { mutableStateOf("") }
        var selectedSourceAccId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }

        AlertDialog(
            onDismissRequest = { potToAllocate = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text("${AppStrings.addFunds(language)} '${forecast.title}'", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == "en") "Source Wallet:" else "Sumber Dana:",
                        style = MaterialTheme.typography.bodySmall,
                        color = dynamicTheme.textSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts, key = { "alloc_src_${it.account.id}" }) { acc ->
                            AccountChip(
                                accountName = acc.account.name,
                                balance = acc.balance,
                                isSelected = selectedSourceAccId == acc.account.id,
                                onClick = { selectedSourceAccId = acc.account.id }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = allocAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) allocAmountText = it },
                        label = { Text(if (language == "en") "Deposit Amount (Rp)" else "Nominal Simpanan (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = allocAmountText.toLongOrNull() ?: 0L
                        if (amountVal > 0) {
                            viewModel.allocateToFundPot(
                                potId = forecast.potId,
                                sourceAccountId = selectedSourceAccId,
                                amount = amountVal,
                                notes = "Tabungan ${forecast.title}",
                                onSuccess = {
                                    potToAllocate = null
                                    Toast.makeText(context, "Dana berhasil dialokasikan", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { potToAllocate = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Tarik / Habiskan Dana dari Pos
    potToWithdraw?.let { forecast ->
        var withdrawAction by remember { mutableStateOf("RETURN_TO_WALLET") } // RETURN_TO_WALLET vs SPEND_DIRECT
        var withdrawAmountText by remember { mutableStateOf(forecast.currentAmount.toString()) }
        var selectedDestAccId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
        var spendDescription by remember { mutableStateOf(forecast.title) }

        AlertDialog(
            onDismissRequest = { potToWithdraw = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(if (language == "en") "Spend / Withdraw '${forecast.title}'" else "Tarik / Habiskan Dana '${forecast.title}'", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${AppStrings.collected(language)}: ${formatRupiah(forecast.currentAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.accent
                    )

                    // Pilihan: Cairkan ke Dompet vs Belanjakan Langsung
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(dynamicTheme.surfaceSecondary)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "RETURN_TO_WALLET" to (if (language == "en") "To Wallet" else "Ke Dompet"),
                            "SPEND_DIRECT" to (if (language == "en") "Spend Directly" else "Belanjakan")
                        ).forEach { (opt, label) ->
                            val isSel = withdrawAction == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) dynamicTheme.surface else Color.Transparent)
                                    .clickable { withdrawAction = opt }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) dynamicTheme.accent else dynamicTheme.textSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = if (withdrawAction == "RETURN_TO_WALLET")
                            (if (language == "en") "Destination Wallet:" else "Dompet Penerima:")
                        else
                            (if (language == "en") "Account Used:" else "Akun Rekening Terkait:"),
                        style = MaterialTheme.typography.bodySmall,
                        color = dynamicTheme.textSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts, key = { "with_dest_${it.account.id}" }) { acc ->
                            AccountChip(
                                accountName = acc.account.name,
                                balance = acc.balance,
                                isSelected = selectedDestAccId == acc.account.id,
                                onClick = { selectedDestAccId = acc.account.id }
                            )
                        }
                    }

                    if (withdrawAction == "SPEND_DIRECT") {
                        OutlinedTextField(
                            value = spendDescription,
                            onValueChange = { spendDescription = it },
                            label = { Text(if (language == "en") "Expense Note (e.g. Vacation ticket)" else "Keterangan Pengeluaran (misal: Tiket liburan)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = themedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) withdrawAmountText = it },
                        label = { Text(if (language == "en") "Amount (Rp)" else "Nominal (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = withdrawAmountText.toLongOrNull() ?: 0L
                        if (amountVal > 0) {
                            if (amountVal > forecast.currentAmount) {
                                Toast.makeText(context, "Nominal melebihi saldo pos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (withdrawAction == "SPEND_DIRECT") {
                                val category = categories.firstOrNull { it.type == "EXPENSE" }?.id
                                viewModel.spendFromFundPot(
                                    potId = forecast.potId,
                                    accountId = selectedDestAccId,
                                    amount = amountVal,
                                    categoryId = category,
                                    description = spendDescription.ifBlank { forecast.title },
                                    notes = "Pengeluaran dari pos ${forecast.title}",
                                    onSuccess = {
                                        potToWithdraw = null
                                        Toast.makeText(context, "Dana pos berhasil dibelanjakan", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                viewModel.withdrawFromFundPot(
                                    potId = forecast.potId,
                                    destinationAccountId = selectedDestAccId,
                                    amount = amountVal,
                                    notes = "Pencairan pos ${forecast.title}",
                                    onSuccess = {
                                        potToWithdraw = null
                                        Toast.makeText(context, "Dana berhasil dicairkan ke dompet", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(
                        if (withdrawAction == "SPEND_DIRECT")
                            (if (language == "en") "Spend" else "Belanjakan")
                        else
                            (if (language == "en") "Withdraw" else "Cairkan"),
                        color = dynamicTheme.onAccent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { potToWithdraw = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Pos
    potToDelete?.let { forecast ->
        AlertDialog(
            onDismissRequest = { potToDelete = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(if (language == "en") "Delete '${forecast.title}'?" else "Hapus Pos '${forecast.title}'?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (language == "en")
                        "This savings pot will be removed permanently."
                    else
                        "Pos tabungan ini akan dihapus secara permanen.",
                    color = dynamicTheme.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFundPot(
                            potId = forecast.potId,
                            onSuccess = {
                                potToDelete = null
                                Toast.makeText(context, "Pos tabungan dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text(AppStrings.delete(language), color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { potToDelete = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun AccountCardItem(
    accountWithBalance: AccountWithBalance,
    isAmountHidden: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        elevation = 1.dp,
        backgroundColor = dynamicTheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(dynamicTheme.surfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (accountWithBalance.account.type == "CASH") Icons.Default.Payments else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = dynamicTheme.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = accountWithBalance.account.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary
                    )
                    Text(
                        text = if (accountWithBalance.account.type == "CASH") "Tunai" else if (accountWithBalance.account.type == "EWALLET") "E-Wallet" else "Bank",
                        style = MaterialTheme.typography.labelSmall,
                        color = dynamicTheme.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isAmountHidden) "Rp •••••" else formatRupiah(accountWithBalance.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (accountWithBalance.balance < 0) ExpenseRed else dynamicTheme.textPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Ubah Akun",
                        tint = dynamicTheme.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Akun",
                        tint = ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FundPotCardItem(
    forecast: SavingsForecast,
    viewModel: FinanceViewModel,
    language: String,
    isAmountHidden: Boolean,
    onEdit: () -> Unit,
    onAllocate: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current
    var showPhotoZoom by remember { mutableStateOf(false) }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        elevation = 1.dp,
        backgroundColor = dynamicTheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon, Judul & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(dynamicTheme.accentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = dynamicTheme.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = forecast.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = AppStrings.edit(language),
                            tint = dynamicTheme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = AppStrings.delete(language),
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Foto Impian / Target Celengan (Jika ada)
            if (forecast.imagePath != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val file = viewModel.mediaManager.getFileFromRelativePath(forecast.imagePath)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(dynamicTheme.surfaceSecondary)
                        .clickable { showPhotoZoom = true }
                ) {
                    AsyncImage(
                        model = file,
                        contentDescription = forecast.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Zoom indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x99000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = AppStrings.zoom(language),
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.zoom(language),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { forecast.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = dynamicTheme.accent,
                trackColor = dynamicTheme.surfaceSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${AppStrings.collected(language)}: ${if (isAmountHidden) "Rp •••" else formatRupiah(forecast.currentAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = dynamicTheme.textSecondary
                )
                Text(
                    text = "${AppStrings.target(language)}: ${if (isAmountHidden) "Rp •••" else formatRupiah(forecast.targetAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = dynamicTheme.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Tambah & Tarik / Pakai Dana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(dynamicTheme.accentLight)
                        .clickable(onClick = onAllocate)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = dynamicTheme.onAccentLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.addFunds(language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.onAccentLight
                        )
                    }
                }

                if (forecast.currentAmount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(dynamicTheme.surfaceSecondary)
                            .clickable(onClick = onWithdraw)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = dynamicTheme.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.withdrawFunds(language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Zoom Foto Impian / Target Celengan
    if (showPhotoZoom && forecast.imagePath != null) {
        val file = viewModel.mediaManager.getFileFromRelativePath(forecast.imagePath)
        Dialog(onDismissRequest = { showPhotoZoom = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(dynamicTheme.surface)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AppStrings.goalPhotoPreview(language)}: ${forecast.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.textPrimary
                        )
                        IconButton(onClick = { showPhotoZoom = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = AppStrings.close(language),
                                tint = dynamicTheme.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = file,
                        contentDescription = forecast.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
