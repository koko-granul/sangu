package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.settings.AppSettingsManager
import com.example.ui.theme.LocalDynamicTheme
import com.example.ui.theme.parseHexColor
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun ThemeColorWheelDialog(
    initialAccentHex: String,
    initialTextHex: String,
    initialBgHex: String,
    language: String,
    onSaveColors: (accent: String, text: String, bg: String) -> Unit,
    onDismiss: () -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current

    // State untuk masing-masing dari 3 target warna
    var currentAccentHex by remember { mutableStateOf(initialAccentHex) }
    var currentTextHex by remember { mutableStateOf(initialTextHex) }
    var currentBgHex by remember { mutableStateOf(initialBgHex) }

    // 0: Aksen, 1: Teks, 2: Background
    var selectedTargetTab by remember { mutableIntStateOf(0) }

    // HSV values for currently active target tab
    var hue by remember { mutableFloatStateOf(160f) }
    var saturation by remember { mutableFloatStateOf(0.9f) }
    var valueBrightness by remember { mutableFloatStateOf(0.75f) }

    // Synchronize HSV whenever target tab changes or external hex updates
    LaunchedEffect(selectedTargetTab) {
        val activeHex = when (selectedTargetTab) {
            0 -> currentAccentHex
            1 -> currentTextHex
            else -> currentBgHex
        }
        val hsv = FloatArray(3)
        try {
            val colorInt = parseHexColor(activeHex).let {
                AndroidColor.argb(
                    (it.alpha * 255).toInt(),
                    (it.red * 255).toInt(),
                    (it.green * 255).toInt(),
                    (it.blue * 255).toInt()
                )
            }
            AndroidColor.colorToHSV(colorInt, hsv)
            hue = hsv[0]
            saturation = hsv[1]
            valueBrightness = hsv[2]
        } catch (_: Exception) {
            hue = 160f
            saturation = 0.9f
            valueBrightness = 0.75f
        }
    }

    // Helper to calculate and assign new color from HSV
    fun updateActiveColorFromHSV(h: Float, s: Float, v: Float) {
        hue = h
        saturation = s.coerceIn(0f, 1f)
        valueBrightness = v.coerceIn(0f, 1f)
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, valueBrightness))
        val hexString = String.format("#%06X", 0xFFFFFF and colorInt)

        when (selectedTargetTab) {
            0 -> currentAccentHex = hexString
            1 -> currentTextHex = hexString
            2 -> currentBgHex = hexString
        }
    }

    val activeColor = remember(hue, saturation, valueBrightness) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, valueBrightness)))
    }

    val activeHex = when (selectedTargetTab) {
        0 -> currentAccentHex
        1 -> currentTextHex
        else -> currentBgHex
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dynamicTheme.surface,
        titleContentColor = dynamicTheme.textPrimary,
        textContentColor = dynamicTheme.textSecondary,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "en") "Theme & Color Settings" else "Pengaturan Tema & Warna",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary
                    )
                    Text(
                        text = if (language == "en") "Wheel, Saturation, & Brightness" else "Roda Warna, Saturasi, & Kecerahan",
                        style = MaterialTheme.typography.bodySmall,
                        color = dynamicTheme.textSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButtonBox(
                        onClick = {
                            currentAccentHex = initialAccentHex
                            currentTextHex = initialTextHex
                            currentBgHex = initialBgHex
                            updateActiveColorFromHSV(160f, 0.9f, 0.75f)
                        },
                        icon = Icons.Default.Refresh,
                        contentDescription = "Reset"
                    )

                    // Tombol X untuk Keluar / Exit
                    IconButtonBox(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = if (language == "en") "Close" else "Tutup"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Selector 3 Target Warna: Aksen, Teks, Background
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dynamicTheme.surfaceSecondary)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val targetTabs = listOf(
                        Triple(0, if (language == "en") "Accent" else "Aksen", currentAccentHex),
                        Triple(1, if (language == "en") "Text" else "Teks", currentTextHex),
                        Triple(2, if (language == "en") "Background" else "Latar", currentBgHex)
                    )

                    targetTabs.forEach { (index, title, hex) ->
                        val isSelected = selectedTargetTab == index
                        val color = parseHexColor(hex)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) dynamicTheme.surface else Color.Transparent)
                                .clickable { selectedTargetTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) dynamicTheme.textPrimary else dynamicTheme.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RODA WARNA INTERAKTIF (COLOR WHEEL)
                Box(
                    modifier = Modifier
                        .size(185.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val rainbowColors = listOf(
                        Color.Red, Color.Yellow, Color.Green,
                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val dist = hypot(dx, dy)
                                    val radius = size.width / 2f
                                    if (dist <= radius) {
                                        val angleDeg = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360) % 360
                                        val sat = (dist / radius).toFloat().coerceIn(0f, 1f)
                                        updateActiveColorFromHSV(angleDeg.toFloat(), sat, valueBrightness)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = change.position.x - center.x
                                    val dy = change.position.y - center.y
                                    val dist = hypot(dx, dy)
                                    val radius = size.width / 2f
                                    val angleDeg = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360) % 360
                                    val sat = (dist / radius).toFloat().coerceIn(0f, 1f)
                                    updateActiveColorFromHSV(angleDeg.toFloat(), sat, valueBrightness)
                                }
                            }
                    ) {
                        val radius = size.width / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // 1. Gambar lingkaran roda spektrum hue (Sweep Gradient)
                        drawCircle(
                            brush = Brush.sweepGradient(rainbowColors, center),
                            radius = radius,
                            center = center
                        )

                        // 2. Gambar radial gradient putih di tengah untuk saturasi
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 1f - saturation * 0.1f), Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )

                        // 3. Shadow border melingkar
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.15f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 4. Handle indikator posisi kursor (Hue & Saturation)
                        val angleRad = Math.toRadians(hue.toDouble())
                        val handleDist = saturation * radius
                        val handleX = center.x + (handleDist * cos(angleRad)).toFloat()
                        val handleY = center.y + (handleDist * sin(angleRad)).toFloat()

                        drawCircle(
                            color = Color.Black.copy(alpha = 0.4f),
                            radius = 12.dp.toPx(),
                            center = Offset(handleX, handleY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 10.dp.toPx(),
                            center = Offset(handleX, handleY)
                        )
                        drawCircle(
                            color = activeColor,
                            radius = 7.dp.toPx(),
                            center = Offset(handleX, handleY)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SLIDER SATURASI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "en") "Saturation" else "Saturasi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = dynamicTheme.textSecondary,
                        modifier = Modifier.width(80.dp)
                    )
                    Slider(
                        value = saturation,
                        onValueChange = { s -> updateActiveColorFromHSV(hue, s, valueBrightness) },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = activeColor,
                            activeTrackColor = activeColor
                        )
                    )
                    Text(
                        text = "${(saturation * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary,
                        modifier = Modifier.width(40.dp)
                    )
                }

                // SLIDER KECERAHAN / BRIGHTNESS (VALUE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "en") "Brightness" else "Kecerahan",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = dynamicTheme.textSecondary,
                        modifier = Modifier.width(80.dp)
                    )
                    Slider(
                        value = valueBrightness,
                        onValueChange = { v -> updateActiveColorFromHSV(hue, saturation, v) },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = activeColor,
                            activeTrackColor = activeColor
                        )
                    )
                    Text(
                        text = "${(valueBrightness * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTheme.textPrimary,
                        modifier = Modifier.width(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hex input & indicator box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(activeColor)
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (selectedTargetTab) {
                                    0 -> if (language == "en") "Accent Color" else "Warna Aksen"
                                    1 -> if (language == "en") "Text Color" else "Warna Teks"
                                    else -> if (language == "en") "Background Color" else "Warna Latar"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = dynamicTheme.textPrimary
                            )
                            Text(
                                text = activeHex,
                                style = MaterialTheme.typography.bodySmall,
                                color = dynamicTheme.textSecondary
                            )
                        }
                    }

                    // Quick Hex field
                    var hexInput by remember(activeHex) { mutableStateOf(activeHex) }
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            hexInput = input
                            val clean = if (input.startsWith("#")) input else "#$input"
                            if (clean.length == 7) {
                                try {
                                    val parsed = parseHexColor(clean)
                                    val hsv = FloatArray(3)
                                    val cInt = AndroidColor.argb(255, (parsed.red * 255).toInt(), (parsed.green * 255).toInt(), (parsed.blue * 255).toInt())
                                    AndroidColor.colorToHSV(cInt, hsv)
                                    updateActiveColorFromHSV(hsv[0], hsv[1], hsv[2])
                                } catch (_: Exception) {}
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.width(110.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeColor,
                            unfocusedBorderColor = dynamicTheme.surfaceSecondary
                        ),
                        textStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LIVE PREVIEW CARD: Menunjukkan bagaimana aksen, teks, dan background berpadu
                Text(
                    text = if (language == "en") "Live Preview:" else "Pratinjau Hasil:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                val previewBg = parseHexColor(currentBgHex)
                val previewText = parseHexColor(currentTextHex)
                val previewAccent = parseHexColor(currentAccentHex)
                val isPreviewBgDark = previewBg.red * 0.299f + previewBg.green * 0.587f + previewBg.blue * 0.114f < 0.5f
                val previewSurface = if (isPreviewBgDark) {
                    Color(
                        (previewBg.red + 0.08f).coerceAtMost(1f),
                        (previewBg.green + 0.08f).coerceAtMost(1f),
                        (previewBg.blue + 0.09f).coerceAtMost(1f)
                    )
                } else {
                    Color(
                        (previewBg.red - 0.04f).coerceAtLeast(0f),
                        (previewBg.green - 0.04f).coerceAtLeast(0f),
                        (previewBg.blue - 0.04f).coerceAtLeast(0f)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = previewBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Layar Aplikasi (Background)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = previewText.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Box Card di dalam background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(previewSurface)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saldo Sangu",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = previewText.copy(alpha = 0.7f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(previewAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Aktif",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = previewAccent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Rp 2.450.000",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = previewText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(previewAccent)
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+ Catat Transaksi",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (previewAccent.red * 0.299f + previewAccent.green * 0.587f + previewAccent.blue * 0.114f > 0.5f) Color(0xFF0F172A) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PALET PRESET 1-SENTUHAN
                Text(
                    text = if (language == "en") "Quick Palette Presets:" else "Palet Pilihan Siap Pakai:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppSettingsManager.PALETTE_PRESETS.forEach { preset ->
                        val pName = if (language == "en") preset.nameEn else preset.nameId
                        val isMatched = currentAccentHex.equals(preset.accentHex, ignoreCase = true) &&
                                currentTextHex.equals(preset.textHex, ignoreCase = true) &&
                                currentBgHex.equals(preset.bgHex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isMatched) dynamicTheme.accentLight else dynamicTheme.surfaceSecondary)
                                .clickable {
                                    currentAccentHex = preset.accentHex
                                    currentTextHex = preset.textHex
                                    currentBgHex = preset.bgHex
                                    // Update active tab HSV
                                    val active = when (selectedTargetTab) {
                                        0 -> preset.accentHex
                                        1 -> preset.textHex
                                        else -> preset.bgHex
                                    }
                                    val hsv = FloatArray(3)
                                    val parsed = parseHexColor(active)
                                    val cInt = AndroidColor.argb(255, (parsed.red * 255).toInt(), (parsed.green * 255).toInt(), (parsed.blue * 255).toInt())
                                    AndroidColor.colorToHSV(cInt, hsv)
                                    updateActiveColorFromHSV(hsv[0], hsv[1], hsv[2])
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(preset.accentHex))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(preset.bgHex))
                                        .border(0.5.dp, Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isMatched) dynamicTheme.accent else dynamicTheme.textPrimary
                                )
                            }
                        }
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveColors(currentAccentHex, currentTextHex, currentBgHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicTheme.accent,
                    contentColor = dynamicTheme.onAccent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = dynamicTheme.onAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "en") "Apply Theme" else "Terapkan Tema",
                    fontWeight = FontWeight.Bold,
                    color = dynamicTheme.onAccent
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = if (language == "en") "Cancel" else "Batal",
                    color = dynamicTheme.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun IconButtonBox(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    val dynamicTheme = LocalDynamicTheme.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(dynamicTheme.surfaceSecondary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = dynamicTheme.textPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}
