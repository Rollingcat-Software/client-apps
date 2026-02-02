package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fivucsas.mobile.android.ui.util.toCompressedJpegBytes
import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricResult
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricState
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Step tracking
// ---------------------------------------------------------------------------

private const val STEP_CARD_CAPTURE = "CARD_CAPTURE"
private const val STEP_FORM = "FORM"
private const val STEP_CAPTURE = "CAPTURE"
private const val STEP_PREVIEW = "PREVIEW"

// ---------------------------------------------------------------------------
// Form state (local UI concern – not persisted in ViewModel)
// ---------------------------------------------------------------------------

private data class EnrollmentFormState(
    val fullName: String = "",
    val email: String = "",
    val idNumber: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val idNumberError: String? = null,
    val phoneNumberError: String? = null,
    val addressError: String? = null,
)

// ---------------------------------------------------------------------------
// Main Screen – orchestrates Form → Capture → Preview → Success
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricEnrollScreen(
    userId: String,
    viewModel: BiometricViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var currentStep by rememberSaveable { mutableStateOf(STEP_CARD_CAPTURE) }
    var formState by remember { mutableStateOf(EnrollmentFormState()) }
    var capturedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var cardFrontBytes by remember { mutableStateOf<ByteArray?>(null) }
    var cardBackBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(Unit) { viewModel.clearState() }

    Scaffold(
        topBar = {
            EnrollmentTopBar(
                currentStep = currentStep,
                isSuccess = state.isSuccess,
                onBack = {
                    when {
                        state.isSuccess -> onNavigateBack()
                        currentStep == STEP_PREVIEW -> {
                            capturedImageBytes = null
                            currentStep = STEP_CAPTURE
                            viewModel.clearState()
                        }
                        currentStep == STEP_CAPTURE -> {
                            currentStep = STEP_FORM
                            viewModel.clearState()
                        }
                        currentStep == STEP_FORM -> {
                            currentStep = STEP_CARD_CAPTURE
                        }
                        else -> onNavigateBack()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isSuccess && state.result != null -> {
                    EnrollmentSuccessContent(
                        result = state.result as? BiometricResult.EnrollmentSuccess,
                        onDone = onNavigateBack
                    )
                }
                currentStep == STEP_PREVIEW && capturedImageBytes != null -> {
                    PhotoPreviewContent(
                        imageBytes = capturedImageBytes!!,
                        enrollmentData = formState.toEnrollmentData(),
                        viewModel = viewModel,
                        biometricState = state,
                        onRetake = {
                            capturedImageBytes = null
                            currentStep = STEP_CAPTURE
                            viewModel.clearState()
                        }
                    )
                }
                currentStep == STEP_CAPTURE -> {
                    FaceCaptureContent(
                        onPhotoCaptured = { bytes ->
                            capturedImageBytes = bytes
                            currentStep = STEP_PREVIEW
                        },
                        onCaptureError = { message ->
                            viewModel.onCaptureError(message)
                        },
                        biometricState = state,
                        onRetry = { viewModel.clearState() }
                    )
                }
                currentStep == STEP_CARD_CAPTURE -> {
                    CardCaptureContent(
                        onCardsCaptured = { front, back ->
                            cardFrontBytes = front
                            cardBackBytes = back
                            currentStep = STEP_FORM
                        },
                        onSkip = {
                            currentStep = STEP_FORM
                        }
                    )
                }
                else -> {
                    EnrollmentFormContent(
                        formState = formState,
                        onFormStateChange = { formState = it },
                        onProceedToCapture = { currentStep = STEP_CAPTURE },
                        cardFrontBytes = cardFrontBytes,
                        cardBackBytes = cardBackBytes
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Top App Bar
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentTopBar(
    currentStep: String,
    isSuccess: Boolean,
    onBack: () -> Unit
) {
    val title = when {
        isSuccess -> "Enrollment Complete"
        currentStep == STEP_PREVIEW -> "Review Photo"
        currentStep == STEP_CAPTURE -> "Capture Face"
        currentStep == STEP_CARD_CAPTURE -> "Scan ID Card"
        else -> "Face Enrollment"
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ---------------------------------------------------------------------------
// Step 1 – Enrollment Form
// ---------------------------------------------------------------------------

@Composable
private fun EnrollmentFormContent(
    formState: EnrollmentFormState,
    onFormStateChange: (EnrollmentFormState) -> Unit,
    onProceedToCapture: () -> Unit,
    cardFrontBytes: ByteArray? = null,
    cardBackBytes: ByteArray? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormHeaderCard()

        if (cardFrontBytes != null || cardBackBytes != null) {
            CardScannedSummary(
                frontBytes = cardFrontBytes,
                backBytes = cardBackBytes
            )
        }

        OutlinedTextField(
            value = formState.fullName,
            onValueChange = {
                onFormStateChange(formState.copy(fullName = it, fullNameError = null))
            },
            label = { Text("Full Name") },
            placeholder = { Text("e.g. Ahmet Yilmaz") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = formState.fullNameError != null,
            supportingText = formState.fullNameError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                autoCorrect = false
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = formState.email,
            onValueChange = {
                onFormStateChange(formState.copy(email = it, emailError = null))
            },
            label = { Text("Email") },
            placeholder = { Text("e.g. ahmet@example.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            isError = formState.emailError != null,
            supportingText = formState.emailError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = formState.idNumber,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(11)
                onFormStateChange(formState.copy(idNumber = filtered, idNumberError = null))
            },
            label = { Text("National ID (TC)") },
            placeholder = { Text("11 digit TC number (optional)") },
            leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
            isError = formState.idNumberError != null,
            supportingText = formState.idNumberError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = formState.phoneNumber,
            onValueChange = {
                onFormStateChange(formState.copy(phoneNumber = it, phoneNumberError = null))
            },
            label = { Text("Phone Number") },
            placeholder = { Text("e.g. +905551234567 (optional)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            isError = formState.phoneNumberError != null,
            supportingText = formState.phoneNumberError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = formState.address,
            onValueChange = {
                onFormStateChange(formState.copy(address = it, addressError = null))
            },
            label = { Text("Address") },
            placeholder = { Text("Home address (optional)") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            isError = formState.addressError != null,
            supportingText = formState.addressError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Validation runs on click but never blocks — errors are advisory.
        // This allows development/testing without filling every field.
        Button(
            onClick = {
                val validated = validateForm(formState)
                onFormStateChange(validated)
                onProceedToCapture()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Proceed to Face Capture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FormHeaderCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Face,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "Step 2: Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Fill in your details below, then proceed to face capture",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Form validation – reuses shared ValidationRules
// ---------------------------------------------------------------------------

/**
 * Validates filled fields only — blank fields are accepted.
 * This makes all fields optional for development/testing while
 * still catching malformed input (e.g. invalid email format).
 */
private fun validateForm(state: EnrollmentFormState): EnrollmentFormState {
    val nameResult = if (state.fullName.isNotBlank()) {
        ValidationRules.validateFullName(state.fullName)
    } else {
        ValidationResult.Success
    }

    val emailResult = if (state.email.isNotBlank()) {
        ValidationRules.validateEmail(state.email)
    } else {
        ValidationResult.Success
    }

    val idResult = if (state.idNumber.isNotBlank()) {
        ValidationRules.validateNationalId(state.idNumber)
    } else {
        ValidationResult.Success
    }

    val phoneResult = if (state.phoneNumber.isNotBlank()) {
        ValidationRules.validatePhoneNumber(state.phoneNumber)
    } else {
        ValidationResult.Success
    }

    val addressResult = if (state.address.isNotBlank()) {
        ValidationRules.validateAddress(state.address)
    } else {
        ValidationResult.Success
    }

    return state.copy(
        fullNameError = nameResult.errorMessage,
        emailError = emailResult.errorMessage,
        idNumberError = idResult.errorMessage,
        phoneNumberError = phoneResult.errorMessage,
        addressError = addressResult.errorMessage
    )
}

private fun EnrollmentFormState.toEnrollmentData(): EnrollmentData {
    return EnrollmentData(
        fullName = fullName.trim(),
        email = email.trim().lowercase(),
        idNumber = idNumber.trim(),
        phoneNumber = phoneNumber.trim(),
        address = address.trim()
    )
}

// ---------------------------------------------------------------------------
// Card Scanned Summary (shown at top of form when card images exist)
// ---------------------------------------------------------------------------

@Composable
private fun CardScannedSummary(
    frontBytes: ByteArray?,
    backBytes: ByteArray?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ID Card Scanned",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    buildString {
                        if (frontBytes != null) append("Front")
                        if (frontBytes != null && backBytes != null) append(" + ")
                        if (backBytes != null) append("Back")
                        append(" captured")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (frontBytes != null) {
                    val frontBitmap = remember(frontBytes) {
                        BitmapFactory.decodeByteArray(frontBytes, 0, frontBytes.size)
                    }
                    if (frontBitmap != null) {
                        Image(
                            bitmap = frontBitmap.asImageBitmap(),
                            contentDescription = "Front of ID card",
                            modifier = Modifier
                                .height(40.dp)
                                .aspectRatio(1.586f)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                if (backBytes != null) {
                    val backBitmap = remember(backBytes) {
                        BitmapFactory.decodeByteArray(backBytes, 0, backBytes.size)
                    }
                    if (backBitmap != null) {
                        Image(
                            bitmap = backBitmap.asImageBitmap(),
                            contentDescription = "Back of ID card",
                            modifier = Modifier
                                .height(40.dp)
                                .aspectRatio(1.586f)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 1 – Card Capture (photograph front + back of ID card)
// ---------------------------------------------------------------------------

private const val CARD_PHASE_CAPTURE_FRONT = "CAPTURE_FRONT"
private const val CARD_PHASE_PREVIEW_FRONT = "PREVIEW_FRONT"
private const val CARD_PHASE_CAPTURE_BACK = "CAPTURE_BACK"
private const val CARD_PHASE_PREVIEW_BACK = "PREVIEW_BACK"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CardCaptureContent(
    onCardsCaptured: (front: ByteArray, back: ByteArray) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var phase by rememberSaveable { mutableStateOf(CARD_PHASE_CAPTURE_FRONT) }
    var frontBytes by remember { mutableStateOf<ByteArray?>(null) }
    var backBytes by remember { mutableStateOf<ByteArray?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraController.unbind() }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!cameraPermissionState.status.isGranted) {
            val permanentlyDenied = permissionRequested
                    && !cameraPermissionState.status.shouldShowRationale
            CameraPermissionContent(
                permanentlyDenied = permanentlyDenied,
                onRequestPermission = {
                    permissionRequested = true
                    cameraPermissionState.launchPermissionRequest()
                },
                onOpenSettings = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }
            )
        } else {
            when (phase) {
                CARD_PHASE_PREVIEW_FRONT -> {
                    CardPreviewContent(
                        imageBytes = frontBytes!!,
                        sideLabel = "Front of ID Card",
                        onRetake = {
                            frontBytes = null
                            phase = CARD_PHASE_CAPTURE_FRONT
                        },
                        onConfirm = { phase = CARD_PHASE_CAPTURE_BACK }
                    )
                }
                CARD_PHASE_CAPTURE_BACK -> {
                    CardCaptureViewfinder(
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        sideLabel = "back",
                        onCapture = {
                            cameraController.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        backBytes = image.toCompressedJpegBytes()
                                        image.close()
                                        phase = CARD_PHASE_PREVIEW_BACK
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        // Stay on capture screen so user can retry
                                    }
                                }
                            )
                        },
                        onSkip = onSkip
                    )
                }
                CARD_PHASE_PREVIEW_BACK -> {
                    CardPreviewContent(
                        imageBytes = backBytes!!,
                        sideLabel = "Back of ID Card",
                        onRetake = {
                            backBytes = null
                            phase = CARD_PHASE_CAPTURE_BACK
                        },
                        onConfirm = { onCardsCaptured(frontBytes!!, backBytes!!) }
                    )
                }
                else -> {
                    // CARD_PHASE_CAPTURE_FRONT (default)
                    CardCaptureViewfinder(
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        sideLabel = "front",
                        onCapture = {
                            cameraController.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        frontBytes = image.toCompressedJpegBytes()
                                        image.close()
                                        phase = CARD_PHASE_PREVIEW_FRONT
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        // Stay on capture screen so user can retry
                                    }
                                }
                            )
                        },
                        onSkip = onSkip
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Card Capture Viewfinder — camera preview with rectangular card guide
// ---------------------------------------------------------------------------

@Composable
private fun CardCaptureViewfinder(
    cameraController: LifecycleCameraController,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    sideLabel: String,
    onCapture: () -> Unit,
    onSkip: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Header card
        CardCaptureHeaderCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // Instruction text
        Text(
            text = "Place the $sideLabel of your ID card within the frame",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .padding(top = 160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Rectangular card guide (credit-card aspect ratio ~1.586:1)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .aspectRatio(1.586f)
                .border(
                    BorderStroke(
                        3.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCapture,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Capture ${sideLabel.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Skip Card Scan")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Card Capture Header Card
// ---------------------------------------------------------------------------

@Composable
private fun CardCaptureHeaderCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "Step 1: Scan Your ID Card",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Photograph the front and back of your ID card, or skip to enter details manually",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Card Preview — shows captured card image (front or back)
// ---------------------------------------------------------------------------

@Composable
private fun CardPreviewContent(
    imageBytes: ByteArray,
    sideLabel: String,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    val previewBitmap = remember(imageBytes) {
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        sideLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Review your captured image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = sideLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.586f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 3 – Face Capture (camera only, hands off bytes to preview)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FaceCaptureContent(
    onPhotoCaptured: (ByteArray) -> Unit,
    onCaptureError: (String) -> Unit,
    biometricState: BiometricState,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraController.unbind() }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraPreviewWithControls(
            cameraController = cameraController,
            lifecycleOwner = lifecycleOwner,
            biometricState = biometricState,
            onCapture = {
                cameraController.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val imageBytes = image.toCompressedJpegBytes()
                            image.close()
                            onPhotoCaptured(imageBytes)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onCaptureError(
                                "Failed to capture image: ${exception.message}"
                            )
                        }
                    }
                )
            },
            onRetry = onRetry
        )
    } else {
        val permanentlyDenied = permissionRequested
                && !cameraPermissionState.status.shouldShowRationale

        CameraPermissionContent(
            permanentlyDenied = permanentlyDenied,
            onRequestPermission = {
                permissionRequested = true
                cameraPermissionState.launchPermissionRequest()
            },
            onOpenSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Camera preview + overlay controls
// ---------------------------------------------------------------------------

@Composable
private fun CameraPreviewWithControls(
    cameraController: LifecycleCameraController,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    biometricState: BiometricState,
    onCapture: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Instructions overlay (shown when idle)
        if (!biometricState.isLoading && biometricState.error == null) {
            InstructionsCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }

        // Processing overlay
        if (biometricState.isLoading) {
            ProcessingOverlay(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }

        // Circular face guide
        if (!biometricState.isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(280.dp)
                    .border(
                        BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                        shape = CircleShape
                    )
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (biometricState.error != null) {
                ErrorCard(
                    message = biometricState.error!!,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Try Again")
                }
            } else {
                CaptureButton(
                    isLoading = biometricState.isLoading,
                    onClick = onCapture
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Small reusable composables
// ---------------------------------------------------------------------------

@Composable
private fun InstructionsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Face,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Position Your Face",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Ensure good lighting\n• Look directly at camera\n• Remove glasses if wearing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProcessingOverlay(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Processing Enrollment...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CaptureButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Processing...", style = MaterialTheme.typography.titleMedium)
        } else {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Capture Face",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 3 – Photo Preview (review before sending)
// ---------------------------------------------------------------------------

@Composable
private fun PhotoPreviewContent(
    imageBytes: ByteArray,
    enrollmentData: EnrollmentData,
    viewModel: BiometricViewModel,
    biometricState: BiometricState,
    onRetake: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Decode once and cache across recompositions
    val previewBitmap = remember(imageBytes) {
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        "Review Your Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Make sure your face is clearly visible and well-lit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Photo preview — mirrored horizontally to match what the user
        // saw in the front-camera preview. The non-mirrored bytes are
        // sent to the backend for correct biometric processing.
        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Captured face photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(20.dp))
                    .graphicsLayer { scaleX = -1f },
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Error from enrollment attempt
        if (biometricState.error != null) {
            ErrorCard(
                message = biometricState.error!!,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !biometricState.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.enrollFace(enrollmentData, imageBytes)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !biometricState.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (biometricState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Success content
// ---------------------------------------------------------------------------

@Composable
private fun EnrollmentSuccessContent(
    result: BiometricResult.EnrollmentSuccess?,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Enrollment Successful",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "User: ${result?.user?.name ?: "Unknown"}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Camera permission request / permanent denial
// ---------------------------------------------------------------------------

@Composable
private fun CameraPermissionContent(
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Face,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (permanentlyDenied) {
                "Camera permission was permanently denied. Please enable it in app settings to continue."
            } else {
                "We need camera access to capture your face for biometric enrollment."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (permanentlyDenied) {
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open App Settings")
            }
        } else {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Camera Permission")
            }
        }
    }
}
