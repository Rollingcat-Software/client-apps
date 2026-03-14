package com.fivucsas.shared.platform

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.fivucsas.shared.config.BiometricConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android Camera Service Implementation
 *
 * Implements camera functionality using CameraX library.
 * Follows Hexagonal Architecture by implementing ICameraService port.
 *
 * Key Features:
 * - CameraX integration for modern Android camera API
 * - Lifecycle-aware camera management
 * - High-quality image capture for biometric processing
 * - Preview support for user feedback
 * - Proper resource management
 *
 * Design Patterns:
 * - Adapter Pattern: Adapts CameraX to ICameraService interface
 * - State Pattern: Manages camera states using CameraState sealed class
 * - Observer Pattern: Exposes StateFlow for reactive state management
 */
class AndroidCameraService(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : ICameraService {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var currentLensFacing: LensFacing = LensFacing.FRONT

    // Preview view that will be provided to the UI
    var previewView: PreviewView? = null
        set(value) {
            field = value
            // If preview is already created, bind it to the new view
            preview?.setSurfaceProvider(value?.surfaceProvider)
        }

    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> = withContext(Dispatchers.Main) {
        if (_cameraState.value == CameraState.Previewing) {
            stopPreview()
        }

        _cameraState.value = CameraState.Initializing
        currentLensFacing = lensFacing

        try {
            // Get camera provider
            cameraProvider = suspendCancellableCoroutine { continuation ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        continuation.resume(cameraProviderFuture.get())
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }

            // Build use cases
            setupUseCases()

            _cameraState.value = CameraState.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    private fun setupUseCases() {
        // Preview use case
        preview = Preview.Builder()
            .setTargetResolution(
                android.util.Size(
                    BiometricConfig.PREFERRED_IMAGE_WIDTH,
                    BiometricConfig.PREFERRED_IMAGE_HEIGHT
                )
            )
            .build()

        // Image capture use case for high-quality photos
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetResolution(
                android.util.Size(
                    BiometricConfig.PREFERRED_IMAGE_WIDTH,
                    BiometricConfig.PREFERRED_IMAGE_HEIGHT
                )
            )
            .setJpegQuality(95)
            .build()

        // Image analysis use case for frame capture
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(
                android.util.Size(
                    BiometricConfig.PREFERRED_IMAGE_WIDTH,
                    BiometricConfig.PREFERRED_IMAGE_HEIGHT
                )
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    override suspend fun startPreview(): Result<Unit> = withContext(Dispatchers.Main) {
        if (_cameraState.value != CameraState.Ready && _cameraState.value != CameraState.Previewing) {
            return@withContext Result.failure(
                IllegalStateException("Camera must be initialized before starting preview")
            )
        }

        try {
            val provider = cameraProvider ?: return@withContext Result.failure(
                IllegalStateException("Camera provider not initialized")
            )

            // Unbind all use cases before rebinding
            provider.unbindAll()

            // Select camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(
                    if (currentLensFacing == LensFacing.FRONT)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
                )
                .build()

            // Bind preview to surface provider if available
            previewView?.let {
                preview?.setSurfaceProvider(it.surfaceProvider)
            }

            // Bind use cases to lifecycle
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            _cameraState.value = CameraState.Previewing
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun stopPreview(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            cameraProvider?.unbindAll()
            _cameraState.value = CameraState.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun captureImage(): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (_cameraState.value != CameraState.Previewing) {
            return@withContext Result.failure(
                IllegalStateException("Camera preview must be active to capture image")
            )
        }

        _cameraState.value = CameraState.Capturing

        try {
            val imageCapture = this@AndroidCameraService.imageCapture
                ?: return@withContext Result.failure(
                    IllegalStateException("Image capture not initialized")
                )

            val rawBytes = suspendCancellableCoroutine<ByteArray> { continuation ->
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            try {
                                val bytes = imageProxyToByteArray(image)
                                image.close()
                                continuation.resume(bytes)
                            } catch (e: Exception) {
                                image.close()
                                continuation.resumeWithException(e)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            continuation.resumeWithException(exception)
                        }
                    }
                )
            }

            // Resize and compress through ImageProcessor
            val processedBytes = ImageProcessor.processForBiometric(rawBytes)

            _cameraState.value = CameraState.Previewing
            Result.success(processedBytes)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun captureFrame(): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (_cameraState.value != CameraState.Previewing) {
            return@withContext Result.failure(
                IllegalStateException("Camera preview must be active to capture frame")
            )
        }

        try {
            val analysis = imageAnalysis ?: return@withContext Result.failure(
                IllegalStateException("Image analysis not initialized")
            )

            val frameBytes = suspendCancellableCoroutine<ByteArray> { continuation ->
                var isResumed = false

                analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    if (!isResumed) {
                        isResumed = true
                        try {
                            val bytes = imageProxyToByteArray(imageProxy)
                            imageProxy.close()
                            // Clear analyzer after capturing one frame
                            analysis.clearAnalyzer()
                            continuation.resume(bytes)
                        } catch (e: Exception) {
                            imageProxy.close()
                            analysis.clearAnalyzer()
                            continuation.resumeWithException(e)
                        }
                    } else {
                        imageProxy.close()
                    }
                }
            }

            Result.success(frameBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_CAMERA_ANY
        )
    }

    override fun hasCamera(lensFacing: LensFacing): Boolean {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(
                if (lensFacing == LensFacing.FRONT)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            )
            .build()

        return try {
            cameraProvider?.hasCamera(cameraSelector) ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun release() {
        withContext(Dispatchers.Main) {
            try {
                cameraProvider?.unbindAll()
                camera = null
                preview = null
                imageCapture = null
                imageAnalysis = null
                cameraProvider = null
                _cameraState.value = CameraState.Released
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e)
            }
        }
    }

    override fun getPreviewDimensions(): Pair<Int, Int> {
        return Pair(BiometricConfig.PREFERRED_IMAGE_WIDTH, BiometricConfig.PREFERRED_IMAGE_HEIGHT)
    }

    override fun getSupportedResolutions(): List<Pair<Int, Int>> {
        // Common resolutions supported by most Android devices
        return listOf(
            Pair(640, 480),   // VGA
            Pair(800, 600),   // SVGA
            Pair(1280, 720),  // HD
            Pair(1920, 1080)  // Full HD
        )
    }

    /**
     * Converts ImageProxy to JPEG ByteArray.
     * Handles both JPEG (from ImageCapture) and YUV_420_888 (from ImageAnalysis).
     */
    private fun imageProxyToByteArray(imageProxy: ImageProxy): ByteArray {
        return when (imageProxy.format) {
            ImageFormat.YUV_420_888 -> yuvToJpeg(imageProxy)
            else -> {
                // JPEG or other single-plane format
                val buffer: ByteBuffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                bytes
            }
        }
    }

    /**
     * Converts a YUV_420_888 ImageProxy to JPEG bytes using Android's YuvImage.
     */
    private fun yuvToJpeg(imageProxy: ImageProxy): ByteArray {
        val yPlane = imageProxy.planes[0]
        val uPlane = imageProxy.planes[1]
        val vPlane = imageProxy.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // NV21 format: Y plane followed by interleaved VU
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            90,
            out
        )
        return out.toByteArray()
    }

    /**
     * Set an analyzer on the ImageAnalysis use case.
     * Used by AndroidCameraPreview for ML Kit face detection.
     */
    fun setImageAnalyzer(
        executor: java.util.concurrent.Executor,
        analyzer: ImageAnalysis.Analyzer
    ) {
        imageAnalysis?.setAnalyzer(executor, analyzer)
    }

    /**
     * Clear the current ImageAnalysis analyzer.
     */
    fun clearImageAnalyzer() {
        imageAnalysis?.clearAnalyzer()
    }

    /**
     * Get camera control for advanced features
     */
    fun getCameraControl(): CameraControl? = camera?.cameraControl

    /**
     * Get camera info for device capabilities
     */
    fun getCameraInfo(): CameraInfo? = camera?.cameraInfo

    /**
     * Sets a continuous frame analyzer on the bound ImageAnalysis use case.
     * Caller is responsible for closing ImageProxy.
     */
    fun setFrameAnalyzer(
        executor: Executor,
        analyzer: (ImageProxy) -> Unit
    ) {
        imageAnalysis?.setAnalyzer(executor) { imageProxy ->
            analyzer(imageProxy)
        }
    }

    fun clearFrameAnalyzer() {
        imageAnalysis?.clearAnalyzer()
    }
}
