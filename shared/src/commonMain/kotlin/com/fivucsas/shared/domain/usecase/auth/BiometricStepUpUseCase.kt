package com.fivucsas.shared.domain.usecase.auth

import com.fivucsas.shared.data.local.BiometricStepUpLocalStore
import com.fivucsas.shared.data.remote.api.BiometricStepUpApi
import com.fivucsas.shared.domain.biometric.BiometricAuthenticator
import com.fivucsas.shared.domain.model.BiometricCapability
import com.fivucsas.shared.domain.model.StepUpDto
import kotlinx.datetime.Clock
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class BiometricStepUpUseCase(
    private val authenticator: BiometricAuthenticator,
    private val api: BiometricStepUpApi,
    private val localStore: BiometricStepUpLocalStore
) {
    suspend fun canAuthenticate(): BiometricCapability = authenticator.canAuthenticate()

    suspend fun ensureRegisteredDevice(deviceLabel: String?): Boolean {
        val keyId = ensureKeyId()
        val publicKeyJwk = authenticator.ensureKeyPair(keyId)
        api.registerDevice(
            keyId = keyId,
            platform = "ANDROID",
            publicKeyJwk = publicKeyJwk,
            deviceLabel = deviceLabel
        )
        // Persist keyId as part of successful registration flow contract.
        localStore.saveKeyId(keyId)
        localStore.setDeviceRegistered(true)
        return true
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun stepUp(reason: String): StepUpDto {
        val keyId = ensureKeyId()
        val challenge = api.createChallenge()
        val nonceBytes = runCatching { Base64.decode(challenge.nonceBase64) }
            .getOrElse { challenge.nonceBase64.encodeToByteArray() }
        val signatureBytes = authenticator.signNonceWithBiometric(
            keyId = keyId,
            nonce = nonceBytes,
            reason = reason
        )
        val stepUp = api.verifyChallenge(
            challengeId = challenge.challengeId,
            keyId = keyId,
            signatureBase64 = Base64.encode(signatureBytes)
        )
        localStore.saveStepUpTokenInMemory(stepUp)
        return stepUp
    }

    fun isDeviceRegistered(): Boolean = localStore.isDeviceRegistered()

    fun getCurrentStepUpToken(): StepUpDto? = localStore.getStepUpTokenInMemory()

    fun getValidStepUpToken(): String? {
        val current = localStore.getStepUpTokenInMemory() ?: return null
        return if (current.expiresAt > Clock.System.now()) current.stepUpToken else null
    }

    private fun ensureKeyId(): String {
        val existing = localStore.getKeyId()
        if (existing != null) return existing
        val newKeyId = createKeyId()
        localStore.saveKeyId(newKeyId)
        return newKeyId
    }

    private fun createKeyId(): String {
        val random = Random.Default.nextLong(1_000_000_000L, 9_999_999_999L)
        return "key-$random"
    }
}
