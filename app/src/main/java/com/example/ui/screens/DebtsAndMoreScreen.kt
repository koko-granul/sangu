package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppStrings
import com.example.core.settings.AppSettingsManager
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import com.example.data.local.entity.DebtEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.SoftCard
import com.example.ui.components.ThemeColorWheelDialog
import com.example.ui.components.ThousandsSeparatorVisualTransformation
import com.example.ui.components.formatRupiah
import com.example.ui.components.themedTextFieldColors
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme
import com.example.ui.theme.parseHexColor
import kotlinx.coroutines.launch

@Composable
fun DebtsAndMoreScreen(
    viewModel: FinanceViewModel,
    listState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val dynamicTheme = LocalDynamicTheme.current

    val language by viewModel.settingsManager.language.collectAsState()
    val userName by viewModel.settingsManager.userName.collectAsState()
    val isPasswordLockEnabled by viewModel.settingsManager.isPasswordLockEnabled.collectAsState()
    val themeConfig by viewModel.settingsManager.themeConfig.collectAsState()

    val userProfileUri by viewModel.settingsManager.userProfileUri.collectAsState()

    val profilePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveUserProfileImage(uri)
            Toast.makeText(context, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    val debts by viewModel.debts.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()
    val isAmountHidden by viewModel.isAmountHidden.collectAsState()

    var selectedDebtType by remember { mutableStateOf("UTANG") } // UTANG vs PIUTANG
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var debtToPay by remember { mutableStateOf<DebtEntity?>(null) }
    var debtToDelete by remember { mutableStateOf<DebtEntity?>(null) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showThemeColorWheelDialog by remember { mutableStateOf(false) }

    val filteredDebts = debts.filter { it.type == selectedDebtType }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Profil Pengguna
        item {
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 2.dp,
                backgroundColor = dynamicTheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(dynamicTheme.accentLight)
                                .clickable { showEditProfileDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfileUri != null) {
                                val model = if (userProfileUri!!.startsWith("/")) File(userProfileUri!!) else userProfileUri
                                AsyncImage(
                                    model = model,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = dynamicTheme.accent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                            Text(
                                text = if (language == "en") "Personal Profile" else "Profil Pribadi",
                                style = MaterialTheme.typography.bodySmall,
                                color = dynamicTheme.textSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(dynamicTheme.surfaceSecondary)
                            .clickable { showEditProfileDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = AppStrings.edit(language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Header Utang & Piutang
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.debts(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(dynamicTheme.accentLight)
                        .clickable { showAddDebtDialog = true }
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
                            text = if (language == "en") "New Entry" else "Catat Baru",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Ringkas: Utang vs Piutang
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(dynamicTheme.surfaceSecondary)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "UTANG" to (if (language == "en") "I Owe (Payable)" else "Utang"),
                    "PIUTANG" to (if (language == "en") "Owed to Me (Receivable)" else "Piutang")
                ).forEach { (typeKey, label) ->
                    val isSelected = selectedDebtType == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) dynamicTheme.surface else Color.Transparent)
                            .clickable { selectedDebtType = typeKey }
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

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (filteredDebts.isEmpty()) {
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = dynamicTheme.surface
                ) {
                    Text(
                        text = if (selectedDebtType == "UTANG")
                            (if (language == "en") "No debt records." else "Tidak ada catatan utang.")
                        else
                            (if (language == "en") "No receivable records." else "Tidak ada catatan piutang."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = dynamicTheme.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredDebts, key = { "debt_item_${it.id}" }) { debt ->
                DebtCardItem(
                    debt = debt,
                    isAmountHidden = isAmountHidden,
                    onPay = { debtToPay = debt },
                    onDelete = { debtToDelete = debt }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 3. Pengaturan Aplikasi & Kustomisasi
        item {
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = if (language == "en") "Settings & Customization" else "Pengaturan & Tampilan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = dynamicTheme.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                elevation = 1.dp,
                backgroundColor = dynamicTheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Fitur Kunci Kata Sandi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTheme.surfaceSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPasswordLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = dynamicTheme.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.security(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicTheme.textPrimary
                                )
                                Text(
                                    text = if (isPasswordLockEnabled)
                                        (if (language == "en") "Lock is Active" else "Kunci Sandi Aktif")
                                    else
                                        (if (language == "en") "Disabled" else "Tidak Aktif"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dynamicTheme.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isPasswordLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showSetPasswordDialog = true
                                } else {
                                    viewModel.settingsManager.disablePasswordLock()
                                    Toast.makeText(context, "Kunci sandi dinonaktifkan", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = dynamicTheme.accent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dynamicTheme.surfaceSecondary))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Ganti Bahasa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTheme.surfaceSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = dynamicTheme.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.languageLabel(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicTheme.textPrimary
                                )
                                Text(
                                    text = if (language == "en") "English (US)" else "Bahasa Indonesia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dynamicTheme.textSecondary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("id" to "ID", "en" to "EN").forEach { (code, label) ->
                                val isSel = language == code
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) dynamicTheme.accent else dynamicTheme.surfaceSecondary)
                                        .clickable { viewModel.settingsManager.setLanguage(code) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) dynamicTheme.onAccent else dynamicTheme.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dynamicTheme.surfaceSecondary))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Pengaturan Tema & Warna Terpadu (Roda Warna Aksen, Teks, & Latar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showThemeColorWheelDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTheme.accentLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = dynamicTheme.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == "en") "Theme & Colors" else "Tema & Warna",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicTheme.textPrimary
                                )
                                Text(
                                    text = if (language == "en") "Accent, Text, & Background" else "Roda Warna Aksen, Teks, & Latar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dynamicTheme.textSecondary
                                )
                            }
                        }

                        // Preview 3 lingkaran warna beririsan (Aksen, Teks, Latar)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((-6).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTheme.accent)
                                    .border(2.dp, dynamicTheme.surface, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTheme.textPrimary)
                                    .border(2.dp, dynamicTheme.surface, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(dynamicTheme.background)
                                    .border(2.dp, dynamicTheme.surface, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // 4. Cadangkan Data (Clean & Functional)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                elevation = 1.dp,
                backgroundColor = dynamicTheme.surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(dynamicTheme.accentLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = dynamicTheme.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == "en") "Backup Data (JSON)" else "Cadangkan Data (JSON)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportDataJson()
                                clipboardManager.setText(AnnotatedString(json))
                                Toast.makeText(context, "Data cadangan berhasil disalin", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (language == "en") "Export to Clipboard" else "Salin Cadangan JSON", color = dynamicTheme.onAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // Modal Edit Nama & Foto Profil
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(AppStrings.profile(language), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Preview Foto Profil dan Tombol Unggah
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(dynamicTheme.accentLight)
                            .clickable { profilePhotoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfileUri != null) {
                            val model = if (userProfileUri!!.startsWith("/")) File(userProfileUri!!) else userProfileUri
                            AsyncImage(
                                model = model,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = dynamicTheme.accent,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { profilePhotoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accentLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = dynamicTheme.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == "en") "Change Photo" else "Pilih Foto",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = dynamicTheme.accent
                            )
                        }

                        if (userProfileUri != null) {
                            TextButton(
                                onClick = {
                                    viewModel.settingsManager.setUserProfileUri(null)
                                    Toast.makeText(context, "Foto profil dihapus", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = if (language == "en") "Remove" else "Hapus",
                                    fontSize = 12.sp,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(AppStrings.name(language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.settingsManager.setUserName(tempName)
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profil diperbarui", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Modal Set Password / PIN
    if (showSetPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSetPasswordDialog = false },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(AppStrings.setPassword(language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == "en") "Enter a 4-6 digit PIN or password to secure your app:" else "Masukkan 4-6 digit PIN atau kata sandi untuk mengunci aplikasi:",
                        style = MaterialTheme.typography.bodySmall,
                        color = dynamicTheme.textSecondary
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text(AppStrings.setPassword(language)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text(if (language == "en") "Confirm Password" else "Ulangi Kata Sandi") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length >= 4 && newPass == confirmPass) {
                            viewModel.settingsManager.setPassword(newPass)
                            showSetPasswordDialog = false
                            Toast.makeText(context, "Kunci sandi berhasil diaktifkan", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Kata sandi minimal 4 karakter dan harus cocok", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent)
                ) {
                    Text(AppStrings.save(language), color = dynamicTheme.onAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPasswordDialog = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Modal Pengaturan Roda Warna, Saturasi, Kecerahan, & Palet Pilihan
    if (showThemeColorWheelDialog) {
        ThemeColorWheelDialog(
            initialAccentHex = themeConfig.accentColorHex,
            initialTextHex = themeConfig.textColorHex,
            initialBgHex = themeConfig.backgroundColorHex,
            language = language,
            onSaveColors = { acc, txt, bg ->
                viewModel.settingsManager.setThemeColors(acc, txt, bg)
                Toast.makeText(
                    context,
                    if (language == "en") "Theme updated successfully!" else "Tema warna berhasil diperbarui!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showThemeColorWheelDialog = false }
        )
    }

    // Dialog Tambah Utang / Piutang
    if (showAddDebtDialog) {
        var personName by remember { mutableStateOf("") }
        var debtAmountText by remember { mutableStateOf("") }
        var debtNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDebtDialog = false },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(if (selectedDebtType == "UTANG") "Catat Utang Baru" else "Catat Piutang Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text(if (language == "en") "Person / Entity Name" else "Nama Teman / Pihak Terkait") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = debtAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) debtAmountText = it },
                        label = { Text(if (language == "en") "Amount (Rp)" else "Nominal Pokok (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = debtNotes,
                        onValueChange = { debtNotes = it },
                        label = { Text(if (language == "en") "Notes (Optional)" else "Catatan (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = themedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = debtAmountText.toLongOrNull() ?: 0L
                        if (personName.isNotBlank() && amountVal > 0) {
                            viewModel.addDebt(
                                type = selectedDebtType,
                                personName = personName.trim(),
                                amount = amountVal,
                                dueDateMillis = System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000,
                                notes = debtNotes.ifBlank { null },
                                onSuccess = {
                                    showAddDebtDialog = false
                                    Toast.makeText(context, "Catatan tersimpan", Toast.LENGTH_SHORT).show()
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
                TextButton(onClick = { showAddDebtDialog = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Bayar / Terima Pelunasan Utang
    debtToPay?.let { debt ->
        var payAmountText by remember { mutableStateOf((debt.amount - debt.paidAmount).toString()) }
        var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }

        val actionTitle = if (debt.type == "UTANG") "Bayar Utang: ${debt.personName}" else "Terima Piutang: ${debt.personName}"

        AlertDialog(
            onDismissRequest = { debtToPay = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text(actionTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (debt.type == "UTANG") "Pilih Akun Sumber:" else "Pilih Akun Penerima:",
                        style = MaterialTheme.typography.bodySmall,
                        color = dynamicTheme.textSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts, key = { "pay_acc_${it.account.id}" }) { acc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedAccountId == acc.account.id) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                                    .clickable { selectedAccountId = acc.account.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.account.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedAccountId == acc.account.id) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAccountId == acc.account.id) dynamicTheme.onAccentLight else dynamicTheme.textPrimary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) payAmountText = it },
                        label = { Text("Nominal Pelunasan (Rp)") },
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
                        val amountVal = payAmountText.toLongOrNull() ?: 0L
                        if (amountVal > 0) {
                            viewModel.payDebt(
                                debtId = debt.id,
                                accountId = selectedAccountId,
                                amount = amountVal,
                                dateMillis = System.currentTimeMillis(),
                                notes = "Pelunasan: ${debt.personName}",
                                onSuccess = {
                                    debtToPay = null
                                    Toast.makeText(context, "Pelunasan berhasil dicatat", Toast.LENGTH_SHORT).show()
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
                TextButton(onClick = { debtToPay = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Utang
    debtToDelete?.let { debt ->
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            containerColor = dynamicTheme.surface,
            titleContentColor = dynamicTheme.textPrimary,
            textContentColor = dynamicTheme.textSecondary,
            title = { Text("Hapus Catatan?", fontWeight = FontWeight.Bold) },
            text = { Text("Hapus catatan utang/piutang '${debt.personName}' sebesar ${formatRupiah(debt.amount)}?", color = dynamicTheme.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDebt(debt.id)
                        debtToDelete = null
                    }
                ) {
                    Text(AppStrings.delete(language), color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun DebtCardItem(
    debt: DebtEntity,
    isAmountHidden: Boolean,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current
    val remaining = (debt.amount - debt.paidAmount).coerceAtLeast(0L)
    val isPaidOff = debt.status == "PAID_OFF" || remaining == 0L

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        elevation = 1.dp,
        backgroundColor = dynamicTheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPaidOff) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaidOff) Icons.Default.CheckCircle else Icons.Default.Handshake,
                            contentDescription = null,
                            tint = if (isPaidOff) dynamicTheme.accent else dynamicTheme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.textPrimary
                        )
                        Text(
                            text = if (debt.type == "UTANG") "Utang ke ${debt.personName}" else "Piutang dari ${debt.personName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = dynamicTheme.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = ExpenseRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Sisa:", style = MaterialTheme.typography.labelSmall, color = dynamicTheme.textSecondary)
                    Text(
                        text = if (isAmountHidden) "Rp •••" else formatRupiah(remaining),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaidOff) dynamicTheme.accent else ExpenseRed
                    )
                }

                if (!isPaidOff) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(dynamicTheme.accentLight)
                            .clickable(onClick = onPay)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (debt.type == "UTANG") "Bayar" else "Terima",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dynamicTheme.accent
                        )
                    }
                }
            }
        }
    }
}
