package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterBiometricDeviceRequestDto(
    val keyId: String,
    val platform: String,
    val publicKeyJwk: String
)

@Serializable
data class CreateBiometricChallengeResponseDto(
    val challengeId: String,
    val nonce: String
)

@Serializable
data class VerifyBiometricSignatureRequestDto(
    val challengeId: String,
    val keyId: String,
    val signatureBase64: String
)

@Serializable
data class VerifyBiometricSignatureResponseDto(
    @SerialName("step_up_token")
    val stepUpTokenSnake: String? = null,
    val stepUpToken: String? = null
) {
    fun resolvedStepUpToken(): String = stepUpToken ?: stepUpTokenSnake.orEmpty()
}

