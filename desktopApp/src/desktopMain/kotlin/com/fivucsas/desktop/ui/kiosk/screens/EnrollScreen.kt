package com.fivucsas.desktop.ui.kiosk.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.components.DesktopBannerType
import com.fivucsas.desktop.ui.components.DesktopInfoBanner
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.desktop.ui.components.DesktopKioskCameraOverlay
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.ui.components.atoms.AppTextField
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerMedium
import org.koin.compose.koinInject

@Composable
fun EnrollScreen(
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val enrollmentData by viewModel.enrollmentData.collectAsState()
    val cameraService: ICameraService = koinInject()
    var pendingSubmit by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UIDimens.SpacingXLarge)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DesktopSectionHeader(
                title = "New Enrollment",
                subtitle = "Please fill in your details"
            )

            Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

            uiState.errorMessage?.let {
                DesktopInfoBanner(type = DesktopBannerType.Error, text = it)
                VerticalSpacerMedium()
            }

            uiState.successMessage?.let {
                DesktopInfoBanner(type = DesktopBannerType.Info, text = it)
                VerticalSpacerMedium()
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UIDimens.SpacingLarge)
                ) {
                    AppTextField(
                        value = enrollmentData.fullName,
                        onValueChange = viewModel::updateFullName,
                        label = "Full Name",
                        placeholder = "Enter your full name",
                        leadingIcon = Icons.Default.Person
                    )
                    VerticalSpacerMedium()
                    AppTextField(
                        value = enrollmentData.email,
                        onValueChange = viewModel::updateEmail,
                        label = "Email Address",
                        placeholder = "your.email@example.com",
                        leadingIcon = Icons.Default.Email
                    )
                    VerticalSpacerMedium()
                    AppTextField(
                        value = enrollmentData.idNumber,
                        onValueChange = viewModel::updateIdNumber,
                        label = "ID Number",
                        placeholder = "Enter your ID number",
                        leadingIcon = Icons.Default.Badge
                    )
                    VerticalSpacerMedium()
                    AppTextField(
                        value = enrollmentData.phoneNumber,
                        onValueChange = viewModel::updatePhoneNumber,
                        label = "Phone Number",
                        placeholder = "+1234567890",
                        leadingIcon = Icons.Default.Phone
                    )
                    VerticalSpacerMedium()
                    AppTextField(
                        value = enrollmentData.address,
                        onValueChange = viewModel::updateAddress,
                        label = "Address (Optional)",
                        placeholder = "Enter your address",
                        leadingIcon = Icons.Default.Home
                    )
                }
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

            if (uiState.capturedImage != null) {
                DesktopInfoBanner(
                    type = DesktopBannerType.Info,
                    text = "Photo captured successfully"
                )
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        if (uiState.capturedImage == null) {
                            pendingSubmit = true
                            viewModel.openCamera()
                        } else {
                            viewModel.submitEnrollment()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text(if (uiState.isLoading) "Processing..." else "Submit Enrollment")
                }
            }
        }

        if (uiState.showCamera) {
            DesktopKioskCameraOverlay(
                cameraService = cameraService,
                onCapture = { imageBytes ->
                    viewModel.setCapturedImage(imageBytes)
                    if (pendingSubmit) {
                        pendingSubmit = false
                        viewModel.submitEnrollment()
                    }
                },
                onClose = {
                    pendingSubmit = false
                    viewModel.closeCamera()
                }
            )
        }
    }
}
