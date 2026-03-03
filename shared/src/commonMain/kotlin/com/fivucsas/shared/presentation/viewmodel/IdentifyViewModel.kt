package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.usecase.verification.IdentifyUserUseCase
import com.fivucsas.shared.presentation.state.IdentifyUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IdentifyViewModel(
    private val identifyUserUseCase: IdentifyUserUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(IdentifyUiState())
    val state: StateFlow<IdentifyUiState> = _state.asStateFlow()

    fun identifyFace(imageBytes: ByteArray) {
        _state.update { IdentifyUiState(isLoading = true) }

        scope.launch {
            identifyUserUseCase(imageBytes).fold(
                onSuccess = { result ->
                    _state.update {
                        IdentifyUiState(
                            isLoading = false,
                            result = result,
                            isSuccess = result.isMatch
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        IdentifyUiState(
                            isLoading = false,
                            errorMessage = error.message ?: "Face identification failed"
                        )
                    }
                }
            )
        }
    }

    fun onCaptureError(message: String) {
        _state.update { IdentifyUiState(errorMessage = message) }
    }

    fun clearState() {
        _state.update { IdentifyUiState() }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
