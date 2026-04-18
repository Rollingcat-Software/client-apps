package com.fivucsas.authenticator.totp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TotpGeneratorTest {

    private val seedSha1 = "12345678901234567890".encodeToByteArray()
    private val seedSha256 = "12345678901234567890123456789012".encodeToByteArray()
    private val seedSha512 =
        "1234567890123456789012345678901234567890123456789012345678901234".encodeToByteArray()

    @Test
    fun rfc6238_sha1_vectors() {
        val cases = listOf(
            59L to "94287082",
            1111111109L to "07081804",
            1111111111L to "14050471",
            1234567890L to "89005924",
            2000000000L to "69279037",
            20000000000L to "65353130"
        )
        for ((t, expected) in cases) {
            val code = TotpGenerator.generate(
                secret = seedSha1,
                epochSeconds = t,
                algorithm = TotpAlgorithm.SHA1,
                digits = 8,
                period = 30
            )
            assertEquals(expected, code, "SHA1 @ t=$t")
        }
    }

    @Test
    fun rfc6238_sha256_vectors() {
        val cases = listOf(
            59L to "46119246",
            1111111109L to "68084774",
            1111111111L to "67062674",
            1234567890L to "91819424",
            2000000000L to "90698825",
            20000000000L to "77737706"
        )
        for ((t, expected) in cases) {
            val code = TotpGenerator.generate(
                secret = seedSha256,
                epochSeconds = t,
                algorithm = TotpAlgorithm.SHA256,
                digits = 8,
                period = 30
            )
            assertEquals(expected, code, "SHA256 @ t=$t")
        }
    }

    @Test
    fun rfc6238_sha512_vectors() {
        val cases = listOf(
            59L to "90693936",
            1111111109L to "25091201",
            1111111111L to "99943326",
            1234567890L to "93441116",
            2000000000L to "38618901",
            20000000000L to "47863826"
        )
        for ((t, expected) in cases) {
            val code = TotpGenerator.generate(
                secret = seedSha512,
                epochSeconds = t,
                algorithm = TotpAlgorithm.SHA512,
                digits = 8,
                period = 30
            )
            assertEquals(expected, code, "SHA512 @ t=$t")
        }
    }

    @Test
    fun default_six_digits_are_zero_padded() {
        val code = TotpGenerator.generate(
            secret = seedSha1,
            epochSeconds = 59L
        )
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun remaining_seconds_counts_down() {
        assertEquals(30, TotpGenerator.remainingSeconds(0L, 30))
        assertEquals(29, TotpGenerator.remainingSeconds(1L, 30))
        assertEquals(1, TotpGenerator.remainingSeconds(29L, 30))
        assertEquals(30, TotpGenerator.remainingSeconds(30L, 30))
    }
}
