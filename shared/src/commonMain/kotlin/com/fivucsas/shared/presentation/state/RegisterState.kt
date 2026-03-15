package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthTokens

data class RegisterState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tokens: AuthTokens? = null,
    val isSuccess: Boolean = false,
    val role: UserRole? = null
)
