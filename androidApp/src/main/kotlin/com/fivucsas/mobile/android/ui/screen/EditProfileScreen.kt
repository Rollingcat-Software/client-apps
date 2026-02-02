package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.AppTextField
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialFirstName: String,
    initialLastName: String,
    email: String,
    initialPhone: String,
    idNumber: String,
    onNavigateBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val firstNameState = remember { mutableStateOf(initialFirstName) }
    val lastNameState = remember { mutableStateOf(initialLastName) }
    val phoneState = remember { mutableStateOf(initialPhone) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onSave(firstNameState.value, lastNameState.value, phoneState.value) }) {
                        Text("Save", style = MaterialTheme.typography.labelLarge, color = AppColors.Primary)
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
            Spacer(modifier = Modifier.size(8.dp))
            AppTextField(
                value = firstNameState.value,
                onValueChange = { firstNameState.value = it },
                label = "First Name",
                leadingIcon = Icons.Default.Person
            )
            AppTextField(
                value = lastNameState.value,
                onValueChange = { lastNameState.value = it },
                label = "Last Name",
                leadingIcon = Icons.Default.Person
            )
            AppTextField(
                value = email,
                onValueChange = {},
                label = "Email (read-only)",
                leadingIcon = Icons.Default.Email,
                enabled = false
            )
            AppTextField(
                value = phoneState.value,
                onValueChange = { phoneState.value = it },
                label = "Phone Number",
                leadingIcon = Icons.Default.Phone
            )
            AppTextField(
                value = idNumber,
                onValueChange = {},
                label = "ID Number",
                leadingIcon = Icons.Default.Badge,
                enabled = false
            )
        }
    }
}
