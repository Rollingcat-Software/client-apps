package com.fivucsas.desktop.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.admin.components.AdminConstants
import com.fivucsas.desktop.ui.admin.dialogs.AddUserDialog
import com.fivucsas.desktop.ui.admin.dialogs.DeleteUserDialog
import com.fivucsas.desktop.ui.admin.dialogs.EditUserDialog
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel

/**
 * Users Tab Component
 *
 * Displays user management interface with search, statistics, and table.
 *
 * @param viewModel Admin view model
 */
@Composable
fun UsersTab(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
    ) {
        // Header with Add User button
        UsersHeader(
            onAddUser = viewModel::showAddUserDialog
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        // Search and filters
        UsersSearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onFilter = { /* TODO: Implement filter */ },
            onExport = { /* TODO: Implement export */ }
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        // Statistics Cards
        UserStatisticsCards(
            totalUsers = uiState.users.size,
            activeUsers = uiState.users.count { it.status == UserStatus.ACTIVE },
            inactiveUsers = uiState.users.count { it.status == UserStatus.INACTIVE },
            pendingUsers = uiState.users.count { it.status == UserStatus.PENDING }
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        // Users Table
        UsersTable(
            users = uiState.filteredUsers,
            onEdit = viewModel::showEditUserDialog,
            onDelete = viewModel::showDeleteConfirmation
        )
    }

    // Dialogs
    if (uiState.showAddUserDialog) {
        AddUserDialog(
            onDismiss = viewModel::hideAddUserDialog,
            onConfirm = { user ->
                viewModel.addUser(user)
            }
        )
    }

    if (uiState.showEditUserDialog && uiState.editingUser != null) {
        EditUserDialog(
            user = uiState.editingUser!!,
            onDismiss = viewModel::hideEditUserDialog,
            onConfirm = { user ->
                viewModel.updateUser(user)
            }
        )
    }

    if (uiState.showDeleteConfirmation && uiState.userToDelete != null) {
        DeleteUserDialog(
            user = uiState.userToDelete!!,
            onDismiss = viewModel::hideDeleteConfirmation,
            onConfirm = viewModel::confirmDelete
        )
    }
}

/**
 * Users Header Component
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
                AdminConstants.USER_MANAGEMENT_TITLE,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                AdminConstants.USER_MANAGEMENT_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(onClick = onAddUser) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
            Text(AdminConstants.ADD_USER)
        }
    }
}

/**
 * Search Bar Component
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
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(AdminConstants.SEARCH_PLACEHOLDER) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        OutlinedButton(onClick = onFilter) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
            Text(AdminConstants.FILTERS)
        }

        OutlinedButton(onClick = onExport) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconSmall)
            )
            Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
            Text(AdminConstants.EXPORT)
        }
    }
}

/**
 * User Statistics Cards
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
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
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
 * Individual Statistic Card
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
            .height(120.dp)
            .shadow(
                elevation = UIDimens.CardElevation,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(UIDimens.SpacingMedium)
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
 * Users Table Component
 */
@Composable
private fun UsersTable(
    users: List<User>,
    onEdit: (User) -> Unit,
    onDelete: (User) -> Unit
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
                    onEdit = { onEdit(user) },
                    onDelete = { onDelete(user) }
                )
            }
        }
    }
}

/**
 * Table Header Component
 */
@Composable
private fun UsersTableHeader() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium),
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
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
 * User Row Component
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
            .padding(UIDimens.SpacingMedium),
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium),
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
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
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
