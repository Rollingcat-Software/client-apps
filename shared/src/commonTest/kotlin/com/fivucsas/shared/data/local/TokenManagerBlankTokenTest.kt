package com.fivucsas.shared.data.local

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F8 hardening — a blank ("") stored token must be treated as ABSENT.
 *
 * The DTO mappers default a missing access/refresh token to "" (see
 * AuthDto.toModel). If TokenManager treated "" as a real token it would (a) make
 * [TokenManager.isAuthenticated] route a session-less user to the dashboard, and
 * (b) drive the refresh path to POST an empty grant the server rejects as
 * invalid_grant → a spurious forced re-login. Both contributed to F8.
 */
class TokenManagerBlankTokenTest {

    private class MutableStorage(
        initialToken: String? = null,
        initialRefresh: String? = null,
    ) : TokenStorage {
        private var storedToken: String? = initialToken
        private var storedRefresh: String? = initialRefresh
        override fun saveToken(token: String) { storedToken = token }
        override fun getToken(): String? = storedToken
        override fun clearToken() { storedToken = null }
        override fun saveRefreshToken(token: String) { storedRefresh = token }
        override fun getRefreshToken(): String? = storedRefresh
        override fun clearRefreshToken() { storedRefresh = null }
        override fun saveRole(role: String) {}
        override fun getRole(): String? = null
        override fun clearRole() {}
    }

    @Test
    fun `blank access token reads back as null`() {
        val tm = TokenManager(MutableStorage(initialToken = "", initialRefresh = "r"))
        assertNull(tm.getAccessToken(), "an empty-string access token must be treated as absent")
    }

    @Test
    fun `blank refresh token reads back as null`() {
        val tm = TokenManager(MutableStorage(initialToken = "a", initialRefresh = ""))
        assertNull(tm.getRefreshToken(), "an empty-string refresh token must be treated as absent")
    }

    @Test
    fun `not authenticated when only a blank refresh token is stored`() {
        val tm = TokenManager(MutableStorage(initialToken = null, initialRefresh = ""))
        assertFalse(tm.isAuthenticated(), "a blank refresh token must NOT count as a session")
    }

    @Test
    fun `authenticated when a real refresh token is stored but access is missing`() {
        val tm = TokenManager(MutableStorage(initialToken = null, initialRefresh = "real-refresh"))
        assertTrue(tm.isAuthenticated(), "a real refresh token keeps the user signed in")
    }
}
