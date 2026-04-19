package com.fivucsas.desktop.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory authoritative source for the current OAuth token bundle.
 *
 * On app start, call [restore] to hydrate from [SecureTokenStorage]. After a
 * successful [OAuthLoopbackClient.login], call [onLoginSuccess] to persist and
 * publish. [logout] wipes both memory and disk.
 *
 * UI observes [tokens] as a StateFlow so Compose recomposes automatically when
 * tokens appear or disappear.
 */
class AuthStateManager(
    private val storage: SecureTokenStorage,
) {

    private val _tokens = MutableStateFlow<AccessTokens?>(null)
    val tokens: StateFlow<AccessTokens?> = _tokens.asStateFlow()

    val isAuthenticated: Boolean
        get() = _tokens.value?.let { !isExpired(it) } == true

    /** Load tokens from secure storage (idempotent). Call once on app start. */
    fun restore() {
        _tokens.value = storage.load()
    }

    /** Persist + publish a freshly-minted token bundle. */
    fun onLoginSuccess(newTokens: AccessTokens) {
        storage.save(newTokens)
        _tokens.value = newTokens
    }

    /** Called by [RefreshInterceptor] after a successful `grant_type=refresh_token` exchange. */
    fun onTokensRefreshed(newTokens: AccessTokens) {
        onLoginSuccess(newTokens)
    }

    /** Clear memory + disk. Safe to call when not signed in. */
    fun logout() {
        storage.clear()
        _tokens.value = null
    }

    private fun isExpired(t: AccessTokens, skewMs: Long = 30_000): Boolean =
        System.currentTimeMillis() >= (t.expiresAt - skewMs)
}
