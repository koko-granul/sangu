package com.example.core.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemeCustomization(
    val accentColorHex: String = "#10B981",
    val textColorHex: String = "#1E293B",
    val backgroundColorHex: String = "#F8F9FA"
)

data class ThemePalettePreset(
    val nameId: String,
    val nameEn: String,
    val accentHex: String,
    val textHex: String,
    val bgHex: String
)

class AppSettingsManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("daily_finance_settings", Context.MODE_PRIVATE)

    // User Profile
    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "User") ?: "User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userProfileUri = MutableStateFlow(prefs.getString(KEY_USER_PROFILE_URI, null))
    val userProfileUri: StateFlow<String?> = _userProfileUri.asStateFlow()

    // Language ("id" or "en")
    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "id") ?: "id")
    val language: StateFlow<String> = _language.asStateFlow()

    // Password Lock
    private val _isPasswordLockEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_PASSWORD_LOCK_ENABLED, false))
    val isPasswordLockEnabled: StateFlow<Boolean> = _isPasswordLockEnabled.asStateFlow()

    // Unlock State (in-memory)
    private val _isUnlocked = MutableStateFlow(!_isPasswordLockEnabled.value)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Theme Customization (Aksen, Teks, Background)
    private val _themeConfig = MutableStateFlow(
        ThemeCustomization(
            accentColorHex = prefs.getString(KEY_ACCENT_COLOR, "#10B981") ?: "#10B981",
            textColorHex = prefs.getString(KEY_TEXT_COLOR, "#1E293B") ?: "#1E293B",
            backgroundColorHex = prefs.getString(KEY_BACKGROUND_COLOR, "#F8F9FA") ?: "#F8F9FA"
        )
    )
    val themeConfig: StateFlow<ThemeCustomization> = _themeConfig.asStateFlow()

    fun setUserName(name: String) {
        val cleanName = name.trim().ifEmpty { "User" }
        prefs.edit().putString(KEY_USER_NAME, cleanName).apply()
        _userName.value = cleanName
    }

    fun setUserProfileUri(uri: String?) {
        prefs.edit().putString(KEY_USER_PROFILE_URI, uri).apply()
        _userProfileUri.value = uri
    }

    fun setLanguage(langCode: String) {
        val code = if (langCode == "en") "en" else "id"
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
        _language.value = code
    }

    fun setPassword(newPass: String) {
        prefs.edit()
            .putString(KEY_PASSWORD_HASH, newPass)
            .putBoolean(KEY_PASSWORD_LOCK_ENABLED, true)
            .apply()
        _isPasswordLockEnabled.value = true
        _isUnlocked.value = true
    }

    fun disablePasswordLock() {
        prefs.edit()
            .remove(KEY_PASSWORD_HASH)
            .putBoolean(KEY_PASSWORD_LOCK_ENABLED, false)
            .apply()
        _isPasswordLockEnabled.value = false
        _isUnlocked.value = true
    }

    fun verifyPassword(input: String): Boolean {
        val saved = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
        val matched = saved == input
        if (matched) {
            _isUnlocked.value = true
        }
        return matched
    }

    fun lockApp() {
        if (_isPasswordLockEnabled.value) {
            _isUnlocked.value = false
        }
    }

    fun setAccentColor(hex: String) {
        val clean = cleanHex(hex, "#10B981")
        prefs.edit().putString(KEY_ACCENT_COLOR, clean).apply()
        _themeConfig.value = _themeConfig.value.copy(accentColorHex = clean)
    }

    fun setTextColor(hex: String) {
        val clean = cleanHex(hex, "#1E293B")
        prefs.edit().putString(KEY_TEXT_COLOR, clean).apply()
        _themeConfig.value = _themeConfig.value.copy(textColorHex = clean)
    }

    fun setBackgroundColor(hex: String) {
        val clean = cleanHex(hex, "#F8F9FA")
        prefs.edit().putString(KEY_BACKGROUND_COLOR, clean).apply()
        _themeConfig.value = _themeConfig.value.copy(backgroundColorHex = clean)
    }

    fun setThemeColors(accentHex: String, textHex: String, bgHex: String) {
        val cleanAccent = cleanHex(accentHex, "#10B981")
        val cleanText = cleanHex(textHex, "#1E293B")
        val cleanBg = cleanHex(bgHex, "#F8F9FA")

        prefs.edit()
            .putString(KEY_ACCENT_COLOR, cleanAccent)
            .putString(KEY_TEXT_COLOR, cleanText)
            .putString(KEY_BACKGROUND_COLOR, cleanBg)
            .apply()

        _themeConfig.value = ThemeCustomization(
            accentColorHex = cleanAccent,
            textColorHex = cleanText,
            backgroundColorHex = cleanBg
        )
    }

    private fun cleanHex(input: String, default: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("#") && (trimmed.length == 7 || trimmed.length == 9)) {
            return trimmed.uppercase()
        }
        if (trimmed.length == 6) {
            return "#${trimmed.uppercase()}"
        }
        return default
    }

    companion object {
        private const val KEY_USER_NAME = "pref_user_name"
        private const val KEY_USER_PROFILE_URI = "pref_user_profile_uri"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_PASSWORD_LOCK_ENABLED = "pref_pwd_lock_enabled"
        private const val KEY_PASSWORD_HASH = "pref_pwd_hash"
        private const val KEY_ACCENT_COLOR = "pref_accent_color"
        private const val KEY_TEXT_COLOR = "pref_text_color"
        private const val KEY_BACKGROUND_COLOR = "pref_background_color"

        val PALETTE_PRESETS = listOf(
            ThemePalettePreset("Emerald Minimal", "Emerald Minimal", "#10B981", "#1E293B", "#F8F9FA"),
            ThemePalettePreset("Dark Slate", "Dark Slate", "#10B981", "#F8FAFC", "#0F172A"),
            ThemePalettePreset("Ocean Sapphire", "Ocean Sapphire", "#2563EB", "#0F172A", "#F0F9FF"),
            ThemePalettePreset("Midnight Neon", "Midnight Neon", "#06B6D4", "#ECFEFF", "#091224"),
            ThemePalettePreset("Sunset Amber", "Sunset Amber", "#F59E0B", "#261302", "#FFFBEB"),
            ThemePalettePreset("Crimson Luxe", "Crimson Luxe", "#E11D48", "#FFE4E6", "#1C0D12"),
            ThemePalettePreset("Royal Violet", "Royal Violet", "#8B5CF6", "#1E1B4B", "#F5F3FF"),
            ThemePalettePreset("Monokrom OLED", "OLED Monochrome", "#94A3B8", "#FFFFFF", "#000000")
        )
    }
}
