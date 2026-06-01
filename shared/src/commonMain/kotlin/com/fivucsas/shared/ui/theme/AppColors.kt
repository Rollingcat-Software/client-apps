package com.fivucsas.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Application Color Palette — theme-aware (light + dark).
 *
 * Ported from the web app design tokens (`web-app/src/theme.ts`): indigo
 * primary, purple secondary, slate neutrals, emerald/amber/red/sky semantics —
 * with the dark palette mapped from the web's `INK.dark` scale.
 *
 * `AppColors.X` accessors are `@Composable` getters that read [LocalAppColors]
 * (provided by `FIVUCSASTheme`), so every existing `AppColors.Primary`-style
 * call site keeps working AND now follows the light/dark theme automatically —
 * no call-site changes. (Constraint: `AppColors.X` can only be read from a
 * @Composable context. There are no non-composable reads today; keep it that
 * way — for a non-composable need, read [LocalAppColors] or pass the value in.)
 *
 * [White] and [Black] stay true constants (some call sites mean "always white",
 * e.g. text on a colored gradient) and are intentionally NOT theme-aware.
 */
data class AppColorScheme(
    val primary: Color,
    val primaryVariant: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val onSecondary: Color,
    val success: Color,
    val successDark: Color,
    val onSuccess: Color,
    val warning: Color,
    val warningDark: Color,
    val onWarning: Color,
    val error: Color,
    val errorDark: Color,
    val onError: Color,
    val info: Color,
    val infoDark: Color,
    val onInfo: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val gray50: Color,
    val gray100: Color,
    val gray200: Color,
    val gray300: Color,
    val gray400: Color,
    val gray500: Color,
    val gray600: Color,
    val gray700: Color,
    val gray800: Color,
    val gray900: Color,
    val bgGradientTop: Color,
    val bgGradientBottom: Color,
)

/** Light scheme — the web `theme.ts` light tokens. */
val LightAppColors = AppColorScheme(
    primary = Color(0xFF6366F1),
    primaryVariant = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF8B5CF6),
    secondaryVariant = Color(0xFF7C3AED),
    onSecondary = Color(0xFFFFFFFF),
    success = Color(0xFF10B981),
    successDark = Color(0xFF059669),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFF59E0B),
    warningDark = Color(0xFFD97706),
    onWarning = Color(0xFF1F2937),
    error = Color(0xFFEF4444),
    errorDark = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF3B82F6),
    infoDark = Color(0xFF2563EB),
    onInfo = Color(0xFFFFFFFF),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    gray50 = Color(0xFFF8FAFC),
    gray100 = Color(0xFFF1F5F9),
    gray200 = Color(0xFFE2E8F0),
    gray300 = Color(0xFFCBD5E1),
    gray400 = Color(0xFF94A3B8),
    gray500 = Color(0xFF64748B),
    gray600 = Color(0xFF475569),
    gray700 = Color(0xFF334155),
    gray800 = Color(0xFF1E293B),
    gray900 = Color(0xFF0F172A),
    bgGradientTop = Color(0xFFF8FAFC),
    bgGradientBottom = Color(0xFFEEF2FF),
)

/** Dark scheme — mapped from the web `theme.ts` `INK.dark` scale + brand dark variants. */
val DarkAppColors = AppColorScheme(
    primary = Color(0xFF818CF8),
    primaryVariant = Color(0xFF6366F1),
    onPrimary = Color(0xFF312E81),
    secondary = Color(0xFFA78BFA),
    secondaryVariant = Color(0xFF8B5CF6),
    onSecondary = Color(0xFF2E1065),
    success = Color(0xFF34D399),
    successDark = Color(0xFF10B981),
    onSuccess = Color(0xFF052E16),
    warning = Color(0xFFFBBF24),
    warningDark = Color(0xFFF59E0B),
    onWarning = Color(0xFF1F2937),
    error = Color(0xFFF87171),
    errorDark = Color(0xFFEF4444),
    onError = Color(0xFF450A0A),
    info = Color(0xFF60A5FA),
    infoDark = Color(0xFF3B82F6),
    onInfo = Color(0xFF0E2251),
    background = Color(0xFF0F1220),
    onBackground = Color(0xFFE6E8F3),
    surface = Color(0xFF1A1F33),
    surfaceVariant = Color(0xFF242941),
    onSurface = Color(0xFFE6E8F3),
    onSurfaceVariant = Color(0xFF8F96AE),
    gray50 = Color(0xFF141828),
    gray100 = Color(0xFF1A1F33),
    gray200 = Color(0xFF242941),
    gray300 = Color(0xFF2E3452),
    gray400 = Color(0xFF505875),
    gray500 = Color(0xFF646C8A),
    gray600 = Color(0xFF8F96AE),
    gray700 = Color(0xFFA9AFC4),
    gray800 = Color(0xFFC7CCDB),
    gray900 = Color(0xFFE6E8F3),
    bgGradientTop = Color(0xFF0F1220),
    bgGradientBottom = Color(0xFF1A1F33),
)

