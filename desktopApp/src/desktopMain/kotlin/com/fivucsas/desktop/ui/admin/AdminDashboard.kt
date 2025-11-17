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
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape

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
    var selectedUser by remember { mutableStateOf<User?>(null) }

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

        // 📊 Statistics Cards
        UserStatisticsCards(
            totalUsers = uiState.users.size,
            activeUsers = uiState.users.count { it.status == UserStatus.ACTIVE },
            inactiveUsers = uiState.users.count { it.status == UserStatus.INACTIVE },
            pendingUsers = uiState.users.count { it.status == UserStatus.PENDING }
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        UsersTable(
            users = uiState.filteredUsers,
            onEdit = { /* TODO: Implement edit */ },
            onDelete = viewModel::deleteUser,
            onUserClick = { selectedUser = it }
        )
    }

    // User Detail Modal
    selectedUser?.let { user ->
        UserDetailModal(
            user = user,
            onDismiss = { selectedUser = null },
            onEdit = { /* TODO: Implement edit */ },
            onDelete = {
                viewModel.deleteUser(user.id)
                selectedUser = null
            }
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
 * User Statistics Cards - Beautiful gradient cards showing user stats
 */
@Composable
private fun UserStatisticsCards(
    totalUsers: Int,
    activeUsers: Int,
    inactiveUsers: Int,
    pendingUsers: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
    ) {
        // Total Users Card - Blue gradient
        StatisticCard(
            modifier = Modifier.weight(1f),
            title = "Total Users",
            value = totalUsers.toString(),
            icon = Icons.Default.People,
            gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF1976D2)
                )
            )
        )

        // Active Users Card - Green gradient
        StatisticCard(
            modifier = Modifier.weight(1f),
            title = "Active",
            value = activeUsers.toString(),
            icon = Icons.Default.CheckCircle,
            gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF388E3C)
                )
            )
        )

        // Inactive Users Card - Red gradient
        StatisticCard(
            modifier = Modifier.weight(1f),
            title = "Inactive",
            value = inactiveUsers.toString(),
            icon = Icons.Default.Block,
            gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF44336),
                    Color(0xFFD32F2F)
                )
            )
        )

        // Pending Users Card - Orange gradient
        StatisticCard(
            modifier = Modifier.weight(1f),
            title = "Pending",
            value = pendingUsers.toString(),
            icon = Icons.Default.Schedule,
            gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF9800),
                    Color(0xFFF57C00)
                )
            )
        )
    }
}

/**
 * Individual Statistic Card - Reusable gradient card component
 */
