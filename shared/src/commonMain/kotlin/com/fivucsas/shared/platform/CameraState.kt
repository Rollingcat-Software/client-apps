package com.fivucsas.shared.platform

/**
 * Camera State Representation
 *
 * Represents all possible states of the camera service.
 * Follows State pattern for clean state management.
 */
sealed class CameraState {
    /**
     * Camera is not initialized
     */
    data object Idle : CameraState()

    /**
     * Camera is initializing
     */
    data object Initializing : CameraState()

    /**
     * Camera is ready but not previewing
     */
    data object Ready : CameraState()

    /**
     * Camera is actively showing preview
     */
    data object Previewing : CameraState()

    /**
     * Camera is capturing an image
     */
    data object Capturing : CameraState()

    /**
     * Camera encountered an error
     * @param error The error that occurred
     */
    data class Error(val error: Throwable) : CameraState()

    /**
     * Camera has been released/closed
     */
    data object Released : CameraState()
}
