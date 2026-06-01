package com.fivucsas.mobile.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fivucsas.mobile.android.R
import com.fivucsas.mobile.android.data.preferences.ThemePreferences
import com.fivucsas.shared.ui.theme.DarkAppColors
import com.fivucsas.shared.ui.theme.LightAppColors
import com.fivucsas.shared.ui.theme.LocalAppColors
import com.fivucsas.shared.ui.theme.LocalThemeMode
import com.fivucsas.shared.ui.theme.ThemeMode
import org.koin.compose.koinInject

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

// Typography aligned with web-app theme.ts: Poppins headings + Inter body.
// Inter is the variable font (weights synthesized below 400-master on API<26);
// Poppins ships as static per-weight files.
private val Inter = FontFamily(Font(R.font.inter))
private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.01).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.02.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    )
)

/**
 * Application theme.
 *
 * Self-wiring: collects [ThemePreferences] from Koin and publishes the
 * user's selected [ThemeMode] on [LocalThemeMode] before rendering the
 * MaterialTheme. Any callsite already wrapped in `FIVUCSASTheme { ... }`
 * (including `MainActivity`) picks up the toggle automatically, so
 * `SettingsScreen` just needs to call `ThemePreferences.setThemeMode(...)`
 * to repaint the whole app.
 *
 * The [darkTheme] parameter is kept for callers that want to force a
 * specific palette (e.g. screenshot tests) and wins over the preference
 * when non-null. Resolution order when [darkTheme] is null:
 *  - `LIGHT` → light palette
 *  - `DARK` → dark palette
 *  - `SYSTEM` → `isSystemInDarkTheme()`
 */
@Composable
fun FIVUCSASTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val prefs: ThemePreferences = koinInject()
    val mode by prefs.themeMode.collectAsState()

    // AppColors is now theme-aware (LocalAppColors), so the whole app — both the
    // MaterialTheme colorScheme AND the shared AppColors.* call sites — follows
    // light/dark together. (This replaces the earlier force-light workaround.)
    val useDark = darkTheme ?: when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (useDark) DarkColors else LightColors
    val appColors = if (useDark) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalThemeMode provides mode,
        LocalAppColors provides appColors,
    ) {

        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            content = content
        )
    }
}
