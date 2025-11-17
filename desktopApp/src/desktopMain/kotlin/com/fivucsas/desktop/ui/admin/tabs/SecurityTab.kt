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
 * Security Tab Component
 *
 * Displays security settings, audit logs, and system monitoring.
 *
 * @param viewModel Admin view model
 */
@Composable
fun SecurityTab(
    viewModel: AdminViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
    ) {
        // Header
        Text(
            AdminConstants.SECURITY_TITLE,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            "Monitor system security and audit logs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

        // Placeholder - can be enhanced later
        EmptyState(
            title = "Security Features Coming Soon",
            message = "Audit logs, security alerts, and system monitoring will be available here"
        )
    }
}
