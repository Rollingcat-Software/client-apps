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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
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
import com.fivucsas.shared.presentation.viewmodel.CardDetectionViewModel
import com.fivucsas.shared.presentation.viewmodel.CardTypeLabels
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

private const val STEP_CAPTURE = "CAPTURE"
private const val STEP_RESULT = "RESULT"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CardDetectionScreen(
    viewModel: CardDetectionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by rememberSaveable { mutableStateOf(STEP_CAPTURE) }

    LaunchedEffect(Unit) { viewModel.reset() }
    LaunchedEffect(uiState.result) { if (uiState.result != null) currentStep = STEP_RESULT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentStep == STEP_RESULT) "Detection Result" else "Card Detection",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == STEP_RESULT) { currentStep = STEP_CAPTURE; viewModel.reset() }
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                currentStep == STEP_RESULT && uiState.result != null -> {
                    CardResultContent(uiState.result!!, uiState.capturedImageBytes,
                        onRetry = { currentStep = STEP_CAPTURE; viewModel.reset() },
                        onDone = onNavigateBack
                    )
                }
                uiState.isProcessing -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Detecting card...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth(0.7f))
                    }
                }
                else -> CardDetectionCaptureContent(viewModel, uiState.errorMessage)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CardDetectionCaptureContent(
    viewModel: CardDetectionViewModel,
    errorMessage: String?
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

    DisposableEffect(Unit) { onDispose { cameraController.unbind() } }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        val permanentlyDenied = permissionRequested && !cameraPermissionState.status.shouldShowRationale
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(24.dp))
            Text("Camera Permission Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                if (permanentlyDenied) "Enable camera in app settings."
                else "Camera is needed for card detection.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            if (permanentlyDenied) {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, null); Spacer(Modifier.width(8.dp)); Text("Open App Settings")
                }
            } else {
                Button(onClick = { permissionRequested = true; cameraPermissionState.launchPermissionRequest() }, Modifier.fillMaxWidth()) {
                    Text("Grant Camera Permission")
                }
            }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            Modifier.fillMaxSize()
        )

        // Header
        Card(
            Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                Column {
                    Text("Card Detection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Point camera at an ID card or document", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Card guide
        Box(
            Modifier.align(Alignment.Center).fillMaxWidth(0.85f).aspectRatio(1.586f)
                .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)), RoundedCornerShape(12.dp))
        )

        // Bottom controls
        Column(
            Modifier.align(Alignment.BottomCenter).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    cameraController.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bytes = image.toCompressedJpegBytes()
                                image.close()
                                viewModel.detectCard(bytes)
                            }
                            override fun onError(e: ImageCaptureException) { /* stay on capture */ }
                        }
                    )
                },
                Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Detect Card", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CardResultContent(
    result: com.fivucsas.shared.presentation.viewmodel.CardDetectionResult,
    imageBytes: ByteArray?,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageBytes?.let { bytes ->
            val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
            if (bitmap != null) {
                Image(
                    bitmap.asImageBitmap(), "Captured card",
                    Modifier.fillMaxWidth().aspectRatio(1.586f).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Card Detected", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.height(16.dp))
                CardDetailRow("Card Type (EN)", result.cardTypeLabel)
                CardDetailRow("Card Type (TR)", CardTypeLabels.getLabel(result.cardType, turkish = true))
                CardDetailRow("Confidence", "${(result.confidence * 100).toInt()}%")
                if (result.boundingBox.isNotEmpty()) {
                    CardDetailRow("Bounding Box", result.boundingBox.joinToString(", ") { "%.1f".format(it) })
                }
                if (result.message.isNotBlank()) CardDetailRow("Message", result.message)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onRetry, Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Scan Another", fontWeight = FontWeight.SemiBold)
            }
            Button(onDone, Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(8.dp)); Text("Done", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CardDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
