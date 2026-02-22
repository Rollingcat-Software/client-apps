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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.platform.AndroidCameraService
import com.fivucsas.shared.platform.CameraState
import com.fivucsas.shared.platform.LensFacing
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginStatus
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginViewModel
import com.fivucsas.shared.ui.platform.AndroidCameraPreview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QRLoginScanScreen(
    onNavigateBack: () -> Unit,
    qrLoginViewModel: QrLoginViewModel = koinInject()
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
    val qrState by qrLoginViewModel.state.collectAsState()
    var manualQrPayload by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            cameraService.initialize(LensFacing.BACK)
            cameraService.startPreview()
        } else if (!permissionRequested) {
            permissionRequested = true
            cameraPermissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                cameraService.stopPreview()
                cameraService.release()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR to Login") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Point your camera at the QR code on desktop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Align the QR code inside the frame",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cameraPermissionState.status.isGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(Color.Black, RoundedCornerShape(16.dp))
                ) {
                    AndroidCameraPreview(
                        cameraService = cameraService,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black, RoundedCornerShape(16.dp))
                    )

                    // QR framing guide
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(240.dp)
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )

                    if (cameraState != CameraState.Previewing) {
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
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )
                    }
                }
            } else {
                val permanentlyDenied = permissionRequested &&
                    !cameraPermissionState.status.shouldShowRationale

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .background(Color.Black, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera permission required",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (permanentlyDenied) {
                                "Camera permission was denied. Enable it in settings to scan QR."
                            } else {
                                "Camera permission is required to scan QR."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Align the QR code inside the frame to continue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = manualQrPayload,
                onValueChange = { manualQrPayload = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Scanned QR Payload") },
                placeholder = { Text("fivucsas://qr-login?session=...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { qrLoginViewModel.submitMobileScan(manualQrPayload) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !qrState.isLoading && manualQrPayload.isNotBlank()
            ) {
                Text("Submit QR Payload")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when (qrState.status) {
                    QrLoginStatus.IDLE -> "Ready to scan QR from desktop/web"
                    QrLoginStatus.WAITING_FOR_MOBILE_SCAN -> "Waiting for camera scan..."
                    QrLoginStatus.WAITING_FOR_DESKTOP_APPROVAL -> "QR accepted. Waiting for desktop..."
                    QrLoginStatus.APPROVED -> "Login request approved. You can return."
                    QrLoginStatus.ERROR -> qrState.error ?: "QR login error"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (qrState.status == QrLoginStatus.ERROR) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )

            if (qrState.status == QrLoginStatus.APPROVED) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun QrLoginScanScreen(
    onNavigateBack: () -> Unit
) {
    QRLoginScanScreen(onNavigateBack = onNavigateBack)
}
