package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.Enrollment

data class EnrollmentUiState(
    val enrollments: List<Enrollment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
