package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpSendRequestDto(
    val email: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class OtpSendResponseDto(
    val success: Boolean = true,
    val message: String = ""
)

@Serializable
data class OtpVerifyRequestDto(
    val code: String
)

@Serializable
data class OtpVerifyResponseDto(
    val success: Boolean = false,
    val message: String = ""
)
