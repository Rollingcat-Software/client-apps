package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.AuthFlowRepository
import com.fivucsas.shared.presentation.state.AuthFlowUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthFlowViewModel(
    private val authFlowRepository: AuthFlowRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(AuthFlowUiState())
    val uiState: StateFlow<AuthFlowUiState> = _uiState.asStateFlow()

    fun loadAuthFlows(tenantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authFlowRepository.getAuthFlows(tenantId).fold(
                onSuccess = { flows ->
                    _uiState.update {
                        it.copy(isLoading = false, flows = flows)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load auth flows")
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
