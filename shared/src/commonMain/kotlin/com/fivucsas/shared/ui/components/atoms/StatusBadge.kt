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

enum class StatusBadgeType(
    val backgroundColor: Color,
    val textColor: Color
) {
    Success(AppColors.Success.copy(alpha = 0.15f), AppColors.Success),
    Failure(AppColors.Error.copy(alpha = 0.15f), AppColors.Error),
    Warning(AppColors.Warning.copy(alpha = 0.15f), AppColors.WarningDark),
    Info(AppColors.Info.copy(alpha = 0.15f), AppColors.InfoDark),
    Neutral(AppColors.Gray200, AppColors.Gray700)
}

@Composable
fun StatusBadge(
    text: String,
    type: StatusBadgeType,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Chip,
        color = type.backgroundColor,
        contentColor = type.textColor
    ) {
        Text(
            text = text,
            color = type.textColor,
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
