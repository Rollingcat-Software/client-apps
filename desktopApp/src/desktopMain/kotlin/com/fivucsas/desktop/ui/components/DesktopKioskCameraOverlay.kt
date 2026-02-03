package com.fivucsas.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import com.fivucsas.desktop.ui.DesktopCameraPreview
import com.fivucsas.shared.platform.CameraState
import com.fivucsas.shared.platform.DesktopCameraServiceImpl
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.LensFacing
import com.fivucsas.shared.ui.components.organisms.CameraPreviewContainer
import com.fivucsas.shared.ui.components.organisms.FaceDetectionOverlay
import kotlinx.coroutines.launch

@Composable
fun DesktopKioskCameraOverlay(
    cameraService: ICameraService,
    onCapture: (ByteArray) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraState by cameraService.cameraState.collectAsState()
    val scope = rememberCoroutineScope()
    val desktopCamera = cameraService as? DesktopCameraServiceImpl

    LaunchedEffect(cameraService) {
        val initResult = cameraService.initialize(LensFacing.FRONT)
        if (initResult.isSuccess) {
            cameraService.startPreview()
        }
    }

    DisposableEffect(cameraService) {
        onDispose {
            scope.launch {
                cameraService.stopPreview()
                cameraService.release()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CameraPreviewContainer(
            cameraState = cameraState,
            onCaptureClick = {
                scope.launch {
                    val result = cameraService.captureImage()
                    if (result.isSuccess) {
                        result.getOrNull()?.let(onCapture)
                    }
                }
            },
            onCloseClick = {
                scope.launch {
                    cameraService.stopPreview()
                    cameraService.release()
                }
                onClose()
            },
            overlayContent = {
                FaceDetectionOverlay(
                    guidanceText = "Center your face in the frame"
                )
            },
            cameraPreviewContent = {
                if (desktopCamera != null) {
                    DesktopCameraPreview(cameraService = desktopCamera)
                } else if (cameraState !is CameraState.Error) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera not available")
                    }
                }
            }
        )
    }
}
