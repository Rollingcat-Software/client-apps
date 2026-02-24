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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fivucsas.mobile.android.ui.util.toCompressedJpegBytes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

// ---------------------------------------------------------------------------
// Step tracking
// ---------------------------------------------------------------------------

private const val STEP_CAPTURE_FRONT = "CAPTURE_FRONT"
private const val STEP_PREVIEW_FRONT = "PREVIEW_FRONT"
private const val STEP_CAPTURE_BACK = "CAPTURE_BACK"
private const val STEP_PREVIEW_BACK = "PREVIEW_BACK"
private const val STEP_SUCCESS = "SUCCESS"

// ---------------------------------------------------------------------------
// Main Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScanScreen(
    onNavigateBack: () -> Unit
) {
    var currentStep by rememberSaveable { mutableStateOf(STEP_CAPTURE_FRONT) }
    var frontBytes by remember { mutableStateOf<ByteArray?>(null) }
    var backBytes by remember { mutableStateOf<ByteArray?>(null) }

    val title = when (currentStep) {
        STEP_PREVIEW_FRONT -> "Review Front"
        STEP_CAPTURE_BACK -> "Scan Back"
        STEP_PREVIEW_BACK -> "Review Back"
        STEP_SUCCESS -> "Card Added"
        else -> "Scan ID Card"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentStep) {
                            STEP_SUCCESS -> onNavigateBack()
                            STEP_PREVIEW_BACK -> {
                                backBytes = null
                                currentStep = STEP_CAPTURE_BACK
                            }
                            STEP_CAPTURE_BACK -> currentStep = STEP_PREVIEW_FRONT
                            STEP_PREVIEW_FRONT -> {
                                frontBytes = null
                                currentStep = STEP_CAPTURE_FRONT
                            }
                            else -> onNavigateBack()
                        }
                    }) {
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
            when (currentStep) {
                STEP_SUCCESS -> {
                    CardSuccessContent(
                        frontBytes = frontBytes,
                        backBytes = backBytes,
                        onDone = onNavigateBack
                    )
                }
                STEP_PREVIEW_BACK -> {
                    CardPreviewContent(
                        imageBytes = backBytes!!,
                        sideLabel = "Back of ID Card",
                        onRetake = {
                            backBytes = null
                            currentStep = STEP_CAPTURE_BACK
                        },
                        onConfirm = { currentStep = STEP_SUCCESS }
                    )
                }
                STEP_CAPTURE_BACK -> {
                    CardCaptureContent(
                        sideLabel = "back",
                        onPhotoCaptured = { bytes ->
                            backBytes = bytes
                            currentStep = STEP_PREVIEW_BACK
                        },
                        onSkip = { currentStep = STEP_SUCCESS }
                    )
                }
                STEP_PREVIEW_FRONT -> {
                    CardPreviewContent(
                        imageBytes = frontBytes!!,
                        sideLabel = "Front of ID Card",
                        onRetake = {
                            frontBytes = null
                            currentStep = STEP_CAPTURE_FRONT
                        },
                        onConfirm = { currentStep = STEP_CAPTURE_BACK }
                    )
                }
                else -> {
                    CardCaptureContent(
                        sideLabel = "front",
                        onPhotoCaptured = { bytes ->
                            frontBytes = bytes
                            currentStep = STEP_PREVIEW_FRONT
                        },
                        onSkip = { currentStep = STEP_SUCCESS }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Card Capture — camera preview with rectangular card guide
// ---------------------------------------------------------------------------

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CardCaptureContent(
    sideLabel: String,
    onPhotoCaptured: (ByteArray) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
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
                            "Scan Your ID Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Photograph the $sideLabel of your ID card",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

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
                    onClick = {
                        cameraController.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bytes = image.toCompressedJpegBytes()
                                    image.close()
                                    onPhotoCaptured(bytes)
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    // Stay on capture screen so user can retry
                                }
                            }
                        )
                    },
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
                    Text("Skip")
                }
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
// Success — card added confirmation
// ---------------------------------------------------------------------------

@Composable
private fun CardSuccessContent(
    frontBytes: ByteArray?,
    backBytes: ByteArray?,
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
                    "Card Added Successfully",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        val parts = mutableListOf<String>()
                        if (frontBytes != null) parts.add("Front")
                        if (backBytes != null) parts.add("Back")
                        if (parts.isEmpty()) {
                            append("No card images captured (skipped)")
                        } else {
                            append(parts.joinToString(" & "))
                            append(" captured")
                        }
                    },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Show thumbnail previews
                if (frontBytes != null || backBytes != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        frontBytes?.let { bytes ->
                            val bitmap = remember(bytes) {
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                            if (bitmap != null) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Front",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Front of ID card",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.586f)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        backBytes?.let { bytes ->
                            val bitmap = remember(bytes) {
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                            if (bitmap != null) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Back",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Back of ID card",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.586f)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
            Icons.Default.CreditCard,
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
                "Camera permission was permanently denied. Please enable it in app settings to scan your ID card."
            } else {
                "We need camera access to photograph your ID card."
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
