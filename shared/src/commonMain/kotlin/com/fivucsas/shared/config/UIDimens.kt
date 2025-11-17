package com.fivucsas.shared.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * UI Dimension Constants
 *
 * Centralizes all UI spacing, sizing, and dimension values
 * to ensure consistency across the application.
 *
 * Follows material design spacing guidelines where applicable.
 */
object UIDimens {

    // ============================================
    // Spacing Constants
    // ============================================

    val SpacingXSmall: Dp = 4.dp
    val SpacingSmall: Dp = 8.dp
    val SpacingMedium: Dp = 16.dp
    val SpacingLarge: Dp = 24.dp
    val SpacingXLarge: Dp = 32.dp
    val SpacingXXLarge: Dp = 64.dp

    // ============================================
    // Icon Sizes
    // ============================================

    val IconXSmall: Dp = 16.dp
    val IconSmall: Dp = 24.dp
    val IconMedium: Dp = 32.dp
    val IconLarge: Dp = 48.dp
    val IconXLarge: Dp = 64.dp
    val IconXXLarge: Dp = 120.dp

    // ============================================
    // Button Sizes
    // ============================================

    val ButtonHeight: Dp = 48.dp
    val ButtonHeightSmall: Dp = 36.dp
    val ButtonHeightLarge: Dp = 56.dp
    val ButtonHeightKiosk: Dp = 80.dp
    val ButtonWidthKiosk: Dp = 250.dp
    val ButtonMinWidth: Dp = 64.dp

    // ============================================
    // Component Sizes
    // ============================================

    val CardRadius: Dp = 12.dp
    val CardElevation: Dp = 4.dp
    val CardPadding: Dp = 16.dp

    val InputFieldHeight: Dp = 56.dp
    val InputFieldMinHeight: Dp = 48.dp

    val DialogWidth: Dp = 400.dp
    val DialogMaxWidth: Dp = 600.dp
    val DialogPadding: Dp = 24.dp

    val DividerThickness: Dp = 1.dp

    // ============================================
    // Kiosk Mode Specific
    // ============================================

    val KioskIconSize: Dp = 120.dp
    val CameraPreviewHeight: Dp = 400.dp
    val CameraPreviewWidth: Dp = 600.dp
    val KioskPadding: Dp = 64.dp

    // ============================================
    // Table/List Dimensions
    // ============================================

    val TableRowHeight: Dp = 56.dp
    val TableHeaderHeight: Dp = 64.dp
    val TableCellPadding: Dp = 16.dp
    val ListItemHeight: Dp = 56.dp
    val ListItemPadding: Dp = 16.dp

    // ============================================
    // Admin Dashboard Specific
    // ============================================

    val StatisticCardHeight: Dp = 120.dp
    val NavigationRailWidth: Dp = 80.dp
    val TopBarHeight: Dp = 64.dp
    val SidebarWidth: Dp = 256.dp

    // ============================================
    // Border and Corner Radius
    // ============================================

    val BorderWidthThin: Dp = 1.dp
    val BorderWidthMedium: Dp = 2.dp
    val BorderWidthThick: Dp = 4.dp

    val CornerRadiusSmall: Dp = 4.dp
    val CornerRadiusMedium: Dp = 8.dp
    val CornerRadiusLarge: Dp = 12.dp
    val CornerRadiusXLarge: Dp = 16.dp
    val CornerRadiusFull: Dp = 999.dp  // For circular shapes

    // ============================================
    // Elevation/Shadow
    // ============================================

    val ElevationNone: Dp = 0.dp
    val ElevationLow: Dp = 2.dp
    val ElevationMedium: Dp = 4.dp
    val ElevationHigh: Dp = 8.dp
    val ElevationVeryHigh: Dp = 16.dp
}
