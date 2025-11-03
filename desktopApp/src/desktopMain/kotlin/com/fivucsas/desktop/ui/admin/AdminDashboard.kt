package com.fivucsas.desktop.ui.admin

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.state.AdminTab
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject

/**
 * Admin Dashboard
 *
 * Management interface for:
 * - User management (view, add, edit, delete)
 * - System configuration
 * - Reports and analytics
 * - Audit logs
 *
 * ARCHITECTURE:
 * - Follows MVVM pattern
 * - Uses ViewModel for state management
 * - Implements SOLID principles
 * - Repository pattern ready
 */

// Constants
private object AdminConfig {
    const val TITLE = "FIVUCSAS Admin Dashboard"
    const val USER_MANAGEMENT_TITLE = "User Management"
    const val USER_MANAGEMENT_SUBTITLE = "Manage registered users and their biometric data"
    const val ANALYTICS_TITLE = "Analytics & Reports"
    const val SECURITY_TITLE = "Security & Audit Logs"
    const val SETTINGS_TITLE = "System Settings"
    const val ADD_USER = "Add User"
    const val SEARCH_PLACEHOLDER = "Search users..."
    const val FILTERS = "Filters"
    const val EXPORT = "Export"
}

private object AdminDimens {
    val IconSize = 64.dp
    val IconMedium = 48.dp
    val IconSmall = 20.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingXLarge = 32.dp
    val CardHeight = 400.dp
    val StatCardHeight = 100.dp
}

// AdminViewModel now imported from shared module
// ✅ Removed local definition - using com.fivucsas.shared.presentation.viewmodel.AdminViewModel

// User, UserStatus, and Statistics now imported from shared module
// ✅ Removed local definitions - using com.fivucsas.shared.domain.model.*

/**
 * Main Admin Dashboard composable - Pure presentation
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
                title = { Text(AdminConfig.TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            AdminNavigationRail(
                selectedTab = selectedTab,
                onTabSelected = viewModel::selectTab
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AdminContent(
                    selectedTab = selectedTab,
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Admin content router - Follows Open/Closed Principle
 */
@Composable
private fun AdminContent(
    selectedTab: AdminTab,
    viewModel: AdminViewModel
) {
    when (selectedTab) {
        AdminTab.USERS -> UsersTab(viewModel = viewModel)
        AdminTab.ANALYTICS -> AnalyticsTab(viewModel = viewModel)
        AdminTab.SECURITY -> SecurityTab()
        AdminTab.SETTINGS -> SettingsTab()
    }
}

/**
 * Navigation rail component - Extracted for reusability
 */
@Composable
private fun AdminNavigationRail(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.height(AdminDimens.SpacingMedium))

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

/**
 * Users Tab - User management interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersTab(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
    ) {
        UsersHeader(
            onAddUser = { /* TODO: Implement add user dialog */ }
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        UsersSearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onFilter = { /* TODO: Implement filter */ },
            onExport = { /* TODO: Implement export */ }
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        UsersTable(
            users = uiState.filteredUsers,
            onEdit = { /* TODO: Implement edit */ },
            onDelete = viewModel::deleteUser
        )
    }
}

/**
 * Users header component
 */
@Composable
private fun UsersHeader(
    onAddUser: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                AdminConfig.USER_MANAGEMENT_TITLE,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                AdminConfig.USER_MANAGEMENT_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(onClick = onAddUser) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(AdminDimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(AdminDimens.SpacingSmall))
            Text(AdminConfig.ADD_USER)
        }
    }
}

/**
 * Search bar component
 */
@Composable
private fun UsersSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilter: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(AdminConfig.SEARCH_PLACEHOLDER) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        OutlinedButton(onClick = onFilter) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(AdminDimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(AdminDimens.SpacingSmall))
            Text(AdminConfig.FILTERS)
        }

        OutlinedButton(onClick = onExport) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(AdminDimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(AdminDimens.SpacingSmall))
            Text(AdminConfig.EXPORT)
        }
    }
}

/**
 * Users table component - With proper keys for performance
 */
@Composable
private fun UsersTable(
    users: List<User>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                UsersTableHeader()
            }

            items(
                items = users,
                key = { user -> user.id }
            ) { user ->
                Divider()
                UserRow(
                    user = user,
                    onEdit = { onEdit(user.id) },
                    onDelete = { onDelete(user.id) }
                )
            }
        }
    }
}

/**
 * Table header component
 */
