package com.fivucsas.desktop.ui.kiosk.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.ui.components.atoms.AppTextField
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerMedium
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Enroll Screen Component
 *
 * User enrollment form with biometric capture.
 *
 * @param viewModel Kiosk view model
 * @param onBack Callback to return to welcome screen
 */
@Composable
fun EnrollScreen(
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val enrollmentData by viewModel.enrollmentData.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UIDimens.SpacingXLarge)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "New Enrollment",
                style = MaterialTheme.typography.displaySmall,
                color = AppColors.Primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Please fill in your details",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

            // Messages
            uiState.errorMessage?.let {
                ErrorMessage(message = it)
                VerticalSpacerMedium()
            }

            uiState.successMessage?.let {
                SuccessMessage(message = it)
                VerticalSpacerMedium()
            }

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UIDimens.SpacingLarge)
                ) {
                    // Full Name
                    AppTextField(
                        value = enrollmentData.fullName,
                        onValueChange = viewModel::updateFullName,
                        label = "Full Name",
                        placeholder = "Enter your full name",
                        leadingIcon = Icons.Default.Person
                    )

                    VerticalSpacerMedium()

                    // Email
                    AppTextField(
                        value = enrollmentData.email,
                        onValueChange = viewModel::updateEmail,
                        label = "Email Address",
                        placeholder = "your.email@example.com",
                        leadingIcon = Icons.Default.Email
                    )

                    VerticalSpacerMedium()

                    // ID Number
                    AppTextField(
                        value = enrollmentData.idNumber,
                        onValueChange = viewModel::updateIdNumber,
                        label = "ID Number",
                        placeholder = "Enter your ID number",
                        leadingIcon = Icons.Default.Badge
                    )

                    VerticalSpacerMedium()

                    // Phone Number
                    AppTextField(
                        value = enrollmentData.phoneNumber,
                        onValueChange = viewModel::updatePhoneNumber,
                        label = "Phone Number",
                        placeholder = "+1234567890",
                        leadingIcon = Icons.Default.Phone
                    )

                    VerticalSpacerMedium()

                    // Address
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

            // Photo Capture Status
            if (uiState.capturedImage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Success.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "✓ Photo captured successfully",
                        modifier = Modifier.padding(UIDimens.SpacingMedium),
                        color = AppColors.Success
                    )
                }
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (uiState.capturedImage == null) {
                            viewModel.captureImage()
                        }
                        viewModel.submitEnrollment()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text(if (uiState.isLoading) "Processing..." else "Submit Enrollment")
                }
            }
        }
    }
}
