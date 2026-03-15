package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.VerificationResult

sealed class BiometricResult {
    data class EnrollmentSuccess(val user: User) : BiometricResult()
    data class VerificationSuccess(val result: VerificationResult) : BiometricResult()
}

data class BiometricState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: BiometricResult? = null,
    val isSuccess: Boolean = false
)
