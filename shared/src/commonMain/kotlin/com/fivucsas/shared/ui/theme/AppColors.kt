package com.fivucsas.shared.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Application Color Palette
 *
 * Ported from the web app design tokens (`web-app/src/theme.ts`) so the mobile
 * app and app.fivucsas.com share ONE FIVUCSAS brand:
 *   - Primary  : Indigo  #6366f1   (was stock Material blue #1976d2)
 *   - Secondary: Purple  #8b5cf6   (was cyan #00acc1)
 *   - Neutrals : Slate scale (#0f172a … #f8fafc)
 *   - Semantic : emerald / amber / red / sky
 *
 * Property NAMES are unchanged so every existing call site keeps compiling;
 * only the values moved onto the web palette. This is what eliminates the
 * blue/cyan-vs-indigo clash on screens that read `AppColors.*` directly while
 * the rest of the app reads the (already web-aligned) `MaterialTheme.colorScheme`.
 *
 * NOTE: these are LIGHT-mode tokens. Theme-aware screens should prefer
 * `MaterialTheme.colorScheme` (Theme.kt mirrors these for both light and dark).
 */
object AppColors {
    // ============================================
    // Primary — Indigo (#6366f1)
    // ============================================

    val Primary = Color(0xFF6366F1)
    val PrimaryVariant = Color(0xFF4F46E5)
    val OnPrimary = Color(0xFFFFFFFF)

    // ============================================
    // Secondary — Purple / iris (#8b5cf6)
    // ============================================

    val Secondary = Color(0xFF8B5CF6)
    val SecondaryVariant = Color(0xFF7C3AED)
    val OnSecondary = Color(0xFFFFFFFF)

    // ============================================
    // Semantic Colors
    // ============================================

    val Success = Color(0xFF10B981)           // Emerald
    val SuccessDark = Color(0xFF059669)
    val OnSuccess = Color(0xFFFFFFFF)

    val Warning = Color(0xFFF59E0B)           // Amber
    val WarningDark = Color(0xFFD97706)
    val OnWarning = Color(0xFF1F2937)         // Slate-800 text on amber

    val Error = Color(0xFFEF4444)             // Red
    val ErrorDark = Color(0xFFDC2626)
    val OnError = Color(0xFFFFFFFF)

    val Info = Color(0xFF3B82F6)              // Sky
    val InfoDark = Color(0xFF2563EB)
    val OnInfo = Color(0xFFFFFFFF)

    // ============================================
    // Surface Colors — slate neutrals
    // ============================================

    val Background = Color(0xFFF8FAFC)        // slate-50 (app background)
    val OnBackground = Color(0xFF0F172A)      // slate-900

    val Surface = Color(0xFFFFFFFF)           // White
    val SurfaceVariant = Color(0xFFF1F5F9)    // slate-100
    val OnSurface = Color(0xFF0F172A)         // slate-900
    val OnSurfaceVariant = Color(0xFF64748B)  // slate-500

    // ============================================
    // Neutral Colors — slate scale
    // ============================================

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    val Gray50 = Color(0xFFF8FAFC)
    val Gray100 = Color(0xFFF1F5F9)
    val Gray200 = Color(0xFFE2E8F0)
    val Gray300 = Color(0xFFCBD5E1)
    val Gray400 = Color(0xFF94A3B8)
    val Gray500 = Color(0xFF64748B)
    val Gray600 = Color(0xFF475569)
    val Gray700 = Color(0xFF334155)
    val Gray800 = Color(0xFF1E293B)
    val Gray900 = Color(0xFF0F172A)

    // ============================================
    // Gradients — mirror the web brand gradients
    // ============================================

    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Primary, Secondary)            // indigo → iris (web primary gradient)
    )

    val SecondaryGradient = Brush.linearGradient(
        colors = listOf(Secondary, Color(0xFFEC4899))  // iris → fuchsia
    )

    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Success, SuccessDark)
    )

    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF))  // slate-50 → indigo-50
    )

    val KioskBackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFEEF2FF), White)
    )

    // ============================================
    // Disabled States
    // ============================================

    val DisabledGradient = Brush.linearGradient(
        colors = listOf(Gray400, Gray500)
    )
}
