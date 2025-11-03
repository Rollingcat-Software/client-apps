package com.fivucsas.desktop.ui.kiosk

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.presentation.state.KioskScreen
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject

/**
 * Kiosk Mode UI
 *
 * Self-service mode for:
 * - Face enrollment
 * - Identity verification
 * - Biometric puzzle completion
 *
 * Designed for fullscreen touchscreen kiosks
 *
 * ARCHITECTURE:
 * - Follows MVVM pattern
 * - Uses ViewModel for state management
 * - Implements SOLID principles
 */

// Constants
private object KioskConfig {
    const val TITLE = "FIVUCSAS Kiosk"
    const val WELCOME_TITLE = "Welcome to FIVUCSAS"
    const val WELCOME_SUBTITLE = "Secure Face-Based Identity Verification"
    const val ENROLL_TITLE = "User Enrollment"
    const val VERIFY_TITLE = "Identity Verification"
    const val ENROLL_BUTTON = "New User Enrollment"
    const val VERIFY_BUTTON = "Identity Verification"
}

private object KioskDimens {
    val IconSize = 120.dp
    val IconMedium = 64.dp
    val IconSmall = 32.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingXLarge = 32.dp
    val SpacingXXLarge = 64.dp
    val ButtonWidth = 250.dp
    val ButtonHeight = 80.dp
    val CameraPreviewHeight = 400.dp
}

// KioskViewModel now imported from shared module
// ✅ Removed local definition - using com.fivucsas.shared.presentation.viewmodel.KioskViewModel

// EnrollmentData now imported from shared module
// ✅ Removed local definition - using com.fivucsas.shared.domain.model.EnrollmentData

/**
 * Main Kiosk Mode composable - Pure presentation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskMode(
    onBack: () -> Unit,
    viewModel: KioskViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentScreen = uiState.currentScreen

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(KioskConfig.TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            KioskContent(
                currentScreen = currentScreen,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Kiosk content router - Follows Open/Closed Principle
 */
@Composable
private fun KioskContent(
    currentScreen: KioskScreen,
    viewModel: KioskViewModel
) {
    when (currentScreen) {
        KioskScreen.WELCOME -> WelcomeScreen(
            onEnroll = viewModel::navigateToEnroll,
            onVerify = viewModel::navigateToVerify
        )

        KioskScreen.ENROLL -> EnrollScreen(
            viewModel = viewModel,
            onBack = viewModel::navigateToWelcome
        )

        KioskScreen.VERIFY -> VerifyScreen(
            onBack = viewModel::navigateToWelcome
        )
    }
}

/**
 * Welcome Screen - Pure presentation component
 */
@Composable
fun WelcomeScreen(
    onEnroll: () -> Unit,
    onVerify: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WelcomeLogo()

        Spacer(modifier = Modifier.height(KioskDimens.SpacingXXLarge))

        ActionButtons(
            onEnroll = onEnroll,
            onVerify = onVerify
        )
    }
}

/**
 * Welcome logo component - Extracted for reusability
 */
@Composable
private fun WelcomeLogo() {
    Icon(
        imageVector = Icons.Default.Face,
        contentDescription = "Face Icon",
        modifier = Modifier.size(KioskDimens.IconSize),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(KioskDimens.SpacingXLarge))

    Text(
        text = KioskConfig.WELCOME_TITLE,
        style = MaterialTheme.typography.displayMedium
    )

    Text(
        text = KioskConfig.WELCOME_SUBTITLE,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Action buttons component - Extracted for reusability
 */
@Composable
private fun ActionButtons(
    onEnroll: () -> Unit,
    onVerify: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KioskDimens.SpacingLarge)
    ) {
        ActionButton(
            text = KioskConfig.ENROLL_BUTTON,
            icon = Icons.Default.PersonAdd,
            onClick = onEnroll
        )

        ActionButton(
            text = KioskConfig.VERIFY_BUTTON,
            icon = Icons.Default.VerifiedUser,
            onClick = onVerify
        )
    }
}

/**
 * Reusable action button component
 */
@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(
            width = KioskDimens.ButtonWidth,
            height = KioskDimens.ButtonHeight
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(KioskDimens.IconSmall)
            )
            Spacer(modifier = Modifier.height(KioskDimens.SpacingSmall))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Enrollment Screen - With proper state management
 */
@Composable
fun EnrollScreen(
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    val enrollmentData by viewModel.enrollmentData.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(KioskDimens.SpacingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = KioskConfig.ENROLL_TITLE,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(KioskDimens.SpacingXLarge))

        Card(
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(modifier = Modifier.padding(KioskDimens.SpacingXLarge)) {
                EnrollmentForm(
                    enrollmentData = enrollmentData,
                    onFullNameChange = viewModel::updateFullName,
                    onEmailChange = viewModel::updateEmail,
                    onIdNumberChange = viewModel::updateIdNumber
                )

                Spacer(modifier = Modifier.height(KioskDimens.SpacingXLarge))

                BiometricCaptureSection()

                Spacer(modifier = Modifier.height(KioskDimens.SpacingXLarge))

                EnrollmentActions(
                    onBack = onBack,
                    onEnroll = { /* TODO: Implement */ },
                    isValid = enrollmentData.fullName.isNotBlank() && 
                              enrollmentData.email.isNotBlank() && 
                              enrollmentData.idNumber.isNotBlank()
                )
            }
        }
    }
}

