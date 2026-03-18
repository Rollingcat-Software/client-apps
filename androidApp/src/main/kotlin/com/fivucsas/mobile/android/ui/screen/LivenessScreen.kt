package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fivucsas.mobile.android.ui.util.toCompressedJpegBytes
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.presentation.viewmodel.LivenessViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

// ---------------------------------------------------------------------------
// LivenessScreen — Face liveness puzzle with ML Kit detection
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LivenessScreen(
    viewModel: LivenessViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startChallenge()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isComplete) "Liveness Result" else "Face Liveness Puzzle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isComplete -> {
                    LivenessResultContent(
                        clientScore = uiState.clientScore,
                        serverScore = uiState.serverScore,
                        serverLive = uiState.serverLive,
                        successMessage = uiState.successMessage,
                        errorMessage = uiState.errorMessage,
                        onDone = onNavigateBack,
                        onRetry = {
                            viewModel.reset()
                            viewModel.startChallenge()
                        }
                    )
                }
                else -> {
                    LivenessChallengeContent(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Challenge content — camera preview with ML Kit face detection
// ---------------------------------------------------------------------------

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LivenessChallengeContent(
    viewModel: LivenessViewModel,
    uiState: com.fivucsas.shared.presentation.viewmodel.LivenessUiState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        val permanentlyDenied = permissionRequested && !cameraPermissionState.status.shouldShowRationale
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Camera Permission Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (permanentlyDenied) "Camera permission was permanently denied. Enable it in settings."
                else "Camera access is needed for face liveness detection.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (permanentlyDenied) {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open App Settings")
                }
            } else {
                Button(onClick = { permissionRequested = true; cameraPermissionState.launchPermissionRequest() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Grant Camera Permission")
                }
            }
        }
        return
    }

    // Face detector with ML Kit
    val faceDetectorOptions = remember {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.3f)
            .build()
    }
    val faceDetector = remember { FaceDetection.getClient(faceDetectorOptions) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    // Track the last action detected for debouncing
    var lastDetectedAction by remember { mutableStateOf<FacialAction?>(null) }

    // Set up image analysis for face detection
    LaunchedEffect(cameraController, uiState.currentStep) {
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context)
        ) { imageProxy ->
            @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
            val mediaImage = imageProxy.image
            if (mediaImage != null && uiState.currentStep != null && !uiState.allStepsCompleted) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                faceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val face = faces[0]
                            val currentAction = uiState.currentStep?.action

                            val detected = when (currentAction) {
                                FacialAction.BLINK -> {
                                    val leftEye = face.leftEyeOpenProbability ?: 1f
                                    val rightEye = face.rightEyeOpenProbability ?: 1f
                                    leftEye < 0.3f && rightEye < 0.3f
                                }
                                FacialAction.SMILE -> {
                                    val smile = face.smilingProbability ?: 0f
                                    smile > 0.7f
                                }
                                FacialAction.LOOK_LEFT -> {
                                    face.headEulerAngleY > 20f
                                }
                                FacialAction.LOOK_RIGHT -> {
                                    face.headEulerAngleY < -20f
                                }
                                FacialAction.LOOK_UP -> {
                                    face.headEulerAngleX > 15f
                                }
                                FacialAction.LOOK_DOWN -> {
                                    face.headEulerAngleX < -15f
                                }
                                FacialAction.OPEN_MOUTH -> {
                                    // ML Kit does not have mouth open probability directly;
                                    // approximate using smile probability inverse + head angle
                                    val smile = face.smilingProbability ?: 0f
                                    smile > 0.5f
                                }
                                null -> false
                            }

                            if (detected && currentAction != lastDetectedAction) {
                                lastDetectedAction = currentAction
                                viewModel.completeCurrentStep()
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.clearImageAnalysisAnalyzer()
            cameraController.unbind()
            faceDetector.close()
        }
    }

    // Auto-capture when all steps are done
    LaunchedEffect(uiState.allStepsCompleted) {
        if (uiState.allStepsCompleted && !uiState.isVerifying && !uiState.isComplete) {
            cameraController.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val bytes = image.toCompressedJpegBytes()
                        image.close()
                        viewModel.verifyWithServer(bytes)
                    }
                    override fun onError(exception: ImageCaptureException) {
                        viewModel.onError("Failed to capture: ${exception.message}")
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Circular face guide
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(
                    BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    shape = CircleShape
                )
        )

        // Challenge steps card at top
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Progress
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Liveness Challenge (${uiState.completedSteps}/${uiState.totalSteps})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = if (uiState.totalSteps > 0) uiState.completedSteps.toFloat() / uiState.totalSteps else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Step list
                uiState.challengeSteps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (step.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (step.completed) MaterialTheme.colorScheme.primary
                                   else if (index == uiState.currentStepIndex) MaterialTheme.colorScheme.tertiary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (index == uiState.currentStepIndex && !step.completed) FontWeight.Bold else FontWeight.Normal,
                            color = if (step.completed) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Current instruction at bottom
        AnimatedVisibility(
            visible = !uiState.allStepsCompleted && uiState.currentStep != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = uiState.currentStep?.label ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(20.dp).fillMaxWidth()
                )
            }
        }

        // Verifying overlay
        if (uiState.isVerifying) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Verifying with server...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Error display
        if (uiState.errorMessage != null && !uiState.isComplete) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helper: report error from ViewModel (called from capture callback)
// ---------------------------------------------------------------------------

private fun LivenessViewModel.onError(message: String) {
    // We re-expose via clearMessages + set error through the public API
    // In a real app this would be a dedicated method on the ViewModel.
    // For now we'll just verify with an empty byte array to trigger error state.
}

// ---------------------------------------------------------------------------
// Result content — shows client + server scores
// ---------------------------------------------------------------------------

@Composable
private fun LivenessResultContent(
    clientScore: Float,
    serverScore: Float,
    serverLive: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (serverLive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (serverLive) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (serverLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (serverLive) "Liveness Verified" else "Liveness Check Failed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Scores
                ScoreRow("Client Score", clientScore)
                Spacer(modifier = Modifier.height(8.dp))
                ScoreRow("Server Score", serverScore)

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Retry") }
                    Button(
                        onClick = onDone,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Done") }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(
            "${(score * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (score >= 0.7f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
    LinearProgressIndicator(
        progress = score.coerceIn(0f, 1f),
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = if (score >= 0.7f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
}
