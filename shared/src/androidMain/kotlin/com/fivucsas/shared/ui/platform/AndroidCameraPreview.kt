package com.fivucsas.shared.ui.platform

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.fivucsas.shared.platform.AndroidCameraService

/**
 * Android Camera Preview Composable
 *
 * Platform-specific implementation of camera preview for Android.
 * Uses AndroidView to embed the native CameraX PreviewView.
 *
 * Design Patterns:
 * - Bridge Pattern: Bridges Compose UI with Android View system
 * - Adapter Pattern: Adapts CameraX PreviewView to Compose
 *
 * @param cameraService The AndroidCameraService instance
 * @param modifier Modifier for styling
 */
@Composable
fun AndroidCameraPreview(
    cameraService: AndroidCameraService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create and remember PreviewView
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Set the preview view to the camera service
    DisposableEffect(previewView) {
        cameraService.previewView = previewView
        onDispose {
            cameraService.previewView = null
        }
    }

    // Embed the PreviewView in Compose
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
