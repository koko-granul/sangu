package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.core.i18n.AppStrings
import com.example.domain.model.TransactionDetail
import com.example.ui.FinanceViewModel
import com.example.ui.components.SoftCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailDialog(
    transaction: TransactionDetail,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current
    val language by viewModel.settingsManager.language.collectAsState()

    var fullImageFile by remember { mutableStateOf<File?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dialogScale = remember { Animatable(0.92f) }
    val dialogAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        dialogScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow))
    }
    LaunchedEffect(Unit) {
        dialogAlpha.animateTo(1f, animationSpec = tween(180))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 720.dp)
                    .graphicsLayer {
                        scaleX = dialogScale.value
                        scaleY = dialogScale.value
                        alpha = dialogAlpha.value
                    }
                    .clickable(enabled = false) {}, // prevent closing when tapping inside
                shape = RoundedCornerShape(24.dp),
                color = dynamicTheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header: Judul & Tutup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.transactionDetails(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.textPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = dynamicTheme.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nominal & Type Badge
                    val isIncome = transaction.type == "INCOME"
                    val isExpense = transaction.type == "EXPENSE"
                    val amountColor = if (isIncome) dynamicTheme.accent else dynamicTheme.textPrimary
                    val prefix = if (isIncome) "+ " else if (isExpense) "- " else ""

                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        backgroundColor = dynamicTheme.surfaceSecondary
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = transaction.description,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$prefix${formatRupiah(transaction.amount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = amountColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val typeLabel = when (transaction.type) {
                                "INCOME" -> AppStrings.income(language)
                                "EXPENSE" -> AppStrings.expense(language)
                                else -> AppStrings.transfer(language)
                            }
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = dynamicTheme.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail Fields
                    DetailInfoRow(
                        label = if (language == "en") "Date & Time" else "Waktu",
                        value = formatFullDate(transaction.dateMillis)
                    )

                    val accountValue = when (transaction.type) {
                        "TRANSFER" -> "${transaction.sourceAccount?.name ?: "?"} → ${transaction.destinationAccount?.name ?: "?"}"
                        "INCOME" -> transaction.destinationAccount?.name ?: "-"
                        else -> transaction.sourceAccount?.name ?: "-"
                    }
                    DetailInfoRow(
                        label = if (transaction.type == "TRANSFER")
                            (if (language == "en") "Accounts Flow" else "Alur Akun")
                        else if (isIncome)
                            (if (language == "en") "Destination Wallet" else "Akun Tujuan")
                        else
                            (if (language == "en") "Source Wallet" else "Sumber Dana"),
                        value = accountValue
                    )

                    if (transaction.category != null) {
                        DetailInfoRow(
                            label = if (language == "en") "Category" else "Kategori",
                            value = transaction.category.name
                        )
                    }

                    if (transaction.classification != "PERSONAL") {
                        DetailInfoRow(
                            label = if (language == "en") "Classification" else "Klasifikasi",
                            value = if (transaction.classification == "TITIPAN_KEWAJIBAN") "Titipan / Kewajiban" else "Belum Ditentukan"
                        )
                    }

                    if (!transaction.notes.isNullOrBlank()) {
                        DetailInfoRow(
                            label = if (language == "en") "Notes" else "Catatan",
                            value = transaction.notes
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bukti / Lampiran Foto Struk
                    Text(
                        text = AppStrings.receiptPhoto(language),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (transaction.attachments.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(dynamicTheme.surfaceSecondary)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = dynamicTheme.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = AppStrings.noReceipt(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = dynamicTheme.textSecondary
                            )
                        }
                    } else {
                        // Jika 1 gambar, tampilkan preview besar langsung!
                        if (transaction.attachments.size == 1) {
                            val att = transaction.attachments.first()
                            val fullFile = viewModel.mediaManager.getFileFromRelativePath(att.relativePath)
                            val thumbFile = viewModel.mediaManager.getFileFromRelativePath(att.thumbnailRelativePath)
                            val displayFile = if (fullFile.exists()) fullFile else thumbFile

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(dynamicTheme.surfaceSecondary)
                                    .clickable { fullImageFile = displayFile }
                            ) {
                                AsyncImage(
                                    model = displayFile,
                                    contentDescription = "Foto Struk",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xAA000000))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = AppStrings.zoom(language),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                itemsIndexed(transaction.attachments, key = { idx, att -> "dialog_att_${att.id}_$idx" }) { _, att ->
                                    val thumbFile = viewModel.mediaManager.getFileFromRelativePath(att.thumbnailRelativePath)
                                    val fullFile = viewModel.mediaManager.getFileFromRelativePath(att.relativePath)
                                    val displayFile = if (fullFile.exists()) fullFile else thumbFile

                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(dynamicTheme.surfaceSecondary)
                                            .clickable { fullImageFile = displayFile }
                                    ) {
                                        AsyncImage(
                                            model = displayFile,
                                            contentDescription = "Lampiran Bukti",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x99000000)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = "Perbesar",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action: Hapus Transaksi
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == "en") "Delete This Transaction" else "Hapus Transaksi Ini",
                            color = ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Modal Full Screen Gambar Foto Struk
    fullImageFile?.let { file ->
        Dialog(
            onDismissRequest = { fullImageFile = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = file,
                    contentDescription = "Foto Struk Penuh",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullImageFile = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .background(Color(0x88000000), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Konfirmasi Hapus Transaksi
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (language == "en") "Delete Transaction?" else "Hapus Transaksi?") },
            text = {
                Text(
                    if (language == "en")
                        "Delete '${transaction.description}' (${formatRupiah(transaction.amount)})? Account balance will be restored."
                    else
                        "Hapus transaksi '${transaction.description}' sebesar ${formatRupiah(transaction.amount)}? Mutasi akun akan dikembalikan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction.id)
                        showDeleteConfirm = false
                        onDismiss()
                    }
                ) {
                    Text(AppStrings.delete(language), color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    val dynamicTheme = LocalDynamicTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = dynamicTheme.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = dynamicTheme.textPrimary
        )
    }
}

fun formatFullDate(millis: Long): String {
    val locale = Locale.getDefault()
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", locale)
    return sdf.format(Date(millis))
}
