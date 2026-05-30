package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.NfcVerifyAuthenticityRequest
import com.fivucsas.shared.data.remote.dto.NfcVerifyAuthenticityResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class NfcAuthenticityApiImpl(
    private val client: HttpClient
) : NfcAuthenticityApi {

    override suspend fun verifyAuthenticity(
        sodB64: String,
        dg1B64: String?,
        dg2B64: String?
    ): NfcVerifyAuthenticityResponse {
        val response: HttpResponse = client.post("nfc/verify-authenticity") {
            contentType(ContentType.Application.Json)
            setBody(NfcVerifyAuthenticityRequest(sod = sodB64, dg1 = dg1B64, dg2 = dg2B64))
        }
        // The server returns a structured verdict body on BOTH 200 (authentic)
        // and 422 (not authentic, fail-closed) — and 400 for a missing SOD.
        // The shared HttpClient does not throw on non-2xx (only 401 is
        // intercepted for refresh), so we can read the body directly.
        return when (response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.UnprocessableEntity -> response.body()
            HttpStatusCode.BadRequest -> NfcVerifyAuthenticityResponse(
                success = false,
                authentic = false,
                errorCode = "NFC_PA_MISSING_SOD",
                reasonCode = "MISSING_SOD"
            )
            else -> NfcVerifyAuthenticityResponse(
                success = false,
                authentic = false,
                errorCode = "NFC_PA_ERROR",
                reasonCode = "HTTP_${response.status.value}"
            )
        }
    }
}
