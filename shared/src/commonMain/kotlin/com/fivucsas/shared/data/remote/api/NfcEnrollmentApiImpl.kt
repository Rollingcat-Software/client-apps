package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.NfcEnrollRequest
import com.fivucsas.shared.data.remote.dto.NfcEnrollResponse
import com.fivucsas.shared.data.remote.dto.NfcVerifyRequest
import com.fivucsas.shared.data.remote.dto.NfcVerifyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NfcEnrollmentApiImpl(
    private val client: HttpClient
) : NfcEnrollmentApi {

    override suspend fun enroll(request: NfcEnrollRequest): NfcEnrollResponse {
        return client.post("nfc/enroll") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verify(request: NfcVerifyRequest): NfcVerifyResponse {
        return client.post("nfc/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
