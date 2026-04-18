package com.fivucsas.mobile.android.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.fivucsas.shared.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user-selected [ThemeMode] and exposes it as a reactive
 * [StateFlow] so Compose can collect it at the root and drive
 * `LocalThemeMode`.
 *
 * A plain `SharedPreferences` is deliberate here — a visual preference is
 * not security-sensitive, so `EncryptedSharedPreferences` would be overkill
 * (and would slow app start for no benefit). Keys/files are shared with
 * `AppPreferences` to keep disk I/O in one place.
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())

    /** Collect this at the root of the app to drive `LocalThemeMode`. */
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Held as a field so it isn't garbage-collected — SharedPreferences uses a WeakHashMap.
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_THEME_MODE) {
            _themeMode.value = readThemeMode()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /** Returns the currently persisted mode, defaulting to [ThemeMode.SYSTEM]. */
    fun getThemeMode(): ThemeMode = readThemeMode()

    /**
     * Persist the new [mode]. The change will propagate through [themeMode]
     * via the registered change listener, so callers never need to emit
     * manually.
     */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        // Fallback: also emit directly. apply() is async and the change
        // listener runs on the main thread; tests and tight loops benefit
        // from the StateFlow reflecting the write immediately.
        _themeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    companion object {
        private const val PREFS_NAME = "fivucsas_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