@Composable
private fun StatisticCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush
) {
    Card(
        modifier = modifier
            .height(AdminDimens.StatCardHeight)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(AdminDimens.SpacingMedium)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )

                // Value and Title
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
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
    onDelete: (String) -> Unit,
    onUserClick: (User) -> Unit
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
                    onDelete = { onDelete(user.id) },
                    onClick = { onUserClick(user) }
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
        // Header
        Text(
            AdminConfig.ANALYTICS_TITLE,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            "Real-time system metrics and verification trends",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingXLarge))

        StatisticsCards(statistics = statistics)

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        // Charts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
        ) {
            // Verification Trends Chart
            VerificationTrendsChart(modifier = Modifier.weight(1f))
            
            // Success Rate Chart
            SuccessRateChart(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(AdminDimens.SpacingMedium))

        // Recent Verifications
        RecentVerificationsCard()
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
 * Verification Trends Chart - Beautiful gradient bar chart
 */
@Composable
private fun VerificationTrendsChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(300.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Verification Trends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(AdminDimens.SpacingMedium))

            // Beautiful bar chart
            val dailyData = listOf(45, 68, 52, 78, 85, 72, 90)
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyData.forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            value.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height((value * 2).dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF1976D2)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            days[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Success Rate Chart - Beautiful circular progress
 */
@Composable
private fun SuccessRateChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(300.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Success Rate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Today's performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Large circular display
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(100.dp)
                        )
                )
                
                // Percentage text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "94.2",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        "%",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "848",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        "Success",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "52",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Text(
                        "Failed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Recent Verifications Card - Timeline view
 */
@Composable
private fun RecentVerificationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AdminDimens.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Verifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "View All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(AdminDimens.SpacingMedium))

            // Recent items
            val recentVerifications = listOf(
                Triple("Ahmet Abdullah Gültekin", "2 mins ago", true),
                Triple("Ayşe Gülsüm Eren", "5 mins ago", true),
                Triple("Unknown User", "12 mins ago", false),
                Triple("Ayşenur Arıcı", "18 mins ago", true),
                Triple("Test User 1", "25 mins ago", false)
            )

            recentVerifications.forEach { (name, time, success) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (success) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    shape = RoundedCornerShape(100.dp)
                                )
                        )
                        Column {
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        if (success) "✓ Success" else "✗ Failed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (success) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
                if (name != recentVerifications.last().first) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
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
        // Header
        Text(
            AdminConfig.SECURITY_TITLE,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            "System access logs and security monitoring",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        // Security Alerts Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
        ) {
            SecurityAlertCard(
                title = "Security Alerts",
                count = "3",
                subtitle = "Require attention",
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            SecurityAlertCard(
                title = "Failed Logins",
                count = "12",
                subtitle = "Last 24 hours",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            SecurityAlertCard(
                title = "Active Sessions",
                count = "8",
                subtitle = "Currently online",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

        // Audit Logs Timeline
        AuditLogsTimeline()
    }
}

/**
 * Security Alert Card Component
 */
@Composable
private fun SecurityAlertCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(AdminDimens.SpacingLarge)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Column {
                    Text(
                        count,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Audit Logs Timeline Component
 */
@Composable
private fun AuditLogsTimeline() {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Audit Logs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Today") }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text("Export") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AdminDimens.SpacingLarge))

            // Timeline items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                val auditLogs = listOf(
                    AuditLogItem("Admin Login", "admin@fivucsas.com", "5 mins ago", "success"),
                    AuditLogItem("User Enrolled", "Ahmet Abdullah Gültekin", "12 mins ago", "success"),
                    AuditLogItem("Failed Login Attempt", "unknown@example.com", "18 mins ago", "error"),
                    AuditLogItem("System Config Changed", "admin@fivucsas.com", "32 mins ago", "warning"),
                    AuditLogItem("User Verification", "Ayşe Gülsüm Eren", "45 mins ago", "success"),
                    AuditLogItem("Biometric Data Updated", "Test User 1", "1 hour ago", "info"),
                    AuditLogItem("Failed Verification", "Unknown User", "1 hour ago", "error"),
                    AuditLogItem("Database Backup", "System", "2 hours ago", "success"),
                    AuditLogItem("API Request Spike", "External Service", "3 hours ago", "warning"),
                    AuditLogItem("User Deleted", "Test User 2", "4 hours ago", "info")
                )

                items(auditLogs.size) { index ->
                    val log = auditLogs[index]
                    AuditLogRow(
                        log = log,
                        isLast = index == auditLogs.size - 1
                    )
                }
            }
        }
    }
}

/**
 * Audit Log Data Class
 */
data class AuditLogItem(
    val action: String,
    val user: String,
    val time: String,
    val type: String
)

/**
 * Audit Log Row Component
 */
@Composable
private fun AuditLogRow(
    log: AuditLogItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (log.type) {
                            "success" -> Color(0xFF4CAF50)
                            "error" -> Color(0xFFF44336)
                            "warning" -> Color(0xFFFF9800)
                            else -> Color(0xFF2196F3)
                        },
                        shape = RoundedCornerShape(100.dp)
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        log.action,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        log.user,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    log.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Settings Tab - System configuration
 */
@Composable
fun SettingsTab() {
    var selectedSettingsSection by remember { mutableStateOf(SettingsSection.PROFILE) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
    ) {
        // Header
        Text(
            AdminConfig.SETTINGS_TITLE,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            "Configure system and user preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AdminDimens.SpacingXLarge))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
        ) {
            // Settings Navigation
            SettingsNavigation(
                selectedSection = selectedSettingsSection,
                onSectionSelected = { selectedSettingsSection = it },
                modifier = Modifier.width(200.dp)
            )

            // Settings Content
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(AdminDimens.SpacingLarge)
                ) {
                    when (selectedSettingsSection) {
                        SettingsSection.PROFILE -> ProfileSettings()
                        SettingsSection.SECURITY -> SecuritySettings()
                        SettingsSection.BIOMETRIC -> BiometricSettings()
                        SettingsSection.SYSTEM -> SystemSettings()
                        SettingsSection.NOTIFICATIONS -> NotificationSettings()
                        SettingsSection.APPEARANCE -> AppearanceSettings()
                    }
                }
            }
        }
    }
}

/**
 * Settings Sections Enum
 */
