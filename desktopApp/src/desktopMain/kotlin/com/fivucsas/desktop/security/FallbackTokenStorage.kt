package com.fivucsas.desktop.security

import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * File-backed [SecureTokenStorage] using PBKDF2(HMAC-SHA256) + AES-256-GCM.
 *
 * This is the **last-resort** backend, chosen when:
 * - The platform is neither Windows (DPAPI) nor Linux+libsecret, OR
 * - libsecret is missing / GNOME Keyring daemon isn't running.
 *
 * Security properties (in decreasing strength):
 *  - AES-256-GCM authenticated encryption, 96-bit random IV per entry.
 *  - Key = PBKDF2WithHmacSHA256(password = machine-id, salt = SHA-256("fivucsas" + hostname + user), iterations = 200_000).
 *  - Each entry is encrypted individually, so file corruption is contained.
 *  - File permissions on POSIX systems are chmod'd to `0600`.
 *
 * Weaknesses vs. a real keystore:
 *  - `/etc/machine-id` is readable by every process on most distros, so any
 *    attacker with local code-exec as the same user can decrypt. DPAPI and
 *    libsecret avoid that by binding to session credentials / kernel state.
 *  - We log a warning at construction so the user knows they're on fallback.
 *
 * The encrypted blob layout per entry is `Base64( IV || ciphertext||tag )`,
 * stored as a line in a Java `Properties` file.
 */
