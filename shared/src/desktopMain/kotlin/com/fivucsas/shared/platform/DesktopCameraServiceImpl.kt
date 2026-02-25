package com.fivucsas.shared.platform

import com.fivucsas.shared.config.BiometricConfig
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

        val deviceIndex = if (lensFacing == LensFacing.FRONT) 0 else 1
        var lastError: Exception? = null

        // Try 1: No format (fastest — avoids dshow negotiation overhead)
        try {
            val g1 = OpenCVFrameGrabber(deviceIndex).apply {
                imageWidth = previewWidth
                imageHeight = previewHeight
            }
            g1.start()
            frameGrabber = g1
            frameConverter = Java2DFrameConverter()
            isPreviewActive = true
            _cameraState.value = CameraState.Previewing
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            lastError = e
        }

        // Try 2: Windows DirectShow format
        try {
            val g2 = OpenCVFrameGrabber(deviceIndex).apply {
                format = "dshow"
                imageWidth = previewWidth
                imageHeight = previewHeight
            }
            g2.start()
            frameGrabber = g2
            frameConverter = Java2DFrameConverter()
            isPreviewActive = true
            _cameraState.value = CameraState.Previewing
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            lastError = e
        }

        // Try 3: Minimal settings fallback
        try {
            val g3 = OpenCVFrameGrabber(deviceIndex)
            g3.start()
            frameGrabber = g3
            frameConverter = Java2DFrameConverter()
            isPreviewActive = true
            _cameraState.value = CameraState.Previewing
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            lastError = e
        }

        _cameraState.value = CameraState.Error(
            Exception(
                "Failed to initialize camera. Please check:\n" +
                        "1. Camera is connected\n" +
                        "2. Camera is not used by another app\n" +
                        "3. Camera permissions are granted\n\n" +
                        "Error: ${lastError?.message}"
            )
        )
        Result.failure(lastError ?: Exception("Camera initialization failed"))
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

            val frame: Frame = grabber.grab() ?: return@withContext Result.failure(
                IllegalStateException("Failed to grab frame")
            )

            val bufferedImage: BufferedImage = converter.convert(frame)
            val imageBytes = bufferedImageToByteArray(bufferedImage, "JPEG")

            _cameraState.value = CameraState.Previewing
            Result.success(imageBytes)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun captureFrame(): Result<ByteArray> = withContext(Dispatchers.IO) {
        captureImage()
    }

    override fun isAvailable(): Boolean {
        return try {
            val testGrabber = OpenCVFrameGrabber(0)
            testGrabber.start()
            testGrabber.stop()
            testGrabber.release()
            true
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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
        return listOf(
            Pair(640, 480),
            Pair(800, 600),
            Pair(1280, 720),
            Pair(1920, 1080)
        )
    }

    private fun bufferedImageToByteArray(image: BufferedImage, format: String): ByteArray {
        ByteArrayOutputStream().use { baos ->
            ImageIO.write(image, format, baos)
            return baos.toByteArray()
        }
    }

    suspend fun getCurrentFrame(): BufferedImage? = withContext(Dispatchers.IO) {
        try {
            val grabber = frameGrabber ?: return@withContext null
            val converter = frameConverter ?: return@withContext null

            if (!isPreviewActive) return@withContext null

            val frame = grabber.grab() ?: return@withContext null
            converter.convert(frame)
        } catch (_: Exception) {
            null
        }
    }
}
