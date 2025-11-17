package com.fivucsas.shared.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Application Color Palette
 *
 * Defines all colors used throughout the application.
 * Based on Material Design 3 color system.
 */
object AppColors {
    // ============================================
    // Primary Colors
    // ============================================

    val Primary = Color(0xFF1976D2)           // Deep Blue
    val PrimaryVariant = Color(0xFF1565C0)    // Darker Blue
    val OnPrimary = Color(0xFFFFFFFF)         // White on primary

    // ============================================
    // Secondary Colors
    // ============================================

    val Secondary = Color(0xFF00ACC1)         // Cyan
    val SecondaryVariant = Color(0xFF0097A7)  // Darker Cyan
    val OnSecondary = Color(0xFFFFFFFF)       // White on secondary

    // ============================================
    // Semantic Colors
    // ============================================

    val Success = Color(0xFF4CAF50)           // Green
    val SuccessDark = Color(0xFF388E3C)       // Darker Green
    val OnSuccess = Color(0xFFFFFFFF)         // White on success

    val Warning = Color(0xFFFFA726)           // Orange
    val WarningDark = Color(0xFFF57C00)       // Darker Orange
    val OnWarning = Color(0xFF000000)         // Black on warning

    val Error = Color(0xFFF44336)             // Red
    val ErrorDark = Color(0xFFD32F2F)         // Darker Red
    val OnError = Color(0xFFFFFFFF)           // White on error

    val Info = Color(0xFF2196F3)              // Blue
    val InfoDark = Color(0xFF1976D2)          // Darker Blue
    val OnInfo = Color(0xFFFFFFFF)            // White on info

    // ============================================
    // Surface Colors
    // ============================================

    val Background = Color(0xFFF5F5F5)        // Light gray
    val OnBackground = Color(0xFF212121)      // Dark gray

    val Surface = Color(0xFFFFFFFF)           // White
    val SurfaceVariant = Color(0xFFFAFAFA)    // Off-white
    val OnSurface = Color(0xFF212121)         // Dark gray
    val OnSurfaceVariant = Color(0xFF757575)  // Medium gray

    // ============================================
    // Neutral Colors
    // ============================================

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)

    // ============================================
    // Gradients
    // ============================================

    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Primary, PrimaryVariant)
    )

    val SecondaryGradient = Brush.linearGradient(
        colors = listOf(Secondary, SecondaryVariant)
    )

    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Success, SuccessDark)
    )

    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), Color(0xFFFAFAFA))
    )

    val KioskBackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), White)
    )

    // ============================================
    // Disabled States
    // ============================================

    val DisabledGradient = Brush.linearGradient(
        colors = listOf(Gray400, Gray500)
    )
}
