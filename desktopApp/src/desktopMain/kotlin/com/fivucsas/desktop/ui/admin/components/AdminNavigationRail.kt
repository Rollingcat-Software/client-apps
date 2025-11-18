package com.fivucsas.desktop.ui.admin.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.state.AdminTab

/**
 * Admin Navigation Rail Component
 *
 * Vertical navigation for admin dashboard tabs.
 *
 * @param selectedTab Currently selected tab
 * @param onTabSelected Callback when tab is selected
 */
@Composable
fun AdminNavigationRail(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        NavigationRailItem(
            icon = { Icon(Icons.Default.People, contentDescription = "Users") },
            label = { Text("Users") },
            selected = selectedTab == AdminTab.USERS,
            onClick = { onTabSelected(AdminTab.USERS) }
        )

        NavigationRailItem(
            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
            label = { Text("Analytics") },
            selected = selectedTab == AdminTab.ANALYTICS,
            onClick = { onTabSelected(AdminTab.ANALYTICS) }
        )

        NavigationRailItem(
            icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
            label = { Text("Security") },
            selected = selectedTab == AdminTab.SECURITY,
            onClick = { onTabSelected(AdminTab.SECURITY) }
        )

        NavigationRailItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedTab == AdminTab.SETTINGS,
            onClick = { onTabSelected(AdminTab.SETTINGS) }
        )
    }
}
