package com.fivucsas.shared.domain.repository

enum class FingerprintStep {
    RegisteringDevice,
    RequestingChallenge,
    ScanningBiometric,
    VerifyingSignature
}

interface FingerprintRepository {
    suspend fun performStepUp(onStep: (FingerprintStep) -> Unit = {}): Result<String>
}

