package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Biometric operation result - sealed class for type safety
 */
sealed class BiometricResult {
    data class EnrollmentSuccess(val user: User) : BiometricResult()
    data class VerificationSuccess(val result: VerificationResult) : BiometricResult()
}

data class BiometricState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: BiometricResult? = null,
    val isSuccess: Boolean = false
)

class BiometricViewModel(
    private val enrollUserUseCase: EnrollUserUseCase,
    private val verifyUserUseCase: VerifyUserUseCase
) {
    private val _state = MutableStateFlow(BiometricState())
    val state: StateFlow<BiometricState> = _state.asStateFlow()

    suspend fun enrollFace(enrollmentData: EnrollmentData, imageBytes: ByteArray) {
        _state.value = BiometricState(isLoading = true)

        enrollUserUseCase(enrollmentData, imageBytes).fold(
            onSuccess = { user ->
                _state.value = BiometricState(
                    isLoading = false,
                    result = BiometricResult.EnrollmentSuccess(user),
                    isSuccess = true
                )
            },
            onFailure = { error ->
                _state.value = BiometricState(
                    isLoading = false,
                    error = error.message ?: "Face enrollment failed"
                )
            }
        )
    }

    suspend fun verifyFace(imageBytes: ByteArray) {
        _state.value = BiometricState(isLoading = true)

        verifyUserUseCase(imageBytes).fold(
            onSuccess = { verificationResult ->
                _state.value = BiometricState(
                    isLoading = false,
                    result = BiometricResult.VerificationSuccess(verificationResult),
                    isSuccess = true
                )
            },
            onFailure = { error ->
                _state.value = BiometricState(
                    isLoading = false,
                    error = error.message ?: "Face verification failed"
                )
            }
        )
    }

    fun clearState() {
        _state.value = BiometricState()
    }
}
