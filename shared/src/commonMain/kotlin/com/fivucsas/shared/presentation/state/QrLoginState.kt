package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthTokens

enum class QrLoginStatus {
    IDLE,
    WAITING_FOR_MOBILE_SCAN,
    WAITING_FOR_DESKTOP_APPROVAL,
    APPROVED,
    ERROR
}

data class QrLoginState(
    val isLoading: Boolean = false,
    val status: QrLoginStatus = QrLoginStatus.IDLE,
    val sessionId: String? = null,
    val qrPayload: String? = null,
    val error: String? = null,
    val role: UserRole? = null,
    val tokens: AuthTokens? = null
)
