package com.fivucsas.desktop.ui.kiosk

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.presentation.state.KioskScreen
import com.fivucsas.shared.presentation.state.VerificationResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.fivucsas.shared.config.UIDimens
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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

// Local KioskDimens removed - now using shared UIDimens from com.fivucsas.shared.config

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
 * Welcome Screen - Modern gradient design with elevated buttons
 */
@Composable
fun WelcomeScreen(
    onEnroll: () -> Unit,
    onVerify: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFFAFAFA)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Logo",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title with shadow effect
            Text(
                text = "FIVUCSAS",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp,
                    color = Color(0xFF1976D2),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.1f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
            
            Text(
                text = "Secure Identity Verification System",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Modern Gradient Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Enroll Button
                Button(
                    onClick = onEnroll,
                    modifier = Modifier
                        .width(200.dp)
                        .height(64.dp)
                        .shadow(8.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1976D2),
                                        Color(0xFF1565C0)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "New Enrollment",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                // Verify Button
                Button(
                    onClick = onVerify,
                    modifier = Modifier
                        .width(200.dp)
                        .height(64.dp)
                        .shadow(8.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00ACC1),
                                        Color(0xFF0097A7)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Verify Identity",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Welcome logo component - Extracted for reusability
 */
@Composable
private fun WelcomeLogo(iconSize: Dp = UIDimens.KioskIconSize) {
    Icon(
        imageVector = Icons.Default.Face,
        contentDescription = "Face Icon",
        modifier = Modifier.size(iconSize),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

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
 * Action buttons component - Responsive layout
 */
@Composable
private fun ActionButtons(
    onEnroll: () -> Unit,
    onVerify: () -> Unit,
    isVertical: Boolean = false,
    buttonSize: Dp = UIDimens.ButtonHeightKiosk
) {
    if (isVertical) {
        Column(
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            ActionButton(
                text = KioskConfig.ENROLL_BUTTON,
                icon = Icons.Default.PersonAdd,
                onClick = onEnroll,
                buttonHeight = buttonSize
            )

            ActionButton(
                text = KioskConfig.VERIFY_BUTTON,
                icon = Icons.Default.VerifiedUser,
                onClick = onVerify,
                buttonHeight = buttonSize
            )
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingLarge)
        ) {
            ActionButton(
                text = KioskConfig.ENROLL_BUTTON,
                icon = Icons.Default.PersonAdd,
                onClick = onEnroll,
                buttonHeight = buttonSize
            )

            ActionButton(
                text = KioskConfig.VERIFY_BUTTON,
                icon = Icons.Default.VerifiedUser,
                onClick = onVerify,
                buttonHeight = buttonSize
            )
        }
    }
}

/**
 * Reusable action button component - Responsive
 */
@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    buttonHeight: Dp = UIDimens.ButtonHeightKiosk
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(
            width = UIDimens.ButtonWidthKiosk,
            height = buttonHeight
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(UIDimens.IconMedium)
            )
            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Enrollment Screen - With proper state management and responsive layout
 */
@Composable
fun EnrollScreen(
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    val enrollmentData by viewModel.enrollmentData.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val sizes = com.fivucsas.desktop.ui.theme.getResponsiveSizes(maxWidth)
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = sizes.horizontalPadding,
                vertical = sizes.verticalPadding
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = KioskConfig.ENROLL_TITLE,
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(sizes.spacing))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(sizes.cardWidth)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(sizes.spacing)) {
                        if (uiState.isLoading) {
                            LoadingIndicator()
                        } else {
                            EnrollmentForm(
                                enrollmentData = enrollmentData,
                                onFullNameChange = viewModel::updateFullName,
                                onEmailChange = viewModel::updateEmail,
                                onIdNumberChange = viewModel::updateIdNumber
                            )

                            Spacer(modifier = Modifier.height(sizes.spacing))

                            if (uiState.showCamera) {
                                CameraSection(
                                    onCapture = { imageBytes ->
                                        viewModel.setCapturedImage(imageBytes)
                                    },
                                    onClose = viewModel::closeCamera
                                )
                            } else if (uiState.capturedImage != null) {
                                CapturedImagePreview(
                                    onRetake = viewModel::openCamera
                                )
                            } else {
                                BiometricCaptureSection()
                            }

                            Spacer(modifier = Modifier.height(sizes.spacing))

                            uiState.successMessage?.let { message ->
                                SuccessMessage(message)
                                Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
                            }

                            uiState.errorMessage?.let { message ->
                                ErrorMessage(message)
                                Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
                            }

                            EnrollmentActions(
                                onBack = onBack,
                                onEnroll = {
                                    if (uiState.capturedImage == null) {
                                        viewModel.openCamera()
                                    } else {
                                        viewModel.submitEnrollment()
                                    }
                                },
                                isValid = enrollmentData.fullName.isNotBlank() && 
                                          enrollmentData.email.isNotBlank() && 
                                          enrollmentData.idNumber.isNotBlank(),
                                hasImage = uiState.capturedImage != null,
                                buttonText = if (uiState.capturedImage == null) "Capture Photo" else "Submit Enrollment"
                            )
                        }
                    }
                }
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

    Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

    ValidatedTextField(
        value = enrollmentData.fullName,
        onValueChange = onFullNameChange,
        label = "Full Name",
        isRequired = true
    )

    Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

    ValidatedTextField(
        value = enrollmentData.email,
        onValueChange = onEmailChange,
        label = "Email",
        isRequired = true,
        keyboardType = KeyboardType.Email
    )

    Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

    ValidatedTextField(
        value = enrollmentData.idNumber,
        onValueChange = onIdNumberChange,
        label = "ID Number",
        isRequired = true,
        keyboardType = KeyboardType.Number
    )
}

/**
 * Validated text field component - Modern design with icons
 */
@Composable
private fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val icon = when (label) {
        "Full Name" -> Icons.Default.Person
        "Email" -> Icons.Default.Email
        "ID Number" -> Icons.Default.Badge
        else -> null
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label + if (isRequired) " *" else "") },
        leadingIcon = if (icon != null) {
            { Icon(icon, contentDescription = null) }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1976D2),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFAFAFA),
            focusedLabelColor = Color(0xFF1976D2),
            unfocusedLabelColor = Color.Gray,
            focusedLeadingIconColor = Color(0xFF1976D2),
            unfocusedLeadingIconColor = Color.Gray
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isRequired && value.isBlank(),
        supportingText = {
            if (isRequired && value.isBlank()) {
                Text("This field is required", color = Color(0xFFF44336))
            }
        },
        singleLine = true
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

    Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(UIDimens.CameraPreviewHeight),
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
                modifier = Modifier.size(UIDimens.IconXLarge),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
            Text(
                "Click button below to capture photo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enrollment action buttons - Modern Gradient Submit Button
 */
@Composable
private fun EnrollmentActions(
    onBack: () -> Unit,
    onEnroll: () -> Unit,
    isValid: Boolean,
    hasImage: Boolean = false,
    buttonText: String = "Start Enrollment"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Cancel", fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onEnroll,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            enabled = isValid,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isValid) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1976D2),
                                    Color(0xFF1565C0)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFBDBDBD),
                                    Color(0xFF9E9E9E)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Loading indicator component - Modern spinner
 */
@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF1976D2),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Processing...",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * Success message component - Modern gradient design
 */
