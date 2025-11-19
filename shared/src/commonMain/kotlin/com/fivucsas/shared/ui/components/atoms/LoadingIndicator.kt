package com.fivucsas.shared.ui.components.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Loading Indicator Component
 *
 * A circular progress indicator with consistent theming.
 *
 * @param modifier Optional modifier
 * @param size Size of the indicator
 * @param color Color of the indicator
 */
@Composable
fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = UIDimens.IconMedium,
    color: androidx.compose.ui.graphics.Color = AppColors.Primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = UIDimens.BorderWidth
    )
}

/**
 * Loading Box Component
 *
 * A centered loading indicator within a Box, useful for full-screen loading states.
 *
 * @param modifier Optional modifier
 * @param message Optional loading message
 */
@Composable
fun LoadingBox(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLoadingIndicator()

            if (message != null) {
                VerticalSpacerMedium()
                BodyMediumText(
                    text = message,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
    }
}
