package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes
import com.fivucsas.shared.ui.theme.AppTypography

data class NotificationItemData(
    val title: String,
    val message: String,
    val time: String,
    val isUnread: Boolean = false,
    val status: StatusBadgeType = StatusBadgeType.Info,
    val icon: ImageVector? = null,
    val iconTint: Color = AppColors.Info
)

@Composable
fun NotificationItem(
    data: NotificationItemData,
    modifier: Modifier = Modifier
) {
    val containerColor = if (data.isUnread) {
        AppColors.Info.copy(alpha = 0.05f)
    } else {
        AppColors.Surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                Text(
                    text = data.title,
                    style = AppTypography.TitleMedium,
                    color = AppColors.OnSurface
                )
                Text(
                    text = data.message,
                    style = AppTypography.BodySmall,
                    color = AppColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = data.time,
                    style = AppTypography.LabelSmall,
                    color = AppColors.OnSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(text = statusLabel(data.status), type = data.status)
                if (data.isUnread) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.Primary, AppShapes.Circle)
                    )
                }
            }
        }
    }
}

private fun statusLabel(type: StatusBadgeType): String {
    return when (type) {
        StatusBadgeType.Success -> "Success"
        StatusBadgeType.Failure -> "Failed"
        StatusBadgeType.Warning -> "Warning"
        StatusBadgeType.Info -> "Info"
        StatusBadgeType.Neutral -> "Info"
    }
}
