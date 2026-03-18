package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.VoiceEnrollRequestDto
import com.fivucsas.shared.data.remote.dto.VoiceEnrollResponseDto
import com.fivucsas.shared.data.remote.dto.VoiceVerifyResponseDto
import com.fivucsas.shared.data.remote.dto.VoiceSearchResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class VoiceApiImpl(
    private val client: HttpClient
) : VoiceApi {

    companion object {
        private const val BASE_PATH = "biometric/voice"
    }

    override suspend fun enroll(userId: String, request: VoiceEnrollRequestDto): VoiceEnrollResponseDto {
        return client.post("$BASE_PATH/enroll/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verify(userId: String, request: VoiceEnrollRequestDto): VoiceVerifyResponseDto {
        return client.post("$BASE_PATH/verify/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun search(request: VoiceEnrollRequestDto): VoiceSearchResponseDto {
        return client.post("$BASE_PATH/search") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
