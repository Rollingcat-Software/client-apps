package com.fivucsas.desktop.platform

import com.fivucsas.shared.config.BiometricConfig
import com.fivucsas.shared.platform.ICameraService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Desktop Camera Service Implementation
 *
 * Desktop implementation of ICameraService using OpenCV or mock data.
 * In production, this would integrate with actual camera hardware.
 */
class DesktopCameraServiceImpl : ICameraService {

    private var isInitialized = false
    private val previewWidth = BiometricConfig.PREFERRED_IMAGE_WIDTH
    private val previewHeight = BiometricConfig.PREFERRED_IMAGE_HEIGHT

    override suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In production: Initialize OpenCV camera
            // For now: Mock initialization
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun captureFrame(): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext Result.failure(IllegalStateException("Camera not initialized"))
        }

        try {
            // In production: Capture actual frame from camera
            // For now: Return mock image data
            val mockImage = ByteArray(previewWidth * previewHeight * 3) {
                Random.nextInt(256).toByte()
            }
            Result.success(mockImage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isAvailable(): Boolean {
        // In production: Check if camera hardware is available
        return true
    }

    override suspend fun release() {
        withContext(Dispatchers.IO) {
            // In production: Release camera resources
            isInitialized = false
        }
    }

    override fun getPreviewDimensions(): Pair<Int, Int> {
        return Pair(previewWidth, previewHeight)
    }
}
