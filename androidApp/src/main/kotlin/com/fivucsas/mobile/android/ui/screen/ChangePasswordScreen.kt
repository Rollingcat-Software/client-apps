package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.AppTextField
import com.fivucsas.shared.ui.components.molecules.PasswordStrengthIndicator
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            AppTextField(
                value = currentPassword.value,
                onValueChange = { currentPassword.value = it },
                label = "Current Password",
                leadingIcon = Icons.Default.Lock,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            AppTextField(
                value = newPassword.value,
                onValueChange = { newPassword.value = it },
                label = "New Password",
                leadingIcon = Icons.Default.Lock,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            PasswordStrengthIndicator(password = newPassword.value)
            AppTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.size(4.dp))
            Button(
                onClick = { onSubmit(currentPassword.value, newPassword.value, confirmPassword.value) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Password")
            }
            Text(
                text = "Password must be at least 8 characters and include upper/lowercase and a number.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant
            )
        }
    }
}
