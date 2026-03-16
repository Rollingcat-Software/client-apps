package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.usecase.admin.GetMyProfileUseCase
import com.fivucsas.shared.presentation.state.UserProfileUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(UserProfileUiState())
    val state: StateFlow<UserProfileUiState> = _state.asStateFlow()

    fun loadProfile() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            getMyProfileUseCase().fold(
                onSuccess = { user ->
                    _state.update {
                        UserProfileUiState(
                            user = user,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load profile"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
