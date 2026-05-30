package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.PendingApproveLogin
import kotlinx.serialization.Serializable

/**
 * Pending approve-login request DTO — server returns camelCase JSON
 * (Spring Boot / Jackson) from `GET /api/v1/auth/approve-login/pending`.
 *
 * `matchNumber` is intentionally a STRING: the backend zero-pads it to two
 * digits (e.g. "07") so the leading zero survives the round-trip. Parsing it
 * as a number on the client would drop the leading zero on the 00–09 prompts.
 */
@Serializable
data class PendingApproveLoginDto(
    val sessionId: String = "",
    val matchNumber: String = "",
    val initiatorIp: String? = null,
    val initiatorUserAgent: String? = null,
    val createdAtEpochSeconds: Long = 0
)

/**
 * Body for `POST /api/v1/auth/approve-login/session/{sessionId}/decide`.
 *
 * `decision` is "allow" or "deny"; `matchNumber` is required on allow and must
 * equal the number shown on this device (number-matching).
 */
@Serializable
data class ApproveLoginDecisionDto(
    val decision: String,
    val matchNumber: String? = null
)

fun PendingApproveLoginDto.toDomain(): PendingApproveLogin = PendingApproveLogin(
    sessionId = sessionId,
    matchNumber = matchNumber,
    initiatorIp = initiatorIp,
    initiatorUserAgent = initiatorUserAgent,
    createdAtEpochSeconds = createdAtEpochSeconds
)
