package com.fivucsas.shared.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.BodyMediumText
import com.fivucsas.shared.ui.components.atoms.HeadlineMediumText
import com.fivucsas.shared.ui.components.atoms.PrimaryButton
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerMedium
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerSmall
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Empty State Component
 *
 * Displays a message when there's no content to show.
 *
 * @param title Empty state title
 * @param message Empty state message
 * @param modifier Optional modifier
 * @param icon Optional icon to display
 * @param actionText Optional action button text
 * @param onActionClick Optional action button click handler
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(UIDimens.SpacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.OnSurfaceVariant,
                    modifier = Modifier.size(UIDimens.IconXXLarge)
                )
                VerticalSpacerMedium()
            }

            // Title
            HeadlineMediumText(
                text = title,
                color = AppColors.OnSurface
            )

            VerticalSpacerSmall()

            // Message
            BodyMediumText(
                text = message,
                color = AppColors.OnSurfaceVariant
            )

            // Action button
            if (actionText != null && onActionClick != null) {
                VerticalSpacerMedium()
                PrimaryButton(
                    onClick = onActionClick,
                    text = actionText
                )
            }
        }
    }
}
