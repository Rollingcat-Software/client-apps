package com.fivucsas.shared.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal carrying the user's selected [ThemeMode].
 *
 * Platforms that support user-driven overrides (currently Android via
 * `ThemePreferences`) wrap their root Compose tree in
 * `CompositionLocalProvider(LocalThemeMode provides currentMode) { ... }`.
 * Platforms that only follow the OS simply read the default
 * ([ThemeMode.SYSTEM]) — keeping iOS / Desktop compilation untouched.
 *
 * Theme composables should read `LocalThemeMode.current` and resolve it to
 * a concrete light/dark palette, falling back to `isSystemInDarkTheme()`
 * when the value is [ThemeMode.SYSTEM].
 */
val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }
