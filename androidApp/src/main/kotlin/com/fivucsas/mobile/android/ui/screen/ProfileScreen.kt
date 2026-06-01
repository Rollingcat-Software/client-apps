package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.molecules.ConfirmationDialog
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import androidx.compose.material3.CircularProgressIndicator
import com.fivucsas.mobile.android.ui.component.ExportDataRow
import com.fivucsas.mobile.android.ui.viewmodel.DataExportViewModel
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userRole: UserRole = UserRole.USER,
    userPhone: String = "",
    enrollmentDate: String = "",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    currentRoute: String,
    onNavigateBottom: (String) -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onReEnroll: () -> Unit,
    /**
     * Performs the real biometric-enrollment deletion (DELETE biometric/face/{userId})
     * and returns its [Result]. The success dialog is shown only when this resolves
     * to [Result.success]; a failure surfaces an inline error instead. Wired in
     * AppNavigation to [BiometricRepository.deleteBiometricData]. When `null`, no
     * real delete capability is available and the delete control is hidden.
     */
    onDeleteEnrollment: (suspend () -> Result<Unit>)? = null,
    onOpenSettings: () -> Unit,
    onOpenLinkedAccounts: () -> Unit = {},
    onOpenLoginRequests: () -> Unit = {},
    navItems: List<com.fivucsas.shared.ui.components.organisms.BottomNavItem> = com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations.items,
    userId: String = "",
    dataExportViewModel: DataExportViewModel? = null,
) {
    val isSelfBiometricRole = userRole == UserRole.USER || userRole == UserRole.TENANT_MEMBER
    val canDeleteEnrollment = onDeleteEnrollment != null &&
        isSelfBiometricRole &&
        userRole.hasPermission(Permission.ENROLL_SELF_DELETE)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteSuccess by remember { mutableStateOf(false) }
    var deleteInProgress by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val deleteScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s(StringKey.PROFILE_MY_PROFILE_TITLE)) },
                actions = {
                    if (userRole.hasPermission(Permission.PROFILE_UPDATE_SELF)) {
                        IconButton(onClick = onEditProfile) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = s(StringKey.A11Y_EDIT_PROFILE))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
                )
            }
            errorMessage?.let {
                ErrorMessage(message = it)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(72.dp)
                )
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                    if (enrollmentDate.isNotBlank()) {
                        Text(
                            text = s(StringKey.PROFILE_MEMBER_SINCE, enrollmentDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                    Text(
                        text = userRole.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Primary
                    )
                }
            }

            SectionHeader(title = s(StringKey.PROFILE_PERSONAL_INFO))
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column(modifier = Modifier.padding(UIDimens.SpacingMedium)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s(StringKey.PROFILE_FIELD_NAME), style = MaterialTheme.typography.bodySmall, color = AppColors.OnSurfaceVariant)
                        Text(userName, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s(StringKey.PROFILE_FIELD_EMAIL), style = MaterialTheme.typography.bodySmall, color = AppColors.OnSurfaceVariant)
                        Text(userEmail, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    if (userPhone.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s(StringKey.PROFILE_FIELD_PHONE), style = MaterialTheme.typography.bodySmall, color = AppColors.OnSurfaceVariant)
                            Text(userPhone, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            SectionHeader(title = s(StringKey.PROFILE_ACCOUNT_ACTIONS))
            Column(verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)) {
                Button(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
                    Text(s(StringKey.CHANGE_PASSWORD_TITLE))
                }
                Button(onClick = onOpenLinkedAccounts, modifier = Modifier.fillMaxWidth()) {
                    Text(s(StringKey.LINKED_ACCOUNTS_TITLE))
                }
                Button(onClick = onOpenLoginRequests, modifier = Modifier.fillMaxWidth()) {
                    Text(s(StringKey.APPROVE_LOGIN_TITLE))
                }
                if (isSelfBiometricRole && userRole.hasPermission(Permission.ENROLL_SELF_UPDATE)) {
                    Button(onClick = onReEnroll, modifier = Modifier.fillMaxWidth()) {
                        Text(s(StringKey.PROFILE_RE_ENROLL_FACE))
                    }
                }
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(s(StringKey.PROFILE_OPEN_SETTINGS))
                }
                // Only shown when a real delete capability is wired in
                // (onDeleteEnrollment != null). If no delete API is available the
                // control is hidden entirely — we never offer an action we cannot honour.
                if (canDeleteEnrollment) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !deleteInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Error
                        )
                    ) {
                        Text(s(StringKey.PROFILE_DELETE_ENROLLMENT))
                    }
                }
            }

            if (showDeleteSuccess) {
                SuccessMessage(message = s(StringKey.PROFILE2_DELETE_SUCCESS))
            }
            deleteError?.let { ErrorMessage(message = it) }

            // ── "My Data" (GDPR Art. 20 / KVKK data portability) ──
            // Only rendered when we have both a user id + a VM wired in by
            // the navigation layer. Positioned below privacy/settings per the
            // ProfileScreen spec (Agent 20B; Agent 20D owns Settings).
            if (dataExportViewModel != null && userId.isNotBlank()) {
                SectionHeader(title = s(StringKey.MY_DATA))
                ExportDataRow(
                    userId = userId,
                    viewModel = dataExportViewModel,
                )
            }
        }
    }

    if (showDeleteDialog && onDeleteEnrollment != null) {
        ConfirmationDialog(
            title = s(StringKey.PROFILE2_DELETE_DIALOG_TITLE),
            message = s(StringKey.PROFILE2_DELETE_DIALOG_MESSAGE),
            confirmText = s(StringKey.DELETE),
            dismissText = s(StringKey.CANCEL),
            onConfirm = {
                showDeleteDialog = false
                deleteError = null
                deleteInProgress = true
                deleteScope.launch {
                    // Success is shown ONLY when the server confirms the delete.
                    // A failure surfaces an inline error — never a fake success.
                    onDeleteEnrollment().fold(
                        onSuccess = {
                            deleteInProgress = false
                            showDeleteSuccess = true
                        },
                        onFailure = { error ->
                            deleteInProgress = false
                            deleteError = error.message ?: s(StringKey.PROFILE2_DELETE_FAILED)
                        }
                    )
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
