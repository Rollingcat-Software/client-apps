package com.fivucsas.authenticator.totp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * OtpScannerParserIntegrationTest
 *
 * End-to-end parser path used by the standalone TOTP Authenticator QR
 * scanner (`OtpQrScannerScreen` on Android). Exercises the same
 * [OtpQrScanFilter] / [OtpauthUri] pipeline that `AuthenticatorViewModel
 * .addFromUri` runs after a successful camera detection.
 *
 * Lives in `commonTest` (KMP) because `androidApp` has no JVM unit-test
 * source-set configured; the logic is 100% pure Kotlin so a single shared
 * test covers Android + Desktop + iOS.
 */
class OtpScannerParserIntegrationTest {

    @Test
    fun google_authenticator_style_qr_parses_correctly() {
        val uri = "otpauth://totp/Example:alice@example.com" +
            "?secret=JBSWY3DPEHPK3PXP&issuer=Example&algorithm=SHA1&digits=6&period=30"

        val verdict = OtpQrScanFilter.accept(uri)

        assertTrue(
            verdict is OtpQrScanResult.Accepted,
            "Valid Google-Authenticator-style QR should be accepted, got $verdict"
        )
        assertEquals(uri, verdict.uri)
        assertEquals("Example", verdict.config.issuer)
        assertEquals("alice@example.com", verdict.config.accountName)
        assertEquals(TotpAlgorithm.SHA1, verdict.config.algorithm)
        assertEquals(6, verdict.config.digits)
        assertEquals(30, verdict.config.period)
        assertTrue(verdict.config.secretBytes.isNotEmpty())

        // And `OtpauthUri.parse` directly should yield the same config (no
        // surprise from the filter layer).
        val direct = runCatching { OtpauthUri.parse(uri) }.getOrNull()
        assertEquals(verdict.config, direct)
    }

    @Test
    fun garbage_string_returns_null_from_direct_parser_and_invalid_from_filter() {
        val garbage = "this-is-not-a-valid-qr-payload"

        // Parser-level: wrap the throw into null so the assertion reads naturally.
        val parsed = runCatching { OtpauthUri.parse(garbage) }.getOrNull()
        assertNull(parsed, "Garbage input must not parse to an OtpauthConfig")

        // Filter-level: scanner UI layer uses this, must surface Invalid.
        val verdict = OtpQrScanFilter.accept(garbage)
        assertTrue(
            verdict is OtpQrScanResult.Invalid,
            "Garbage input must be rejected by the scan filter, got $verdict"
        )
        assertEquals(OtpQrScanReason.WRONG_SCHEME, verdict.reason)
    }
}
