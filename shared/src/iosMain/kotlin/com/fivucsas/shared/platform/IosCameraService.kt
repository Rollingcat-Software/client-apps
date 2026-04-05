package com.fivucsas.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.*
import platform.CoreMedia.*
import platform.CoreVideo.*
import platform.Foundation.NSError
import platform.UIKit.UIDevice
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlinx.cinterop.*
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.CoreGraphics.*
import platform.Foundation.NSData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS Camera Service Implementation
 *
 * Uses AVFoundation framework for camera access on iOS devices.
 * Follows Hexagonal Architecture by implementing ICameraService interface.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle: Implements platform abstraction
 * - Single Responsibility: Handles only camera operations
 * - Observer Pattern: Uses StateFlow for reactive state management
 *
 * AVFoundation Components Used:
 * - AVCaptureSession: Manages camera capture pipeline
 * - AVCaptureDevice: Represents physical camera
 * - AVCaptureDeviceInput: Provides camera input
 * - AVCapturePhotoOutput: Handles photo capture
 * - AVCaptureVideoDataOutput: Handles frame capture
 */
@OptIn(ExperimentalForeignApi::class)
class IosCameraService : ICameraService {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var captureSession: AVCaptureSession? = null
    private var currentDevice: AVCaptureDevice? = null
    private var photoOutput: AVCapturePhotoOutput? = null
    private var videoOutput: AVCaptureVideoDataOutput? = null
    private var currentInput: AVCaptureDeviceInput? = null

    private var previewWidth: Int = 1920
    private var previewHeight: Int = 1080

    /** Delegate that captures a single photo and resumes a continuation. */
    private var photoCaptureDelegate: PhotoCaptureDelegate? = null