@Composable
private fun UsersTableHeader() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AdminDimens.SpacingMedium),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
        ) {
            Text(
                "Name",
                modifier = Modifier.weight(0.25f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "Email",
                modifier = Modifier.weight(0.3f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "ID Number",
                modifier = Modifier.weight(0.2f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "Status",
                modifier = Modifier.weight(0.15f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "Actions",
                modifier = Modifier.weight(0.1f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * User row component
 */
@Composable
private fun UserRow(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AdminDimens.SpacingMedium),
        horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.name, modifier = Modifier.weight(0.25f))
        Text(
            user.email,
            modifier = Modifier.weight(0.3f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            user.idNumber,
            modifier = Modifier.weight(0.2f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.weight(0.15f)) {
            AssistChip(
                onClick = {},
                label = { Text(user.status.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (user.status == UserStatus.ACTIVE)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            )
        }

        Row(
            modifier = Modifier.weight(0.1f),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingSmall)
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Analytics Tab - Statistics and reports
 */
@Composable
fun AnalyticsTab(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val statistics = uiState.statistics

    Column(
        modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
    ) {
        Text(
            AdminConfig.ANALYTICS_TITLE,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingXLarge))

        StatisticsCards(statistics = statistics)

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        ChartsPlaceholder()
    }
}

/**
 * Statistics cards component - With remember for optimization
 */
@Composable
private fun StatisticsCards(
    statistics: Statistics
) {
    val statCards = remember(statistics) {
        listOf(
            StatCardData(
                title = "Total Users",
                value = statistics.totalUsers.toString(),
                icon = Icons.Default.People
            ),
            StatCardData(
                title = "Verifications Today",
                value = statistics.verificationsToday.toString(),
                icon = Icons.Default.VerifiedUser
            ),
            StatCardData(
                title = "Success Rate",
                value = "${statistics.successRate}%",
                icon = Icons.Default.TrendingUp
            ),
            StatCardData(
                title = "Failed Attempts",
                value = statistics.failedAttempts.toString(),
                icon = Icons.Default.Warning
            )
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
    ) {
        statCards.forEach { data ->
            StatCard(
                data = data,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Stat card data model
 */
data class StatCardData(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Stat card component - Reusable
 */
@Composable
fun StatCard(
    data: StatCardData,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(AdminDimens.SpacingMedium),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = data.title,
                modifier = Modifier.size(AdminDimens.IconMedium),
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Charts placeholder component
 */
@Composable
private fun ChartsPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth().height(AdminDimens.CardHeight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = "Charts",
                modifier = Modifier.size(AdminDimens.IconSize),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AdminDimens.SpacingMedium))
            Text(
                "Charts and Analytics (To be implemented)",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(AdminDimens.SpacingSmall))
            Text(
                "Usage statistics, verification trends, and system performance metrics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Security Tab - Audit logs and security
 */
@Composable
fun SecurityTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
    ) {
        Text(
            AdminConfig.SECURITY_TITLE,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        PlaceholderCard(
            icon = Icons.Default.Security,
            title = "Security Logs (To be implemented)",
            description = "Access logs, failed attempts, suspicious activities, and KVKK/GDPR compliance reports"
        )
    }
}

/**
 * Settings Tab - System configuration
 */
@Composable
fun SettingsTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
    ) {
        Text(
            AdminConfig.SETTINGS_TITLE,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        PlaceholderCard(
            icon = Icons.Default.Settings,
            title = "Configuration (To be implemented)",
            description = "Biometric thresholds, liveness detection settings, API configurations, and system parameters"
        )
    }
}

/**
 * Placeholder card component - Reusable
 */
@Composable
private fun PlaceholderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(AdminDimens.IconSize),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AdminDimens.SpacingMedium))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(AdminDimens.SpacingSmall))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// AdminTab now imported from shared module
// ✅ Removed local definition - using com.fivucsas.shared.presentation.state.AdminTab

/**
 * Sample data for demonstration
 */
private val sampleUsers = listOf(
    User(
        id = "1",
        name = "Ahmet Abdullah Gültekin",
        email = "ahmet@example.com",
        idNumber = "150121025",
        status = UserStatus.ACTIVE
    ),
    User(
        id = "2",
        name = "Ayşe Gülsüm Eren",
        email = "ayse@example.com",
        idNumber = "150120005",
        status = UserStatus.ACTIVE
    ),
    User(
        id = "3",
        name = "Ayşenur Arıcı",
        email = "aysenur@example.com",
        idNumber = "150123825",
        status = UserStatus.ACTIVE
    ),
    User(
        id = "4",
        name = "Test User 1",
        email = "test1@example.com",
        idNumber = "123456789",
        status = UserStatus.INACTIVE
    ),
    User(
        id = "5",
        name = "Test User 2",
        email = "test2@example.com",
        idNumber = "987654321",
        status = UserStatus.ACTIVE
    )
)
