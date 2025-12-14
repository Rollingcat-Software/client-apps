package com.fivucsas.desktop.platform

import com.fivucsas.shared.config.BiometricConfig
import com.fivucsas.shared.platform.CameraState
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.LensFacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Desktop Camera Service Implementation
 *
 * Desktop implementation of ICameraService using JavaCV for webcam access.
 * Follows Hexagonal Architecture by implementing ICameraService port.
 *
 * Key Features:
 * - JavaCV integration for cross-platform webcam access
 * - OpenCV-based frame grabbing
 * - High-quality image capture for biometric processing
 * - State management using Flow
 *
 * Design Patterns:
 * - Adapter Pattern: Adapts JavaCV to ICameraService interface
 * - State Pattern: Manages camera states
 * - Observer Pattern: Exposes StateFlow for reactive state management
 *
 * Note: On desktop, LensFacing.FRONT typically maps to the first available camera
 */
class DesktopCameraServiceImpl : ICameraService {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var frameGrabber: FrameGrabber? = null
    private var frameConverter: Java2DFrameConverter? = null
    private var currentLensFacing: LensFacing = LensFacing.FRONT
    private var isPreviewActive = false

    private val previewWidth = BiometricConfig.PREFERRED_IMAGE_WIDTH
    private val previewHeight = BiometricConfig.PREFERRED_IMAGE_HEIGHT

    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> = withContext(Dispatchers.IO) {
        if (_cameraState.value == CameraState.Previewing) {
            stopPreview()
        }

        _cameraState.value = CameraState.Initializing
        currentLensFacing = lensFacing

        try {
            // Initialize OpenCV frame grabber
            // Device index 0 is typically the default/front camera
            val deviceIndex = if (lensFacing == LensFacing.FRONT) 0 else 1

            frameGrabber = OpenCVFrameGrabber(deviceIndex).apply {
                imageWidth = previewWidth
                imageHeight = previewHeight
                frameRate = BiometricConfig.CAMERA_FRAME_RATE_FPS.toDouble()
            }

            frameConverter = Java2DFrameConverter()

            _cameraState.value = CameraState.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun startPreview(): Result<Unit> = withContext(Dispatchers.IO) {
        if (_cameraState.value != CameraState.Ready && _cameraState.value != CameraState.Previewing) {
            return@withContext Result.failure(
                IllegalStateException("Camera must be initialized before starting preview")
            )
        }

        if (isPreviewActive) {
            return@withContext Result.success(Unit)
        }

        try {
            val grabber = frameGrabber ?: return@withContext Result.failure(
                IllegalStateException("Frame grabber not initialized")
            )

            grabber.start()
            isPreviewActive = true
            _cameraState.value = CameraState.Previewing
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun stopPreview(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            frameGrabber?.stop()
            isPreviewActive = false
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
            val grabber = frameGrabber ?: return@withContext Result.failure(
                IllegalStateException("Frame grabber not initialized")
            )

            val converter = frameConverter ?: return@withContext Result.failure(
                IllegalStateException("Frame converter not initialized")
            )

            // Grab a frame
            val frame: Frame = grabber.grab() ?: return@withContext Result.failure(
                IllegalStateException("Failed to grab frame")
            )

            // Convert frame to BufferedImage
            val bufferedImage: BufferedImage = converter.convert(frame)

            // Convert BufferedImage to JPEG byte array
            val imageBytes = bufferedImageToByteArray(bufferedImage, "JPEG")

            _cameraState.value = CameraState.Previewing
            Result.success(imageBytes)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun captureFrame(): Result<ByteArray> = withContext(Dispatchers.IO) {
        // For desktop, captureFrame and captureImage are similar
        // captureFrame could use lower quality if needed
        captureImage()
    }

    override fun isAvailable(): Boolean {
        return try {
            // Try to create a frame grabber to check availability
            val testGrabber = OpenCVFrameGrabber(0)
            testGrabber.start()
            testGrabber.stop()
            testGrabber.release()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun hasCamera(lensFacing: LensFacing): Boolean {
        return try {
            val deviceIndex = if (lensFacing == LensFacing.FRONT) 0 else 1
            val testGrabber = OpenCVFrameGrabber(deviceIndex)
            testGrabber.start()
            testGrabber.stop()
            testGrabber.release()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun release() {
        withContext(Dispatchers.IO) {
            try {
                if (isPreviewActive) {
                    frameGrabber?.stop()
                }
                frameGrabber?.release()
                frameGrabber = null
                frameConverter = null
                isPreviewActive = false
                _cameraState.value = CameraState.Released
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e)
            }
        }
    }

    override fun getPreviewDimensions(): Pair<Int, Int> {
        return Pair(previewWidth, previewHeight)
    }

    override fun getSupportedResolutions(): List<Pair<Int, Int>> {
        // Common webcam resolutions
        return listOf(
            Pair(640, 480),   // VGA
            Pair(800, 600),   // SVGA
            Pair(1280, 720),  // HD
            Pair(1920, 1080)  // Full HD
        )
    }

    /**
     * Converts BufferedImage to ByteArray in specified format
     * @param image The BufferedImage to convert
     * @param format Image format (JPEG, PNG, etc.)
     * @return ByteArray containing the encoded image
     */
    private fun bufferedImageToByteArray(image: BufferedImage, format: String): ByteArray {
        ByteArrayOutputStream().use { baos ->
            ImageIO.write(image, format, baos)
            return baos.toByteArray()
        }
    }

    /**
     * Get the current frame as BufferedImage for preview
     * This can be used by UI components to display the camera feed
     */
    suspend fun getCurrentFrame(): BufferedImage? = withContext(Dispatchers.IO) {
        try {
            val grabber = frameGrabber ?: return@withContext null
            val converter = frameConverter ?: return@withContext null

            if (!isPreviewActive) return@withContext null

            val frame = grabber.grab() ?: return@withContext null
            converter.convert(frame)
        } catch (e: Exception) {
            null
        }
    }
}
