package com.fivucsas.desktop.security

/**
 * Selects the best-available [SecureTokenStorage] for the current platform.
 *
 * Order of preference:
 *  1. Windows  -> DPAPI via JNA.
 *  2. Linux    -> libsecret via `secret-tool`; falls back to file+AES-GCM if absent.
 *  3. Other    -> file+AES-GCM (no macOS Keychain support in this round).
 *
 * The call is idempotent but not cached here; callers (e.g. Agent B's OAuth
 * loopback client) are expected to hold a single instance per process, typically
 * wired through Koin as a singleton alongside `TokenManager`.
 */
object TokenStorageFactory {
    fun create(): SecureTokenStorage = when (osName()) {
        OsName.WINDOWS -> runCatching { DpapiTokenStorage() }.getOrElse {
            logFallback("DPAPI", it)
            FallbackTokenStorage()
        }
        OsName.LINUX -> runCatching { LibsecretTokenStorage() }.getOrElse {
            logFallback("libsecret", it)
            FallbackTokenStorage()
        }
        OsName.OTHER -> FallbackTokenStorage()
    }

    private fun logFallback(backend: String, cause: Throwable) {
        System.err.println(
            "[TokenStorageFactory] WARNING: $backend unavailable (${cause.message}); " +
                "falling back to file-based AES-GCM storage.",
        )
    }
}
