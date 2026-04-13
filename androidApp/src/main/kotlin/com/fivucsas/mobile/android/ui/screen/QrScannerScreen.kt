package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.AndroidCameraService
import com.fivucsas.shared.platform.CameraState
import com.fivucsas.shared.platform.LensFacing
import com.fivucsas.shared.ui.platform.AndroidCameraPreview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

/**
 * QrScannerScreen
 *
 * A general-purpose QR code scanning screen backed by CameraX + ML Kit.
 * Used in the MFA flow to let the user scan a QR code presented on a
 * web browser or desktop client.
 *
 * @param onQrScanned Called once with the raw QR payload when a QR code is detected.
 * @param onBack Called when the user presses the back button.
 * @param title Optional top-bar title override.
 * @param instruction Optional instruction text shown above the viewfinder.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit,
    title: String = s(StringKey.MFA_METHOD_QR_CODE),
    instruction: String = s(StringKey.MFA_POINT_CAMERA_AT_QR)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val cameraService = remember(context, lifecycleOwner) {
        AndroidCameraService(context, lifecycleOwner)
    }
    val cameraState by cameraService.cameraState.collectAsState()

    // Guard against firing onQrScanned multiple times for the same detection burst
    var scanConsumed by remember { mutableStateOf(false) }

    val barcodeScanner = remember { BarcodeScanning.getClient() }

    // Initialize camera + barcode analysis once permission is granted
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            cameraService.initialize(LensFacing.BACK)
            cameraService.startPreview()
            cameraService.setFrameAnalyzer(
                executor = ContextCompat.getMainExecutor(context)
            ) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null || scanConsumed) {
                    imageProxy.close()
                    return@setFrameAnalyzer
                }

                val inputImage = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        if (scanConsumed) return@addOnSuccessListener
                        val rawValue = barcodes.firstOrNull {
                            it.format == Barcode.FORMAT_QR_CODE && !it.rawValue.isNullOrBlank()
                        }?.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            scanConsumed = true
                            onQrScanned(rawValue)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        } else if (!permissionRequested) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraService.clearFrameAnalyzer()
            barcodeScanner.close()
            scope.launch {
                cameraService.stopPreview()
                cameraService.release()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = s(StringKey.MFA_BACK_TO_METHODS)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cameraPermissionState.status.isGranted) {
                // ── Camera viewfinder with framing guide ─────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.Black, RoundedCornerShape(16.dp))
                ) {
                    AndroidCameraPreview(
                        cameraService = cameraService,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black, RoundedCornerShape(16.dp))
                    )

                    // Semi-transparent dark overlay corners (top)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .align(Alignment.TopCenter)
                    )

                    // Semi-transparent dark overlay corners (bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .align(Alignment.BottomCenter)
                    )

                    // Center scan rectangle
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(220.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            )
                    )

                    // "Point camera at QR code" hint inside viewfinder
                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .padding(horizontal = 16.dp)
                    )

                    // Camera initializing state overlay
                    if (cameraState != CameraState.Previewing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (cameraState) {
                                    CameraState.Initializing -> "Initializing camera..."
                                    CameraState.Ready -> "Starting preview..."
                                    is CameraState.Error -> "Camera error"
                                    CameraState.Idle -> "Camera not initialized"
                                    CameraState.Capturing -> "Camera busy"
                                    CameraState.Released -> "Camera released"
                                    CameraState.Previewing -> ""
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // ── Permission denied state ───────────────────────────────────
                val permanentlyDenied = permissionRequested &&
                    !cameraPermissionState.status.shouldShowRationale

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.Black, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (permanentlyDenied) {
                                "Camera permission was denied. Enable it in Settings to scan QR codes."
                            } else {
                                "Camera permission is required to scan QR codes."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (permanentlyDenied) {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                    )
                                } else {
                                    permissionRequested = true
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Text(if (permanentlyDenied) "Open Settings" else "Grant Permission")
                        }
                    }
                }
            }
        }
    }
}
