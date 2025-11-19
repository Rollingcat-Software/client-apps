package com.fivucsas.desktop.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}

data class ResponsiveSizes(
    val windowSize: WindowSize,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cardWidth: Float,
    val iconSize: Dp,
    val buttonHeight: Dp,
    val spacing: Dp
)

fun getResponsiveSizes(width: Dp): ResponsiveSizes {
    return when {
        width < 600.dp -> ResponsiveSizes(
            windowSize = WindowSize.COMPACT,
            horizontalPadding = 8.dp,
            verticalPadding = 8.dp,
            cardWidth = 1f,
            iconSize = 40.dp,
            buttonHeight = 48.dp,
            spacing = 8.dp
        )

        width < 840.dp -> ResponsiveSizes(
            windowSize = WindowSize.MEDIUM,
            horizontalPadding = 16.dp,
            verticalPadding = 16.dp,
            cardWidth = 0.9f,
            iconSize = 56.dp,
            buttonHeight = 56.dp,
            spacing = 16.dp
        )

        else -> ResponsiveSizes(
            windowSize = WindowSize.EXPANDED,
            horizontalPadding = 32.dp,
            verticalPadding = 24.dp,
            cardWidth = 0.75f,
            iconSize = 64.dp,
            buttonHeight = 64.dp,
            spacing = 24.dp
        )
    }
}
