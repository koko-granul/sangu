package com.example.core.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("daily_finance_user_prefs", Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "Pengguna") ?: "Pengguna")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _hasPassword = MutableStateFlow(!prefs.getString(KEY_PASSWORD_HASH, null).isNullOrBlank())
    val hasPassword: StateFlow<Boolean> = _hasPassword.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(!_hasPassword.value)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "id") ?: "id")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "light") ?: "light")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _accentColorHex = MutableStateFlow(prefs.getString(KEY_ACCENT_HEX, "#10B981") ?: "#10B981")
    val accentColorHex: StateFlow<String> = _accentColorHex.asStateFlow()

    private val _wallpaperType = MutableStateFlow(prefs.getString(KEY_WALLPAPER, "none") ?: "none")
    val wallpaperType: StateFlow<String> = _wallpaperType.asStateFlow()

    fun setUserName(name: String) {
        val trimmed = name.trim().ifBlank { "Pengguna" }
        prefs.edit().putString(KEY_USER_NAME, trimmed).apply()
        _userName.value = trimmed
    }

    fun setPassword(password: String) {
        val hash = hashPassword(password)
        prefs.edit().putString(KEY_PASSWORD_HASH, hash).apply()
        _hasPassword.value = true
        _isAppUnlocked.value = true
    }

    fun removePassword() {
        prefs.edit().remove(KEY_PASSWORD_HASH).apply()
        _hasPassword.value = false
        _isAppUnlocked.value = true
    }

    fun verifyPassword(password: String): Boolean {
        val savedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return true
        val inputHash = hashPassword(password)
        val matches = savedHash == inputHash
        if (matches) {
            _isAppUnlocked.value = true
        }
        return matches
    }

    fun lockApp() {
        if (_hasPassword.value) {
            _isAppUnlocked.value = false
        }
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setAccentColor(hex: String) {
        prefs.edit().putString(KEY_ACCENT_HEX, hex).apply()
        _accentColorHex.value = hex
    }

    fun setWallpaper(type: String) {
        prefs.edit().putString(KEY_WALLPAPER, type).apply()
        _wallpaperType.value = type
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_PASSWORD_HASH = "key_password_hash"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ACCENT_HEX = "key_accent_hex"
        private const val KEY_WALLPAPER = "key_wallpaper"
    }
}
