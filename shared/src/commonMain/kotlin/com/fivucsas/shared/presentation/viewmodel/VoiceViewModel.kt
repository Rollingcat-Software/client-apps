package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.VoiceRepository
import com.fivucsas.shared.presentation.state.VoiceMode
import com.fivucsas.shared.presentation.state.VoiceSearchUiResult
import com.fivucsas.shared.presentation.state.VoiceUiState
import com.fivucsas.shared.presentation.state.VoiceVerifyUiResult
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoiceViewModel(
    private val voiceRepository: VoiceRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    fun setMode(mode: VoiceMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    fun updateRecordingSeconds(seconds: Int) {
        _uiState.update { it.copy(recordingSeconds = seconds) }
    }

    fun enroll(userId: String, voiceBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }

            voiceRepository.enroll(userId, voiceBase64).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            enrollSuccess = result.success,
                            successMessage = if (result.success) result.message.ifBlank { "Voice enrolled successfully" } else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "enroll voice")
                        )
                    }
                }
            )
        }
    }

    fun verify(userId: String, voiceBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }

            voiceRepository.verify(userId, voiceBase64).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            verifyResult = VoiceVerifyUiResult(
                                verified = result.verified,
                                confidence = result.confidence,
                                message = result.message
                            ),
                            successMessage = if (result.verified) "Voice verified successfully" else null,
                            errorMessage = if (!result.verified) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "verify voice")
                        )
                    }
                }
            )
        }
    }

    fun search(voiceBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null, successMessage = null) }

            voiceRepository.search(voiceBase64).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            searchResult = VoiceSearchUiResult(
                                found = result.found,
                                userId = result.userId,
                                confidence = result.confidence,
                                message = result.message
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "search voice")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun reset() {
        _uiState.value = VoiceUiState()
    }
}
