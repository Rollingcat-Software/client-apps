package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.OtpSendRequestDto
import com.fivucsas.shared.data.remote.dto.OtpSendResponseDto
import com.fivucsas.shared.data.remote.dto.OtpVerifyRequestDto
import com.fivucsas.shared.data.remote.dto.OtpVerifyResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OtpApiImpl(
    private val client: HttpClient
) : OtpApi {

    companion object {
        private const val BASE_PATH = "otp"
    }

    override suspend fun sendEmailOtp(userId: String, request: OtpSendRequestDto): OtpSendResponseDto {
        return client.post("$BASE_PATH/email/send/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verifyEmailOtp(userId: String, request: OtpVerifyRequestDto): OtpVerifyResponseDto {
        return client.post("$BASE_PATH/email/verify/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun sendSmsOtp(userId: String, request: OtpSendRequestDto): OtpSendResponseDto {
        return client.post("$BASE_PATH/sms/send/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verifySmsOtp(userId: String, request: OtpVerifyRequestDto): OtpVerifyResponseDto {
        return client.post("$BASE_PATH/sms/verify/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
