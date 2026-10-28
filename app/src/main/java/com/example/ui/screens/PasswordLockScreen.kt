package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppStrings
import com.example.ui.FinanceViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalDynamicTheme

@Composable
fun PasswordLockScreen(viewModel: FinanceViewModel) {
    val dynamicTheme = LocalDynamicTheme.current
    val language by viewModel.settingsManager.language.collectAsState()
    val userName by viewModel.settingsManager.userName.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicTheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(dynamicTheme.accentLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = dynamicTheme.accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (language == "en") "Welcome Back, $userName" else "Selamat Datang, $userName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = dynamicTheme.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = AppStrings.enterPassword(language),
                style = MaterialTheme.typography.bodyMedium,
                color = dynamicTheme.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                    errorMessage = null
                },
                placeholder = { Text("••••", color = dynamicTheme.textSecondary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val verified = viewModel.settingsManager.verifyPassword(passwordInput)
                    if (!verified) {
                        errorMessage = AppStrings.wrongPassword(language)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = dynamicTheme.accent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = AppStrings.unlock(language),
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.onAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showResetConfirm = true }) {
                Text(
                    text = if (language == "en") "Forgot Password?" else "Lupa Kata Sandi?",
                    style = MaterialTheme.typography.bodySmall,
                    color = dynamicTheme.textSecondary
                )
            }
        }
    }

    if (showResetConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(if (language == "en") "Reset Password?" else "Atur Ulang Kata Sandi?") },
            text = {
                Text(
                    if (language == "en")
                        "Resetting password will disable the lock so you can regain access."
                    else
                        "Mengatur ulang kata sandi akan menonaktifkan kunci agar Anda dapat mengakses aplikasi kembali."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.settingsManager.disablePasswordLock()
                        showResetConfirm = false
                    }
                ) {
                    Text(if (language == "en") "Reset" else "Atur Ulang", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(AppStrings.cancel(language), color = dynamicTheme.textSecondary)
                }
            }
        )
    }
}
