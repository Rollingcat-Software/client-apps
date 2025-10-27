package com.fivucsas.mobile.presentation.biometric

import com.fivucsas.mobile.domain.model.BiometricResult
import com.fivucsas.mobile.domain.usecase.EnrollFaceUseCase
import com.fivucsas.mobile.domain.usecase.VerifyFaceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BiometricState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: BiometricResult? = null,
    val isSuccess: Boolean = false
)

class BiometricViewModel(
    private val enrollFaceUseCase: EnrollFaceUseCase,
    private val verifyFaceUseCase: VerifyFaceUseCase
) {
    private val _state = MutableStateFlow(BiometricState())
    val state: StateFlow<BiometricState> = _state.asStateFlow()

    suspend fun enrollFace(userId: String, imageBytes: ByteArray) {
        _state.value = BiometricState(isLoading = true)

        enrollFaceUseCase(userId, imageBytes).fold(
            onSuccess = { result ->
                _state.value = BiometricState(
                    isLoading = false,
                    result = result,
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

    suspend fun verifyFace(userId: String, imageBytes: ByteArray) {
        _state.value = BiometricState(isLoading = true)

        verifyFaceUseCase(userId, imageBytes).fold(
            onSuccess = { result ->
                _state.value = BiometricState(
                    isLoading = false,
                    result = result,
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
