package com.fivucsas.desktop.security

import com.sun.jna.platform.win32.Crypt32Util
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64
import java.util.Properties

/**
 * Windows [SecureTokenStorage] backed by DPAPI (Data Protection API).
 *
 * Calls [Crypt32Util.cryptProtectData] / [Crypt32Util.cryptUnprotectData]
 * with the default flags, which uses `CRYPTPROTECT_UI_FORBIDDEN` under the
 * hood and ties the ciphertext to the **current Windows user account**.
 * A second user on the same machine cannot decrypt these tokens; neither
 * can the user after a Windows password reset that invalidated their keys.
 *
 * Storage layout: one `Properties` file at `%APPDATA%\FIVUCSAS\tokens.dat`
 * containing `key = Base64(dpapi-blob)` lines. Each entry is individually
 * wrapped so corruption of one doesn't affect the rest.
 *
 * Should only be instantiated when [osName] == [OsName.WINDOWS]. Construction
 * on a non-Windows JVM will throw [StorageUnavailableException] on first use
 * because Crypt32 isn't loadable.
 */
class DpapiTokenStorage(
    private val file: File = defaultFile(),
) : SecureTokenStorage {

    private val lock = Any()

    init {
        file.parentFile?.mkdirs()
    }

    override fun save(key: String, value: String) = synchronized(lock) {
        val blob = try {
            Crypt32Util.cryptProtectData(value.toByteArray(StandardCharsets.UTF_8))
        } catch (ex: Throwable) {
            throw StorageUnavailableException("DPAPI encrypt failed", ex)
        }
        val props = readProps()
        props.setProperty(key, Base64.getEncoder().encodeToString(blob))
        writeProps(props)
    }

    override fun load(key: String): String? = synchronized(lock) {
        val encoded = readProps().getProperty(key) ?: return null
        return try {
            val blob = Base64.getDecoder().decode(encoded)
            val plain = Crypt32Util.cryptUnprotectData(blob)
            String(plain, StandardCharsets.UTF_8)
        } catch (_: Throwable) {
            // Different user, rotated machine, or tampered blob — caller gets null.
            null
        }
    }

    override fun clear(key: String) = synchronized(lock) {
        val props = readProps()
        if (props.remove(key) != null) writeProps(props)
    }

    override fun clearAll() = synchronized(lock) {
        if (file.exists()) file.delete()
    }

    // ----- internals -----

    private fun readProps(): Properties {
        val p = Properties()
        if (file.exists()) {
            file.inputStream().use { p.load(it) }
        }
        return p
    }

    private fun writeProps(props: Properties) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.outputStream().use { props.store(it, "FIVUCSAS tokens (DPAPI). Do not edit.") }
        try {
            Files.move(
                tmp.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: IOException) {
            // Some Windows filesystems reject ATOMIC_MOVE; fall back to non-atomic.
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    companion object {
        private fun defaultFile(): File {
            val appData = System.getenv("APPDATA")
            val base = if (!appData.isNullOrBlank()) {
                File(appData, "FIVUCSAS")
            } else {
                File(System.getProperty("user.home") ?: ".", "AppData/Roaming/FIVUCSAS")
            }
            return File(base, "tokens.dat")
        }
    }
}
