package com.fivucsas.shared.data.remote.dto

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

/**
 * Server returns camelCase JSON: { "stepUpToken": "..." }
 */
@Serializable
data class VerifyBiometricSignatureResponseDto(
    val stepUpToken: String = ""
)

