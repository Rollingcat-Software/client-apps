package com.fivucsas.desktop.security

/**
 * Coarse operating-system classification for picking a [SecureTokenStorage]
 * backend. macOS is intentionally absent from this round — the factory
 * routes non-Windows / non-Linux hosts to the AES-GCM [FallbackTokenStorage].
 */
internal enum class OsName { WINDOWS, LINUX, OTHER }

internal fun osName(): OsName {
    val raw = (System.getProperty("os.name") ?: "").lowercase()
    return when {
        "win" in raw -> OsName.WINDOWS
        "nux" in raw || "nix" in raw || "aix" in raw -> OsName.LINUX
        else -> OsName.OTHER
    }
}