/** Provided by `FIVUCSASTheme`. Static: the scheme only changes at the theme root. */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppColors {
    // Brand
    val Primary: Color           @Composable @ReadOnlyComposable get() = LocalAppColors.current.primary
    val PrimaryVariant: Color    @Composable @ReadOnlyComposable get() = LocalAppColors.current.primaryVariant
    val OnPrimary: Color         @Composable @ReadOnlyComposable get() = LocalAppColors.current.onPrimary
    val Secondary: Color         @Composable @ReadOnlyComposable get() = LocalAppColors.current.secondary
    val SecondaryVariant: Color  @Composable @ReadOnlyComposable get() = LocalAppColors.current.secondaryVariant
    val OnSecondary: Color       @Composable @ReadOnlyComposable get() = LocalAppColors.current.onSecondary

    // Semantic
    val Success: Color     @Composable @ReadOnlyComposable get() = LocalAppColors.current.success
    val SuccessDark: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.successDark
    val OnSuccess: Color   @Composable @ReadOnlyComposable get() = LocalAppColors.current.onSuccess
    val Warning: Color     @Composable @ReadOnlyComposable get() = LocalAppColors.current.warning
    val WarningDark: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.warningDark
    val OnWarning: Color   @Composable @ReadOnlyComposable get() = LocalAppColors.current.onWarning
    val Error: Color       @Composable @ReadOnlyComposable get() = LocalAppColors.current.error
    val ErrorDark: Color   @Composable @ReadOnlyComposable get() = LocalAppColors.current.errorDark
    val OnError: Color     @Composable @ReadOnlyComposable get() = LocalAppColors.current.onError
    val Info: Color        @Composable @ReadOnlyComposable get() = LocalAppColors.current.info
    val InfoDark: Color    @Composable @ReadOnlyComposable get() = LocalAppColors.current.infoDark
    val OnInfo: Color      @Composable @ReadOnlyComposable get() = LocalAppColors.current.onInfo

    // Surfaces
    val Background: Color        @Composable @ReadOnlyComposable get() = LocalAppColors.current.background
    val OnBackground: Color      @Composable @ReadOnlyComposable get() = LocalAppColors.current.onBackground
    val Surface: Color           @Composable @ReadOnlyComposable get() = LocalAppColors.current.surface
    val SurfaceVariant: Color    @Composable @ReadOnlyComposable get() = LocalAppColors.current.surfaceVariant
    val OnSurface: Color         @Composable @ReadOnlyComposable get() = LocalAppColors.current.onSurface
    val OnSurfaceVariant: Color  @Composable @ReadOnlyComposable get() = LocalAppColors.current.onSurfaceVariant

    // True constants (intentionally NOT theme-aware — "always white/black")
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // Neutral / slate ramp (role-mapped in dark)
    val Gray50: Color  @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray50
    val Gray100: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray100
    val Gray200: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray200
    val Gray300: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray300
    val Gray400: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray400
    val Gray500: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray500
    val Gray600: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray600
    val Gray700: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray700
    val Gray800: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray800
    val Gray900: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray900

    // Gradients — derived from the active scheme
    val PrimaryGradient: Brush
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.let {
            Brush.linearGradient(listOf(it.primary, it.secondary))
        }
    val SecondaryGradient: Brush
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.let {
            Brush.linearGradient(listOf(it.secondary, Color(0xFFEC4899)))
        }
    val SuccessGradient: Brush
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.let {
            Brush.linearGradient(listOf(it.success, it.successDark))
        }
    val BackgroundGradient: Brush
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.let {
            Brush.verticalGradient(listOf(it.bgGradientTop, it.bgGradientBottom))
        }
    val KioskBackgroundGradient: Brush
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.let {
            Brush.verticalGradient(listOf(it.bgGradientBottom, it.surface))
        }
    val DisabledGradient: Brush
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.let {
            Brush.linearGradient(listOf(it.gray400, it.gray500))
        }
}