enum class SettingsSection(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PROFILE("Profile", Icons.Default.Person),
    SECURITY("Security", Icons.Default.Security),
    BIOMETRIC("Biometric", Icons.Default.Fingerprint),
    SYSTEM("System", Icons.Default.Settings),
    NOTIFICATIONS("Notifications", Icons.Default.Email),
    APPEARANCE("Appearance", Icons.Default.Edit)
}

/**
 * Settings Navigation Component
 */
@Composable
private fun SettingsNavigation(
    selectedSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingSmall)
    ) {
        SettingsSection.values().forEach { section ->
            SettingsNavigationItem(
                section = section,
                isSelected = selectedSection == section,
                onClick = { onSectionSelected(section) }
            )
        }
    }
}

/**
 * Settings Navigation Item
 */
@Composable
private fun SettingsNavigationItem(
    section: SettingsSection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(AdminDimens.SpacingMedium),
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                section.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                section.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Profile Settings
 */
@Composable
private fun ProfileSettings() {
    var fullName by remember { mutableStateOf("Admin User") }
    var email by remember { mutableStateOf("admin@fivucsas.com") }
    var role by remember { mutableStateOf("System Administrator") }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
    ) {
        Text(
            "Profile Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Profile Picture
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AU",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Button(onClick = { }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Change Photo")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { }) {
                    Text("Remove")
                }
            }
        }

        Divider()

        // Profile Information
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = role,
            onValueChange = { },
            label = { Text("Role") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        // Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { }) {
                Text("Cancel")
            }
            Spacer(Modifier.width(AdminDimens.SpacingMedium))
            Button(onClick = { }) {
                Text("Save Changes")
            }
        }
    }
}

/**
 * Security Settings
 */
