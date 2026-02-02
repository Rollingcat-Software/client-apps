package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.BodyMediumText
import com.fivucsas.shared.ui.components.atoms.BodySmallText
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.atoms.TitleMediumText
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes

data class ActivityItemData(
    val title: String,
    val description: String,
    val timestamp: String,
    val score: String? = null,
    val status: StatusBadgeType = StatusBadgeType.Info,
    val icon: ImageVector? = null,
    val iconTint: Color = AppColors.Primary
)

@Composable
fun ActivityItem(
    data: ActivityItemData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = UIDimens.ElevationLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            if (data.icon != null) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.iconTint,
                    modifier = Modifier.size(UIDimens.IconMedium)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                TitleMediumText(text = data.title)
                BodySmallText(text = data.description, color = AppColors.OnSurfaceVariant)
                Spacer(modifier = Modifier.size(4.dp))
                BodySmallText(text = data.timestamp, color = AppColors.OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    text = when (data.status) {
                        StatusBadgeType.Success -> "Success"
                        StatusBadgeType.Failure -> "Failed"
                        StatusBadgeType.Warning -> "Warning"
                        StatusBadgeType.Info -> "Info"
                        StatusBadgeType.Neutral -> "Info"
                    },
                    type = data.status
                )
                if (data.score != null) {
                    Spacer(modifier = Modifier.size(6.dp))
                    BodyMediumText(
                        text = data.score,
                        color = AppColors.OnSurface
                    )
                }
            }
        }
    }
}
