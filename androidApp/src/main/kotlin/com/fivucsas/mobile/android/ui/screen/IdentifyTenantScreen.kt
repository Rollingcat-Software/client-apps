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
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
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
import com.fivucsas.shared.ui.util.disposeOnLeave
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.IdentifyViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun IdentifyTenantScreen(
    onNavigateBack: () -> Unit,
    viewModel: IdentifyViewModel = koinInject<IdentifyViewModel>().disposeOnLeave()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        s(StringKey.IDENTIFY_TITLE),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = s(StringKey.BACK))
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
                IdentifyCameraContent(
                    cameraController = cameraController,
                    lifecycleOwner = lifecycleOwner,
                    state = state,
                    onCapture = {
                        cameraController.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val imageBytes = image.toCompressedJpegBytes()
                                    image.close()
                                    viewModel.identifyFace(imageBytes)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    viewModel.onCaptureError(
                                        s(StringKey.FACE_VERIFY_CAPTURE_ERROR, exception.message ?: "")
                                    )
                                }
                            }
                        )
                    },
                    onRetry = { viewModel.clearState() },
                    onDone = onNavigateBack
                )
            } else {
                val permanentlyDenied = permissionRequested &&
                    !cameraPermissionState.status.shouldShowRationale

                IdentifyPermissionContent(
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

@Composable
private fun IdentifyCameraContent(
    cameraController: LifecycleCameraController,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    state: com.fivucsas.shared.presentation.state.IdentifyUiState,
    onCapture: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit
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

        // Instructions overlay
        if (!state.isSuccess && state.errorMessage == null && !state.isLoading) {
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s(StringKey.IDENTIFY_HEADER),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s(StringKey.IDENTIFY_INSTRUCTIONS),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Loading overlay
        if (state.isLoading) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
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
                        text = s(StringKey.IDENTIFY_LOADING),
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

        // Face guide frame
        if (!state.isSuccess && !state.isLoading) {
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
            // Error
            if (state.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(s(StringKey.COMMON_TRY_AGAIN))
                }
            }

            // Result card
            if (state.isSuccess && state.result != null) {
                val result = state.result!!
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isMatch)
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
                            imageVector = if (result.isMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (result.isMatch)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (result.isMatch) s(StringKey.IDENTIFY_MATCH_FOUND) else s(StringKey.IDENTIFY_NO_MATCH),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (result.isMatch)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )

                        if (result.isMatch) {
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
                                        text = s(StringKey.IDENTIFY_NAME_LABEL),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = result.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = s(StringKey.FACE_VERIFY_CONFIDENCE_LABEL),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${(result.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = s(StringKey.IDENTIFY_USER_ID_LABEL, result.userId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                                Text(s(StringKey.IDENTIFY_SCAN_AGAIN))
                            }
                            Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                                Text(s(StringKey.COMMON_DONE))
                            }
                        }
                    }
                }
            }

            // Capture button (idle state)
            if (!state.isSuccess && state.errorMessage == null) {
                Button(
                    onClick = onCapture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(s(StringKey.IDENTIFY_LOADING), style = MaterialTheme.typography.titleMedium)
                    } else {
                        Icon(Icons.Default.PersonSearch, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            s(StringKey.IDENTIFY_BUTTON),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentifyPermissionContent(
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
            text = s(StringKey.COMMON_CAMERA_PERMISSION_REQUIRED),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (permanentlyDenied) {
                s(StringKey.IDENTIFY_PERMISSION_PERMANENTLY_DENIED)
            } else {
                s(StringKey.IDENTIFY_PERMISSION_RATIONALE)
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (permanentlyDenied) {
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(s(StringKey.COMMON_OPEN_APP_SETTINGS))
            }
        } else {
            Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                Text(s(StringKey.COMMON_GRANT_CAMERA_PERMISSION))
            }
        }
    }
}
