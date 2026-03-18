package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.EnrollmentRepository
import com.fivucsas.shared.presentation.state.EnrollmentUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnrollmentViewModel(
    private val enrollmentRepository: EnrollmentRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(EnrollmentUiState())
    val uiState: StateFlow<EnrollmentUiState> = _uiState.asStateFlow()

    fun loadEnrollments(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            enrollmentRepository.getEnrollments(userId).fold(
                onSuccess = { enrollments ->
                    _uiState.update {
                        it.copy(isLoading = false, enrollments = enrollments)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load enrollments")
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
