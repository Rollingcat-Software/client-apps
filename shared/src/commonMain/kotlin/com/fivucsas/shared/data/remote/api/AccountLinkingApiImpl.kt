package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.IdentityMeDto
import com.fivucsas.shared.data.remote.dto.LinkConfirmRequest
import com.fivucsas.shared.data.remote.dto.LinkInitiateRequest
import com.fivucsas.shared.data.remote.dto.SwitchMembershipRequest
import com.fivucsas.shared.data.remote.dto.SwitchMembershipResponse
import com.fivucsas.shared.data.remote.dto.UnlinkRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AccountLinkingApiImpl(
    private val client: HttpClient
) : AccountLinkingApi {

    override suspend fun getIdentityMe(): IdentityMeDto =
        client.get("identity/me").body()

    override suspend fun initiateLink(email: String) {
        client.post("identity/link/initiate") {
            contentType(ContentType.Application.Json)
            setBody(LinkInitiateRequest(email))
        }
    }

    override suspend fun confirmLink(email: String, otp: String, password: String) {
        client.post("identity/link/confirm") {
            contentType(ContentType.Application.Json)
            setBody(LinkConfirmRequest(email = email, otp = otp, password = password))
        }
    }

    override suspend fun unlink(membershipUserId: String) {
        client.post("identity/unlink") {
            contentType(ContentType.Application.Json)
            setBody(UnlinkRequest(membershipUserId))
        }
    }

    override suspend fun switchMembership(targetUserId: String): SwitchMembershipResponse =
        client.post("auth/switch-membership") {
            contentType(ContentType.Application.Json)
            setBody(SwitchMembershipRequest(targetUserId))
        }.body()
}
