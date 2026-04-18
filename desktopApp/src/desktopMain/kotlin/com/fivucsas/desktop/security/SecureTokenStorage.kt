package com.fivucsas.desktop.security

/**
 * Cross-platform secure token storage contract for the FIVUCSAS desktop app.
 *
 * Implementations should wrap the underlying OS keystore where available:
 * - Windows: DPAPI (Data Protection API) via JNA — [DpapiTokenStorage]
 * - Linux:   libsecret / GNOME Keyring via `secret-tool` — [LibsecretTokenStorage]
 * - Any:     AES-GCM encrypted file (machine-id-derived key) — [FallbackTokenStorage]
 *
 * Use [TokenStorageFactory.create] to obtain an instance appropriate for the
 * current platform. Instances are expected to be thread-safe for the
 * save/load/clear operations (implementations guard with coarse locks).
 *
 * Agent B's OAuth loopback client consumes this interface to persist
 * access/refresh tokens returned by `verify.fivucsas.com/oauth2/token`.
 */
interface SecureTokenStorage {
    /**
     * Persist [value] under [key]. Overwrites any prior value.
     * @throws StorageUnavailableException if the backend is not available.
     */
    fun save(key: String, value: String)

    /**
     * Return the value previously stored under [key], or `null` if no entry
     * exists or decryption fails (e.g. machine-id rotated, DPAPI scope mismatch).
     */
    fun load(key: String): String?

    /**
     * Remove the entry for [key]. No-op if the entry does not exist.
     */
    fun clear(key: String)

    /**
     * Remove every entry created by this storage. Used on sign-out or on
     * detection of a compromised device.
     */
    fun clearAll()
}

/**
 * Thrown when a backend required for [SecureTokenStorage] is not installed
 * or cannot be reached. The caller should catch this and fall back to
 * [FallbackTokenStorage] (already handled inside [TokenStorageFactory]).
 */
class StorageUnavailableException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
