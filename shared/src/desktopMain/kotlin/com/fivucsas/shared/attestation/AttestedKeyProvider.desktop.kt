package com.fivucsas.shared.attestation

/** Desktop JVM: no hardware-backed key attestation is available. */
actual fun provideAttestedKeyProvider(): AttestedKeyProvider = UnsupportedAttestedKeyProvider("desktop")

internal class UnsupportedAttestedKeyProvider(private val platform: String) : AttestedKeyProvider {
    override fun isSupported(): Boolean = false

    override suspend fun signWithAttestedKey(challenge: ByteArray, payload: ByteArray): AttestedSignature =
        throw AttestationUnsupportedException("Hardware key attestation is not available on $platform")
}
