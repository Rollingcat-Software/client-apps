package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [MfaHandoff] — the Login → MfaFlow route payload that
 * replaced reading MFA session state off a fresh LoginViewModel factory
 * instance (the v5.2.1 "can't pass MFA" login-bounce bug).
 */
class MfaHandoffTest {

    @Test
    fun `encode then decode is a lossless round-trip`() {
        val original = MfaHandoff(
            sessionToken = "mfa-session-token-abc123",
            methods = listOf(
                AvailableMethodDto(methodType = "TOTP", name = "Authenticator", enrolled = true, preferred = true),
                AvailableMethodDto(methodType = "EMAIL_OTP", name = "Email code", enrolled = true)
            ),
            step = 1,
            total = 2
        )

        val decoded = MfaHandoff.decode(original.encode())

        assertEquals(original, decoded)
    }

    @Test
    fun `decode preserves the session token used to drive the MFA flow`() {
        val handoff = MfaHandoff(sessionToken = "tok-xyz", step = 2, total = 3)

        val decoded = MfaHandoff.decode(handoff.encode())

        assertEquals("tok-xyz", decoded?.sessionToken)
        assertEquals(2, decoded?.step)
        assertEquals(3, decoded?.total)
    }

    @Test
    fun `decode survives an empty methods list`() {
        val handoff = MfaHandoff(sessionToken = "tok", methods = emptyList())

        val decoded = MfaHandoff.decode(handoff.encode())

        assertEquals("tok", decoded?.sessionToken)
        assertTrue(decoded?.methods?.isEmpty() == true)
    }

    @Test
    fun `decode of null returns null instead of throwing`() {
        assertNull(MfaHandoff.decode(null))
    }

    @Test
    fun `decode of blank returns null`() {
        assertNull(MfaHandoff.decode("   "))
    }

    @Test
    fun `decode of malformed json returns null instead of throwing`() {
        assertNull(MfaHandoff.decode("{not valid json"))
    }
}
