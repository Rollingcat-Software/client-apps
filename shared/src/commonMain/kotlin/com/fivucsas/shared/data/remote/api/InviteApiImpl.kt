package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateInviteRequestDto
import com.fivucsas.shared.data.remote.dto.InviteDto
import com.fivucsas.shared.data.remote.dto.ReceivedInviteDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Invite API implementation using Ktor HttpClient.
 *
 * Uses the Identity Core HTTP client (same as AuthApi, IdentityApi).
 */
class InviteApiImpl(
    private val client: HttpClient
) : InviteApi {

    companion object {
        // Backend invite/guest endpoints live under /api/v1/guests (UserController,
        // merged from GuestController) — there is NO /api/v1/invites controller.
        private const val BASE_PATH = "guests"
        // NOTE: member-side "received invites" endpoints (RECEIVED_PATH) have NO
        // backend equivalent. The only inbound flow the API exposes is the
        // token-based POST /api/v1/guests/accept (public, no listing / decline).
        // These paths are left as-is pending a backend listing endpoint.
        private const val RECEIVED_PATH = "invites/received"
    }

    // ── Admin operations ────────────────────────────────────────────────────

    override suspend fun getInvites(): List<InviteDto> {
        // GET /api/v1/guests — list guest invitations for the current tenant.
        return client.get(BASE_PATH).body()
    }

    override suspend fun createInvite(request: CreateInviteRequestDto): InviteDto {
        // POST /api/v1/guests/invite — create + send a guest invitation.
        return client.post("$BASE_PATH/invite") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun revokeInvite(inviteId: String): InviteDto {
        // POST /api/v1/guests/invitations/{id}/revoke — revoke a PENDING invitation.
        return client.post("$BASE_PATH/invitations/$inviteId/revoke").body()
    }

    override suspend fun resendInvite(inviteId: String): InviteDto {
        // POST /api/v1/guests/{id}/resend — resend a pending invitation email.
        return client.post("$BASE_PATH/$inviteId/resend").body()
    }

    // ── Member operations ───────────────────────────────────────────────────

    override suspend fun getReceivedInvites(): List<ReceivedInviteDto> {
        return client.get(RECEIVED_PATH).body()
    }

    override suspend fun acceptInvite(inviteId: String): ReceivedInviteDto {
        return client.put("$RECEIVED_PATH/$inviteId/accept").body()
    }

    override suspend fun declineInvite(inviteId: String): ReceivedInviteDto {
        return client.put("$RECEIVED_PATH/$inviteId/decline").body()
    }
}
