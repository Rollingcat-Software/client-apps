package com.fivucsas.mobile.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Design tokens aligned with web-app theme.ts
// Primary: Indigo (#6366f1), Secondary: Purple (#8b5cf6)
// Success: Emerald (#10b981), Warning: Amber (#f59e0b)
// Error: Red (#ef4444), Info: Blue (#3b82f6)

private val PrimaryColor = Color(0xFF6366F1)
private val PrimaryLight = Color(0xFF818CF8)
private val PrimaryDark = Color(0xFF4F46E5)
private val SecondaryColor = Color(0xFF8B5CF6)
private val SecondaryLight = Color(0xFFA78BFA)
private val SecondaryDark = Color(0xFF7C3AED)
private val ErrorColor = Color(0xFFEF4444)
private val ErrorLight = Color(0xFFF87171)
private val ErrorDark = Color(0xFFDC2626)
private val SuccessColor = Color(0xFF10B981)
private val WarningColor = Color(0xFFF59E0B)
private val InfoColor = Color(0xFF3B82F6)

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = PrimaryDark,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F3FF),
    onSecondaryContainer = SecondaryDark,
    tertiary = InfoColor,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEFF6FF),
    onTertiaryContainer = Color(0xFF2563EB),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E293B),
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF1F5F9),
    error = ErrorColor,
    onError = Color.White,
    errorContainer = Color(0xFFFEF2F2),
    onErrorContainer = ErrorDark
)

private val DarkColors = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF312E81),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = PrimaryLight,
    secondary = SecondaryLight,
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = Color(0xFF2E1065),
    onSecondaryContainer = SecondaryLight,
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFF172554),
    tertiaryContainer = Color(0xFF172554),
    onTertiaryContainer = Color(0xFF60A5FA),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = ErrorLight,
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = ErrorLight
)

// Typography aligned with web-app: Inter body, Poppins headings
// Using system sans-serif as a fallback (Inter/Poppins require bundled fonts)
private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.01).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.02.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    )
)

@Composable
fun FIVUCSASTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