@Composable
private fun SecuritySettings() {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var sessionTimeout by remember { mutableStateOf(30) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
    ) {
        Text(
            "Security Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Password Change
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Change Password",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = { },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Update Password")
                }
            }
        }

        // Two-Factor Authentication
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(AdminDimens.SpacingLarge).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Two-Factor Authentication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add an extra layer of security to your account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                androidx.compose.material3.Switch(
                    checked = twoFactorEnabled,
                    onCheckedChange = { twoFactorEnabled = it }
                )
            }
        }

        // Session Timeout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Session Timeout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Automatically log out after: $sessionTimeout minutes",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = sessionTimeout.toFloat(),
                    onValueChange = { sessionTimeout = it.toInt() },
                    valueRange = 5f..120f,
                    steps = 23
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5 min", style = MaterialTheme.typography.bodySmall)
                    Text("120 min", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * Biometric Settings
 */
@Composable
private fun BiometricSettings() {
    var faceMatchThreshold by remember { mutableStateOf(0.6f) }
    var livenessThreshold by remember { mutableStateOf(0.75f) }
    var qualityThreshold by remember { mutableStateOf(0.5f) }
    var maxRetries by remember { mutableStateOf(3) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
    ) {
        Text(
            "Biometric Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Face Match Threshold
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Face Match Threshold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Minimum similarity score required for face verification: ${String.format("%.2f", faceMatchThreshold)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = faceMatchThreshold,
                    onValueChange = { faceMatchThreshold = it },
                    valueRange = 0.3f..0.9f,
                    steps = 11
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Less Strict (0.3)", style = MaterialTheme.typography.bodySmall)
                    Text("More Strict (0.9)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Liveness Detection Threshold
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Liveness Detection Threshold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Minimum score to pass anti-spoofing check: ${String.format("%.2f", livenessThreshold)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = livenessThreshold,
                    onValueChange = { livenessThreshold = it },
                    valueRange = 0.5f..1.0f,
                    steps = 9
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Less Strict (0.5)", style = MaterialTheme.typography.bodySmall)
                    Text("Most Strict (1.0)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Image Quality Threshold
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Image Quality Threshold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Minimum image quality for enrollment: ${String.format("%.2f", qualityThreshold)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = qualityThreshold,
                    onValueChange = { qualityThreshold = it },
                    valueRange = 0.3f..0.8f,
                    steps = 9
                )
            }
        }

        // Max Retry Attempts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Maximum Retry Attempts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Number of retries allowed for failed verification: $maxRetries",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = maxRetries.toFloat(),
                    onValueChange = { maxRetries = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { }) {
                Text("Reset to Defaults")
            }
            Spacer(Modifier.width(AdminDimens.SpacingMedium))
            Button(onClick = { }) {
                Text("Save Settings")
            }
        }
    }
}

/**
 * System Settings
 */
@Composable
private fun SystemSettings() {
    var apiUrl by remember { mutableStateOf("http://localhost:8080") }
    var biometricServiceUrl by remember { mutableStateOf("http://localhost:8001") }
    var enableLogging by remember { mutableStateOf(true) }
    var logLevel by remember { mutableStateOf("INFO") }
    var maxConcurrentJobs by remember { mutableStateOf(10) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
    ) {
        Text(
            "System Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // API Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "API Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    label = { Text("Identity Core API URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = biometricServiceUrl,
                    onValueChange = { biometricServiceUrl = it },
                    label = { Text("Biometric Processor URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Logging Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Logging Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable System Logging")
                    androidx.compose.material3.Switch(
                        checked = enableLogging,
                        onCheckedChange = { enableLogging = it }
                    )
                }
                
                Text("Log Level: $logLevel", style = MaterialTheme.typography.bodyMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DEBUG", "INFO", "WARN", "ERROR").forEach { level ->
                        AssistChip(
                            onClick = { logLevel = level },
                            label = { Text(level) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (logLevel == level) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        // Performance Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingMedium)
            ) {
                Text(
                    "Performance Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Max Concurrent Jobs: $maxConcurrentJobs",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = maxConcurrentJobs.toFloat(),
                    onValueChange = { maxConcurrentJobs = it.toInt() },
                    valueRange = 1f..50f,
                    steps = 48
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { }) {
                Text("Save System Settings")
            }
        }
    }
}

/**
 * Notification Settings
 */
@Composable
private fun NotificationSettings() {
    var emailNotifications by remember { mutableStateOf(true) }
    var loginAlerts by remember { mutableStateOf(true) }
    var failedVerifications by remember { mutableStateOf(true) }
    var systemAlerts by remember { mutableStateOf(true) }
    var weeklyReport by remember { mutableStateOf(true) }
    var monthlyReport by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
    ) {
        Text(
            "Notification Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
            ) {
                Text(
                    "Email Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                NotificationToggle(
                    title = "Enable Email Notifications",
                    description = "Receive notifications via email",
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it }
                )
                
                Divider()
                
                NotificationToggle(
                    title = "Login Alerts",
                    description = "Notify on new login attempts",
                    checked = loginAlerts,
                    onCheckedChange = { loginAlerts = it }
                )
                
                NotificationToggle(
                    title = "Failed Verifications",
                    description = "Alert on failed biometric verifications",
                    checked = failedVerifications,
                    onCheckedChange = { failedVerifications = it }
                )
                
                NotificationToggle(
                    title = "System Alerts",
                    description = "Critical system notifications",
                    checked = systemAlerts,
                    onCheckedChange = { systemAlerts = it }
                )
                
                Divider()
                
                Text(
                    "Reports",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                NotificationToggle(
                    title = "Weekly Report",
                    description = "Summary of system activity",
                    checked = weeklyReport,
                    onCheckedChange = { weeklyReport = it }
                )
                
                NotificationToggle(
                    title = "Monthly Report",
                    description = "Detailed monthly analytics",
                    checked = monthlyReport,
                    onCheckedChange = { monthlyReport = it }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { }) {
                Text("Save Preferences")
            }
        }
    }
}

/**
 * Notification Toggle Component
 */
@Composable
private fun NotificationToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Appearance Settings
 */
@Composable
private fun AppearanceSettings() {
    var darkMode by remember { mutableStateOf(false) }
    var compactView by remember { mutableStateOf(false) }
    var showAvatars by remember { mutableStateOf(true) }
    var animationsEnabled by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
    ) {
        Text(
            "Appearance Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
            ) {
                Text(
                    "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Dark Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Use dark theme for the interface",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(AdminDimens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AdminDimens.SpacingLarge)
            ) {
                Text(
                    "Display Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                NotificationToggle(
                    title = "Compact View",
                    description = "Show more content with tighter spacing",
                    checked = compactView,
                    onCheckedChange = { compactView = it }
                )
                
                NotificationToggle(
                    title = "Show User Avatars",
                    description = "Display avatars in user lists",
                    checked = showAvatars,
                    onCheckedChange = { showAvatars = it }
                )
                
                NotificationToggle(
                    title = "Enable Animations",
                    description = "Smooth transitions and effects",
                    checked = animationsEnabled,
                    onCheckedChange = { animationsEnabled = it }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { }) {
                Text("Apply Changes")
            }
        }
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
