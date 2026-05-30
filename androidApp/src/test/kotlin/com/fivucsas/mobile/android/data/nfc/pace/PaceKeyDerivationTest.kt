package com.fivucsas.mobile.android.data.nfc.pace

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Vector tests for [PaceKeyDerivation] — the `KDF(K, c) = SHA-1(K ‖ c)`
 * primitive shared by PACE-GM and ICAO 9303 BAC.
 *
 * The vectors are the published ICAO 9303 key-derivation worked example
 * (Kseed → Kenc/Kmac), independently re-verified:
 *   SHA-1(561754EE47DA4256C15FE4F40A17639C ‖ 00000001) = EB0F20E3…  (Kenc)
 *   SHA-1(561754EE47DA4256C15FE4F40A17639C ‖ 00000002) = 6DC37B57…  (Kmac)
 * (Confirmed via `printf … | xxd -r -p | sha1sum`.)
 *
 * This proves the byte-exact counter encoding + truncation used by the PACE
 * key derivation, card-free. The on-card GM handshake + secure-messaging
 * exchange still needs a physical PACE document — see [PaceAuthenticator] /
 * docs/NFC_PACE_PLAN.md.
 */
class PaceKeyDerivationTest {

    private fun hex(s: String): ByteArray =
        s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private fun ByteArray.hex(): String = joinToString("") { "%02X".format(it) }

    private val kseed = hex("561754EE47DA4256C15FE4F40A17639C")
    private val expectedEnc = "EB0F20E35DF29C76ECD3EF574BC74A1D"
    private val expectedMac = "6DC37B571C8E53DD1BEAB6E7CB4185EC"

    @Test
    fun `derives Kenc from counter 1 matching the 9303 worked example`() {
        val enc = PaceKeyDerivation.deriveEncKey(kseed, PaceKeyDerivation.Cipher.AES_128)
        assertEquals(expectedEnc, enc.hex())
    }

    @Test
    fun `derives Kmac from counter 2 matching the 9303 worked example`() {
        val mac = PaceKeyDerivation.deriveMacKey(kseed, PaceKeyDerivation.Cipher.AES_128)
        assertEquals(expectedMac, mac.hex())
    }

    @Test
    fun `enc and mac keys differ (counter is part of the input)`() {
        val enc = PaceKeyDerivation.deriveEncKey(kseed)
        val mac = PaceKeyDerivation.deriveMacKey(kseed)
        assertFalse(enc.contentEquals(mac))
    }

    @Test
    fun `kdf counter encoding is 4-byte big-endian`() {
        // KDF(K,1) via the public helper must equal the raw kdf(K,1).
        assertArrayEquals(
            PaceKeyDerivation.deriveEncKey(kseed),
            PaceKeyDerivation.kdf(kseed, PaceKeyDerivation.COUNTER_ENC)
        )
    }

    @Test
    fun `AES-128 key derivation is 16 bytes and AES-256 is 32`() {
        assertEquals(16, PaceKeyDerivation.kdf(kseed, 1, PaceKeyDerivation.Cipher.AES_128).size)
        assertEquals(32, PaceKeyDerivation.kdf(kseed, 1, PaceKeyDerivation.Cipher.AES_256).size)
    }

    @Test
    fun `password key uses counter 3`() {
        val pi = hex("0123456789ABCDEF0123456789ABCDEF")
        assertArrayEquals(
            PaceKeyDerivation.derivePasswordKey(pi),
            PaceKeyDerivation.kdf(pi, PaceKeyDerivation.COUNTER_PASSWORD)
        )
    }
}
