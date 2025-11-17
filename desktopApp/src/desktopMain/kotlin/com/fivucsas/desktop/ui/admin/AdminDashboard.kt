package com.fivucsas.desktop.ui.admin

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fivucsas.desktop.ui.admin.components.AdminConstants
import com.fivucsas.desktop.ui.admin.components.AdminNavigationRail
import com.fivucsas.desktop.ui.admin.tabs.AnalyticsTab
import com.fivucsas.desktop.ui.admin.tabs.SecurityTab
import com.fivucsas.desktop.ui.admin.tabs.SettingsTab
import com.fivucsas.desktop.ui.admin.tabs.UsersTab
import com.fivucsas.shared.presentation.state.AdminTab
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import org.koin.compose.koinInject

/**
 * Admin Dashboard - Refactored
 *
 * Clean, maintainable dashboard using extracted components.
 *
 * Benefits:
 * - Modular architecture with separated concerns
 * - Each tab in its own file for better maintainability
 * - Reusable components following Atomic Design
 * - Easy to test individual components
 * - Follows Single Responsibility Principle
 *
 * @param onBack Callback when back button is clicked
 * @param viewModel Admin view model (injected via Koin)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onBack: () -> Unit,
    viewModel: AdminViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab = uiState.selectedTab

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AdminConstants.TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.selectTab(AdminTab.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Navigation Rail
            AdminNavigationRail(
                selectedTab = selectedTab,
                onTabSelected = viewModel::selectTab
            )

            // Main Content
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AdminContent(
                    selectedTab = selectedTab,
                    viewModel = viewModel,
                    errorMessage = uiState.errorMessage,
                    successMessage = uiState.successMessage
                )
            }
        }
    }
}

/**
 * Admin Content Router
 *
 * Routes to the appropriate tab based on selection.
 * Follows Open/Closed Principle - add new tabs without modifying existing code.
 *
 * @param selectedTab Currently selected tab
 * @param viewModel Admin view model
 * @param errorMessage Optional error message to display
 * @param successMessage Optional success message to display
 */
@Composable
private fun AdminContent(
    selectedTab: AdminTab,
    viewModel: AdminViewModel,
    errorMessage: String?,
    successMessage: String?
) {
    // Display messages at the top
    if (errorMessage != null || successMessage != null) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize()
        ) {
            errorMessage?.let {
                ErrorMessage(
                    message = it,
                    modifier = Modifier.padding(com.fivucsas.shared.config.UIDimens.SpacingMedium)
                )
            }

            successMessage?.let {
                SuccessMessage(
                    message = it,
                    modifier = Modifier.padding(com.fivucsas.shared.config.UIDimens.SpacingMedium)
                )
            }

            // Tab content
            TabContent(selectedTab = selectedTab, viewModel = viewModel)
        }
    } else {
        TabContent(selectedTab = selectedTab, viewModel = viewModel)
    }
}

/**
 * Tab Content Component
 *
 * Displays the selected tab content.
 */
@Composable
private fun TabContent(
    selectedTab: AdminTab,
    viewModel: AdminViewModel
) {
    when (selectedTab) {
        AdminTab.USERS -> UsersTab(viewModel = viewModel)
        AdminTab.ANALYTICS -> AnalyticsTab(viewModel = viewModel)
        AdminTab.SECURITY -> SecurityTab(viewModel = viewModel)
        AdminTab.SETTINGS -> SettingsTab(viewModel = viewModel)
    }
}
