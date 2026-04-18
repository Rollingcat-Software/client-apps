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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.fivucsas.authenticator.totp.OtpQrScanFilter
import com.fivucsas.authenticator.totp.OtpQrScanResult
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
 * OtpQrScannerScreen
 *
 * Camera-based scanner dedicated to the standalone TOTP Authenticator.
 * Unlike [QrScannerScreen] (which accepts any QR payload for the MFA web-login
 * flow), this screen enforces an `otpauth://` scheme denylist + full structural
 * validation via [OtpQrScanFilter]. Website URLs, Wi-Fi payloads, vCards, etc.
 * are rejected with a user-visible error so they never land in the TOTP vault.
 *
 * Pattern mirrors [QrScannerScreen]: CameraX preview via [AndroidCameraService],
 * ML Kit [BarcodeScanning] analyzer, Accompanist permission state, viewfinder
 * overlay, and the same initializing/permission-denied UI states.
 *
 * @param onAccepted Called once with the trimmed `otpauth://` URI when a valid
 *                   authenticator QR code is detected. Pass this to
 *                   `AuthenticatorViewModel.addFromUri(uri)`.
 * @param onBack     Called when the user cancels (back arrow / cancel button).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OtpQrScannerScreen(
    onAccepted: (String) -> Unit,
    onBack: () -> Unit,
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

    // Guard against firing onAccepted more than once per detection burst.
    var scanConsumed by remember { mutableStateOf(false) }

    // Transient error banner shown when we detect a QR that fails the denylist
    // or otpauth parse check. Cleared automatically by the next valid scan.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // TODO(i18n): promote to StringKey.OTP_SCAN_UNSUPPORTED once string table is updated.
    // Tracked in /tmp/i18n_agent_20E.txt (agent 20E).
    val invalidQrText = "Unsupported QR code"
    val barcodeScanner = remember { BarcodeScanning.getClient() }

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
                            it.format == Barcode.FORMAT_QR_CODE &&
                                !it.rawValue.isNullOrBlank()
                        }?.rawValue ?: return@addOnSuccessListener

                        when (val verdict = OtpQrScanFilter.accept(rawValue)) {
                            is OtpQrScanResult.Accepted -> {
                                scanConsumed = true
                                errorMessage = null
                                onAccepted(verdict.uri)
                            }
                            is OtpQrScanResult.Invalid -> {
                                // Non-otpauth scheme or malformed URI — show
                                // error, but keep scanning so user can retry.
                                errorMessage = invalidQrText
                            }
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
                // TODO(i18n): promote to StringKey.OTP_SCAN_TITLE once string table is updated.
                title = { Text("Scan authenticator QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s(StringKey.BACK)
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
                // TODO(i18n): promote to StringKey.OTP_SCAN_HINT once string table is updated.
                text = "Point the camera at an otpauth:// QR code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cameraPermissionState.status.isGranted) {
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

                    // Top dark band
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .align(Alignment.TopCenter)
                    )
                    // Bottom dark band
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .align(Alignment.BottomCenter)
                    )

                    // Viewfinder rectangle
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

                    // Inline error banner when a non-otpauth QR was scanned.
                    errorMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer
                                        .copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

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

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBack) {
                    Text(s(StringKey.CANCEL))
                }
            } else {
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
