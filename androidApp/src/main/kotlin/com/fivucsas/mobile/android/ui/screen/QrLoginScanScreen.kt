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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.core.content.ContextCompat
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.AndroidCameraService
import com.fivucsas.shared.platform.CameraState
import com.fivucsas.shared.platform.LensFacing
import com.fivucsas.shared.presentation.state.QrLoginStatus
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginViewModel
import com.fivucsas.shared.ui.platform.AndroidCameraPreview
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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
    var autoScanSubmitted by rememberSaveable { mutableStateOf(false) }
    val barcodeScanner = remember {
        BarcodeScanning.getClient()
    }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            cameraService.initialize(LensFacing.BACK)
            cameraService.startPreview()
            cameraService.setFrameAnalyzer(
                executor = ContextCompat.getMainExecutor(context)
            ) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null || autoScanSubmitted) {
                    imageProxy.close()
                    return@setFrameAnalyzer
                }

                val input = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                barcodeScanner.process(input)
                    .addOnSuccessListener { barcodes ->
                        if (autoScanSubmitted) return@addOnSuccessListener
                        val qrRawValue = barcodes.firstOrNull {
                            it.format == Barcode.FORMAT_QR_CODE && !it.rawValue.isNullOrBlank()
                        }?.rawValue
                        if (!qrRawValue.isNullOrBlank()) {
                            autoScanSubmitted = true
                            manualQrPayload = qrRawValue
                            qrLoginViewModel.submitMobileScan(qrRawValue)
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
                title = { Text(s(StringKey.QR_LOGIN_SCAN_TITLE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = s(StringKey.QR_LOGIN_POINT_CAMERA),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s(StringKey.QR_LOGIN_ALIGN_FRAME),
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
                                CameraState.Initializing -> s(StringKey.QR_LOGIN_CAMERA_INITIALIZING)
                                CameraState.Ready -> s(StringKey.QR_LOGIN_CAMERA_STARTING_PREVIEW)
                                is CameraState.Error -> s(StringKey.QR_LOGIN_CAMERA_ERROR)
                                CameraState.Idle -> s(StringKey.QR_LOGIN_CAMERA_NOT_INITIALIZED)
                                CameraState.Capturing -> s(StringKey.QR_LOGIN_CAMERA_BUSY)
                                CameraState.Released -> s(StringKey.QR_LOGIN_CAMERA_RELEASED)
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
                            contentDescription = s(StringKey.QR_LOGIN_PERMISSION_REQUIRED_DESC),
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (permanentlyDenied) {
                                s(StringKey.QR_LOGIN_PERMISSION_DENIED_MSG)
                            } else {
                                s(StringKey.QR_LOGIN_PERMISSION_NEEDED_MSG)
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
                            Text(
                                if (permanentlyDenied) {
                                    s(StringKey.QR_LOGIN_OPEN_SETTINGS)
                                } else {
                                    s(StringKey.QR_LOGIN_GRANT_PERMISSION)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = s(StringKey.QR_LOGIN_ALIGN_FRAME_CONTINUE),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = manualQrPayload,
                onValueChange = { manualQrPayload = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(s(StringKey.QR_LOGIN_PAYLOAD_LABEL)) },
                placeholder = { Text("fivucsas://qr-login?session=...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { qrLoginViewModel.submitMobileScan(manualQrPayload) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !qrState.isLoading && manualQrPayload.isNotBlank()
            ) {
                if (qrState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(s(StringKey.QR_LOGIN_SUBMIT_PAYLOAD))
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when (qrState.status) {
                    QrLoginStatus.IDLE -> s(StringKey.QR_LOGIN_STATUS_IDLE)
                    QrLoginStatus.WAITING_FOR_MOBILE_SCAN -> s(StringKey.QR_LOGIN_STATUS_WAITING_SCAN)
                    QrLoginStatus.WAITING_FOR_DESKTOP_APPROVAL -> s(StringKey.QR_LOGIN_STATUS_WAITING_DESKTOP)
                    QrLoginStatus.APPROVED -> s(StringKey.QR_LOGIN_STATUS_APPROVED)
                    QrLoginStatus.ERROR -> qrState.error ?: s(StringKey.QR_LOGIN_STATUS_ERROR)
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
                    Text(s(StringKey.QR_LOGIN_DONE))
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
