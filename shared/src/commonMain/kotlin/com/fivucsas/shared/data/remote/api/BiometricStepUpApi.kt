package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.domain.model.ChallengeDto
import com.fivucsas.shared.domain.model.PublicKeyJwk
import com.fivucsas.shared.domain.model.StepUpDto

interface BiometricStepUpApi {
    suspend fun registerDevice(
        keyId: String,
        platform: String,
        publicKeyJwk: PublicKeyJwk,
        deviceLabel: String? = null
    ): String

    suspend fun createChallenge(): ChallengeDto

    suspend fun verifyChallenge(
        challengeId: String,
        keyId: String,
        signatureBase64: String
    ): StepUpDto
}
