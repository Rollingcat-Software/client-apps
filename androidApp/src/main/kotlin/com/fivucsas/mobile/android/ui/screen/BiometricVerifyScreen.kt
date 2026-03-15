package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.fivucsas.shared.domain.model.GuestFaceCheckOutcome
import com.fivucsas.shared.domain.model.confidenceToBand
import com.fivucsas.shared.presentation.state.BiometricResult
import com.fivucsas.shared.presentation.state.BiometricState
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BiometricVerifyScreen(
    userId: String,
    viewModel: BiometricViewModel,
    guestMode: Boolean = false,
    onGuestResult: ((GuestFaceCheckOutcome, com.fivucsas.shared.domain.model.ConfidenceBand?) -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

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
        viewModel.clearState()
        if (!cameraPermissionState.status.isGranted) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(state.result, state.isSuccess, guestMode) {
        if (!guestMode || !state.isSuccess) return@LaunchedEffect
        val result = (state.result as? BiometricResult.VerificationSuccess)?.result ?: return@LaunchedEffect
        val outcome = if (result.isVerified) {
            GuestFaceCheckOutcome.FOUND
        } else {
            GuestFaceCheckOutcome.NOT_FOUND
        }
        onGuestResult?.invoke(outcome, confidenceToBand(result.confidence))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Face Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (cameraPermissionState.status.isGranted) {
                VerificationCameraContent(
                    cameraController = cameraController,
                    lifecycleOwner = lifecycleOwner,
                    biometricState = state,
                    guestMode = guestMode,
                    onCapture = {
                        cameraController.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val imageBytes = image.toCompressedJpegBytes()
                                    image.close()
                                    scope.launch {
                                        viewModel.verifyFace(imageBytes)
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    viewModel.onCaptureError(
                                        "Failed to capture image: ${exception.message}"
                                    )
                                }
                            }
                        )
                    },
                    onRetry = { viewModel.clearState() },
                    onDone = onNavigateBack
                )
            } else {
                val permanentlyDenied = permissionRequested
                        && !cameraPermissionState.status.shouldShowRationale

                VerificationPermissionContent(
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
    }
}

// ---------------------------------------------------------------------------
// Camera + verification overlay
// ---------------------------------------------------------------------------

@Composable
private fun VerificationCameraContent(
    cameraController: LifecycleCameraController,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    biometricState: BiometricState,
    guestMode: Boolean,
    onCapture: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Instructions overlay
        if (!biometricState.isSuccess && biometricState.error == null && !biometricState.isLoading) {
            VerificationInstructionsCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }

        // Loading overlay
        if (biometricState.isLoading) {
            VerificationLoadingCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }

        // Face guide frame
        if (!biometricState.isSuccess && !biometricState.isLoading) {
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
            // Error state
        if (biometricState.error != null) {
            val displayMessage = if (guestMode) {
                "Could not reach face-check service. Please check backend/DB connection and try again."
            } else {
                biometricState.error!!
            }
            VerificationErrorCard(
                message = displayMessage,
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
            }

            // Verification result
            if (biometricState.isSuccess && biometricState.result != null) {
                VerificationResultCard(
                    result = biometricState.result as? BiometricResult.VerificationSuccess,
                    guestMode = guestMode,
                    onRetry = onRetry,
                    onDone = onDone
                )
            }

            // Capture button (idle state)
            if (!biometricState.isSuccess && biometricState.error == null) {
                VerificationCaptureButton(
                    isLoading = biometricState.isLoading,
                    onClick = onCapture
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Verification result card
// ---------------------------------------------------------------------------

@Composable
private fun VerificationResultCard(
    result: BiometricResult.VerificationSuccess?,
    guestMode: Boolean,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    val isVerified = result?.result?.isVerified ?: false

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isVerified)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isVerified)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isVerified) {
                    if (guestMode) "FOUND" else "Verified Successfully"
                } else {
                    if (guestMode) "NOT FOUND" else "Verification Failed"
                },
                color = if (isVerified)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (guestMode) {
                    "No personal identity details are shown in guest mode."
                } else {
                    result?.result?.message ?: "Verification complete"
                },
                color = if (isVerified)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if (isVerified) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Confidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${((result?.result?.confidence ?: 0f) * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (!guestMode) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "User ID",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = result?.result?.userId ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Try Again")
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Small composables
// ---------------------------------------------------------------------------

@Composable
private fun VerificationInstructionsCard(modifier: Modifier = Modifier) {
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
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verify Your Identity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Position face in frame\n• Look directly at camera\n• Stay still during capture",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VerificationLoadingCard(modifier: Modifier = Modifier) {
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
                text = "Verifying...",
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
private fun VerificationErrorCard(message: String, modifier: Modifier = Modifier) {
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
private fun VerificationCaptureButton(isLoading: Boolean, onClick: () -> Unit) {
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
            Text("Verifying...", style = MaterialTheme.typography.titleMedium)
        } else {
            Text(
                "Verify Face",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Permission content
// ---------------------------------------------------------------------------

@Composable
private fun VerificationPermissionContent(
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
            imageVector = Icons.Default.Face,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (permanentlyDenied) {
                "Camera permission was permanently denied. Please enable it in app settings to continue."
            } else {
                "We need camera access to verify your identity using face recognition."
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
