package com.fivucsas.authenticator.totp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers the parser-integration path used by the Android
 * `OtpQrScannerScreen`. Plain JVM tests so we don't need Espresso or
 * instrumented ML Kit; the scanner screen simply delegates its accept/reject
 * decision to [OtpQrScanFilter.accept].
 */
class OtpQrScanFilterTest {

    @Test
    fun accepts_google_authenticator_style_qr() {
        val raw =
            "otpauth://totp/Google%3Aalice%40gmail.com?secret=JBSWY3DPEHPK3PXP&issuer=Google"

        val result = OtpQrScanFilter.accept(raw)

        assertTrue(result is OtpQrScanResult.Accepted, "expected Accepted but got $result")
        assertEquals(raw, result.uri)
        assertEquals("Google", result.config.issuer)
        assertEquals("alice@gmail.com", result.config.accountName)
    }

    @Test
    fun accepts_minimal_totp_uri_and_trims_whitespace() {
        val raw = "  otpauth://totp/Acme:alice?secret=JBSWY3DPEHPK3PXP  "

        val result = OtpQrScanFilter.accept(raw)

        assertTrue(result is OtpQrScanResult.Accepted)
        assertEquals("otpauth://totp/Acme:alice?secret=JBSWY3DPEHPK3PXP", result.uri)
    }

    @Test
    fun rejects_arbitrary_https_url() {
        val result = OtpQrScanFilter.accept("https://example.com/login?token=abc")

        assertTrue(result is OtpQrScanResult.Invalid, "expected Invalid but got $result")
        assertEquals(OtpQrScanReason.WRONG_SCHEME, result.reason)
    }

    @Test
    fun rejects_wifi_qr_payload() {
        // Standard ZXing WIFI payload format — common non-auth QR mis-scanned.
        val result = OtpQrScanFilter.accept("WIFI:S:MyNetwork;T:WPA;P:pass123;;")

        assertTrue(result is OtpQrScanResult.Invalid)
        assertEquals(OtpQrScanReason.WRONG_SCHEME, result.reason)
    }

    @Test
    fun rejects_hotp_scheme_as_unparseable() {
        // scheme is otpauth but type is hotp — filter passes to parser which throws.
        val result = OtpQrScanFilter.accept(
            "otpauth://hotp/Acme:alice?secret=JBSWY3DPEHPK3PXP&counter=1"
        )

        assertTrue(result is OtpQrScanResult.Invalid)
        assertEquals(OtpQrScanReason.UNPARSEABLE, result.reason)
    }

    @Test
    fun rejects_blank_and_null() {
        assertEquals(
            OtpQrScanReason.EMPTY,
            (OtpQrScanFilter.accept("") as OtpQrScanResult.Invalid).reason
        )
        assertEquals(
            OtpQrScanReason.EMPTY,
            (OtpQrScanFilter.accept("   ") as OtpQrScanResult.Invalid).reason
        )
        assertEquals(
            OtpQrScanReason.EMPTY,
            (OtpQrScanFilter.accept(null) as OtpQrScanResult.Invalid).reason
        )
    }
}
