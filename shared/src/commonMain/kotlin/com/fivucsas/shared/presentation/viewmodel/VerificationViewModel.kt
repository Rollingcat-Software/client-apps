package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.VerificationRepository
import com.fivucsas.shared.presentation.state.VerificationUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerificationViewModel(
    private val verificationRepository: VerificationRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun loadFlows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            verificationRepository.getFlows().fold(
                onSuccess = { flows ->
                    _uiState.update { it.copy(isLoading = false, flows = flows) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load verification flows")
                        )
                    }
                }
            )
        }
    }

    fun loadSessions(status: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, statusFilter = status) }

            verificationRepository.getSessions(status).fold(
                onSuccess = { sessions ->
                    _uiState.update { it.copy(isLoading = false, sessions = sessions) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load verification sessions")
                        )
                    }
                }
            )
        }
    }

    fun loadSessionDetail(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            verificationRepository.getSession(sessionId).fold(
                onSuccess = { session ->
                    _uiState.update { it.copy(isLoading = false, selectedSession = session) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load session detail")
                        )
                    }
                }
            )
        }
    }

    fun startSession(flowId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            verificationRepository.startSession(flowId, userId).fold(
                onSuccess = { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedSession = session,
                            successMessage = "Verification session started"
                        )
                    }
                    // Refresh sessions list
                    loadSessions(_uiState.value.statusFilter)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "start verification session")
                        )
                    }
                }
            )
        }
    }

    fun filterByStatus(status: String?) {
        loadSessions(status)
    }

    fun clearSelectedSession() {
        _uiState.update { it.copy(selectedSession = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
