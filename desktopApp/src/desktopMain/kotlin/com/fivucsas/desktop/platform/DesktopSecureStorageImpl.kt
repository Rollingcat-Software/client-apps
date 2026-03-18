package com.fivucsas.desktop.platform

import com.fivucsas.shared.platform.ISecureStorage
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop Secure Storage Implementation
 *
 * Desktop implementation of ISecureStorage using Java Preferences API
 * with AES-256-GCM encryption. The encryption key is derived from
 * machine-specific data (hostname + user + OS) to tie storage to this device.
 */
class DesktopSecureStorageImpl : ISecureStorage {

    private val preferences: Preferences = Preferences.userNodeForPackage(
        DesktopSecureStorageImpl::class.java
    )

    private val secretKey: SecretKeySpec by lazy { deriveKey() }

    companion object {
        private const val AES_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    /**
     * Derive a 256-bit AES key from machine-specific data.
     * Uses hostname, username, and OS name as entropy sources.
     */
    private fun deriveKey(): SecretKeySpec {
        val hostname = try { InetAddress.getLocalHost().hostName } catch (_: Exception) { "unknown-host" }
        val username = System.getProperty("user.name") ?: "unknown-user"
        val osName = System.getProperty("os.name") ?: "unknown-os"
        val seed = "fivucsas-desktop:$hostname:$username:$osName"

        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(seed.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypt a plaintext string using AES-256-GCM.
     * Returns a Base64-encoded string containing IV + ciphertext.
     */
    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv // GCM generates a random IV
        val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

        // Prepend IV to ciphertext
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypt a Base64-encoded string (IV + ciphertext) using AES-256-GCM.
     * Returns null if decryption fails (e.g., data was stored unencrypted before migration).
     */
    private fun decrypt(encoded: String): String? {
        return try {
            val combined = Base64.getDecoder().decode(encoded)
            if (combined.size < GCM_IV_LENGTH) return null

            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            // If decryption fails, the value may be a legacy unencrypted value.
            // Return it as-is for backward compatibility, but re-encrypt on next save.
            null
        }
    }

    override fun saveString(key: String, value: String) {
        preferences.put(key, encrypt(value))
        preferences.flush()
    }

    override fun getString(key: String): String? {
        val raw = preferences.get(key, null) ?: return null
        // Try decryption first; fall back to raw value for legacy unencrypted data
        val decrypted = decrypt(raw)
        if (decrypted != null) return decrypted
        // Legacy unencrypted value — re-encrypt transparently
        saveString(key, raw)
        return raw
    }

    override fun saveBoolean(key: String, value: Boolean) {
        preferences.putBoolean(key, value)
        preferences.flush()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun saveInt(key: String, value: Int) {
        preferences.putInt(key, value)
        preferences.flush()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    override fun saveLong(key: String, value: Long) {
        preferences.putLong(key, value)
        preferences.flush()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }

    override fun remove(key: String) {
        preferences.remove(key)
        preferences.flush()
    }

    override fun clear() {
        preferences.clear()
        preferences.flush()
    }

    override fun contains(key: String): Boolean {
        return preferences.get(key, null) != null
    }
}
