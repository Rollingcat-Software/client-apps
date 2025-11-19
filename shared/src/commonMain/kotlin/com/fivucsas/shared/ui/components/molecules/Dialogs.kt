package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.BodyMediumText
import com.fivucsas.shared.ui.components.atoms.PrimaryButton
import com.fivucsas.shared.ui.components.atoms.SecondaryButton
import com.fivucsas.shared.ui.components.atoms.TitleLargeText
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerMedium
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes

/**
 * Confirmation Dialog Component
 *
 * A dialog for confirming actions (e.g., delete confirmations).
 *
 * @param title Dialog title
 * @param message Dialog message
 * @param confirmText Confirm button text
 * @param dismissText Dismiss button text
 * @param onConfirm Callback when confirmed
 * @param onDismiss Callback when dismissed
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleLargeText(text = title) },
        text = { BodyMediumText(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = AppColors.Error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = AppColors.OnSurface
                )
            }
        },
        shape = AppShapes.Dialog,
        containerColor = AppColors.Surface
    )
}

/**
 * Info Dialog Component
 *
 * A simple dialog for displaying information.
 *
 * @param title Dialog title
 * @param message Dialog message
 * @param buttonText Button text
 * @param onDismiss Callback when dismissed
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    buttonText: String = "OK",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { TitleLargeText(text = title) },
        text = { BodyMediumText(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = buttonText,
                    color = AppColors.Primary
                )
            }
        },
        shape = AppShapes.Dialog,
        containerColor = AppColors.Surface
    )
}

/**
 * Custom Dialog Component
 *
 * A flexible dialog component for custom content.
 *
 * @param onDismiss Callback when dismissed
 * @param content Dialog content
 */
@Composable
fun CustomDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = AppShapes.Dialog,
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = UIDimens.CardElevation
            )
        ) {
            Box(modifier = Modifier.padding(UIDimens.SpacingLarge)) {
                content()
            }
        }
    }
}

/**
 * Form Dialog Component
 *
 * A dialog with title, content, and action buttons.
 *
 * @param title Dialog title
 * @param onDismiss Callback when dismissed
 * @param onConfirm Callback when confirmed
 * @param confirmText Confirm button text
 * @param dismissText Dismiss button text
 * @param content Dialog content
 */
@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Save",
    dismissText: String = "Cancel",
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = AppShapes.Dialog,
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = UIDimens.CardElevation
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UIDimens.SpacingLarge)
            ) {
                // Title
                TitleLargeText(text = title)

                VerticalSpacerMedium()

                // Content
                content()

                VerticalSpacerMedium()

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        onClick = onDismiss,
                        text = dismissText,
                        modifier = Modifier.weight(1f)
                    )

                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.padding(horizontal = UIDimens.SpacingSmall)
                    )

                    PrimaryButton(
                        onClick = onConfirm,
                        text = confirmText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
