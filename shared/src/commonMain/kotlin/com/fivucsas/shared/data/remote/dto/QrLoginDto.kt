package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.QrLoginSession
import com.fivucsas.shared.domain.model.QrLoginSessionStatus
import kotlinx.serialization.Serializable

@Serializable
data class QrLoginCreateSessionRequestDto(
    val platform: String
)

@Serializable
data class QrLoginApproveRequestDto(
    val approverPlatform: String
)

@Serializable
data class QrLoginSessionResponseDto(
    val sessionId: String,
    val qrContent: String,
    val status: String,
    val expiresAtEpochSeconds: Long? = null,
    val message: String? = null
)

fun QrLoginSessionResponseDto.toModel(): QrLoginSession {
    return QrLoginSession(
        sessionId = sessionId,
        qrContent = qrContent,
        status = status.toSessionStatus(),
        expiresAtEpochSeconds = expiresAtEpochSeconds,
        message = message
    )
}

private fun String.toSessionStatus(): QrLoginSessionStatus {
    return when (trim().uppercase()) {
        "PENDING_SCAN" -> QrLoginSessionStatus.PENDING_SCAN
        "PENDING_APPROVAL" -> QrLoginSessionStatus.PENDING_APPROVAL
        "APPROVED" -> QrLoginSessionStatus.APPROVED
        "EXPIRED" -> QrLoginSessionStatus.EXPIRED
        "REJECTED" -> QrLoginSessionStatus.REJECTED
        else -> QrLoginSessionStatus.FAILED
    }
}
