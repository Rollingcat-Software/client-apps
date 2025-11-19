package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.BodyMediumText
import com.fivucsas.shared.ui.components.atoms.BodySmallText
import com.fivucsas.shared.ui.components.atoms.HeadlineMediumText
import com.fivucsas.shared.ui.components.atoms.TitleMediumText
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerSmall
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerXSmall
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes

/**
 * App Card Component
 *
 * A reusable card component with consistent elevation and styling.
 *
 * @param modifier Optional modifier
 * @param content Card content
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UIDimens.CardElevation
        )
    ) {
        content()
    }
}

/**
 * Info Card Component
 *
 * A card displaying an icon, title, and description.
 *
 * @param title Card title
 * @param description Card description
 * @param icon Optional icon
 * @param iconTint Icon tint color
 * @param modifier Optional modifier
 */
@Composable
fun InfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = AppColors.Primary
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(UIDimens.IconLarge)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = UIDimens.SpacingMedium))
            }

            Column(modifier = Modifier.weight(1f)) {
                TitleMediumText(text = title)
                VerticalSpacerXSmall()
                BodySmallText(
                    text = description,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
    }
}

/**
 * Stat Card Component
 *
 * A card for displaying a statistic with a label.
 *
 * @param value Statistic value
 * @param label Statistic label
 * @param modifier Optional modifier
 * @param icon Optional icon
 * @param iconTint Icon tint color
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = AppColors.Primary
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(UIDimens.IconLarge)
                )
                VerticalSpacerSmall()
            }

            HeadlineMediumText(
                text = value,
                color = AppColors.OnSurface
            )

            VerticalSpacerXSmall()

            BodyMediumText(
                text = label,
                color = AppColors.OnSurfaceVariant
            )
        }
    }
}

/**
 * Clickable Card Component
 *
 * A card that can be clicked.
 *
 * @param onClick Callback when card is clicked
 * @param modifier Optional modifier
 * @param content Card content
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UIDimens.CardElevation
        )
    ) {
        content()
    }
}
