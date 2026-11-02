package com.fivucsas.shared.attestation

/**
 * Result of signing with a freshly generated, hardware-attested key.
 *
 * @property certificateChainDer the key's attestation chain, leaf first (DER)
 * @property signatureDer        ECDSA P-256 / SHA-256 signature (ASN.1 DER) over the payload
 * @property strongBox           true when the key lived in StrongBox, false for TEE
 */
class AttestedSignature(
    val certificateChainDer: List<ByteArray>,
    val signatureDer: ByteArray,
    val strongBox: Boolean
)

/** Thrown when the platform cannot produce hardware key attestation. */
class AttestationUnsupportedException(message: String) : Exception(message)

/**
 * Platform contract for the attested puzzle binding (identity-core-api
 * `docs/ATTESTED_PUZZLE_BINDING.md`). One key per puzzle session: generated with
 * the server's challenge as the attestation challenge, used for exactly one
 * signature, then deleted.
 *
 * Android: AndroidKeyStore EC P-256, StrongBox when available else TEE
 * (`AttestedKeyManager`). Desktop: unsupported (no hardware attestation on the
 * JVM). iOS: unsupported until App Attest is implemented (design doc §10).
 */
interface AttestedKeyProvider {

    /** Whether hardware key attestation is available on this platform/device. */
    fun isSupported(): Boolean

    /**
     * Generates an attested key bound to [challenge], signs [payload], exports the
     * chain and deletes the key (also on failure).
     *
     * @throws AttestationUnsupportedException when attestation is unavailable
     */
    suspend fun signWithAttestedKey(challenge: ByteArray, payload: ByteArray): AttestedSignature
}

/** The platform's [AttestedKeyProvider]. */
expect fun provideAttestedKeyProvider(): AttestedKeyProvider
