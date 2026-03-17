package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.model.ReceivedInvite

/**
 * Invite repository interface
 *
 * Handles all invitation operations for both admin (outbound)
 * and member (inbound) perspectives.
 *
 * Implementations can target the API or local cache.
 */
interface InviteRepository {

    // ── Admin operations (outbound) ─────────────────────────────────────────

    /**
     * Get all invitations sent by the current tenant.
     * @return Result with invite list or error
     */
    suspend fun getInvites(): Result<List<Invite>>

    /**
     * Create and send a new invitation.
     * @param email Recipient email address
     * @param role Role to assign upon acceptance
     * @param tenantId Target tenant ID (optional for root-level invites)
     * @return Result with created invite or error
     */
    suspend fun createInvite(
        email: String,
        role: String,
        tenantId: String? = null
    ): Result<Invite>

    /**
     * Revoke a pending invitation.
     * @param inviteId Invitation ID to revoke
     * @return Result with updated invite or error
     */
    suspend fun revokeInvite(inviteId: String): Result<Invite>

    /**
     * Resend an expired or pending invitation.
     * @param inviteId Invitation ID to resend
     * @return Result with updated invite or error
     */
    suspend fun resendInvite(inviteId: String): Result<Invite>

    // ── Member operations (inbound) ─────────────────────────────────────────

    /**
     * Get all invitations received by the current user.
     * @return Result with received invite list or error
     */
    suspend fun getReceivedInvites(): Result<List<ReceivedInvite>>

    /**
     * Accept a pending invitation.
     * @param inviteId Invitation ID to accept
     * @return Result with updated invite or error
     */
    suspend fun acceptInvite(inviteId: String): Result<ReceivedInvite>

    /**
     * Decline a pending invitation.
     * @param inviteId Invitation ID to decline
     * @return Result with updated invite or error
     */
    suspend fun declineInvite(inviteId: String): Result<ReceivedInvite>
}
