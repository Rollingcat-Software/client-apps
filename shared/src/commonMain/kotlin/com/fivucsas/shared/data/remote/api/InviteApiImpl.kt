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
        private const val BASE_PATH = "invites"
        private const val RECEIVED_PATH = "$BASE_PATH/received"
    }

    // ── Admin operations ────────────────────────────────────────────────────

    override suspend fun getInvites(): List<InviteDto> {
        return client.get(BASE_PATH).body()
    }

    override suspend fun createInvite(request: CreateInviteRequestDto): InviteDto {
        return client.post(BASE_PATH) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun revokeInvite(inviteId: String): InviteDto {
        return client.put("$BASE_PATH/$inviteId/revoke").body()
    }

    override suspend fun resendInvite(inviteId: String): InviteDto {
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
