package com.fivucsas.shared.ui.components.organisms

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppTypography

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemSelected: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = AppColors.Surface,
        contentColor = AppColors.Primary
    ) {
        items.forEach { item ->
            val selected = item.route == currentRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        style = AppTypography.LabelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Primary,
                    selectedTextColor = AppColors.Primary,
                    unselectedIconColor = AppColors.OnSurfaceVariant,
                    unselectedTextColor = AppColors.OnSurfaceVariant,
                    indicatorColor = AppColors.Primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
