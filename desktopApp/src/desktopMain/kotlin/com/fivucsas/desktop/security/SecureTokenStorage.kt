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

/**
 * MO-H3 (2026-04-19 audit).
 *
 * Thrown by [FallbackTokenStorage] when no real keystore is available AND the
 * environment did not opt in to the insecure file-based fallback. Callers
 * (typically the OAuth flow) should surface this to the user as
 * "secure storage unavailable — please sign in interactively" rather than
 * silently deriving an encryption key from world-readable `/etc/machine-id`.
 *
 * Opt-in override: set the environment variable
 * `FIVUCSAS_ALLOW_INSECURE_FALLBACK=1` (or the system property
 * `fivucsas.allowInsecureFallback=true`). Intended only for CI / headless
 * tests; production MUST NOT enable this.
 */
class SecureStorageUnavailableException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
