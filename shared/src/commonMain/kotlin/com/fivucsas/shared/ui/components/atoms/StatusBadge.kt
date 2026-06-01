package com.fivucsas.shared.ui.components.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes

enum class StatusBadgeType { Success, Failure, Warning, Info, Neutral }

/** Background + text color for a badge type, resolved against the active (light/dark) theme. */
@Composable
private fun StatusBadgeType.colors(): Pair<Color, Color> = when (this) {
    StatusBadgeType.Success -> AppColors.Success.copy(alpha = 0.15f) to AppColors.Success
    StatusBadgeType.Failure -> AppColors.Error.copy(alpha = 0.15f) to AppColors.Error
    StatusBadgeType.Warning -> AppColors.Warning.copy(alpha = 0.15f) to AppColors.WarningDark
    StatusBadgeType.Info -> AppColors.Info.copy(alpha = 0.15f) to AppColors.InfoDark
    StatusBadgeType.Neutral -> AppColors.Gray200 to AppColors.Gray700
}

@Composable
fun StatusBadge(
    text: String,
    type: StatusBadgeType,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
) {
    val (badgeBackground, badgeText) = type.colors()
    Surface(
        modifier = modifier,
        shape = AppShapes.Chip,
        color = badgeBackground,
        contentColor = badgeText
    ) {
        Text(
            text = text,
            color = badgeText,
            style = com.fivucsas.shared.ui.theme.AppTypography.LabelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(contentPadding)
        )
    }
}

/**
 * Convenience overload for simple positive/negative status badges
 */
@Composable
fun StatusBadge(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    StatusBadge(
        text = text,
        type = if (isPositive) StatusBadgeType.Success else StatusBadgeType.Neutral,
        modifier = modifier
    )
}
