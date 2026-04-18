package com.fivucsas.shared.ui.theme

/**
 * User-selectable theme mode.
 *
 * - [SYSTEM] follows the platform / OS dark-mode setting (the default, and the only
 *   value currently honoured on iOS and Desktop).
 * - [LIGHT] forces the light palette regardless of system setting.
 * - [DARK] forces the dark palette regardless of system setting.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
