package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthTokens

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tokens: AuthTokens? = null,
    val isSuccess: Boolean = false,
    val role: UserRole? = null,
    // MFA fields
    val mfaRequired: Boolean = false,
    val mfaSessionToken: String? = null,
    val mfaAvailableMethods: List<AvailableMethodDto>? = null,
    val mfaCurrentStep: Int = 1,
    val mfaTotalSteps: Int = 1
)