    /** Delegate that captures the latest video sample buffer. */
    private var videoSampleDelegate: VideoSampleDelegate? = null

    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> {
        return try {
            _cameraState.value = CameraState.Initializing

            // Get camera device
            val device = getCameraDevice(lensFacing)
                ?: return Result.failure(Exception("Camera not available for $lensFacing"))

            currentDevice = device

            // Create capture session
            val session = AVCaptureSession()
            captureSession = session

            // Configure session for high quality photo
            session.sessionPreset = AVCaptureSessionPresetPhoto

            // Create device input
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as? AVCaptureDeviceInput
                ?: return Result.failure(Exception("Failed to create camera input"))

            // Add input to session
            if (session.canAddInput(input)) {
                session.addInput(input)
                currentInput = input
            } else {
                return Result.failure(Exception("Cannot add camera input to session"))
            }

            // Setup photo output
            val photoOut = AVCapturePhotoOutput()
            if (session.canAddOutput(photoOut)) {
                session.addOutput(photoOut)
                photoOutput = photoOut
            } else {
                return Result.failure(Exception("Cannot add photo output to session"))
            }

            // Setup video output for frame capture
            val videoOut = AVCaptureVideoDataOutput()
            videoOut.videoSettings = mapOf(
                kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32BGRA
            )
            videoOut.alwaysDiscardsLateVideoFrames = true

            // Attach a sample buffer delegate so captureFrame() can grab the latest frame
            val sampleDelegate = VideoSampleDelegate()
            videoSampleDelegate = sampleDelegate
            videoOut.setSampleBufferDelegate(sampleDelegate, dispatch_get_main_queue())

            if (session.canAddOutput(videoOut)) {
                session.addOutput(videoOut)
                videoOutput = videoOut
            }

            _cameraState.value = CameraState.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun startPreview(): Result<Unit> {
        return try {
            val session = captureSession
                ?: return Result.failure(Exception("Camera not initialized"))

            if (!session.running) {
                session.startRunning()
            }

            _cameraState.value = CameraState.Previewing
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun stopPreview(): Result<Unit> {
        return try {
            val session = captureSession
                ?: return Result.failure(Exception("Camera not initialized"))

            if (session.running) {
                session.stopRunning()
            }

            _cameraState.value = CameraState.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun captureImage(): Result<ByteArray> {
        return try {
            _cameraState.value = CameraState.Capturing

            val output = photoOutput
                ?: return Result.failure(Exception("Photo output not initialized"))

            val settings = AVCapturePhotoSettings.photoSettings()

            val imageBytes = suspendCancellableCoroutine<ByteArray> { continuation ->
                val delegate = PhotoCaptureDelegate { data, error ->
                    if (data != null) {
                        val uiImage = UIImage(data = data)
                        val jpegData = UIImageJPEGRepresentation(uiImage, 0.85)
                        if (jpegData != null) {
                            val bytes = ByteArray(jpegData.length.toInt())
                            bytes.usePinned { pinned ->
                                jpegData.getBytes(pinned.addressOf(0), jpegData.length)
                            }
                            continuation.resume(bytes)
                        } else {
                            continuation.resumeWithException(Exception("JPEG conversion failed"))
                        }
                    } else {
                        continuation.resumeWithException(
                            Exception("Photo capture failed: ${error?.localizedDescription ?: "unknown"}")
                        )
                    }
                }
                photoCaptureDelegate = delegate
                output.capturePhotoWithSettings(settings, delegate = delegate)
            }

            _cameraState.value = CameraState.Previewing
            Result.success(imageBytes)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun captureFrame(): Result<ByteArray> {
        return try {
            _cameraState.value = CameraState.Capturing

            val delegate = videoSampleDelegate
                ?: return Result.failure(Exception("Video output delegate not initialized"))

            val imageData = delegate.lastFrameAsJpeg()
                ?: return Result.failure(Exception("No frame available yet — camera may still be starting"))

            _cameraState.value = CameraState.Previewing
            Result.success(imageData)
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
            Result.failure(e)
        }
    }

    override fun isAvailable(): Boolean {
        return AVCaptureDevice.devices().isNotEmpty()
    }

    override fun hasCamera(lensFacing: LensFacing): Boolean {
        return getCameraDevice(lensFacing) != null
    }

    override suspend fun release() {
        try {
            captureSession?.stopRunning()

            currentInput?.let { captureSession?.removeInput(it) }
            photoOutput?.let { captureSession?.removeOutput(it) }
            videoOutput?.let { captureSession?.removeOutput(it) }

            captureSession = null
            currentDevice = null
            currentInput = null
            photoOutput = null
            videoOutput = null
            photoCaptureDelegate = null
            videoSampleDelegate = null

            _cameraState.value = CameraState.Released
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
        }
    }

    override fun getPreviewDimensions(): Pair<Int, Int> {
        return Pair(previewWidth, previewHeight)
    }

    override fun getSupportedResolutions(): List<Pair<Int, Int>> {
        return listOf(
            Pair(1920, 1080),
            Pair(1280, 720),
            Pair(640, 480)
        )
    }

    /**
     * Get camera device for specified lens facing
     */
    private fun getCameraDevice(lensFacing: LensFacing): AVCaptureDevice? {
        val position = when (lensFacing) {
            LensFacing.FRONT -> AVCaptureDevicePositionFront
            LensFacing.BACK -> AVCaptureDevicePositionBack
        }

        // Get default device for position
        return AVCaptureDevice.defaultDeviceWithDeviceType(
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
            AVMediaTypeVideo,
            position
        )
    }
}

/**
 * AVCapturePhotoCaptureDelegate implementation via Kotlin/Native.
 *
 * Receives the captured photo data and forwards it to the provided callback.
 */
@OptIn(ExperimentalForeignApi::class)
private class PhotoCaptureDelegate(
    private val onCapture: (NSData?, NSError?) -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        val data = didFinishProcessingPhoto.fileDataRepresentation()
        onCapture(data, error)
    }
}

/**
 * AVCaptureVideoDataOutputSampleBufferDelegate implementation.
 *
 * Holds the latest video frame so captureFrame() can grab it on demand
 * without blocking on a callback.
 */
@OptIn(ExperimentalForeignApi::class)
private class VideoSampleDelegate : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    private var latestImageData: ByteArray? = null

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        val sampleBuffer = didOutputSampleBuffer ?: return
        val imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) ?: return

        CVPixelBufferLockBaseAddress(imageBuffer, 0u)

        val ciImage = platform.CoreImage.CIImage(cVPixelBuffer = imageBuffer)

        CVPixelBufferUnlockBaseAddress(imageBuffer, 0u)

        // Convert CIImage to UIImage directly (avoids CIContext.createCGImage K/N binding issues)
        val uiImage = UIImage(cIImage = ciImage)
        val jpegData = UIImageJPEGRepresentation(uiImage, 0.80)
        if (jpegData != null) {
            val bytes = ByteArray(jpegData.length.toInt())
            bytes.usePinned { pinned ->
                jpegData.getBytes(pinned.addressOf(0), jpegData.length)
            }
            latestImageData = bytes
        }
    }

    /**
     * Returns the latest captured frame as JPEG bytes, or null if no frame yet.
     */
    fun lastFrameAsJpeg(): ByteArray? = latestImageData
}