/**
 * Enrollment form component - Extracted for clarity
 */
@Composable
private fun EnrollmentForm(
    enrollmentData: EnrollmentData,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onIdNumberChange: (String) -> Unit
) {
    Text(
        "Step 1: Provide Information",
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(KioskDimens.SpacingLarge))

    ValidatedTextField(
        value = enrollmentData.fullName,
        onValueChange = onFullNameChange,
        label = "Full Name",
        isRequired = true
    )

    Spacer(modifier = Modifier.height(KioskDimens.SpacingMedium))

    ValidatedTextField(
        value = enrollmentData.email,
        onValueChange = onEmailChange,
        label = "Email",
        isRequired = true,
        keyboardType = KeyboardType.Email
    )

    Spacer(modifier = Modifier.height(KioskDimens.SpacingMedium))

    ValidatedTextField(
        value = enrollmentData.idNumber,
        onValueChange = onIdNumberChange,
        label = "ID Number",
        isRequired = true,
        keyboardType = KeyboardType.Number
    )
}

/**
 * Validated text field component - Reusable with validation
 */
@Composable
private fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label + if (isRequired) " *" else "") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isRequired && value.isBlank(),
        supportingText = {
            if (isRequired && value.isBlank()) {
                Text("This field is required")
            }
        }
    )
}

/**
 * Biometric capture section - Placeholder for camera
 */
@Composable
private fun BiometricCaptureSection() {
    Text(
        "Step 2: Biometric Capture",
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(KioskDimens.SpacingMedium))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(KioskDimens.CameraPreviewHeight),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Camera",
                modifier = Modifier.size(KioskDimens.IconMedium),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(KioskDimens.SpacingMedium))
            Text(
                "Camera Preview (To be implemented)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(KioskDimens.SpacingSmall))
            Text(
                "Face detection, biometric puzzle, and enrollment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enrollment action buttons
 */
@Composable
private fun EnrollmentActions(
    onBack: () -> Unit,
    onEnroll: () -> Unit,
    isValid: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KioskDimens.SpacingMedium)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onEnroll,
            modifier = Modifier.weight(1f),
            enabled = isValid
        ) {
            Text("Start Enrollment")
        }
    }
}

/**
 * Verification Screen - Biometric puzzle challenge
 */
@Composable
fun VerifyScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(KioskDimens.SpacingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = KioskConfig.VERIFY_TITLE,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(KioskDimens.SpacingXLarge))

        Card(
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(modifier = Modifier.padding(KioskDimens.SpacingXLarge)) {
                VerificationHeader()

                Spacer(modifier = Modifier.height(KioskDimens.SpacingLarge))

                BiometricCaptureSection()

                Spacer(modifier = Modifier.height(KioskDimens.SpacingLarge))

                PuzzleInstructions()

                Spacer(modifier = Modifier.height(KioskDimens.SpacingXLarge))

                VerificationActions(onBack = onBack)
            }
        }
    }
}

/**
 * Verification header component
 */
@Composable
private fun VerificationHeader() {
    Text(
        "Biometric Puzzle Challenge",
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(KioskDimens.SpacingMedium))

    Text(
        "Complete the following actions to verify your identity:",
        style = MaterialTheme.typography.bodyLarge
    )
}

/**
 * Puzzle instructions component - Shows verification steps
 */
@Composable
private fun PuzzleInstructions() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(KioskDimens.SpacingMedium)) {
            PuzzleStep(
                number = 1,
                description = "Smile",
                isComplete = true
            )
            Spacer(modifier = Modifier.height(KioskDimens.SpacingSmall))

            PuzzleStep(
                number = 2,
                description = "Blink both eyes",
                isComplete = false
            )
            Spacer(modifier = Modifier.height(KioskDimens.SpacingSmall))

            PuzzleStep(
                number = 3,
                description = "Turn head right",
                isComplete = false
            )
        }
    }
}

/**
 * Individual puzzle step component
 */
@Composable
private fun PuzzleStep(
    number: Int,
    description: String,
    isComplete: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isComplete) Icons.Default.Check else Icons.Default.HourglassEmpty,
            contentDescription = if (isComplete) "Complete" else "Pending",
            tint = if (isComplete)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(KioskDimens.SpacingSmall))
        Text(
            "$number. $description",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Verification action buttons
 */
@Composable
private fun VerificationActions(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KioskDimens.SpacingMedium)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }

        Button(
            onClick = { /* TODO: Implement verification */ },
            modifier = Modifier.weight(1f)
        ) {
            Text("Start Verification")
        }
    }
}

// KioskScreen now imported from shared module
// ✅ Removed local definition - using com.fivucsas.shared.presentation.state.KioskScreen
