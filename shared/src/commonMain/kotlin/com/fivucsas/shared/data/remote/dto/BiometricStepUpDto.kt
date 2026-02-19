package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.ChallengeDto
import com.fivucsas.shared.domain.model.PublicKeyJwk
import com.fivucsas.shared.domain.model.StepUpDto
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequestDto(
    @SerialName("key_id")
    val keyId: String,
    val platform: String,
    @SerialName("public_key_jwk")
    val publicKeyJwk: PublicKeyJwkRequestDto,
    @SerialName("device_label")
    val deviceLabel: String? = null
)

@Serializable
data class PublicKeyJwkRequestDto(
    val kty: String,
    val crv: String,
    val x: String,
    val y: String,
    val kid: String? = null
)

@Serializable
data class RegisterDeviceResponseDto(
    val deviceId: String? = null,
    @SerialName("device_id")
    val deviceIdSnake: String? = null
)

@Serializable
data class CreateChallengeResponseDto(
    val challengeId: String? = null,
    @SerialName("challenge_id")
    val challengeIdSnake: String? = null,
    val nonceBase64: String? = null,
    @SerialName("nonce_base64")
    val nonceBase64Snake: String? = null,
    val expiresAt: String? = null,
    @SerialName("expires_at")
    val expiresAtSnake: String? = null
)

@Serializable
data class VerifyChallengeRequestDto(
    @SerialName("challenge_id")
    val challengeId: String,
    @SerialName("key_id")
    val keyId: String,
    @SerialName("signature_base64")
    val signatureBase64: String
)

@Serializable
data class VerifyChallengeResponseDto(
    val stepUpToken: String? = null,
    @SerialName("step_up_token")
    val stepUpTokenSnake: String? = null,
    val expiresAt: String? = null,
    @SerialName("expires_at")
    val expiresAtSnake: String? = null
)

@Serializable
data class BiometricStepUpErrorDto(
    val message: String? = null,
    val error: String? = null,
    val detail: String? = null
)

fun PublicKeyJwk.toRequestDto(): PublicKeyJwkRequestDto = PublicKeyJwkRequestDto(
    kty = kty,
    crv = crv,
    x = x,
    y = y,
    kid = kid
)

fun CreateChallengeResponseDto.toChallengeDto(): ChallengeDto {
    val id = challengeId ?: challengeIdSnake
    val nonce = nonceBase64 ?: nonceBase64Snake
    val expiresAtRaw = expiresAt ?: expiresAtSnake
    require(!id.isNullOrBlank()) { "Missing challenge id in response." }
    require(!nonce.isNullOrBlank()) { "Missing challenge nonce in response." }
    require(!expiresAtRaw.isNullOrBlank()) { "Missing challenge expiry in response." }
    return ChallengeDto(
        challengeId = id,
        nonceBase64 = nonce,
        expiresAt = Instant.parse(expiresAtRaw)
    )
}

fun VerifyChallengeResponseDto.toStepUpDto(): StepUpDto {
    val token = stepUpToken ?: stepUpTokenSnake
    val expiresAtRaw = expiresAt ?: expiresAtSnake
    require(!token.isNullOrBlank()) { "Missing step-up token in response." }
    require(!expiresAtRaw.isNullOrBlank()) { "Missing step-up expiry in response." }
    return StepUpDto(
        stepUpToken = token,
        expiresAt = Instant.parse(expiresAtRaw)
    )
}
