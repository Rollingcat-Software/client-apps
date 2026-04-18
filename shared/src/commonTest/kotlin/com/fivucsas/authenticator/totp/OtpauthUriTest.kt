package com.fivucsas.authenticator.totp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OtpauthUriTest {

    @Test
    fun parses_minimal_valid_uri() {
        val cfg = OtpauthUri.parse("otpauth://totp/Acme:alice@example.com?secret=JBSWY3DPEHPK3PXP")
        assertEquals("Acme", cfg.issuer)
        assertEquals("alice@example.com", cfg.accountName)
        assertEquals(TotpAlgorithm.SHA1, cfg.algorithm)
        assertEquals(6, cfg.digits)
        assertEquals(30, cfg.period)
        assertTrue(cfg.secretBytes.isNotEmpty())
    }

    @Test
    fun parses_all_parameters() {
        val uri = "otpauth://totp/GitHub:octocat?secret=KRSXG5CTMVRXEZLU&issuer=GitHub" +
            "&algorithm=SHA256&digits=8&period=60"
        val cfg = OtpauthUri.parse(uri)
        assertEquals("GitHub", cfg.issuer)
        assertEquals("octocat", cfg.accountName)
        assertEquals(TotpAlgorithm.SHA256, cfg.algorithm)
        assertEquals(8, cfg.digits)
        assertEquals(60, cfg.period)
    }

    @Test
    fun missing_secret_throws() {
        assertFailsWith<IllegalArgumentException> {
            OtpauthUri.parse("otpauth://totp/Acme:alice?issuer=Acme")
        }
    }

    @Test
    fun non_totp_type_throws() {
        assertFailsWith<IllegalArgumentException> {
            OtpauthUri.parse("otpauth://hotp/Acme:alice?secret=JBSWY3DPEHPK3PXP&counter=1")
        }
    }

    @Test
    fun wrong_scheme_throws() {
        assertFailsWith<IllegalArgumentException> {
            OtpauthUri.parse("https://example.com/totp?secret=JBSWY3DPEHPK3PXP")
        }
    }

    @Test
    fun lowercase_base32_is_tolerated() {
        val cfg = OtpauthUri.parse("otpauth://totp/x:y?secret=jbswy3dpehpk3pxp")
        assertTrue(cfg.secretBytes.isNotEmpty())
    }

    @Test
    fun padded_base32_is_tolerated() {
        val cfg = OtpauthUri.parse("otpauth://totp/x:y?secret=JBSWY3DPEHPK3PXP====")
        assertTrue(cfg.secretBytes.isNotEmpty())
    }

    @Test
    fun url_encoded_label_is_decoded() {
        val cfg = OtpauthUri.parse(
            "otpauth://totp/Big%20Corp:alice%40example.com?secret=JBSWY3DPEHPK3PXP"
        )
        assertEquals("Big Corp", cfg.issuer)
        assertEquals("alice@example.com", cfg.accountName)
    }

    @Test
    fun issuer_query_param_wins_over_label_prefix() {
        val cfg = OtpauthUri.parse(
            "otpauth://totp/OldName:alice?secret=JBSWY3DPEHPK3PXP&issuer=NewName"
        )
        assertEquals("NewName", cfg.issuer)
        assertEquals("alice", cfg.accountName)
    }

    @Test
    fun google_authenticator_style_example_parses() {
        val uri =
            "otpauth://totp/Google%3Aalice%40gmail.com?secret=JBSWY3DPEHPK3PXP&issuer=Google"
        val cfg = OtpauthUri.parse(uri)
        assertEquals("Google", cfg.issuer)
        assertEquals("alice@gmail.com", cfg.accountName)
    }

    @Test
    fun microsoft_authenticator_style_example_parses() {
        val uri =
            "otpauth://totp/Microsoft:alice?secret=ONSWG4TFOQ%3D%3D%3D%3D%3D%3D&issuer=Microsoft" +
                "&algorithm=SHA1&digits=6&period=30"
        val cfg = OtpauthUri.parse(uri)
        assertEquals("Microsoft", cfg.issuer)
        assertEquals("alice", cfg.accountName)
        assertEquals(TotpAlgorithm.SHA1, cfg.algorithm)
    }

    @Test
    fun label_without_issuer_prefix_falls_back_to_account_only() {
        val cfg = OtpauthUri.parse("otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&issuer=Acme")
        assertEquals("Acme", cfg.issuer)
        assertEquals("alice", cfg.accountName)
    }

    @Test
    fun invalid_digits_throws() {
        assertFailsWith<IllegalArgumentException> {
            OtpauthUri.parse("otpauth://totp/x:y?secret=JBSWY3DPEHPK3PXP&digits=7")
        }
    }
}
