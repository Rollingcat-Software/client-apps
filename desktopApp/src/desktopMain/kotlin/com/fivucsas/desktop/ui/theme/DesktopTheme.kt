package com.fivucsas.desktop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DesktopDarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1220),
    primaryContainer = Color(0xFF1D4ED8),
    onPrimaryContainer = Color(0xFFEFF6FF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF155E75),
    onSecondaryContainer = Color(0xFFECFEFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF052E2B),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFF87171),
    onError = Color(0xFF2A0D0D),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F2937)
)

@Composable
fun DesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DesktopDarkColors,
        content = content
    )
}
