package com.fivucsas.shared.data.local

import com.fivucsas.shared.domain.repository.AuthTokens
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Token Manager - Centralized token management
 *
 * Handles access token and refresh token storage/retrieval.
 * Used by NetworkModule for automatic JWT injection.
 */
class TokenManager(
    private val tokenStorage: TokenStorage
) {
    private var cachedTokens: AuthTokens? = null
    private var cachedRole: String? = null

    private var cachedUserName: String? = null
    private var cachedUserEmail: String? = null
    private var cachedUserId: String? = null
    private var cachedTenantId: String? = null

    /**
     * Save authentication tokens
     */
    fun saveTokens(tokens: AuthTokens) {
        cachedTokens = tokens
        tokenStorage.saveToken(tokens.accessToken)
        tokenStorage.saveRefreshToken(tokens.refreshToken)
        tokenStorage.saveOAuthSession(tokens.oauthSession)
        tokenStorage.saveRole(tokens.role)
        cachedRole = tokens.role
        if (tokens.userName.isNotBlank()) {
            tokenStorage.saveUserName(tokens.userName)
            cachedUserName = tokens.userName
        }
        if (tokens.userEmail.isNotBlank()) {
            tokenStorage.saveUserEmail(tokens.userEmail)
            cachedUserEmail = tokens.userEmail
        }
        if (tokens.userId.isNotBlank()) {
            tokenStorage.saveUserId(tokens.userId)
            cachedUserId = tokens.userId
        }
        if (tokens.tenantId.isNotBlank()) {
            tokenStorage.saveTenantId(tokens.tenantId)
            cachedTenantId = tokens.tenantId
        }
    }

    /**
     * Get current access token.
     *
     * A blank stored value is treated as ABSENT (returns null): the DTO mappers
     * default a missing token to "", and a "" must never look like a usable token.
     */
    fun getAccessToken(): String? {
        return (cachedTokens?.accessToken ?: tokenStorage.getToken())?.takeIf { it.isNotBlank() }
    }

    /**
     * Get current refresh token (from cache or persistent storage).
     *
     * A blank stored token is treated as ABSENT (returns null): the DTO mappers
     * default a missing `refresh_token` to "", and a "" must never look like a
     * usable token — otherwise the refresh path would POST an empty grant, the
     * server would (rightly) reject it as `invalid_grant`, and the user would be
     * spuriously forced to re-login. Treating blank as absent here also makes
     * [isAuthenticated] fall back correctly.
     */
    fun getRefreshToken(): String? {
        return (cachedTokens?.refreshToken ?: tokenStorage.getRefreshToken())?.takeIf { it.isNotBlank() }
    }

    /**
     * True when the stored session originated from the hosted-first OAuth login,
     * so silent refresh must use `grant_type=refresh_token` against
     * `/oauth2/token` rather than the legacy `/auth/refresh`.
     */
    fun isOAuthSession(): Boolean {
        return cachedTokens?.oauthSession ?: tokenStorage.getOAuthSession()
    }

    /**
     * Check if user is authenticated.
     *
     * Returns true when EITHER:
     *  - a non-expired access token is present, OR
     *  - the access token is missing/expired but a refresh token exists.
     *
     * The second case is what keeps a returning user signed in after the short
     * (15-min) access-token lifetime: previously this checked token *presence*
     * only, so the app routed to the dashboard and the first data call 401'd into
     * a perceived logout; now the access token is allowed to be stale because the
     * NetworkModule 401 handler transparently refreshes it and retries. We never
     * grant access on a missing/expired access token WITHOUT a refresh token.
     */
    fun isAuthenticated(): Boolean {
        val accessToken = getAccessToken() ?: return getRefreshToken() != null
        if (!isAccessTokenExpired(accessToken)) return true
        // Access token is expired — still authenticated if we can silently refresh.
        return getRefreshToken() != null
    }

    /**
     * True when the access token's `exp` claim is in the past (with a small skew
     * margin). If the token can't be parsed as a JWT we treat it as NOT expired
     * (fail-open) so opaque/unknown token formats keep working — the server's 401
     * remains the ultimate authority and drives the refresh path.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun isAccessTokenExpired(token: String, skewSeconds: Long = 30): Boolean {
        val expSeconds = jwtExpirySeconds(token) ?: return false
        val nowSeconds = Clock.System.now().toEpochMilliseconds() / 1000
        return nowSeconds >= (expSeconds - skewSeconds)
    }

    /**
     * Decode a JWT's `exp` claim (epoch seconds) from its payload without
     * verifying the signature — verification is the server's job; we only need
     * the expiry to decide whether to pre-emptively refresh on startup. Returns
     * null for non-JWT/unparseable tokens.
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun jwtExpirySeconds(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            // JWT payloads are base64url, normally WITHOUT padding;
            // ABSENT_OPTIONAL tolerates either padded or unpadded input.
            val payloadBytes = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                .decode(parts[1])
            val payload = Json.parseToJsonElement(payloadBytes.decodeToString()).jsonObject
            payload["exp"]?.jsonPrimitive?.longOrNull
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get current user role
     */
    fun getRole(): String? {
        return cachedRole ?: tokenStorage.getRole()
    }

    /**
     * Get current user name
     */
    fun getUserName(): String? {
        return cachedUserName ?: tokenStorage.getUserName()
    }

    /**
     * Get current user email
     */
    fun getUserEmail(): String? {
        return cachedUserEmail ?: tokenStorage.getUserEmail()
    }

    /**
     * Get current user id
     */
    fun getUserId(): String? {
        return cachedUserId ?: tokenStorage.getUserId()
    }

    /**
     * Get current tenant id
     */
    fun getTenantId(): String? {
        return cachedTenantId ?: tokenStorage.getTenantId()
    }

    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        cachedTokens = null
        cachedRole = null
        cachedUserName = null
        cachedUserEmail = null
        cachedUserId = null
        cachedTenantId = null
        tokenStorage.clearToken()
        tokenStorage.clearRefreshToken()
        tokenStorage.clearOAuthSession()
        tokenStorage.clearRole()
        tokenStorage.clearUserName()
        tokenStorage.clearUserEmail()
        tokenStorage.clearUserId()
        tokenStorage.clearTenantId()
    }

    /**
     * Update tokens after refresh
     */
    fun updateTokens(tokens: AuthTokens) {
        saveTokens(tokens)
    }
}