@Composable
private fun SuccessMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                message,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Error message component - Prominent error display
 */
@Composable
private fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "Error",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    message,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Camera section for capturing photo - With REAL CAMERA
 */
@Composable
private fun CameraSection(
    onCapture: (ByteArray) -> Unit,
    onClose: () -> Unit
) {
    val cameraService = remember { com.fivucsas.desktop.data.DesktopCameraService() }
    
    com.fivucsas.desktop.ui.components.CameraPreview(
        cameraService = cameraService,
        onCapture = { imageBytes ->
            onCapture(imageBytes)  // Pass real image bytes
        },
        onClose = onClose
    )
}

/**
 * Captured image preview
 */
@Composable
private fun CapturedImagePreview(
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Step 2: Photo Captured",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(UIDimens.SpacingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Captured",
                    modifier = Modifier.size(UIDimens.IconXLarge),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))
                Text(
                    "Photo captured successfully!",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))
                OutlinedButton(onClick = onRetake) {
                    Text("Retake Photo")
                }
            }
        }
    }
}

/**
 * Verification Screen - Modern Beautiful Design
 */
@Composable
fun VerifyScreen(
    onBack: () -> Unit,
    viewModel: KioskViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Identity Verification",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoading) {
                        // Modern Loading State
                        ModernLoadingIndicator()
                    } else if (uiState.verificationResult != null) {
                        // Beautiful Result Display
                        BeautifulVerificationResult(
                            result = uiState.verificationResult!!,
                            onDone = onBack,
                            onTryAgain = {
                                viewModel.closeCamera()
                                viewModel.openCamera()
                            }
                        )
                    } else {
                        // Verification Process
                        if (uiState.showCamera) {
                            CameraSection(
                                onCapture = { imageBytes ->
                                    viewModel.setCapturedImage(imageBytes)
                                },
                                onClose = viewModel::closeCamera
                            )
                        } else if (uiState.capturedImage != null) {
                            // Photo Captured - Show Verify Button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Captured",
                                    modifier = Modifier.size(80.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    "Photo Captured Successfully!",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF212121)
                                    )
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Ready to verify your identity",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF757575)
                                    )
                                )
                                Spacer(Modifier.height(32.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::openCamera,
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(28.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, null, Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Retake")
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.verifyWithCapturedImage() },
                                        modifier = Modifier
                                            .width(200.dp)
                                            .height(56.dp)
                                            .shadow(8.dp, RoundedCornerShape(28.dp)),
                                        shape = RoundedCornerShape(28.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF00ACC1),
                                                            Color(0xFF0097A7)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.VerifiedUser,
                                                    null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    "Verify Now",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Initial State - Start Verification
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.size(120.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF00ACC1),
                                                        Color(0xFF0097A7)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Videocam,
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(32.dp))
                                
                                Text(
                                    "Capture Your Face",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF212121)
                                    )
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                Text(
                                    "Position your face in front of the camera",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF757575)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(Modifier.height(8.dp))
                                
                                Text(
                                    "Make sure you are in a well-lit area",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF9E9E9E)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(Modifier.height(48.dp))
                                
                                Button(
                                    onClick = viewModel::openCamera,
                                    modifier = Modifier
                                        .width(250.dp)
                                        .height(64.dp)
                                        .shadow(12.dp, RoundedCornerShape(32.dp)),
                                    shape = RoundedCornerShape(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF00ACC1),
                                                        Color(0xFF0097A7)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Start Verification",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Error Message
                        uiState.errorMessage?.let { message ->
                            Spacer(Modifier.height(24.dp))
                            ErrorMessage(message)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Beautiful Verification Result Display - Modern Success/Failure Cards
 */
@Composable
private fun BeautifulVerificationResult(
    result: VerificationResult,
    onDone: () -> Unit,
    onTryAgain: () -> Unit
) {
    val isSuccess = result.isVerified
    val backgroundColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
    val accentColor = if (isSuccess) Color(0xFF66BB6A) else Color(0xFFEF5350)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Icon Circle
        Card(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                backgroundColor,
                                accentColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.VerifiedUser else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Result Title
        Text(
            text = if (isSuccess) "✅ Verified!" else "❌ Verification Failed",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
        )
        
        Spacer(Modifier.height(24.dp))
        
        if (isSuccess) {
            // Success Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF1F8E9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Welcome Back!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF33691E)
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // User Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = Color(0xFF689F38),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            result.userName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF212121)
                            )
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Confidence Score
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            null,
                            tint = Color(0xFF689F38),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Confidence: ${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF424242)
                            )
                        )
                    }
                    
                    // Progress Bar for Confidence
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = result.confidence,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFDCEDC8)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Message
                    Text(
                        result.message,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF616161),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Success Action Button
            Button(
                onClick = onDone,
                modifier = Modifier
                    .width(240.dp)
                    .height(60.dp)
                    .shadow(8.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF66BB6A)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Done",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        } else {
            // Failure Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Unable to Verify",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Reason
                    Text(
                        result.message,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF616161),
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Confidence Score (if any)
                    if (result.confidence > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Match: ${(result.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF424242)
                                )
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = result.confidence,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFF44336),
                            trackColor = Color(0xFFFFCDD2)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Failure Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier
                        .width(160.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(2.dp, Color(0xFF757575))
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Button(
                    onClick = onTryAgain,
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFF44336),
                                        Color(0xFFEF5350)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Try Again",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Loading Indicator for Verification
 */
@Composable
private fun ModernLoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            color = Color(0xFF00ACC1),
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Verifying Identity...",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Please wait while we process your biometric data",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF757575)
            ),
            textAlign = TextAlign.Center
        )
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

    Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

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
        Column(modifier = Modifier.padding(UIDimens.SpacingMedium)) {
            PuzzleStep(
                number = 1,
                description = "Smile",
                isComplete = true
            )
            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

            PuzzleStep(
                number = 2,
                description = "Blink both eyes",
                isComplete = false
            )
            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

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
        Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
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
    onBack: () -> Unit,
    onVerify: () -> Unit,
    hasImage: Boolean,
    buttonText: String
) {
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
            onClick = onVerify,
            modifier = Modifier.weight(1f)
        ) {
            Text(buttonText)
        }
    }
}

/**
 * Verification result card
 */
@Composable
private fun VerificationResultCard(result: VerificationResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (result.isVerified)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(UIDimens.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (result.isVerified) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = if (result.isVerified) "Verified" else "Not Verified",
                modifier = Modifier.size(UIDimens.IconXLarge),
                tint = if (result.isVerified)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
            
            Text(
                text = if (result.isVerified) "✅ Verified" else "❌ Not Verified",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))
            
            Text(
                text = "User: ${result.userName}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Confidence: ${result.confidence.toInt()}%",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// KioskScreen now imported from shared module
// ✅ Removed local definition - using com.fivucsas.shared.presentation.state.KioskScreen
