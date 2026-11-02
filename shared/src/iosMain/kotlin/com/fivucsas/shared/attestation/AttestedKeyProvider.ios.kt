package com.fivucsas.shared.attestation

/**
 * iOS: not yet supported. The twin is Apple App Attest (DCAppAttestService), see
 * identity-core-api docs/ATTESTED_PUZZLE_BINDING.md §10.
 */
actual fun provideAttestedKeyProvider(): AttestedKeyProvider = object : AttestedKeyProvider {
    override fun isSupported(): Boolean = false

    override suspend fun signWithAttestedKey(challenge: ByteArray, payload: ByteArray): AttestedSignature =
        throw AttestationUnsupportedException("App Attest is not implemented yet on iOS")
}
