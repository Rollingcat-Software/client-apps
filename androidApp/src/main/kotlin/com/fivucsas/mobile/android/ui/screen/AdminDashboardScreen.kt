package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.ui.util.disposeOnLeave
import com.fivucsas.shared.ui.components.atoms.SearchTextField
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.StatCard
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.components.organisms.QuickActionGrid
import com.fivucsas.shared.ui.components.organisms.QuickActionItem
import com.fivucsas.shared.ui.theme.AppColors
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    userRole: UserRole = UserRole.USER,
    currentRoute: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToIdentify: () -> Unit,
    onNavigateToInvitations: () -> Unit,
    onNavigateToExamEntry: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    viewModel: AdminViewModel = koinInject<AdminViewModel>().disposeOnLeave()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = s(StringKey.ADMIN_TITLE_BADGE),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = s(StringKey.ADMIN_DASHBOARD_TITLE),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = s(StringKey.NAV_NOTIFICATIONS)
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = s(StringKey.NAV_PROFILE)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = BottomNavDestinations.adminItems,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            // [A] Error/Success banners
            uiState.errorMessage?.let { error ->
                ErrorMessage(message = error)
            }
            uiState.successMessage?.let { success ->
                SuccessMessage(message = success)
            }

            // [B] System Overview - 2x2 StatCard grid
            SectionHeader(title = s(StringKey.ADMIN_SYSTEM_OVERVIEW))

            val stats = uiState.statistics
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall),
                maxItemsInEachRow = 2
            ) {
                StatCard(
                    value = stats.totalUsers.toString(),
                    label = s(StringKey.TOTAL_USERS),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.People,
                    iconTint = AppColors.Primary
                )
                StatCard(
                    value = stats.activeUsers.toString(),
                    label = s(StringKey.ACTIVE_USERS),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.VerifiedUser,
                    iconTint = AppColors.Success
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall),
                maxItemsInEachRow = 2
            ) {
                StatCard(
                    value = stats.verificationsToday.toString(),
                    label = s(StringKey.VERIFICATIONS),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Security,
                    iconTint = AppColors.Info
                )
                StatCard(
                    value = "${(stats.successRate * 100).toInt()}%",
                    label = s(StringKey.ANALYTICS_SUCCESS_RATE),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Analytics,
                    iconTint = if (stats.successRate >= 0.8) AppColors.Success else AppColors.Warning
                )
            }

            // [C] Quick Actions
            SectionHeader(title = s(StringKey.DASH_QUICK_ACTIONS))
            val adminQuickActions = buildList {
                if (userRole.hasPermission(Permission.TENANT_USERS_READ)) {
                    add(QuickActionItem(s(StringKey.ADMIN_MANAGE_USERS), Icons.Default.Group, onNavigateToUsers))
                }
                if (userRole.hasPermission(Permission.HISTORY_READ_TENANT)) {
                    add(QuickActionItem(s(StringKey.ADMIN_VIEW_ANALYTICS), Icons.Default.Analytics, onNavigateToHistory))
                }
                add(QuickActionItem(s(StringKey.ADMIN_ACTIVITY_LOG), Icons.Default.History, onNavigateToHistory))
                if (userRole.hasPermission(Permission.TENANT_SETTINGS_READ)) {
                    add(QuickActionItem(s(StringKey.NAV_SETTINGS), Icons.Default.Settings, onNavigateToSettings))
                }
                if (userRole.hasPermission(Permission.IDENTIFY_TENANT)) {
                    add(QuickActionItem(s(StringKey.ADMIN_IDENTIFY), Icons.Default.PersonSearch, onNavigateToIdentify))
                }
                if (userRole.hasPermission(Permission.TENANT_INVITE_CREATE)) {
                    add(QuickActionItem(s(StringKey.DASH_INVITATIONS), Icons.Default.Mail, onNavigateToInvitations))
                }
                add(QuickActionItem(s(StringKey.ADMIN_EXAM_ENTRY), Icons.Default.Nfc, onNavigateToExamEntry))
            }
            QuickActionGrid(actions = adminQuickActions)

            // [D] Users section with search (gated)
            if (userRole.hasPermission(Permission.TENANT_USERS_READ)) {
            SectionHeader(title = s(StringKey.ADMIN_USERS_SECTION))
            SearchTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = s(StringKey.SEARCH_USERS),
                modifier = Modifier.fillMaxWidth()
            )

            // [E] User list (up to 5)
            val displayUsers = uiState.filteredUsers.take(5)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                displayUsers.forEach { user ->
                    UserListItem(user = user)
                }
            }

            // [F] "View all N users" link
            if (uiState.filteredUsers.size > 5) {
                Text(
                    text = s(StringKey.ADMIN_VIEW_ALL_USERS, uiState.filteredUsers.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .clickable { onNavigateToUsers() }
                        .padding(vertical = UIDimens.SpacingSmall)
                )
            }
            } // end TENANT_USERS_READ gate

            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
        }
    }
}

@Composable
private fun UserListItem(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = AppColors.Primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StatusBadge(
            text = user.status.name,
            type = when (user.status) {
                UserStatus.ACTIVE -> StatusBadgeType.Success
                UserStatus.INACTIVE -> StatusBadgeType.Neutral
                UserStatus.PENDING -> StatusBadgeType.Warning
                UserStatus.SUSPENDED -> StatusBadgeType.Failure
            }
        )
    }
}
