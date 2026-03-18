package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.AuthSession
import com.fivucsas.shared.domain.repository.SessionRepository
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.SessionUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel(
    private val sessionRepository: SessionRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            sessionRepository.getSessions().fold(
                onSuccess = { sessions ->
                    _uiState.update {
                        it.copy(isLoading = false, sessions = sessions)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load sessions")
                        )
                    }
                }
            )
        }
    }

    fun showRevokeDialog(session: AuthSession) {
        _uiState.update {
            it.copy(showRevokeDialog = true, sessionToRevoke = session)
        }
    }

    fun hideRevokeDialog() {
        _uiState.update {
            it.copy(showRevokeDialog = false, sessionToRevoke = null)
        }
    }

    fun confirmRevoke() {
        val session = _uiState.value.sessionToRevoke ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showRevokeDialog = false) }

            sessionRepository.revokeSession(session.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sessionToRevoke = null,
                            successMessage = s(StringKey.SESSION_REVOKED)
                        )
                    }
                    loadSessions()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "revoke session")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
