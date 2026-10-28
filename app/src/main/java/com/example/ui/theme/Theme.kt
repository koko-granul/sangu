package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.core.settings.ThemeCustomization

data class DynamicThemeColors(
    val accent: Color,
    val accentLight: Color,
    val onAccent: Color,
    val onAccentLight: Color,
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean
)

val LocalDynamicTheme = staticCompositionLocalOf {
    DynamicThemeColors(
        accent = EmeraldPrimary,
        accentLight = EmeraldLight,
        onAccent = Color.White,
        onAccentLight = EmeraldDark,
        background = BackgroundOffWhite,
        surface = SurfaceCard,
        surfaceSecondary = SurfaceSecondary,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        isDark = false
    )
}

@Composable
fun MyApplicationTheme(
    customization: ThemeCustomization = ThemeCustomization(),
    content: @Composable () -> Unit
) {
    val accent = parseHexColor(customization.accentColorHex, EmeraldPrimary)
    val textPrimary = parseHexColor(customization.textColorHex, TextPrimary)
    val background = parseHexColor(customization.backgroundColorHex, BackgroundOffWhite)

    val bgLum = background.luminance()
    val isDark = bgLum < 0.45f

    // Calculate elegant surface elevated above background
    val surface = if (isDark) {
        Color(
            red = (background.red + 0.07f).coerceIn(0f, 1f),
            green = (background.green + 0.07f).coerceIn(0f, 1f),
            blue = (background.blue + 0.08f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else {
        if (bgLum > 0.94f) Color(0xFFFFFFFF) else Color(
            red = (background.red + 0.03f).coerceIn(0f, 1f),
            green = (background.green + 0.03f).coerceIn(0f, 1f),
            blue = (background.blue + 0.03f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    val surfaceSecondary = if (isDark) {
        Color(
            red = (surface.red + 0.08f).coerceIn(0f, 1f),
            green = (surface.green + 0.08f).coerceIn(0f, 1f),
            blue = (surface.blue + 0.09f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else {
        Color(
            red = (background.red - 0.04f).coerceIn(0f, 1f),
            green = (background.green - 0.04f).coerceIn(0f, 1f),
            blue = (background.blue - 0.04f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    val textSecondary = textPrimary.copy(alpha = 0.65f)
    val onAccent = getContrastingTextColor(accent)
    val accentLight = getAccentLight(accent, isDark)
    val onAccentLight = getOnAccentLightColor(accent, isDark)

    val dynamicColors = DynamicThemeColors(
        accent = accent,
        accentLight = accentLight,
        onAccent = onAccent,
        onAccentLight = onAccentLight,
        background = background,
        surface = surface,
        surfaceSecondary = surfaceSecondary,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        isDark = isDark
    )

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentLight,
            onPrimaryContainer = accent,
            secondary = accent,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceSecondary,
            onSurfaceVariant = textSecondary,
            error = ExpenseRed
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentLight,
            onPrimaryContainer = accent,
            secondary = accent,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceSecondary,
            onSurfaceVariant = textSecondary,
            error = ExpenseRed
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = background.toArgb()
            window.navigationBarColor = background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalDynamicTheme provides dynamicColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
