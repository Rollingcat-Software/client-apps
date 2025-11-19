package com.fivucsas.desktop.ui.admin.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.ui.components.atoms.AppTextField
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerSmall
import com.fivucsas.shared.ui.components.molecules.FormDialog

/**
 * Edit User Dialog
 *
 * Dialog for editing an existing user's information.
 *
 * @param user User to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when user is updated
 */
@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var idNumber by remember { mutableStateOf(user.idNumber) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var idNumberError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        // Reset errors
        nameError = null
        emailError = null
        idNumberError = null

        // Validation
        var hasError = false

        if (name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        }

        if (email.isBlank()) {
            emailError = "Email is required"
            hasError = true
        } else if (!email.contains("@")) {
            emailError = "Invalid email format"
            hasError = true
        }

        if (idNumber.isBlank()) {
            idNumberError = "ID number is required"
            hasError = true
        }

        if (!hasError) {
            val updatedUser = user.copy(
                name = name,
                email = email,
                idNumber = idNumber,
                phoneNumber = phoneNumber
            )
            onConfirm(updatedUser)
        }
    }

    FormDialog(
        title = "Edit User",
        onDismiss = onDismiss,
        onConfirm = { validateAndSubmit() },
        confirmText = "Update User",
        dismissText = "Cancel"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AppTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = "Full Name",
                placeholder = "Enter full name",
                leadingIcon = Icons.Default.Person,
                isError = nameError != null,
                errorMessage = nameError
            )

            VerticalSpacerSmall()

            AppTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email",
                placeholder = "user@example.com",
                leadingIcon = Icons.Default.Email,
                isError = emailError != null,
                errorMessage = emailError
            )

            VerticalSpacerSmall()

            AppTextField(
                value = idNumber,
                onValueChange = {
                    idNumber = it
                    idNumberError = null
                },
                label = "ID Number",
                placeholder = "Enter ID number",
                leadingIcon = Icons.Default.Badge,
                isError = idNumberError != null,
                errorMessage = idNumberError
            )

            VerticalSpacerSmall()

            AppTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                placeholder = "+1234567890",
                leadingIcon = Icons.Default.Phone
            )
        }
    }
}
