package com.fivucsas.shared.domain.model

/**
 * A pending number-matching approve-login request awaiting this (authenticated)
 * user's allow/deny decision. Surfaced on the "Login requests" screen by
 * polling `GET /api/v1/auth/approve-login/pending`.
 *
 * @property matchNumber two-digit number (kept as a String so a leading zero
 *   like "07" survives) that the initiator's device also shows; the approver
 *   must match it to allow.
 */
data class PendingApproveLogin(
    val sessionId: String,
    val matchNumber: String,
    val initiatorIp: String?,
    val initiatorUserAgent: String?,
    val createdAtEpochSeconds: Long
)
