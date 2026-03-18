package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.TotpSetupResponseDto
import com.fivucsas.shared.data.remote.dto.TotpVerifyRequestDto
import com.fivucsas.shared.data.remote.dto.TotpVerifyResponseDto
import com.fivucsas.shared.data.remote.dto.TotpStatusResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TotpApiImpl(
    private val client: HttpClient
) : TotpApi {

    companion object {
        private const val BASE_PATH = "totp"
    }

    override suspend fun setup(userId: String): TotpSetupResponseDto {
        return client.post("$BASE_PATH/setup/$userId").body()
    }

    override suspend fun verifySetup(userId: String, request: TotpVerifyRequestDto): TotpVerifyResponseDto {
        return client.post("$BASE_PATH/verify-setup/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getStatus(userId: String): TotpStatusResponseDto {
        return client.get("$BASE_PATH/status/$userId").body()
    }
}
