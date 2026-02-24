package com.fivucsas.shared.presentation.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IdentifyResult(
    val userId: String,
    val name: String,
    val confidence: Float,
    val isMatch: Boolean
)

data class IdentifyUiState(
    val isLoading: Boolean = false,
    val result: IdentifyResult? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class IdentifyViewModel {
    private val _state = MutableStateFlow(IdentifyUiState())
    val state: StateFlow<IdentifyUiState> = _state.asStateFlow()

    fun identifyFace(imageBytes: ByteArray) {
        _state.value = IdentifyUiState(isLoading = true)

        // Mock: simulate a successful 1:N identification
        _state.value = IdentifyUiState(
            isLoading = false,
            result = IdentifyResult(
                userId = "USR-0042",
                name = "Jane Doe",
                confidence = 0.93f,
                isMatch = true
            ),
            isSuccess = true
        )
    }

    fun onCaptureError(message: String) {
        _state.value = IdentifyUiState(errorMessage = message)
    }

    fun clearState() {
        _state.value = IdentifyUiState()
    }
}
