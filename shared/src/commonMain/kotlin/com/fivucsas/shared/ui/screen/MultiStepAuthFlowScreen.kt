package com.fivucsas.shared.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.SessionStep
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.StepDisplayStatus
import com.fivucsas.shared.presentation.viewmodel.MultiStepAuthViewModel
import com.fivucsas.shared.ui.components.organisms.StepIndicatorItem
import com.fivucsas.shared.ui.components.organisms.StepProgressIndicator
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Multi-Step Authentication Flow Screen.
 *
 * Orchestrates sequential authentication steps (e.g., Password -> Face -> TOTP).
 * Displays a step progress indicator at the top, renders the appropriate step UI
 * based on the current step type, and handles step completion/failure/skip.
 *
 * @param viewModel The MultiStepAuthViewModel managing flow state
 * @param sessionId Optional session ID to resume an existing session
 * @param onComplete Callback when all steps are completed successfully
 * @param onCancel Callback when the user cancels the flow
 * @param onStepAction Callback for platform-specific step actions (e.g., camera for FACE, biometric prompt for FINGERPRINT).
 *   The callback receives (methodType: String, stepOrder: Int, onResult: (Map<String, Any?>) -> Unit)
 */
@Composable
fun MultiStepAuthFlowScreen(
    viewModel: MultiStepAuthViewModel,
    sessionId: String? = null,
    onComplete: (accessToken: String?, userId: String?) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {},
    onStepAction: ((methodType: String, stepOrder: Int, onResult: (Map<String, Any?>) -> Unit) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize the flow on first composition
    LaunchedEffect(sessionId) {
        if (sessionId != null && uiState.sessionId.isBlank()) {
            viewModel.initWithSessionId(sessionId)
        }
    }

    // Trigger completion callback
    LaunchedEffect(uiState.flowComplete) {
        if (uiState.flowComplete) {
            onComplete(uiState.accessToken, uiState.userId)
        }
    }

    // Trigger cancel callback
    LaunchedEffect(uiState.flowCancelled) {
        if (uiState.flowCancelled) {
            onCancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with title and cancel button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s(StringKey.MULTI_STEP_AUTH_TITLE),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )

            if (!uiState.flowComplete && !uiState.isLoading) {
                IconButton(
                    onClick = { viewModel.cancelFlow() },
                    enabled = !uiState.isSubmitting
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = s(StringKey.CANCEL),
                        tint = AppColors.Gray600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = s(StringKey.LOADING),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }
                }
            }

            uiState.flowComplete -> {
                // Flow complete - success state
                FlowCompleteContent()
            }

            uiState.steps.isEmpty() && uiState.errorMessage != null -> {
                // Error loading session
                ErrorContent(
                    message = uiState.errorMessage ?: s(StringKey.ERROR_UNKNOWN),
                    onRetry = {
                        viewModel.clearError()
                        if (sessionId != null) {
                            viewModel.initWithSessionId(sessionId)
                        }
                    }
                )
            }

            uiState.steps.isNotEmpty() -> {
                // Active flow with steps
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Step progress indicator
                    StepProgressIndicator(
                        steps = uiState.steps.map { step ->
                            StepIndicatorItem(
                                label = step.authMethodType,
                                methodType = step.authMethodType,
                                status = uiState.getStepDisplayStatus(
                                    uiState.steps.indexOf(step)
                                )
                            )
                        },
                        currentStepIndex = uiState.currentStepIndex
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current step content with animated transitions
                    AnimatedContent(
                        targetState = uiState.currentStepIndex,
                        transitionSpec = {
                            (slideInHorizontally { width -> width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                        },
                        label = "step_transition"
                    ) { stepIndex ->
                        val step = uiState.steps.getOrNull(stepIndex)
                        if (step != null) {
                            StepContent(
                                step = step,
                                stepNumber = stepIndex + 1,
                                totalSteps = uiState.totalSteps,
                                isSubmitting = uiState.isSubmitting,
                                errorMessage = uiState.errorMessage,
                                onSubmit = { data ->
                                    viewModel.completeCurrentStep(data)
                                },
                                onRetry = { viewModel.clearError() },
                                onStepAction = onStepAction
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Skip button for optional steps
                    val currentStep = uiState.currentStep
                    if (currentStep != null && !currentStep.isRequired && !uiState.flowComplete) {
                        TextButton(
                            onClick = { viewModel.skipCurrentStep() },
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = s(StringKey.MULTI_STEP_SKIP))
                        }
                    }

                    // Step counter
                    if (!uiState.flowComplete && currentStep != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s(
                                StringKey.MULTI_STEP_COUNTER,
                                uiState.currentStepNumber,
                                uiState.totalSteps
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Gray500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the UI for a specific auth step based on its method type.
 */
@Composable
private fun StepContent(
    step: SessionStep,
    stepNumber: Int,
    totalSteps: Int,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (Map<String, Any?>) -> Unit,
    onRetry: () -> Unit,
    onStepAction: ((String, Int, (Map<String, Any?>) -> Unit) -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step icon
            val icon = methodTypeToIcon(step.authMethodType)
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = AppColors.Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = step.authMethodType,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step title
            Text(
                text = methodTypeToTitle(step.authMethodType),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Step description
            Text(
                text = methodTypeToDescription(step.authMethodType),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600,
                textAlign = TextAlign.Center
            )

            if (step.isRequired) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = s(StringKey.MULTI_STEP_REQUIRED),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Error,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = s(StringKey.RETRY))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action button / loading indicator
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s(StringKey.MULTI_STEP_VERIFYING),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            } else {
                Button(
                    onClick = {
                        if (onStepAction != null) {
                            // Delegate to platform-specific handler
                            onStepAction(step.authMethodType, step.stepOrder) { result ->
                                onSubmit(result)
                            }
                        } else {
                            // Default: submit empty data (for steps that don't need input)
                            onSubmit(emptyMap())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = methodTypeToActionLabel(step.authMethodType))
                }
            }
        }
    }
}

/**
 * Success state shown when all steps are complete.
 */
@Composable
private fun FlowCompleteContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = AppColors.Success
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = s(StringKey.MULTI_STEP_COMPLETE),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s(StringKey.MULTI_STEP_COMPLETE_DESC),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Error state for session loading failures.
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = s(StringKey.ERROR),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRetry) {
                Text(text = s(StringKey.RETRY))
            }
        }
    }
}

// -- Helper functions mapping method types to UI properties --

private fun methodTypeToIcon(methodType: String): ImageVector {
    return when (methodType.uppercase()) {
        "PASSWORD" -> Icons.Default.Lock
        "FACE" -> Icons.Default.Face
        "VOICE" -> Icons.Default.Mic
        "TOTP" -> Icons.Default.Password
        "EMAIL_OTP" -> Icons.Default.Email
        "SMS_OTP" -> Icons.Default.PhoneAndroid
        "QR_CODE" -> Icons.Default.QrCode
        "FINGERPRINT" -> Icons.Default.Fingerprint
        "HARDWARE_KEY" -> Icons.Default.Key
        "NFC_DOCUMENT" -> Icons.Default.Nfc
        else -> Icons.Default.Lock
    }
}

private fun methodTypeToTitle(methodType: String): String {
    return when (methodType.uppercase()) {
        "PASSWORD" -> s(StringKey.PASSWORD)
        "FACE" -> s(StringKey.FACE_RECOGNITION)
        "VOICE" -> s(StringKey.VOICE_RECOGNITION)
        "TOTP" -> s(StringKey.TOTP)
        "EMAIL_OTP" -> s(StringKey.EMAIL_OTP)
        "SMS_OTP" -> s(StringKey.SMS_OTP)
        "QR_CODE" -> "QR Code"
        "FINGERPRINT" -> s(StringKey.FINGERPRINT)
        "HARDWARE_KEY" -> s(StringKey.HARDWARE_KEY)
        "NFC_DOCUMENT" -> s(StringKey.NFC_DOCUMENT)
        else -> methodType
    }
}

private fun methodTypeToDescription(methodType: String): String {
    return when (methodType.uppercase()) {
        "PASSWORD" -> s(StringKey.MULTI_STEP_DESC_PASSWORD)
        "FACE" -> s(StringKey.MULTI_STEP_DESC_FACE)
        "VOICE" -> s(StringKey.MULTI_STEP_DESC_VOICE)
        "TOTP" -> s(StringKey.MULTI_STEP_DESC_TOTP)
        "EMAIL_OTP" -> s(StringKey.MULTI_STEP_DESC_EMAIL_OTP)
        "SMS_OTP" -> s(StringKey.MULTI_STEP_DESC_SMS_OTP)
        "QR_CODE" -> s(StringKey.MULTI_STEP_DESC_QR_CODE)
        "FINGERPRINT" -> s(StringKey.MULTI_STEP_DESC_FINGERPRINT)
        "HARDWARE_KEY" -> s(StringKey.MULTI_STEP_DESC_HARDWARE_KEY)
        "NFC_DOCUMENT" -> s(StringKey.MULTI_STEP_DESC_NFC)
        else -> ""
    }
}

private fun methodTypeToActionLabel(methodType: String): String {
    return when (methodType.uppercase()) {
        "PASSWORD" -> s(StringKey.MULTI_STEP_ACTION_PASSWORD)
        "FACE" -> s(StringKey.MULTI_STEP_ACTION_FACE)
        "VOICE" -> s(StringKey.MULTI_STEP_ACTION_VOICE)
        "TOTP" -> s(StringKey.MULTI_STEP_ACTION_TOTP)
        "EMAIL_OTP" -> s(StringKey.MULTI_STEP_ACTION_EMAIL_OTP)
        "SMS_OTP" -> s(StringKey.MULTI_STEP_ACTION_SMS_OTP)
        "QR_CODE" -> s(StringKey.MULTI_STEP_ACTION_QR_CODE)
        "FINGERPRINT" -> s(StringKey.MULTI_STEP_ACTION_FINGERPRINT)
        "HARDWARE_KEY" -> s(StringKey.MULTI_STEP_ACTION_HARDWARE_KEY)
        "NFC_DOCUMENT" -> s(StringKey.MULTI_STEP_ACTION_NFC)
        else -> s(StringKey.CONFIRM)
    }
}
