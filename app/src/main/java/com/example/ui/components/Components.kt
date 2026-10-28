package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LocalDynamicTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

private val rupiahLocale: Locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
private val rupiahFormatterThreadLocal = ThreadLocal.withInitial {
    NumberFormat.getCurrencyInstance(rupiahLocale).apply {
        maximumFractionDigits = 0
    }
}

fun formatRupiah(amount: Long): String {
    val formatter = rupiahFormatterThreadLocal.get() ?: NumberFormat.getCurrencyInstance(rupiahLocale).apply {
        maximumFractionDigits = 0
    }
    return formatter.format(amount).replace("Rp", "Rp ")
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 2.dp,
    backgroundColor: Color = SurfaceCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = Color(0x12000000),
            ambientColor = Color(0x08000000)
        )
        .clip(shape)
        .background(backgroundColor)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceSecondary,
    iconTint: Color = TextPrimary,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val dynamicTheme = LocalDynamicTheme.current

    SoftCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 2.dp,
        backgroundColor = dynamicTheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(dynamicTheme.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = dynamicTheme.accent,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = dynamicTheme.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = dynamicTheme.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

class ThousandsSeparatorVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
        }

        val digits = original.filter { it.isDigit() }
        if (digits.isEmpty()) {
            return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
        }

        val formatted = buildString {
            val len = digits.length
            for (i in 0 until len) {
                append(digits[i])
                val remaining = len - 1 - i
                if (remaining > 0 && remaining % 3 == 0) {
                    append('.')
                }
            }
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, original.length)
                val digitsCount = original.take(clamped).count { it.isDigit() }
                if (digitsCount == 0) return 0

                var passedDigits = 0
                var transformedIndex = 0
                while (transformedIndex < formatted.length && passedDigits < digitsCount) {
                    if (formatted[transformedIndex].isDigit()) {
                        passedDigits++
                    }
                    transformedIndex++
                }
                return transformedIndex.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                val digitsBefore = formatted.take(clamped).count { it.isDigit() }
                return digitsBefore.coerceIn(0, original.length)
            }
        }

        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(formatted),
            offsetMapping
        )
    }
}

@Composable
fun themedTextFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = LocalDynamicTheme.current.textPrimary,
    unfocusedTextColor = LocalDynamicTheme.current.textPrimary,
    focusedBorderColor = LocalDynamicTheme.current.accent,
    unfocusedBorderColor = LocalDynamicTheme.current.textSecondary.copy(alpha = 0.35f),
    cursorColor = LocalDynamicTheme.current.accent,
    focusedLabelColor = LocalDynamicTheme.current.accent,
    unfocusedLabelColor = LocalDynamicTheme.current.textSecondary,
    focusedContainerColor = LocalDynamicTheme.current.surfaceSecondary,
    unfocusedContainerColor = LocalDynamicTheme.current.surfaceSecondary
)

