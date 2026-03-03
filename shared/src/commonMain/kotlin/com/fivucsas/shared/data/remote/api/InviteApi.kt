package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateInviteRequestDto
import com.fivucsas.shared.data.remote.dto.InviteDto
import com.fivucsas.shared.data.remote.dto.ReceivedInviteDto

/**
 * Invite API interface
 *
 * Defines contract for invitation management.
 * Uses the Identity Core HTTP client (port 8080).
 *
 * Admin endpoints (outbound invitations):
 * - GET    /invites              → getInvites()
 * - POST   /invites              → createInvite()
 * - PUT    /invites/{id}/revoke  → revokeInvite()
 * - POST   /invites/{id}/resend → resendInvite()
 *
 * Member endpoints (inbound invitations):
 * - GET    /invites/received             → getReceivedInvites()
 * - PUT    /invites/received/{id}/accept  → acceptInvite()
 * - PUT    /invites/received/{id}/decline → declineInvite()
 */
interface InviteApi {

    // ── Admin operations (outbound) ─────────────────────────────────────────

    /**
     * List all invitations sent by the current tenant.
     * GET /invites
     */
    suspend fun getInvites(): List<InviteDto>

    /**
     * Create and send a new invitation.
     * POST /invites
     */
    suspend fun createInvite(request: CreateInviteRequestDto): InviteDto

    /**
     * Revoke a pending invitation.
     * PUT /invites/{id}/revoke
     */
    suspend fun revokeInvite(inviteId: String): InviteDto

    /**
     * Resend an expired or pending invitation.
     * POST /invites/{id}/resend
     */
    suspend fun resendInvite(inviteId: String): InviteDto

    // ── Member operations (inbound) ─────────────────────────────────────────

    /**
     * List all invitations received by the current user.
     * GET /invites/received
     */
    suspend fun getReceivedInvites(): List<ReceivedInviteDto>

    /**
     * Accept a pending invitation.
     * PUT /invites/received/{id}/accept
     */
    suspend fun acceptInvite(inviteId: String): ReceivedInviteDto

    /**
     * Decline a pending invitation.
     * PUT /invites/received/{id}/decline
     */
    suspend fun declineInvite(inviteId: String): ReceivedInviteDto
}
