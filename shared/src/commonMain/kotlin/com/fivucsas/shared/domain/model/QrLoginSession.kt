package com.fivucsas.shared.domain.model

enum class QrLoginSessionStatus {
    PENDING_SCAN,
    PENDING_APPROVAL,
    APPROVED,
    EXPIRED,
    REJECTED,
    FAILED
}

data class QrLoginSession(
    val sessionId: String,
    val qrContent: String,
    val status: QrLoginSessionStatus,
    val expiresAtEpochSeconds: Long? = null,
    val message: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val role: String? = null
)
