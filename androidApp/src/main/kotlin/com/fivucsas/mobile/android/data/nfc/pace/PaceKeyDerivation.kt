package com.fivucsas.mobile.android.data.nfc.pace

import java.security.MessageDigest

/**
 * PACE / TR-03110 key-derivation function (KDF), shared with ICAO 9303 BAC.
 *
 * `KDF(K, c) = H(K ‖ c)` where `c` is a 4-byte big-endian counter and `H` is
 * SHA-1 (for 3DES / AES-128 the first 16 bytes are taken) or SHA-256 (for
 * AES-192/256). The standardized counters are:
 *   - 1 → encryption key (`K_enc`)
 *   - 2 → MAC key (`K_mac`)
 *   - 3 → key derived from the PACE secret π to decrypt the nonce (`K_π`)
 *
 * This is the byte-exact, vector-testable core of PACE-GM (see
 * [PaceKeyDerivationTest], which asserts against the published ICAO 9303
 * worked-example vectors). It is independent of any card — pure crypto.
 */
object PaceKeyDerivation {

    /** Standardized KDF counters (TR-03110 / ICAO 9303). */
    const val COUNTER_ENC = 1
    const val COUNTER_MAC = 2
    const val COUNTER_PASSWORD = 3

    /** Cipher families a PACE protocol may select. */
    enum class Cipher(val keyLengthBytes: Int, val digest: String) {
        AES_128(16, "SHA-1"),
        AES_192(24, "SHA-256"),
        AES_256(32, "SHA-256"),
        TDES(16, "SHA-1")
    }

    /**
     * KDF(K, c): `H(K ‖ c)` truncated to the cipher's key length.
     *
     * @param k the shared secret / seed (PACE: the ECDH shared secret; BAC:
     *          Kseed; nonce-key: the PACE secret π).
     * @param counter 1=enc, 2=mac, 3=password (or any caller-supplied value).
     */
    fun kdf(k: ByteArray, counter: Int, cipher: Cipher = Cipher.AES_128): ByteArray {
        val md = MessageDigest.getInstance(cipher.digest)
        md.update(k)
        md.update(counterBytes(counter))
        val digest = md.digest()
        return digest.copyOf(cipher.keyLengthBytes)
    }

    /** Derive the encryption key (counter 1). */
    fun deriveEncKey(k: ByteArray, cipher: Cipher = Cipher.AES_128): ByteArray =
        kdf(k, COUNTER_ENC, cipher)

    /** Derive the MAC key (counter 2). */
    fun deriveMacKey(k: ByteArray, cipher: Cipher = Cipher.AES_128): ByteArray =
        kdf(k, COUNTER_MAC, cipher)

    /**
     * Derive the nonce-decryption key K_π from the PACE secret π (counter 3).
     * π itself is `SHA-1(MRZ-seed)` (or the CAN), computed by the caller.
     */
    fun derivePasswordKey(pi: ByteArray, cipher: Cipher = Cipher.AES_128): ByteArray =
        kdf(pi, COUNTER_PASSWORD, cipher)

    private fun counterBytes(counter: Int): ByteArray = byteArrayOf(
        (counter ushr 24).toByte(),
        (counter ushr 16).toByte(),
        (counter ushr 8).toByte(),
        counter.toByte()
    )
}
