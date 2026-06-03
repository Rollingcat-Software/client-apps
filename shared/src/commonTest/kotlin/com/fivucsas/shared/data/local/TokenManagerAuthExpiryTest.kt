package com.fivucsas.shared.data.local

import com.fivucsas.shared.domain.repository.AuthTokens
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pre-demo 2026-06-03 — guards the session-expiry fix (#2).
 *
 * isAuthenticated() used to check token PRESENCE only, so after the ~15-min
 * access-token lifetime the app routed to the dashboard and the first data call
 * 401'd into a perceived logout. It now tolerates an expired access token when a
 * refresh token exists (the NetworkModule 401 handler refreshes + retries), and
 * still rejects a missing/expired token with no refresh token.
 */
class TokenManagerAuthExpiryTest {

    @OptIn(ExperimentalEncodingApi::class)
    private fun jwtWithExp(expEpochSeconds: Long): String {
        val header = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode("""{"alg":"none","typ":"JWT"}""".encodeToByteArray())
        val payload = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode("""{"sub":"u1","exp":$expEpochSeconds}""".encodeToByteArray())
        return "$header.$payload.sig"
    }

    private fun nowSeconds() = Clock.System.now().toEpochMilliseconds() / 1000

    private fun manager() = TokenManager(InMemoryTokenStorage())

    private fun tokens(access: String, refresh: String) = AuthTokens(
        accessToken = access,
        refreshToken = refresh,
        expiresIn = 900,
        role = "USER",
        userName = "Test",
        userEmail = "t@example.com",
        userId = "u1",
        tenantId = "11111111-1111-1111-1111-111111111111",
    )

    @Test
    fun `fresh access token is authenticated`() {
        val tm = manager()
        tm.saveTokens(tokens(jwtWithExp(nowSeconds() + 3600), "refresh-1"))
        assertTrue(tm.isAuthenticated())
        assertFalse(tm.isAccessTokenExpired(tm.getAccessToken()!!))
    }

    @Test
    fun `expired access token WITH refresh token is still authenticated`() {
        val tm = manager()
        tm.saveTokens(tokens(jwtWithExp(nowSeconds() - 3600), "refresh-1"))
        assertTrue(tm.isAccessTokenExpired(tm.getAccessToken()!!))
        // The whole point of #2: don't force re-login when a silent refresh is possible.
        assertTrue(tm.isAuthenticated())
    }

    @Test
    fun `no tokens at all is not authenticated`() {
        val tm = manager()
        assertFalse(tm.isAuthenticated())
    }

    @Test
    fun `opaque non-JWT token is treated as not-expired (fail-open)`() {
        val tm = manager()
        // Server's 401 remains the authority; we don't lock out unknown formats.
        assertFalse(tm.isAccessTokenExpired("not-a-jwt"))
    }

    private class InMemoryTokenStorage : TokenStorage {
        private val store = mutableMapOf<String, String?>()
        override fun saveToken(token: String) { store["token"] = token }
        override fun getToken(): String? = store["token"]
        override fun clearToken() { store.remove("token") }
        override fun saveRefreshToken(token: String) { store["refresh"] = token }
        override fun getRefreshToken(): String? = store["refresh"]
        override fun clearRefreshToken() { store.remove("refresh") }
        override fun saveRole(role: String) { store["role"] = role }
        override fun getRole(): String? = store["role"]
        override fun clearRole() { store.remove("role") }
        override fun saveUserName(name: String) { store["userName"] = name }
        override fun getUserName(): String? = store["userName"]
        override fun clearUserName() { store.remove("userName") }
        override fun saveUserEmail(email: String) { store["userEmail"] = email }
        override fun getUserEmail(): String? = store["userEmail"]
        override fun clearUserEmail() { store.remove("userEmail") }
        override fun saveUserId(id: String) { store["userId"] = id }
        override fun getUserId(): String? = store["userId"]
        override fun clearUserId() { store.remove("userId") }
        override fun saveTenantId(tenantId: String) { store["tenantId"] = tenantId }
        override fun getTenantId(): String? = store["tenantId"]
        override fun clearTenantId() { store.remove("tenantId") }
        override fun saveOAuthSession(oauth: Boolean) { store["oauth"] = oauth.toString() }
        override fun getOAuthSession(): Boolean = store["oauth"]?.toBoolean() ?: false
        override fun clearOAuthSession() { store.remove("oauth") }
    }
}
