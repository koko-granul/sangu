package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Default Emerald Palette
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldDark = Color(0xFF047857)
val EmeraldLight = Color(0xFFD1FAE5)
val EmeraldTonal = Color(0xFFA7F3D0)

// Surfaces & Backgrounds - Light Mode
val BackgroundOffWhite = Color(0xFFF8F9FA)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceSecondary = Color(0xFFF0F2F1)

// Surfaces & Backgrounds - Dark Mode
val BackgroundDark = Color(0xFF0F172A)
val SurfaceCardDark = Color(0xFF1E293B)
val SurfaceSecondaryDark = Color(0xFF334155)

// Text Colors
val TextPrimary = Color(0xFF1E293B)
val TextSecondary = Color(0xFF64748B)
val TextTertiary = Color(0xFF94A3B8)

val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)

val ExpenseRed = Color(0xFFEF4444)
val IncomeEmerald = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val InfoBlue = Color(0xFF3B82F6)
val PurplePot = Color(0xFF8B5CF6)

val ShadowSoft = Color(0x0A000000)
val BorderHairline = Color(0x0D000000)

/**
 * Utility to parse hex color safely with fallback.
 */
fun parseHexColor(hex: String, fallback: Color = EmeraldPrimary): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
            8 -> cleanHex.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Utility to get readable text color (white or dark) based on background luminance.
 */
fun getContrastingTextColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5f) Color(0xFF0F172A) else Color.White
}

/**
 * Generates lighter tint of an accent color for chip/button backgrounds.
 */
fun getAccentLight(accent: Color, isDark: Boolean): Color {
    return if (isDark) {
        accent.copy(alpha = 0.22f)
    } else {
        accent.copy(alpha = 0.16f)
    }
}

/**
 * Psychological contrast calculation to ensure text on accentLight is always sharp & readable.
 * Solves the issue where light accents (Amber, Teal, etc.) have low contrast against tinted boxes.
 */
fun getOnAccentLightColor(accent: Color, isDark: Boolean): Color {
    return if (isDark) {
        Color(0xFFF8FAFC)
    } else {
        // In light mode, if accent has high luminance (> 0.30 like Amber Gold),
        // text must be sharp dark slate to avoid unreadable yellow-on-yellow.
        if (accent.luminance() > 0.30f) {
            Color(0xFF0F172A)
        } else {
            // Deepen saturated color for darker accents (Blue, Violet, Emerald, Rose)
            Color(
                red = (accent.red * 0.65f).coerceIn(0f, 1f),
                green = (accent.green * 0.65f).coerceIn(0f, 1f),
                blue = (accent.blue * 0.65f).coerceIn(0f, 1f),
                alpha = 1f
            )
        }
    }
}
