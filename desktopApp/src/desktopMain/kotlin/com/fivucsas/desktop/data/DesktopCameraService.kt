package com.fivucsas.desktop.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Desktop Camera Service using JavaCV
 * Captures images from webcam for biometric enrollment and verification
 */
class DesktopCameraService {

    private var grabber: FrameGrabber? = null
    private var converter: Java2DFrameConverter? = null
    private var isInitialized = false

    /**
     * Initialize the camera with multiple fallback options
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) {
                return@withContext Result.success(Unit)
            }

            // Try different camera initialization approaches
            var lastError: Exception? = null

            // Try 1: OpenCV with default settings
            try {
                grabber = OpenCVFrameGrabber(0).apply {
                    // Set format first
                    format = "dshow" // Windows DirectShow
                    imageWidth = 640
                    imageHeight = 480
                    frameRate = 30.0

                    // Start with timeout
                    start()

                    // Test grab
                    val testFrame = grab()
                    if (testFrame == null) {
                        stop()
                        release()
                        throw Exception("Could not grab test frame")
                    }
                }

                converter = Java2DFrameConverter()
                isInitialized = true
                return@withContext Result.success(Unit)

            } catch (e: Exception) {
                lastError = e
                grabber?.stop()
                grabber?.release()
                grabber = null
            }

            // Try 2: Without explicit format
            try {
                grabber = OpenCVFrameGrabber(0).apply {
                    imageWidth = 640
                    imageHeight = 480
                    start()

                    // Warm up - grab and discard a few frames
                    repeat(5) { grab() }
                }

                converter = Java2DFrameConverter()
                isInitialized = true
                return@withContext Result.success(Unit)

            } catch (e: Exception) {
                lastError = e
                grabber?.stop()
                grabber?.release()
                grabber = null
            }

            // Try 3: Minimal settings
            try {
                grabber = OpenCVFrameGrabber(0).apply {
                    start()
                }

                converter = Java2DFrameConverter()
                isInitialized = true
                return@withContext Result.success(Unit)

            } catch (e: Exception) {
                lastError = e
            }

            Result.failure(
                Exception(
                    "Failed to initialize camera. Please check:\n" +
                            "1. Camera is connected\n" +
                            "2. Camera is not used by another app\n" +
                            "3. Camera permissions are granted\n\n" +
                            "Error: ${lastError?.message}"
                )
            )

        } catch (e: Exception) {
            Result.failure(Exception("Camera initialization error: ${e.message}"))
        }
    }

    /**
     * Capture a frame from the camera
     * Returns JPEG image as ByteArray
     */
    suspend fun captureFrame(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized) {
                val initResult = initialize()
                if (initResult.isFailure) {
                    return@withContext Result.failure(
                        initResult.exceptionOrNull()
                            ?: Exception("Failed to initialize")
                    )
                }
            }

            // Grab a few frames to ensure we get a fresh one
            var frame = grabber?.grab()
            repeat(3) {
                frame = grabber?.grab() ?: frame
            }

            if (frame == null) {
                return@withContext Result.failure(Exception("Failed to grab frame from camera"))
            }

            val bufferedImage = converter?.convert(frame)
                ?: return@withContext Result.failure(Exception("Failed to convert frame"))

            // Convert BufferedImage to JPEG ByteArray
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "jpg", outputStream)
            val imageBytes = outputStream.toByteArray()

            if (imageBytes.isEmpty()) {
                return@withContext Result.failure(Exception("Captured image is empty"))
            }

            Result.success(imageBytes)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to capture frame: ${e.message}"))
        }
    }

    /**
     * Get a live frame for preview
     */
    suspend fun getPreviewFrame(): Result<BufferedImage> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized) {
                val initResult = initialize()
                if (initResult.isFailure) {
                    return@withContext Result.failure(
                        initResult.exceptionOrNull()
                            ?: Exception("Camera not initialized")
                    )
                }
            }

            val frame = grabber?.grab()
                ?: return@withContext Result.failure(Exception("Failed to grab preview frame"))

            val bufferedImage = converter?.convert(frame)
                ?: return@withContext Result.failure(Exception("Failed to convert preview frame"))

            Result.success(bufferedImage)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get preview: ${e.message}"))
        }
    }

    /**
     * Release camera resources
     */
    suspend fun release() = withContext(Dispatchers.IO) {
        try {
            grabber?.stop()
            grabber?.release()
            grabber = null
            converter = null
            isInitialized = false
        } catch (e: Exception) {
            // Ignore errors during release
        }
    }

    /**
     * Check if camera is available
     */
    fun isCameraAvailable(): Boolean {
        return try {
            val testGrabber = OpenCVFrameGrabber(0)
            testGrabber.start()
            val frame = testGrabber.grab()
            testGrabber.stop()
            testGrabber.release()
            frame != null
        } catch (e: Exception) {
            false
        }
    }
}
