package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Register device request DTO — matches server:
 * POST /step-up/register-device
 * Server expects: { keyId, publicKeyJwk, devicePlatform }
 */
@Serializable
data class RegisterBiometricDeviceRequestDto(
    val keyId: String,
    val publicKeyJwk: String,
    val devicePlatform: String
)

/**
 * Challenge response DTO — matches server:
 * POST /step-up/challenge
 * Server returns: { challengeId, nonce }
 */
@Serializable
data class CreateBiometricChallengeResponseDto(
    val challengeId: String,
    val nonce: String
)

/**
 * Verify challenge request DTO — matches server:
 * POST /step-up/verify-challenge
 * Server expects: { keyId, challengeId, signatureBase64 }
 */
@Serializable
data class VerifyBiometricSignatureRequestDto(
    val keyId: String,
    val challengeId: String,
    val signatureBase64: String
)

/**
 * Verify challenge response DTO — server returns:
 * { "stepUpToken": "..." }
 */
@Serializable
data class VerifyBiometricSignatureResponseDto(
    val stepUpToken: String = ""
)
