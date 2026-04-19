package com.fivucsas.desktop.auth

import com.fivucsas.desktop.security.SecureTokenStorage
import com.fivucsas.desktop.security.TokenStorageFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * In-memory authoritative source for the current OAuth token bundle.
 *
 * On app start, call [restore] to hydrate from the underlying
 * [SecureTokenStorage] (DPAPI on Windows / libsecret on Linux / AES-GCM file
 * fallback on anything else). After a successful [OAuthLoopbackClient.login],
 * call [onLoginSuccess] to persist and publish. [logout] wipes both memory and
 * the backing store.
 *
 * UI observes [tokens] as a StateFlow so Compose recomposes automatically when
 * tokens appear or disappear.
 *
 * MO-C3 (2026-04-19 audit): previously delegated to a parallel bundle-level
 * `com.fivucsas.desktop.auth.SecureTokenStorage` interface and a
 * `FileBackedTokenStorage` adapter. Those have been collapsed into this class:
 * we serialize the full bundle to JSON and put it under a single well-known
 * key in the key/value store. One interface, one storage surface.
 */
class AuthStateManager(
    private val storage: SecureTokenStorage = TokenStorageFactory.create(),
    private val key: String = DEFAULT_KEY,
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _tokens = MutableStateFlow<AccessTokens?>(null)
    val tokens: StateFlow<AccessTokens?> = _tokens.asStateFlow()

    val isAuthenticated: Boolean
        get() = _tokens.value?.let { !isExpired(it) } == true

    /** Load tokens from secure storage (idempotent). Call once on app start. */
    fun restore() {
        _tokens.value = loadFromStorage()
    }

    /** Persist + publish a freshly-minted token bundle. */
    fun onLoginSuccess(newTokens: AccessTokens) {
        saveToStorage(newTokens)
        _tokens.value = newTokens
    }

    /** Called by [RefreshInterceptor] after a successful `grant_type=refresh_token` exchange. */
    fun onTokensRefreshed(newTokens: AccessTokens) {
        onLoginSuccess(newTokens)
    }

    /** Clear memory + disk. Safe to call when not signed in. */
    fun logout() {
        storage.clear(key)
        _tokens.value = null
    }

    private fun saveToStorage(tokens: AccessTokens) {
        val payload = json.encodeToString(AccessTokens.serializer(), tokens)
        storage.save(key, payload)
    }

    private fun loadFromStorage(): AccessTokens? {
        val payload = storage.load(key) ?: return null
        return runCatching { json.decodeFromString(AccessTokens.serializer(), payload) }
            .getOrNull()
    }

    private fun isExpired(t: AccessTokens, skewMs: Long = 30_000): Boolean =
        System.currentTimeMillis() >= (t.expiresAt - skewMs)

    companion object {
        /** Single well-known key under which the full OAuth token bundle is stored. */
        const val DEFAULT_KEY = "oauth_tokens"
    }
}
