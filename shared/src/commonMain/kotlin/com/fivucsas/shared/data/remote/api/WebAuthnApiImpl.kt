package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.WebAuthnRegistrationOptionsDto
import com.fivucsas.shared.data.remote.dto.WebAuthnRegistrationVerifyRequestDto
import com.fivucsas.shared.data.remote.dto.WebAuthnRegistrationVerifyResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * WebAuthn API implementation using Ktor HTTP client.
 *
 * Maps to Identity Core API WebAuthn endpoints:
 * - POST /api/v1/webauthn/register/options/{userId}
 * - POST /api/v1/webauthn/register/verify
 */
class WebAuthnApiImpl(
    private val client: HttpClient
) : WebAuthnApi {

    override suspend fun getRegistrationOptions(userId: String): WebAuthnRegistrationOptionsDto {
        return client.post("webauthn/register/options/$userId").body()
    }

    override suspend fun verifyRegistration(
        request: WebAuthnRegistrationVerifyRequestDto
    ): WebAuthnRegistrationVerifyResponseDto {
        return client.post("webauthn/register/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
