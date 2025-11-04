package com.fivucsas.desktop.viewmodel

import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.desktop.data.DesktopCameraService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Desktop-specific extension for KioskViewModel
 * Adds real camera integration
 */
class DesktopKioskViewModel(
    private val kioskViewModel: KioskViewModel
) {
    private val cameraService = DesktopCameraService()
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _capturedImageBytes = MutableStateFlow<ByteArray?>(null)
    val capturedImageBytes: StateFlow<ByteArray?> = _capturedImageBytes.asStateFlow()
    
    /**
     * Capture image from real camera
     */
    fun captureFromCamera(onSuccess: (ByteArray) -> Unit, onError: (String) -> Unit) {
        scope.launch {
            val result = cameraService.captureFrame()
            if (result.isSuccess) {
                result.getOrNull()?.let { imageBytes ->
                    _capturedImageBytes.value = imageBytes
                    onSuccess(imageBytes)
                }
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to capture image")
            }
        }
    }
    
    /**
     * Release camera resources
     */
    fun releaseCamera() {
        scope.launch {
            cameraService.release()
        }
    }
    
    /**
     * Get camera service for preview
     */
    fun getCameraService(): DesktopCameraService = cameraService
}