class FallbackTokenStorage(
    private val file: File = defaultFile(),
    private val machineSecret: ByteArray = readMachineSecret(),
) : SecureTokenStorage {

    private val lock = Any()
    private val secretKey: SecretKeySpec by lazy { deriveKey() }

    init {
        // Surface the fact that we're on fallback so ops can spot it in logs.
        // Using println keeps this dependency-free; upstream wiring can replace
        // with a proper logger later.
        System.err.println(
            "[FallbackTokenStorage] WARNING: using file-based AES-GCM storage at ${file.absolutePath}. " +
                "Prefer DPAPI (Windows) or libsecret (Linux) when available.",
        )
        ensureDirectory(file.parentFile)
    }


    override fun save(key: String, value: String) = synchronized(lock) {
        val props = readProps()
        props.setProperty(key, encrypt(value))
        writeProps(props)
    }

    override fun load(key: String): String? = synchronized(lock) {
        val raw = readProps().getProperty(key) ?: return null
        return try {
            decrypt(raw)
        } catch (ex: Exception) {
            // Swallow: wrong key / tampered blob / legacy value. Caller gets null.
            null
        }
    }

    override fun clear(key: String) = synchronized(lock) {
        val props = readProps()
        if (props.remove(key) != null) writeProps(props)
    }

    override fun clearAll() = synchronized(lock) {
        if (file.exists()) {
            // Overwrite once before deleting to discourage casual recovery.
            try {
                file.outputStream().use { it.write(ByteArray(0)) }
            } catch (_: IOException) {
                // best-effort
            }
            file.delete()
        }
    }

    // ----- internals -----

    private fun deriveKey(): SecretKeySpec {
        val host = try { InetAddress.getLocalHost().hostName } catch (_: Exception) { "unknown-host" }
        val user = System.getProperty("user.name") ?: "unknown-user"
        val saltSource = "fivucsas:$host:$user".toByteArray(StandardCharsets.UTF_8)
        val salt = MessageDigest.getInstance("SHA-256").digest(saltSource)

        // PBKDF2 with the machine-id as "password". Even though machine-id is
        // readable, the iteration count raises the cost of brute force against
        // a stolen tokens.enc (e.g. exfiltrated via a separate-user read).
        val spec: KeySpec = PBEKeySpec(
            String(machineSecret, StandardCharsets.UTF_8).toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            256,
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun encrypt(plaintext: String): String {
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ct = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val combined = ByteArray(iv.size + ct.size).also {
            System.arraycopy(iv, 0, it, 0, iv.size)
            System.arraycopy(ct, 0, it, iv.size, ct.size)
        }
        return Base64.getEncoder().encodeToString(combined)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.getDecoder().decode(encoded)
        require(combined.size > GCM_IV_LENGTH) { "ciphertext too short" }
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ct = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ct), StandardCharsets.UTF_8)
    }

    private fun readProps(): Properties {
        val p = Properties()
        if (file.exists()) {
            file.inputStream().use { p.load(it) }
        }
        return p
    }

    private fun writeProps(props: Properties) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.outputStream().use { props.store(it, "FIVUCSAS tokens (AES-GCM). Do not edit.") }
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        tightenPermissions(file)
    }

    private fun ensureDirectory(dir: File?) {
        if (dir == null) return
        if (!dir.exists()) dir.mkdirs()
        tightenPermissions(dir)
    }

    private fun tightenPermissions(path: File) {
        // Best-effort: POSIX platforms get 0600 (files) / 0700 (dirs).
        // Windows relies on DPAPI backend anyway; fallback there is acceptable.
        try {
            val perms = if (path.isDirectory) {
                PosixFilePermissions.fromString("rwx------")
            } else {
                PosixFilePermissions.fromString("rw-------")
            }
            Files.setPosixFilePermissions(path.toPath(), perms)
        } catch (_: UnsupportedOperationException) {
            // Non-POSIX filesystem (e.g. Windows NTFS) — skip.
        } catch (_: IOException) {
            // Best-effort only.
        } catch (_: SecurityException) {
            // Best-effort only.
        }
    }

    companion object {
        private const val AES_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val GCM_IV_LENGTH = 12
        private const val PBKDF2_ITERATIONS = 200_000

        private fun defaultFile(): File {
            val home = System.getProperty("user.home") ?: "."
            val os = osName()
            val base = when (os) {
                OsName.WINDOWS -> {
                    val appData = System.getenv("APPDATA")
                    if (!appData.isNullOrBlank()) File(appData, "FIVUCSAS")
                    else File(home, "AppData/Roaming/FIVUCSAS")
                }
                OsName.LINUX -> File(home, ".config/fivucsas")
                OsName.OTHER -> File(home, ".fivucsas")
            }
            return File(base, "tokens.enc")
        }

        /**
         * Environment variable / system property used to opt in to the insecure
         * file-based fallback when no real keystore is available. Intended only
         * for CI / headless integration tests; production builds must never set
         * this.
         */
        const val INSECURE_FALLBACK_ENV = "FIVUCSAS_ALLOW_INSECURE_FALLBACK"
        const val INSECURE_FALLBACK_PROP = "fivucsas.allowInsecureFallback"

        private fun insecureFallbackAllowed(): Boolean {
            val env = System.getenv(INSECURE_FALLBACK_ENV)?.trim()
            if (env == "1" || env.equals("true", ignoreCase = true)) return true
            val prop = System.getProperty(INSECURE_FALLBACK_PROP)?.trim()
            return prop.equals("true", ignoreCase = true) || prop == "1"
        }

        /**
         * Derive a stable machine secret.
         *
         * MO-H3 (2026-04-19 audit): previously fell back to a
         * `hostname+user+os.name` string when `/etc/machine-id` was unavailable,
         * which is a trivially guessable "encryption" key. We now REFUSE to
         * derive a secret in that case and throw
         * [SecureStorageUnavailableException]. CI / headless test rigs may
         * opt in explicitly via [INSECURE_FALLBACK_ENV] /
         * [INSECURE_FALLBACK_PROP]; those paths emit a loud warning to stderr.
         */
        private fun readMachineSecret(): ByteArray {
            val candidates = listOf(
                File("/etc/machine-id"),
                File("/var/lib/dbus/machine-id"),
            )
            for (f in candidates) {
                if (f.canRead()) {
                    val txt = runCatching { f.readText().trim() }.getOrNull()
                    if (!txt.isNullOrBlank()) return txt.toByteArray(StandardCharsets.UTF_8)
                }
            }
            if (!insecureFallbackAllowed()) {
                throw SecureStorageUnavailableException(
                    "No platform keystore available (DPAPI/libsecret failed) and no readable " +
                        "machine-id — refusing to derive an encryption key from hostname+user. " +
                        "Set $INSECURE_FALLBACK_ENV=1 only if you understand the risk (CI / headless tests).",
                )
            }
            System.err.println(
                "[FallbackTokenStorage] WARNING: $INSECURE_FALLBACK_ENV override in effect. " +
                    "Deriving encryption key from hostname+user+os.name — this is INSECURE and " +
                    "must never be used in production.",
            )
            val host = try { InetAddress.getLocalHost().hostName } catch (_: Exception) { "unknown-host" }
            val user = System.getProperty("user.name") ?: "unknown-user"
            val os = System.getProperty("os.name") ?: "unknown-os"
            return "fivucsas-fallback:$host:$user:$os".toByteArray(StandardCharsets.UTF_8)
        }
    }
}
