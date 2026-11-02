package com.fivucsas.shared.attestation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DesktopAttestedKeyProviderTest {

    @Test
    fun desktopHasNoHardwareAttestation() = runTest {
        val provider = provideAttestedKeyProvider()
        assertFalse(provider.isSupported())
        assertFailsWith<AttestationUnsupportedException> {
            provider.signWithAttestedKey(ByteArray(32), ByteArray(1))
        }
    }

    @Test
    fun matchesJcaSha256() {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        for (n in listOf(0, 1, 55, 56, 63, 64, 65, 1000)) {
            val input = ByteArray(n) { (it * 31 + 7).toByte() }
            kotlin.test.assertContentEquals(md.digest(input), Sha256.digest(input))
        }
    }
}
