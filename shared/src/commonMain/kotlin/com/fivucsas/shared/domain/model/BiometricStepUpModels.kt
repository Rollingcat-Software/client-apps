package com.fivucsas.shared.domain.model

import kotlinx.datetime.Instant

sealed interface BiometricCapability {
    data object Supported : BiometricCapability
    data object NotEnrolled : BiometricCapability
    data object NoHardware : BiometricCapability
    data object Unsupported : BiometricCapability
    data object UnknownError : BiometricCapability
}

sealed interface BiometricError {
    data object Canceled : BiometricError
    data object Lockout : BiometricError
    data object NotEnrolled : BiometricError
    data object NoHardware : BiometricError
    data object KeyInvalidated : BiometricError
    data object Failed : BiometricError
    data class Unknown(val message: String? = null) : BiometricError
}

data class PublicKeyJwk(
    val kty: String,
    val crv: String,
    val x: String,
    val y: String,
    val kid: String? = null
)

data class ChallengeDto(
    val challengeId: String,
    val nonceBase64: String,
    val expiresAt: Instant
)

data class StepUpDto(
    val stepUpToken: String,
    val expiresAt: Instant
)

class BiometricStepUpException(
    val error: BiometricError,
    override val message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
