package com.fivucsas.shared.domain.model

/**
 * Invitation sent by a tenant admin or root admin to a user.
 *
 * Represents an outbound invitation record — the admin perspective.
 * For the recipient's perspective, see [ReceivedInvite].
 */
data class Invite(
    val id: String,
    val email: String,
    val role: String,
    val tenantId: String? = null,
    val tenantName: String? = null,
    val status: InviteStatus,
    val createdAt: String,
    val expiresAt: String
)

/**
 * Status of an outbound invitation (admin perspective).
 */
enum class InviteStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    REVOKED;

    companion object {
        fun fromString(value: String): InviteStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}

/**
 * Invitation received by a user from a tenant.
 *
 * Represents an inbound invitation record — the recipient perspective.
 * For the admin's perspective, see [Invite].
 */
data class ReceivedInvite(
    val id: String,
    val tenantName: String,
    val invitedBy: String,
    val role: String,
    val receivedAt: String,
    val expiresAt: String,
    val status: ReceivedInviteStatus
)

/**
 * Status of an inbound invitation (recipient perspective).
 *
 * Differs from [InviteStatus] because recipients can DECLINE
 * (whereas admins can REVOKE).
 */
enum class ReceivedInviteStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED;

    companion object {
        fun fromString(value: String): ReceivedInviteStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}
