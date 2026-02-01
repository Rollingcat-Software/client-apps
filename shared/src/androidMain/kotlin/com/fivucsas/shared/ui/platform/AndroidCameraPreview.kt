package com.fivucsas.shared.ui.platform

import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.fivucsas.shared.platform.AndroidCameraService

/**
 * Android Camera Preview Composable
 *
 * Platform-specific implementation of camera preview for Android.
 * Uses AndroidView to embed the native CameraX PreviewView.
 * Optionally runs ML Kit face detection on each analysis frame.
 *
 * @param cameraService The AndroidCameraService instance
 * @param modifier Modifier for styling
 * @param onFaceDetected Callback invoked with detected faces on each frame
 */
@Composable
fun AndroidCameraPreview(
    cameraService: AndroidCameraService,
    modifier: Modifier = Modifier,
    onFaceDetected: ((List<Face>) -> Unit)? = null
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

    // ML Kit face detector (only created when callback is provided)
    val faceDetector = remember(onFaceDetected) {
        if (onFaceDetected != null) {
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.15f)
                .build()
            FaceDetection.getClient(options)
        } else null
    }

    // Set the preview view and optional face-detection analyzer
    DisposableEffect(previewView, onFaceDetected) {
        cameraService.previewView = previewView

        if (faceDetector != null && onFaceDetected != null) {
            cameraService.setImageAnalyzer(
                ContextCompat.getMainExecutor(context)
            ) { imageProxy ->
                processImageForFaceDetection(imageProxy, faceDetector, onFaceDetected)
            }
        }

        onDispose {
            cameraService.previewView = null
            cameraService.clearImageAnalyzer()
            faceDetector?.close()
        }
    }

    // Embed the PreviewView in Compose
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * Runs ML Kit face detection on an ImageProxy frame.
 * Always closes the imageProxy when done.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageForFaceDetection(
    imageProxy: ImageProxy,
    faceDetector: com.google.mlkit.vision.face.FaceDetector,
    onFaceDetected: (List<Face>) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    faceDetector.process(inputImage)
        .addOnSuccessListener { faces ->
            onFaceDetected(faces)
        }
        .addOnFailureListener {
            // Silently ignore detection failures on individual frames
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
