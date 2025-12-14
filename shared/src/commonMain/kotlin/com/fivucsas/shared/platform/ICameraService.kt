package com.fivucsas.shared.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Camera Service Interface
 *
 * Platform abstraction for camera operations following Hexagonal Architecture.
 * Enables testability, cross-platform support, and maintainability.
 *
 * Design Principles Applied:
 * - Interface Segregation Principle (ISP): Focused interface for camera operations
 * - Dependency Inversion Principle (DIP): Abstracts platform-specific implementations
 * - Single Responsibility Principle (SRP): Only handles camera operations
 */
interface ICameraService {
    /**
     * Observable camera state
     * Enables reactive UI updates following Observer pattern
     */
    val cameraState: StateFlow<CameraState>

    /**
     * Initialize the camera for the specified lens facing
     * @param lensFacing Camera lens to use (FRONT for selfie, BACK for rear camera)
     * @return Result indicating success or failure
     */
    suspend fun initialize(lensFacing: LensFacing = LensFacing.FRONT): Result<Unit>

    /**
     * Start camera preview
     * Must be called after initialize()
     * @return Result indicating success or failure
     */
    suspend fun startPreview(): Result<Unit>

    /**
     * Stop camera preview
     * Camera remains initialized, can restart preview
     * @return Result indicating success or failure
     */
    suspend fun stopPreview(): Result<Unit>

    /**
     * Capture a high-quality image from the camera
     * Suitable for biometric processing
     * @return Result containing JPEG image bytes or error
     */
    suspend fun captureImage(): Result<ByteArray>

    /**
     * Capture a single frame from the camera preview
     * Lower quality than captureImage, suitable for quick checks
     * @return Result containing image bytes or error
     */
    suspend fun captureFrame(): Result<ByteArray>

    /**
     * Check if camera is available on the device
     * @return true if camera is available
     */
    fun isAvailable(): Boolean

    /**
     * Check if a specific camera lens is available
     * @param lensFacing The lens to check
     * @return true if the specified lens is available
     */
    fun hasCamera(lensFacing: LensFacing): Boolean

    /**
     * Release camera resources
     * Must be called when done using camera
     */
    suspend fun release()

    /**
     * Get camera preview dimensions
     * @return Pair of width and height in pixels
     */
    fun getPreviewDimensions(): Pair<Int, Int>

    /**
     * Get supported camera resolutions
     * @return List of supported width x height resolutions
     */
    fun getSupportedResolutions(): List<Pair<Int, Int>>
}

/**
 * Camera Lens Facing Direction
 */
enum class LensFacing {
    /** Front-facing camera (selfie) */
    FRONT,

    /** Back-facing camera (rear) */
    BACK
}
