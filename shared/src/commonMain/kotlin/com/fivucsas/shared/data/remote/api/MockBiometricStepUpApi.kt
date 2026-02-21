package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.domain.model.ChallengeDto
import com.fivucsas.shared.domain.model.PublicKeyJwk
import com.fivucsas.shared.domain.model.StepUpDto
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class MockBiometricStepUpApi : BiometricStepUpApi {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun createChallenge(): ChallengeDto {
        val nonce = Random.Default.nextBytes(32)
        return ChallengeDto(
            challengeId = pseudoUuid(),
            nonceBase64 = Base64.encode(nonce),
            expiresAt = nowPlusMillis(90_000L)
        )
    }

    override suspend fun verifyChallenge(
        challengeId: String,
        keyId: String,
        signatureBase64: String
    ): StepUpDto {
        return StepUpDto(
            stepUpToken = "mock-step-up-token",
            expiresAt = nowPlusMillis(300_000L)
        )
    }

    override suspend fun registerDevice(
        keyId: String,
        platform: String,
        publicKeyJwk: PublicKeyJwk,
        deviceLabel: String?
    ): String = pseudoUuid()

    private fun nowPlusMillis(deltaMillis: Long): Instant {
        val now = Clock.System.now().toEpochMilliseconds()
        return Instant.fromEpochMilliseconds(now + deltaMillis)
    }

    private fun pseudoUuid(): String {
        val bytes = Random.Default.nextBytes(16)
        bytes[6] = (bytes[6].toInt() and 0x0f or 0x40).toByte()
        bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte()
        val hexChars = "0123456789abcdef"
        val hex = buildString(32) {
            bytes.forEach { b ->
                val value = b.toInt() and 0xff
                append(hexChars[value ushr 4])
                append(hexChars[value and 0x0f])
            }
        }
        return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20, 32)}"
    }
}
