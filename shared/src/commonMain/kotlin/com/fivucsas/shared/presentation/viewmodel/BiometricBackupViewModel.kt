package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.EnrollmentRepository
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BiometricBackupUiState(
    val isLoading: Boolean = false,
    val enrollments: List<Enrollment> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleteConfirmDialogVisible: Boolean = false
)

/**
 * ViewModel for biometric data export/backup screen.
 * GDPR/KVKK compliance: shows enrolled biometrics and allows deletion.
 */
class BiometricBackupViewModel(
    private val enrollmentRepository: EnrollmentRepository,
    private val biometricRepository: BiometricRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(BiometricBackupUiState())
    val uiState: StateFlow<BiometricBackupUiState> = _uiState.asStateFlow()

    fun loadEnrollments(userId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            enrollmentRepository.getEnrollments(userId).fold(
                onSuccess = { enrollments ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            enrollments = enrollments
                        )
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

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(deleteConfirmDialogVisible = true) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(deleteConfirmDialogVisible = false) }
    }

    fun deleteAllBiometricData(userId: String) {
        scope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteConfirmDialogVisible = false) }

            biometricRepository.deleteBiometricData(userId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            enrollments = emptyList(),
                            successMessage = "All biometric data has been deleted successfully."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "delete biometric data")
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
