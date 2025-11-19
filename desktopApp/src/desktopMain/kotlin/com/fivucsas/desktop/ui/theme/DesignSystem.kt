package com.fivucsas.desktop.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design System - Central source of truth for all design tokens
 */

// Brand Colors
object AppColors {
    val Primary = Color(0xFF1976D2)           // Deep Blue
    val PrimaryVariant = Color(0xFF1565C0)    // Darker Blue
    val Secondary = Color(0xFF00ACC1)         // Cyan
    val SecondaryVariant = Color(0xFF0097A7)  // Darker Cyan

    val Success = Color(0xFF4CAF50)           // Green
    val Warning = Color(0xFFFFA726)           // Orange
    val Error = Color(0xFFF44336)             // Red
    val Info = Color(0xFF2196F3)              // Blue

    val Background = Color(0xFFF5F5F5)        // Light gray
    val Surface = Color(0xFFFFFFFF)           // White
    val SurfaceVariant = Color(0xFFFAFAFA)    // Off-white

    val OnPrimary = Color(0xFFFFFFFF)         // White on primary
    val OnSurface = Color(0xFF212121)         // Dark gray
    val OnSurfaceVariant = Color(0xFF757575)  // Medium gray
    val OnBackground = Color(0xFF212121)      // Dark gray

    // Gradients
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Primary, PrimaryVariant)
    )

    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Success, Color(0xFF388E3C))
    )

    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), Color(0xFFFAFAFA))
    )
}

// Spacing System
object Spacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
    val xxxl: Dp = 64.dp
}

// Corner Radius
object CornerRadius {
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val xlarge: Dp = 24.dp
    val round: Dp = 28.dp
    val circle: Dp = 9999.dp
}

// Elevation
object Elevation {
    val none: Dp = 0.dp
    val small: Dp = 2.dp
    val medium: Dp = 4.dp
    val large: Dp = 8.dp
    val xlarge: Dp = 16.dp
}

// Icon Sizes
object IconSize {
    val small: Dp = 16.dp
    val medium: Dp = 24.dp
    val large: Dp = 32.dp
    val xlarge: Dp = 48.dp
    val xxlarge: Dp = 64.dp
    val huge: Dp = 120.dp
}

// Button Sizes
object ButtonSize {
    val small: Dp = 40.dp
    val medium: Dp = 48.dp
    val large: Dp = 56.dp
    val xlarge: Dp = 64.dp
}

// Animation Durations (milliseconds)
object AnimationDuration {
    const val fast = 150
    const val normal = 300
    const val slow = 500
}
