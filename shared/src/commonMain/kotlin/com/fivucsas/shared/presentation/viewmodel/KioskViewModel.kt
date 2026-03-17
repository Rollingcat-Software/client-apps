package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.config.AnimationConfig
import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.verification.CheckLivenessUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase
import com.fivucsas.shared.presentation.state.KioskScreen
import com.fivucsas.shared.presentation.state.KioskUiState
import com.fivucsas.shared.presentation.state.VerificationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Kiosk ViewModel - Enrollment and verification flows
 *
 * Features:
 * - Full enrollment flow with validation
 * - Camera capture (platform-provided via setCapturedImage)
 * - Identity verification via backend API
 * - Error handling with user-friendly messages
 */
class KioskViewModel(
    private val enrollUserUseCase: EnrollUserUseCase,
    private val verifyUserUseCase: VerifyUserUseCase,
    private val checkLivenessUseCase: CheckLivenessUseCase
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(KioskUiState())
    val uiState: StateFlow<KioskUiState> = _uiState.asStateFlow()

    private val _enrollmentData = MutableStateFlow(EnrollmentData())
    val enrollmentData: StateFlow<EnrollmentData> = _enrollmentData.asStateFlow()

    // NAVIGATION
    fun navigateToWelcome() {
        _uiState.update {
            it.copy(
                currentScreen = KioskScreen.WELCOME,
                errorMessage = null,
                successMessage = null,
                verificationResult = null,
                capturedImage = null
            )
        }
        resetEnrollmentData()
    }

    fun navigateToEnroll() {
        _uiState.update {
            it.copy(
                currentScreen = KioskScreen.ENROLL,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun navigateToVerify() {
        _uiState.update {
            it.copy(
                currentScreen = KioskScreen.VERIFY,
                errorMessage = null,
                successMessage = null,
                verificationResult = null,
                capturedImage = null
            )
        }
    }

    // ENROLLMENT DATA UPDATES
    fun updateFullName(name: String) {
        _enrollmentData.update { it.copy(fullName = name) }
    }

    fun updateEmail(email: String) {
        _enrollmentData.update { it.copy(email = email) }
    }

    fun updateIdNumber(idNumber: String) {
        _enrollmentData.update { it.copy(idNumber = idNumber) }
    }

    fun updatePhoneNumber(phone: String) {
        _enrollmentData.update { it.copy(phoneNumber = phone) }
    }

    fun updateAddress(address: String) {
        _enrollmentData.update { it.copy(address = address) }
    }

    // CAMERA CONTROL
    fun openCamera() {
        _uiState.update {
            it.copy(
                showCamera = true,
                errorMessage = null
            )
        }
    }

    fun startEnrollment() {
        // Start by opening camera for photo capture
        openCamera()
    }

    fun closeCamera() {
        _uiState.update { it.copy(showCamera = false) }
    }

    fun setCapturedImage(imageBytes: ByteArray) {
        _uiState.update {
            it.copy(
                capturedImage = imageBytes,
                showCamera = false,
                successMessage = "📸 Photo captured successfully!"
            )
        }

        // Auto-clear success message
        viewModelScope.launch {
            kotlinx.coroutines.delay(AnimationConfig.TOAST_DISPLAY_DURATION)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun captureImage() {
        // Platform camera should provide real image data via setCapturedImage().
        // This fallback generates a placeholder when no camera is available.
        _uiState.update {
            it.copy(
                showCamera = true,
                errorMessage = "Please use the camera to capture a photo"
            )
        }
    }

    // ENROLLMENT SUBMISSION
    fun submitEnrollment() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            val data = _enrollmentData.value

            // Validation
            val errors = mutableListOf<String>()
            if (data.fullName.isBlank()) errors.add("• Full name is required")
            if (data.email.isBlank()) errors.add("• Email is required")
            if (data.idNumber.isBlank()) errors.add("• ID number is required")
            if (_uiState.value.capturedImage == null) errors.add("• Photo is required")

            if (errors.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "❌ Validation failed:\n" + errors.joinToString("\n")
                    )
                }
                return@launch
            }

            try {
                // Call backend API through use case
                val result = enrollUserUseCase(data, _uiState.value.capturedImage!!)

                if (result.isSuccess) {
                    val enrollResult = result.getOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "✅ Enrollment Successful!\n\n" +
                                    "User: ${data.fullName}\n" +
                                    "Email: ${data.email}\n" +
                                    "ID: ${data.idNumber}\n\n" +
                                    "✓ Connected to live backend",
                            errorMessage = null
                        )
                    }

                    // Navigate back after 3 seconds
                    kotlinx.coroutines.delay(AnimationConfig.DELAY_SCREEN_TRANSITION)
                    navigateToWelcome()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Enrollment failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}\n\nPlease try again.",
                            successMessage = null
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Enrollment failed: ${e.message}\n\nPlease check your connection and try again."
                    )
                }
            }
        }
    }

    // VERIFICATION
    fun startVerification() {
        openCamera()
    }

    fun verifyWithCapturedImage() {
        if (_uiState.value.capturedImage == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "❌ Please capture a photo first"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                // First check liveness, then verify identity
                val livenessResult = checkLivenessUseCase(_uiState.value.capturedImage!!)
                if (livenessResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Liveness check failed: ${livenessResult.exceptionOrNull()?.message ?: "Please try again"}"
                        )
                    }
                    return@launch
                }

                // Use verifyUserUseCase with a placeholder userId for 1:N identification
                // The backend will match against enrolled faces
                val verifyResult = verifyUserUseCase("kiosk-identify", _uiState.value.capturedImage!!)

                if (verifyResult.isSuccess) {
                    val verification = verifyResult.getOrNull()
                    val result = VerificationResult(
                        isVerified = verification?.isVerified ?: false,
                        userName = verification?.message ?: "Unknown",
                        confidence = verification?.confidence ?: 0f,
                        message = if (verification?.isVerified == true) {
                            "Identity verified successfully!"
                        } else {
                            "Could not verify identity. Please try again."
                        }
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verificationResult = result,
                            successMessage = if (result.isVerified) {
                                "Verification Successful!\n\n" +
                                        "User: ${result.userName}\n" +
                                        "Confidence: ${result.confidence.toInt()}%\n" +
                                        "Status: Verified"
                            } else null,
                            errorMessage = if (!result.isVerified) {
                                "Verification Failed\n\n" +
                                        "${result.message}\n" +
                                        "Confidence: ${result.confidence.toInt()}%"
                            } else null
                        )
                    }

                    // Auto-clear after 5 seconds if successful
                    if (result.isVerified) {
                        kotlinx.coroutines.delay(AnimationConfig.DELAY_AUTO_DISMISS_LONG)
                        navigateToWelcome()
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Verification failed: ${verifyResult.exceptionOrNull()?.message ?: "Unknown error"}"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Verification service unavailable: ${e.message}"
                    )
                }
            }
        }
    }

    // MESSAGE CONTROL
    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    // HELPER
    private fun resetEnrollmentData() {
        _enrollmentData.value = EnrollmentData()
    }
}
