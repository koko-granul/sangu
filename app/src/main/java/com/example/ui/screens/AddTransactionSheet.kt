package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import coil.compose.AsyncImage
import com.example.core.i18n.AppStrings
import com.example.data.local.entity.AttachmentEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.SoftCard
import com.example.ui.components.ThousandsSeparatorVisualTransformation
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    viewModel: FinanceViewModel,
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dynamicTheme = LocalDynamicTheme.current

    val language by viewModel.settingsManager.language.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var transactionType by remember { mutableStateOf(initialType) } // EXPENSE, INCOME, TRANSFER
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedSourceAccountId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.account?.id) }
    var selectedDestAccountId by remember { mutableStateOf<Long?>(accounts.getOrNull(1)?.account?.id ?: accounts.firstOrNull()?.account?.id) }
    var classification by remember { mutableStateOf("PERSONAL") } // PERSONAL, TITIPAN_KEWAJIBAN, UNSPECIFIED

    val attachments = remember { mutableStateListOf<AttachmentEntity>() }
    var isOcrProcessing by remember { mutableStateOf(false) }

    // Galeri Image Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val att = viewModel.processImageAttachment(uri, "RECEIPT")
                    attachments.add(att)
                } catch (e: Exception) {
                    Toast.makeText(context, "${if (language == "en") "Failed to process image" else "Gagal memproses gambar"}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // OCR Scanner launcher
    val ocrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isOcrProcessing = true
                try {
                    val att = viewModel.processImageAttachment(uri, "RECEIPT")
                    attachments.add(att)

                    val draft = viewModel.scanReceipt(uri)
                    if (draft.totalAmount != null && draft.totalAmount > 0) {
                        amountText = draft.totalAmount.toString()
                    }
                    if (!draft.merchant.isNullOrBlank()) {
                        description = draft.merchant
                    }
                    Toast.makeText(
                        context,
                        AppStrings.ocrSuccess(language),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, AppStrings.ocrFailed(language, e.message ?: ""), Toast.LENGTH_SHORT).show()
                } finally {
                    isOcrProcessing = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = dynamicTheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(dynamicTheme.surfaceSecondary)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Judul & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.addTransaction(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppStrings.close(language),
                        tint = dynamicTheme.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Type Chips (Expense, Income, Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(dynamicTheme.surfaceSecondary)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "EXPENSE" to AppStrings.expense(language),
                    "INCOME" to AppStrings.income(language),
                    "TRANSFER" to AppStrings.transfer(language)
                ).forEach { (type, label) ->
                    val isSelected = transactionType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) dynamicTheme.surface else Color.Transparent)
                            .clickable { transactionType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                when (type) {
                                    "EXPENSE" -> ExpenseRed
                                    "INCOME" -> dynamicTheme.accent
                                    else -> dynamicTheme.accent
                                }
                            } else dynamicTheme.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nominal Input
            Text(
                text = AppStrings.amountLabel(language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = dynamicTheme.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        amountText = input
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(AppStrings.amountPlaceholder(language), color = dynamicTheme.textSecondary.copy(alpha = 0.7f)) },
                prefix = {
                    Text(
                        text = "Rp ",
                        fontWeight = FontWeight.Bold,
                        color = if (transactionType == "INCOME") dynamicTheme.accent else dynamicTheme.textPrimary
                    )
                },
                singleLine = true,
                visualTransformation = ThousandsSeparatorVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = dynamicTheme.surfaceSecondary,
                    unfocusedContainerColor = dynamicTheme.surfaceSecondary,
                    focusedTextColor = dynamicTheme.textPrimary,
                    unfocusedTextColor = dynamicTheme.textPrimary,
                    focusedBorderColor = dynamicTheme.accent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = dynamicTheme.accent
                )
            )

            // Quick Chips (+10rb, +20rb, +50rb, +100rb)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10000L, 20000L, 50000L, 100000L).forEach { quickNominal ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(dynamicTheme.surfaceSecondary)
                            .clickable {
                                val current = amountText.toLongOrNull() ?: 0L
                                amountText = (current + quickNominal).toString()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${formatRupiah(quickNominal).replace("Rp ", "")}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = dynamicTheme.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // PEMILIH AKUN
            if (transactionType == "TRANSFER") {
                // Transfer: Dari Akun & Ke Akun
                Text(
                    text = AppStrings.fromAccountLabel(language),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = dynamicTheme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts, key = { "from_${it.account.id}" }) { accWithBal ->
                        val isSelected = selectedSourceAccountId == accWithBal.account.id
                        AccountChip(
                            accountName = accWithBal.account.name,
                            balance = accWithBal.balance,
                            isSelected = isSelected,
                            onClick = { selectedSourceAccountId = accWithBal.account.id }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.toAccountLabel(language),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = dynamicTheme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts, key = { "to_${it.account.id}" }) { accWithBal ->
                        val isSelected = selectedDestAccountId == accWithBal.account.id
                        AccountChip(
                            accountName = accWithBal.account.name,
                            balance = accWithBal.balance,
                            isSelected = isSelected,
                            onClick = { selectedDestAccountId = accWithBal.account.id }
                        )
                    }
                }
            } else {
                val accountLabel = if (transactionType == "EXPENSE") AppStrings.sourceAccountLabel(language) else AppStrings.destAccountLabel(language)
                Text(
                    text = accountLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = dynamicTheme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts, key = { "acc_${it.account.id}" }) { accWithBal ->
                        val isSelected = if (transactionType == "EXPENSE") {
                            selectedSourceAccountId == accWithBal.account.id
                        } else {
                            selectedDestAccountId == accWithBal.account.id
                        }
                        AccountChip(
                            accountName = accWithBal.account.name,
                            balance = accWithBal.balance,
                            isSelected = isSelected,
                            onClick = {
                                if (transactionType == "EXPENSE") {
                                    selectedSourceAccountId = accWithBal.account.id
                                } else {
                                    selectedDestAccountId = accWithBal.account.id
                                }
                            }
                        )
                    }
                }
            }

            // Peringatan jika saldo menjadi negatif
            if (transactionType == "EXPENSE") {
                val currentAccount = accounts.firstOrNull { it.account.id == selectedSourceAccountId }
                val amountVal = amountText.toLongOrNull() ?: 0L
                if (currentAccount != null && currentAccount.balance < amountVal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (dynamicTheme.isDark) Color(0xFF450A0A).copy(alpha = 0.5f) else Color(0xFFFEF2F2))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.exceedsBalance(language, currentAccount.account.name, formatRupiah(currentAccount.balance)),
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Kategori (Hanya untuk Pemasukan dan Pengeluaran)
            if (transactionType != "TRANSFER") {
                val filteredCategories = categories.filter { it.type == transactionType }
                Text(
                    text = AppStrings.categoryLabel(language),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = dynamicTheme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(AppStrings.categoryName(language, cat.name), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = dynamicTheme.accentLight,
                                selectedLabelColor = dynamicTheme.onAccentLight,
                                containerColor = dynamicTheme.surfaceSecondary,
                                labelColor = dynamicTheme.textPrimary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Deskripsi Transaksi
            Text(
                text = AppStrings.descriptionLabel(language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = dynamicTheme.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(AppStrings.descriptionPlaceholder(language), color = dynamicTheme.textSecondary.copy(alpha = 0.7f)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = dynamicTheme.surfaceSecondary,
                    unfocusedContainerColor = dynamicTheme.surfaceSecondary,
                    focusedTextColor = dynamicTheme.textPrimary,
                    unfocusedTextColor = dynamicTheme.textPrimary,
                    focusedBorderColor = dynamicTheme.accent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = dynamicTheme.accent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Klasifikasi Dana
            Text(
                text = AppStrings.fundClassification(language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = dynamicTheme.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "PERSONAL" to AppStrings.personalFund(language),
                    "TITIPAN_KEWAJIBAN" to AppStrings.obligationFund(language),
                    "UNSPECIFIED" to AppStrings.unspecifiedFund(language)
                ).forEach { (cls, label) ->
                    val isSelected = classification == cls
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                            .clickable { classification = cls }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) dynamicTheme.onAccentLight else dynamicTheme.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lampiran & OCR Kamera/Galeri
            Text(
                text = AppStrings.attachmentsTitle(language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = dynamicTheme.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Tombol OCR Struk Offline
                Button(
                    onClick = { ocrLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dynamicTheme.accent,
                        contentColor = dynamicTheme.onAccent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isOcrProcessing) {
                        CircularProgressIndicator(
                            color = dynamicTheme.onAccent,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.scanning(language), fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.scanReceipt(language), fontSize = 12.sp)
                    }
                }

                // Tombol Tambah Gambar biasa
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dynamicTheme.surfaceSecondary,
                        contentColor = dynamicTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = dynamicTheme.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.choosePhoto(language), color = dynamicTheme.textPrimary, fontSize = 12.sp)
                }
            }

            // Thumbnail List Lampiran
            if (attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(attachments, key = { idx, att -> "sheet_att_${att.id}_$idx" }) { _, att ->
                        val file = viewModel.mediaManager.getFileFromRelativePath(att.thumbnailRelativePath)
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(dynamicTheme.surfaceSecondary)
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = AppStrings.receiptPhoto(language),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .clickable {
                                        viewModel.mediaManager.deleteAttachmentFiles(att)
                                        attachments.remove(att)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = AppStrings.delete(language),
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Tombol Simpan Transaksi
            Button(
                onClick = {
                    val amountVal = amountText.toLongOrNull()
                    if (amountVal == null || amountVal <= 0L) {
                        Toast.makeText(context, AppStrings.amountMustBePositive(language), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val now = System.currentTimeMillis()
                    val desc = description.ifBlank {
                        if (transactionType == "TRANSFER") AppStrings.interAccountTransfer(language) else AppStrings.newTransactionTitle(language)
                    }

                    when (transactionType) {
                        "EXPENSE" -> {
                            val sourceAcc = selectedSourceAccountId ?: accounts.firstOrNull()?.account?.id ?: 1L
                            viewModel.recordExpense(
                                accountId = sourceAcc,
                                amount = amountVal,
                                categoryId = selectedCategoryId,
                                description = desc,
                                notes = notes.ifBlank { null },
                                dateMillis = now,
                                classification = classification,
                                attachments = attachments.toList(),
                                onSuccess = onDismiss
                            )
                        }
                        "INCOME" -> {
                            val destAcc = selectedDestAccountId ?: accounts.firstOrNull()?.account?.id ?: 1L
                            viewModel.recordIncome(
                                accountId = destAcc,
                                amount = amountVal,
                                categoryId = selectedCategoryId,
                                description = desc,
                                notes = notes.ifBlank { null },
                                dateMillis = now,
                                classification = classification,
                                attachments = attachments.toList(),
                                onSuccess = onDismiss
                            )
                        }
                        "TRANSFER" -> {
                            val from = selectedSourceAccountId ?: 1L
                            val to = selectedDestAccountId ?: 2L
                            if (from == to) {
                                Toast.makeText(context, AppStrings.sameSourceDestError(language), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.recordTransfer(
                                fromAccountId = from,
                                toAccountId = to,
                                amount = amountVal,
                                description = desc,
                                notes = notes.ifBlank { null },
                                dateMillis = now,
                                onSuccess = onDismiss,
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicTheme.accent,
                    contentColor = dynamicTheme.onAccent
                )
            ) {
                Text(
                    text = AppStrings.confirmAndSave(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = dynamicTheme.onAccent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccountChip(
    accountName: String,
    balance: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = accountName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) dynamicTheme.onAccentLight else dynamicTheme.textPrimary
            )
            Text(
                text = formatRupiah(balance),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) dynamicTheme.onAccentLight.copy(alpha = 0.85f) else dynamicTheme.textSecondary
            )
        }
    }
}
