package com.fivucsas.desktop.ui.admin.dialogs

import androidx.compose.runtime.Composable
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.ui.components.molecules.ConfirmationDialog

/**
 * Delete User Dialog
 *
 * Confirmation dialog for deleting a user from the system.
 *
 * @param user User to delete
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmationDialog(
        title = "Delete User",
        message = "Are you sure you want to delete ${user.name}? This action cannot be undone.",
        confirmText = "Delete",
        dismissText = "Cancel",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
