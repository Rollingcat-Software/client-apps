package com.fivucsas.mobile.android.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.ui.components.atoms.AppTextField
import com.fivucsas.shared.ui.components.atoms.SearchTextField
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ConfirmationDialog
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.FormDialog
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.components.organisms.EmptyState
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.util.disposeOnLeave
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementScreen(
    currentRoute: String,
    userRole: UserRole = UserRole.TENANT_ADMIN,
    onNavigateBack: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    onNavigateToEnrollUser: (String) -> Unit = {},
    viewModel: AdminViewModel = koinInject<AdminViewModel>().disposeOnLeave()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEnrollDeleteDialog by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s(StringKey.A11Y_NAVIGATE_BACK)
                        )
                    }
                },
                title = {
                    Text(
                        text = s(StringKey.USERSMGMT_TITLE),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddUserDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = s(StringKey.USERSMGMT_A11Y_ADD_USER)
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddUserDialog() },
                containerColor = AppColors.Primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = s(StringKey.USERSMGMT_A11Y_ADD_USER),
                    tint = AppColors.OnPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
        ) {
            // Error/Success banners
            uiState.errorMessage?.let { error ->
                ErrorMessage(message = error)
            }
            uiState.successMessage?.let { success ->
                SuccessMessage(message = success)
            }

            // Search bar
            SearchTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = s(StringKey.SEARCH_USERS),
                modifier = Modifier.fillMaxWidth()
            )

            // User count label
            Text(
                text = s(StringKey.USERSMGMT_USERS_FOUND, uiState.filteredUsers.size),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = UIDimens.SpacingLarge),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (uiState.filteredUsers.isEmpty()) {
                // Empty state
                EmptyState(
                    title = s(StringKey.USERSMGMT_NO_USERS_FOUND),
                    message = if (uiState.searchQuery.isNotBlank())
                        s(StringKey.USERSMGMT_NO_USERS_MATCH, uiState.searchQuery)
                    else
                        s(StringKey.USERSMGMT_NO_USERS_YET),
                    icon = Icons.Default.PersonSearch,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // User list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.filteredUsers,
                        key = { it.id }
                    ) { user ->
                        UserRow(
                            user = user,
                            userRole = userRole,
                            onEdit = { viewModel.showEditUserDialog(user) },
                            onDelete = { viewModel.showDeleteConfirmation(user) },
                            onEnrollUser = { onNavigateToEnrollUser(user.id) },
                            onDeleteEnrollment = { showEnrollDeleteDialog = user }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // FAB clearance
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (uiState.showAddUserDialog) {
        AddEditUserDialog(
            user = null,
            onDismiss = { viewModel.hideAddUserDialog() },
            onConfirm = { user -> viewModel.addUser(user) }
        )
    }

    // Edit User Dialog
    if (uiState.showEditUserDialog) {
        AddEditUserDialog(
            user = uiState.editingUser,
            onDismiss = { viewModel.hideEditUserDialog() },
            onConfirm = { user -> viewModel.updateUser(user) }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmation) {
        ConfirmationDialog(
            title = s(StringKey.USERSMGMT_DELETE_USER_TITLE),
            message = s(
                StringKey.USERSMGMT_DELETE_USER_MESSAGE,
                uiState.userToDelete?.name ?: s(StringKey.USERSMGMT_DELETE_USER_FALLBACK)
            ),
            confirmText = s(StringKey.DELETE),
            dismissText = s(StringKey.CANCEL),
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.hideDeleteConfirmation() }
        )
    }

    // Delete Enrollment Confirmation Dialog
    showEnrollDeleteDialog?.let { user ->
        ConfirmationDialog(
            title = s(StringKey.USERSMGMT_DELETE_ENROLLMENT_TITLE),
            message = s(StringKey.USERSMGMT_DELETE_ENROLLMENT_MESSAGE, user.name),
            confirmText = s(StringKey.USERSMGMT_DELETE_ENROLLMENT_CONFIRM),
            dismissText = s(StringKey.CANCEL),
            onConfirm = {
                viewModel.updateUser(user.copy(hasBiometric = false))
                showEnrollDeleteDialog = null
            },
            onDismiss = { showEnrollDeleteDialog = null }
        )
    }
}

@Composable
private fun UserRow(
    user: User,
    userRole: UserRole = UserRole.TENANT_ADMIN,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEnrollUser: () -> Unit = {},
    onDeleteEnrollment: () -> Unit = {}
) {
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
                fontWeight = FontWeight.Bold,
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
            if (user.phoneNumber.isNotBlank()) {
                Text(
                    text = user.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
        if (!user.hasBiometric && userRole.hasPermission(Permission.ENROLL_TENANT_CREATE)) {
            IconButton(onClick = onEnrollUser) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = s(StringKey.USERSMGMT_A11Y_ENROLL_USER),
                    tint = AppColors.Primary
                )
            }
        }
        if (user.hasBiometric && userRole.hasPermission(Permission.ENROLL_TENANT_DELETE)) {
            IconButton(onClick = onDeleteEnrollment) {
                Icon(
                    imageVector = Icons.Default.NoAccounts,
                    contentDescription = s(StringKey.USERSMGMT_A11Y_DELETE_ENROLLMENT),
                    tint = AppColors.Error
                )
            }
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = s(StringKey.USERSMGMT_A11Y_EDIT_USER),
                tint = AppColors.Primary
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = s(StringKey.USERSMGMT_A11Y_DELETE_USER),
                tint = AppColors.Error
            )
        }
    }
}

@Composable
private fun AddEditUserDialog(
    user: User?,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    val isEditMode = user != null
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var idNumber by remember(user) { mutableStateOf(user?.idNumber ?: "") }
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }

    val isValid = name.isNotBlank() && email.isNotBlank()

    FormDialog(
        title = if (isEditMode) s(StringKey.EDIT_USER) else s(StringKey.ADD_USER),
        onDismiss = onDismiss,
        onConfirm = {
            if (isValid) {
                val newUser = User(
                    id = user?.id ?: "user_${System.currentTimeMillis()}",
                    name = name.trim(),
                    email = email.trim(),
                    idNumber = idNumber.trim(),
                    phoneNumber = phoneNumber.trim(),
                    status = user?.status ?: UserStatus.ACTIVE,
                    enrollmentDate = user?.enrollmentDate ?: "",
                    hasBiometric = user?.hasBiometric ?: false,
                    role = user?.role ?: com.fivucsas.shared.domain.model.UserRole.USER
                )
                onConfirm(newUser)
            }
        },
        confirmText = if (isEditMode) s(StringKey.SAVE) else s(StringKey.USERSMGMT_ADD),
        dismissText = s(StringKey.CANCEL)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)) {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = s(StringKey.USER_NAME),
                placeholder = s(StringKey.USERSMGMT_NAME_PLACEHOLDER),
                isError = name.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = s(StringKey.USER_EMAIL),
                placeholder = s(StringKey.USERSMGMT_EMAIL_PLACEHOLDER),
                isError = email.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            AppTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = s(StringKey.USERSMGMT_ID_NUMBER),
                placeholder = s(StringKey.USERSMGMT_ID_NUMBER_PLACEHOLDER),
                modifier = Modifier.fillMaxWidth()
            )
            AppTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = s(StringKey.USERSMGMT_PHONE_NUMBER),
                placeholder = s(StringKey.USERSMGMT_PHONE_NUMBER_PLACEHOLDER),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
