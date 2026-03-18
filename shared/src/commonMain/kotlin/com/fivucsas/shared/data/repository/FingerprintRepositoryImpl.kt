package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.local.StepUpTokenManager
import com.fivucsas.shared.data.remote.api.AuthBiometricApi
import com.fivucsas.shared.data.remote.dto.RegisterBiometricDeviceRequestDto
import com.fivucsas.shared.data.remote.dto.VerifyBiometricSignatureRequestDto
import com.fivucsas.shared.domain.repository.FingerprintRepository
import com.fivucsas.shared.domain.repository.FingerprintStep
import com.fivucsas.shared.platform.FingerprintAuthException
import com.fivucsas.shared.platform.FingerprintAuthenticator
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class FingerprintRepositoryImpl(
    private val authBiometricApi: AuthBiometricApi,
    private val fingerprintAuthenticator: FingerprintAuthenticator,
    private val stepUpTokenManager: StepUpTokenManager
) : FingerprintRepository {

    override suspend fun performStepUp(onStep: (FingerprintStep) -> Unit): Result<String> {
        return runCatching {
            if (!fingerprintAuthenticator.isSupported()) {
                throw FingerprintAuthException(
                    message = "Fingerprint is not supported on this device.",
                    recoverable = false
                )
            }

            onStep(FingerprintStep.RegisteringDevice)
            val keyId = fingerprintAuthenticator.getOrCreateKeyId()
            val publicKeyJwk = fingerprintAuthenticator.getPublicKeyJwk()
            ensureDeviceRegistered(keyId, publicKeyJwk)

            onStep(FingerprintStep.RequestingChallenge)
            val challenge = authBiometricApi.createChallenge(deviceKeyId = keyId)

            onStep(FingerprintStep.ScanningBiometric)
            val signature = fingerprintAuthenticator.signNonce(challenge.nonce)

            onStep(FingerprintStep.VerifyingSignature)
            val response = authBiometricApi.verifySignature(
                VerifyBiometricSignatureRequestDto(
                    challengeId = challenge.challengeId,
                    keyId = keyId,
                    signatureBase64 = signature
                )
            )

            val stepUpToken = response.stepUpToken
            if (stepUpToken.isBlank()) {
                throw FingerprintAuthException(
                    message = "Step-up token was missing in backend response.",
                    recoverable = true
                )
            }

            stepUpTokenManager.saveToken(stepUpToken)
            stepUpToken
        }.recoverCatching { throwable ->
            throw mapToUiFriendlyException(throwable)
        }
    }

    private suspend fun ensureDeviceRegistered(keyId: String, publicKeyJwk: String) {
        try {
            authBiometricApi.registerDevice(
                RegisterBiometricDeviceRequestDto(
                    keyId = keyId,
                    publicKeyJwk = publicKeyJwk,
                    devicePlatform = "ANDROID"
                )
            )
        } catch (e: ClientRequestException) {
            if (e.response.status != HttpStatusCode.Conflict) {
                throw e
            }
        }
    }

    private fun mapToUiFriendlyException(throwable: Throwable): Throwable {
        if (throwable is FingerprintAuthException) return throwable
        if (throwable is ClientRequestException) {
            return when (throwable.response.status.value) {
                401, 403 -> FingerprintAuthException(
                    message = "Your session is not authorized for fingerprint verification.",
                    recoverable = false
                )

                409 -> FingerprintAuthException(
                    message = "Challenge is already used or expired. Please retry.",
                    recoverable = true
                )

                423, 429 -> FingerprintAuthException(
                    message = "Too many attempts. Fingerprint is temporarily locked.",
                    recoverable = true
                )

                else -> FingerprintAuthException(
                    message = "Server rejected fingerprint verification. Please retry.",
                    recoverable = true
                )
            }
        }

        return FingerprintAuthException(
            message = throwable.message ?: "Fingerprint verification failed.",
            recoverable = true
        )
    }
}

