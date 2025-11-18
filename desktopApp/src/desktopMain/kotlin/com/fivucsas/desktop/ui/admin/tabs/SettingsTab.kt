package com.fivucsas.desktop.ui.admin.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fivucsas.desktop.ui.admin.components.AdminConstants
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.ui.components.organisms.EmptyState

/**
 * Settings Tab Component
 *
 * Displays system settings and configuration options.
 *
 * @param viewModel Admin view model
 */
@Composable
fun SettingsTab(
    viewModel: AdminViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
    ) {
        // Header
        Text(
            AdminConstants.SETTINGS_TITLE,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            "Configure system settings and preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

        // Placeholder - can be enhanced later
        EmptyState(
            title = "Settings Coming Soon",
            message = "System configuration, user preferences, and application settings will be available here"
        )
    }
}
