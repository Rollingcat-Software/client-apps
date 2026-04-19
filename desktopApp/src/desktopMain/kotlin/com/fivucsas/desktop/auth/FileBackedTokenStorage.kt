package com.fivucsas.desktop.auth

import com.fivucsas.desktop.security.TokenStorageFactory
import com.fivucsas.desktop.security.SecureTokenStorage as PlatformKvStorage
import kotlinx.serialization.json.Json

/**
 * [SecureTokenStorage] backed by Agent C's platform-aware key/value storage
 * (DPAPI on Windows, libsecret on Linux, AES-GCM file on any other OS).
 *
 * The whole token bundle is stored under a single key `oauth_tokens` as JSON.
 * Nothing about this class depends on a specific backend — swap the underlying
 * [PlatformKvStorage] by handing a different instance to the constructor.
 *
 * The name `FileBackedTokenStorage` predates the DPAPI/libsecret integration;
 * it is kept for source-compat with the rest of the hosted-first OAuth work
 * and because the fallback backend *is* file-backed on unsupported platforms.
 */
class FileBackedTokenStorage(
    private val backend: PlatformKvStorage = TokenStorageFactory.create(),
    private val key: String = DEFAULT_KEY,
) : SecureTokenStorage {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun save(tokens: AccessTokens) {
        val payload = json.encodeToString(AccessTokens.serializer(), tokens)
        backend.save(key, payload)
    }

    override fun load(): AccessTokens? {
        val payload = backend.load(key) ?: return null
        return runCatching { json.decodeFromString(AccessTokens.serializer(), payload) }
            .getOrNull()
    }

    override fun clear() {
        backend.clear(key)
    }

    companion object {
        /** Single well-known key — the whole OAuth bundle lives here. */
        const val DEFAULT_KEY = "oauth_tokens"
    }
}
