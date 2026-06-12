package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.domain.repository.AuthTokens
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F8 — mobile random-logout regression guards.
 *
 * The dominant root cause was [refreshAccessToken] wiping the ENTIRE persisted
 * session on ANY refresh failure (a blanket `catch { clearTokens() }` plus
 * clear-on-any-non-200). Transient network blips therefore forced a full
 * re-login — the intermittent "sometimes asks, sometimes doesn't".
 *
 * The fix: clear tokens ONLY on a DEFINITIVE auth rejection (400/401
 * invalid_grant); on a transient failure (IOException/timeout/5xx) keep the
 * session so the next request can retry.
 */
class RefreshTokenResilienceTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Minimal in-memory TokenStorage so we can observe clear/preserve. */
    private class FakeTokenStorage : TokenStorage {
        private var storedToken: String? = null
        private var storedRefresh: String? = null
        private var storedOauth: Boolean = false
        private var storedRole: String? = null

        override fun saveToken(token: String) { storedToken = token }
        override fun getToken(): String? = storedToken
        override fun clearToken() { storedToken = null }
        override fun saveRefreshToken(token: String) { storedRefresh = token }
        override fun getRefreshToken(): String? = storedRefresh
        override fun clearRefreshToken() { storedRefresh = null }
        override fun saveRole(role: String) { storedRole = role }
        override fun getRole(): String? = storedRole
        override fun clearRole() { storedRole = null }
        override fun saveOAuthSession(oauth: Boolean) { storedOauth = oauth }
        override fun getOAuthSession(): Boolean = storedOauth
        override fun clearOAuthSession() { storedOauth = false }
    }

    private fun seededManager(
        access: String = "stale-access",
        refresh: String = "refresh-123",
        oauth: Boolean = true,
    ): Pair<TokenManager, FakeTokenStorage> {
        val storage = FakeTokenStorage()
        val tm = TokenManager(storage)
        tm.saveTokens(
            AuthTokens(
                accessToken = access,
                refreshToken = refresh,
                expiresIn = 300,
                oauthSession = oauth,
            ),
        )
        return tm to storage
    }

    private fun client(handler: io.ktor.client.engine.mock.MockRequestHandler): HttpClient =
        HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(json) }
        }

    // ---- transient failures must NOT wipe the session ----

    @Test
    fun `oauth refresh on transient 503 returns false but KEEPS tokens`() = runTest {
        val (tm, _) = seededManager()
        val httpClient = client { respond("upstream unavailable", HttpStatusCode.ServiceUnavailable) }

        val ok = refreshAccessToken(httpClient, tm, "Bearer stale-access")

        assertFalse(ok, "a transient 503 cannot produce a usable token")
        assertNotNull(tm.getRefreshToken(), "session must SURVIVE a transient 503")
        assertEquals("refresh-123", tm.getRefreshToken())
    }

    @Test
    fun `oauth refresh on a transport error returns false but KEEPS tokens`() = runTest {
        val (tm, _) = seededManager()
        // The MockEngine handler throwing models a dropped keep-alive / Traefik RST
        // / offline — exactly the transient failure the broad catch must NOT punish
        // with a session wipe.
        val httpClient = client { throw RuntimeException("dropped keep-alive / Traefik RST") }

        val ok = refreshAccessToken(httpClient, tm, "Bearer stale-access")

        assertFalse(ok)
        assertNotNull(tm.getRefreshToken(), "a transport error must not log the user out")
    }

    @Test
    fun `legacy refresh on transient 500 returns false but KEEPS tokens`() = runTest {
        val (tm, _) = seededManager(oauth = false)
        val httpClient = client { respond("boom", HttpStatusCode.InternalServerError) }

        val ok = refreshAccessToken(httpClient, tm, "Bearer stale-access")

        assertFalse(ok)
        assertNotNull(tm.getRefreshToken(), "legacy session must survive a transient 5xx too")
    }

    // ---- definitive auth failures DO wipe the session ----

    @Test
    fun `oauth refresh on definitive 400 invalid_grant CLEARS tokens`() = runTest {
        val (tm, _) = seededManager()
        val httpClient = client {
            respond(
                content = "{\"error\":\"invalid_grant\"}",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val ok = refreshAccessToken(httpClient, tm, "Bearer stale-access")

        assertFalse(ok)
        assertNull(tm.getRefreshToken(), "a dead refresh token MUST clear the session")
        assertNull(tm.getAccessToken())
    }

    @Test
    fun `oauth refresh on definitive 401 CLEARS tokens`() = runTest {
        val (tm, _) = seededManager()
        val httpClient = client { respond("unauthorized", HttpStatusCode.Unauthorized) }

        val ok = refreshAccessToken(httpClient, tm, "Bearer stale-access")

        assertFalse(ok)
        assertNull(tm.getRefreshToken())
    }

    // ---- happy path ----

    @Test
    fun `oauth refresh on 200 updates tokens and returns true`() = runTest {
        val (tm, _) = seededManager()
        val httpClient = client {
            respond(
                content = "{\"access_token\":\"new-access\",\"refresh_token\":\"new-refresh\",\"expires_in\":900}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val ok = refreshAccessToken(httpClient, tm, "Bearer stale-access")

        assertTrue(ok)
        assertEquals("new-access", tm.getAccessToken())
        assertEquals("new-refresh", tm.getRefreshToken())
    }

    // ---- isDefinitiveAuthFailure classification ----

    @Test
    fun `only 400 and 401 are definitive auth failures`() {
        assertTrue(isDefinitiveAuthFailure(HttpStatusCode.BadRequest))
        assertTrue(isDefinitiveAuthFailure(HttpStatusCode.Unauthorized))
        assertFalse(isDefinitiveAuthFailure(HttpStatusCode.InternalServerError))
        assertFalse(isDefinitiveAuthFailure(HttpStatusCode.ServiceUnavailable))
        assertFalse(isDefinitiveAuthFailure(HttpStatusCode.BadGateway))
        assertFalse(isDefinitiveAuthFailure(HttpStatusCode.TooManyRequests))
        assertFalse(isDefinitiveAuthFailure(HttpStatusCode.Forbidden))
    }
}
