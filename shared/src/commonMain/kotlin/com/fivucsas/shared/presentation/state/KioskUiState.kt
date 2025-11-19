package com.fivucsas.shared.presentation.state

/**
 * UI State for Kiosk screens
 *
 * Represents the current state of the kiosk UI.
 * Immutable data class - changes create new instances.
 *
 * Benefits:
 * - Type-safe state management
 * - Easy to test
 * - Clear state transitions
 * - Replayable (for debugging)
 */
data class KioskUiState(
    val currentScreen: KioskScreen = KioskScreen.WELCOME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val capturedImage: ByteArray? = null,
    val showCamera: Boolean = false,
    val verificationResult: VerificationResult? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as KioskUiState
        if (currentScreen != other.currentScreen) return false
        if (isLoading != other.isLoading) return false
        if (errorMessage != other.errorMessage) return false
        if (successMessage != other.successMessage) return false
        if (capturedImage != null) {
            if (other.capturedImage == null) return false
            if (!capturedImage.contentEquals(other.capturedImage)) return false
        } else if (other.capturedImage != null) return false
        if (showCamera != other.showCamera) return false
        if (verificationResult != other.verificationResult) return false
        return true
    }

    override fun hashCode(): Int {
        var result = currentScreen.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (successMessage?.hashCode() ?: 0)
        result = 31 * result + (capturedImage?.contentHashCode() ?: 0)
        result = 31 * result + showCamera.hashCode()
        result = 31 * result + (verificationResult?.hashCode() ?: 0)
        return result
    }
}

data class VerificationResult(
    val isVerified: Boolean,
    val userName: String,
    val confidence: Float,
    val message: String
)

/**
 * Kiosk screen enumeration
 */
enum class KioskScreen {
    WELCOME,
    ENROLL,
    VERIFY
}
