package com.fivucsas.desktop.auth

/**
 * Bundle-level token storage used by [AuthStateManager].
 *
 * This wraps the full OAuth token bundle as a single unit so the OAuth layer
 * does not have to know about platform-specific key schemes. Real persistence
 * is delegated to Agent C's
 * [com.fivucsas.desktop.security.SecureTokenStorage] (key/value, per-platform
 * DPAPI on Windows / libsecret on Linux / AES-GCM file fallback).
 *
 * Default implementation: [PlatformTokenStorage].
 */
interface SecureTokenStorage {
    fun save(tokens: AccessTokens)
    fun load(): AccessTokens?
    fun clear()
}
