package com.fivucsas.shared.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Application Shape System
 *
 * Defines all shapes used throughout the application.
 * Based on Material Design 3 shape scale.
 */
object AppShapes {

    // ============================================
    // Corner Radius Shapes
    // ============================================

    val None: Shape = RoundedCornerShape(0.dp)
    val ExtraSmall: Shape = RoundedCornerShape(4.dp)
    val Small: Shape = RoundedCornerShape(8.dp)
    val Medium: Shape = RoundedCornerShape(12.dp)
    val Large: Shape = RoundedCornerShape(16.dp)
    val ExtraLarge: Shape = RoundedCornerShape(24.dp)
    val Round: Shape = RoundedCornerShape(28.dp)
    val Circle: Shape = RoundedCornerShape(9999.dp)

    // ============================================
    // Component-Specific Shapes
    // ============================================

    val Button: Shape = Medium
    val Card: Shape = Medium
    val Dialog: Shape = Large
    val TextField: Shape = Small
    val Chip: Shape = Circle
}
