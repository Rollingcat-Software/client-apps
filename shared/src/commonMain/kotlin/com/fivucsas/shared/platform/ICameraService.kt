package com.fivucsas.shared.platform

/**
 * Camera Service Interface
 *
 * Platform abstraction for camera operations.
 * Enables testability and cross-platform support.
 */
interface ICameraService {
    /**
     * Initialize the camera
     * @return Result indicating success or failure
     */
    suspend fun initialize(): Result<Unit>

    /**
     * Capture a single frame from the camera
     * @return Result containing image bytes or error
     */
    suspend fun captureFrame(): Result<ByteArray>

    /**
     * Check if camera is available on the device
     * @return true if camera is available
     */
    fun isAvailable(): Boolean

    /**
     * Release camera resources
     */
    suspend fun release()

    /**
     * Get camera preview dimensions
     * @return Pair of width and height
     */
    fun getPreviewDimensions(): Pair<Int, Int>
}
