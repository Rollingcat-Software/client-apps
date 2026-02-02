package com.fivucsas.shared.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.molecules.ClickableCard
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppTypography

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickActionGrid(
    actions: List<QuickActionItem>,
    modifier: Modifier = Modifier
) {
    val rows = actions.chunked(2)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                rowItems.forEach { action ->
                    ClickableCard(
                        onClick = action.onClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UIDimens.SpacingMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(UIDimens.IconMedium)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = action.title,
                                style = AppTypography.TitleSmall,
                                color = AppColors.OnSurface
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
