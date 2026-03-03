package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.model.InviteStatus
import com.fivucsas.shared.domain.model.ReceivedInvite
import com.fivucsas.shared.domain.model.ReceivedInviteStatus
import kotlinx.serialization.Serializable

/**
 * Response DTO for an outbound invitation (admin perspective).
 *
 * Returned by:
 * - GET /invites
 * - POST /invites
 * - PUT /invites/{id}/revoke
 */
@Serializable
data class InviteDto(
    val id: String,
    val email: String,
    val role: String,
    val tenantId: String? = null,
    val tenantName: String? = null,
    val status: String,
    val createdAt: String,
    val expiresAt: String
)

/**
 * Request DTO for creating a new invitation.
 *
 * Used by: POST /invites
 */
@Serializable
data class CreateInviteRequestDto(
    val email: String,
    val role: String,
    val tenantId: String? = null
)

/**
 * Response DTO for an inbound invitation (recipient perspective).
 *
 * Returned by:
 * - GET /invites/received
 * - PUT /invites/{id}/accept
 * - PUT /invites/{id}/decline
 */
@Serializable
data class ReceivedInviteDto(
    val id: String,
    val tenantName: String,
    val invitedBy: String,
    val role: String,
    val receivedAt: String,
    val expiresAt: String,
    val status: String
)

// ── Outbound invite mappers ─────────────────────────────────────────────────

fun InviteDto.toModel(): Invite {
    return Invite(
        id = id,
        email = email,
        role = role,
        tenantId = tenantId,
        tenantName = tenantName,
        status = InviteStatus.fromString(status),
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}

fun Invite.toDto(): InviteDto {
    return InviteDto(
        id = id,
        email = email,
        role = role,
        tenantId = tenantId,
        tenantName = tenantName,
        status = status.name,
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}

fun List<InviteDto>.toModels(): List<Invite> = map { it.toModel() }

fun List<Invite>.toDtos(): List<InviteDto> = map { it.toDto() }

// ── Received invite mappers ─────────────────────────────────────────────────

fun ReceivedInviteDto.toModel(): ReceivedInvite {
    return ReceivedInvite(
        id = id,
        tenantName = tenantName,
        invitedBy = invitedBy,
        role = role,
        receivedAt = receivedAt,
        expiresAt = expiresAt,
        status = ReceivedInviteStatus.fromString(status)
    )
}

fun ReceivedInvite.toDto(): ReceivedInviteDto {
    return ReceivedInviteDto(
        id = id,
        tenantName = tenantName,
        invitedBy = invitedBy,
        role = role,
        receivedAt = receivedAt,
        expiresAt = expiresAt,
        status = status.name
    )
}

fun List<ReceivedInviteDto>.toReceivedModels(): List<ReceivedInvite> = map { it.toModel() }
