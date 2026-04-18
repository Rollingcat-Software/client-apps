package com.fivucsas.desktop.security

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests for [FallbackTokenStorage]. The DPAPI and libsecret backends require
 * real OS surfaces and are not covered here; see the manual-test notes at the
 * bottom of this file.
 */
class FallbackTokenStorageTest {

    private val secret = "machine-id-test-12345".toByteArray(StandardCharsets.UTF_8)

    private fun storage(dir: File, keyBytes: ByteArray = secret): FallbackTokenStorage =
        FallbackTokenStorage(file = File(dir, "tokens.enc"), machineSecret = keyBytes)

    @Test
    fun `save then load returns the original value`(@TempDir dir: File) {
        val sut = storage(dir)
        sut.save("access_token", "eyJhbGciOiJIUzI1NiJ9.payload.signature")

        assertEquals("eyJhbGciOiJIUzI1NiJ9.payload.signature", sut.load("access_token"))
    }

    @Test
    fun `multiple entries are isolated`(@TempDir dir: File) {
        val sut = storage(dir)
        sut.save("access_token", "A")
        sut.save("refresh_token", "R")

        assertEquals("A", sut.load("access_token"))
        assertEquals("R", sut.load("refresh_token"))
    }

    @Test
    fun `overwrite replaces prior ciphertext`(@TempDir dir: File) {
        val sut = storage(dir)
        sut.save("k", "v1")
        sut.save("k", "v2")

        assertEquals("v2", sut.load("k"))
    }

    @Test
    fun `clear removes the specified key only`(@TempDir dir: File) {
        val sut = storage(dir)
        sut.save("a", "1")
        sut.save("b", "2")

        sut.clear("a")

        assertNull(sut.load("a"))
        assertEquals("2", sut.load("b"))
    }

    @Test
    fun `clearAll deletes the entire file`(@TempDir dir: File) {
        val file = File(dir, "tokens.enc")
        val sut = FallbackTokenStorage(file = file, machineSecret = secret)
        sut.save("a", "1")
        assertTrue(file.exists(), "file should exist after save")

        sut.clearAll()

        assertFalse(file.exists(), "file should be gone after clearAll")
        assertNull(sut.load("a"))
    }

    @Test
    fun `load returns null for missing key`(@TempDir dir: File) {
        val sut = storage(dir)
        assertNull(sut.load("never-set"))
    }

    @Test
    fun `encrypting same plaintext twice yields different ciphertexts (IV randomization)`(@TempDir dir: File) {
        val file = File(dir, "tokens.enc")
        val sut = FallbackTokenStorage(file = file, machineSecret = secret)

        sut.save("k", "same-value")
        val first = readRawProps(file).getProperty("k")
        assertNotNull(first)

        sut.save("k", "same-value")
        val second = readRawProps(file).getProperty("k")
        assertNotNull(second)

        assertNotEquals(
            first, second,
            "AES-GCM IV should differ per-save so identical plaintext produces distinct ciphertexts",
        )
        // And both must still decrypt to the same plaintext.
        assertEquals("same-value", sut.load("k"))
    }

    @Test
    fun `load returns null when machine secret (key) changes`(@TempDir dir: File) {
        val file = File(dir, "tokens.enc")
        val writer = FallbackTokenStorage(
            file = file,
            machineSecret = "secret-A".toByteArray(StandardCharsets.UTF_8),
        )
        writer.save("k", "hello")

        // Simulate a rotated /etc/machine-id or moved config to a new host.
        val reader = FallbackTokenStorage(
            file = file,
            machineSecret = "secret-B".toByteArray(StandardCharsets.UTF_8),
        )
        // GCM tag verification fails -> we return null rather than throw, so
        // the caller can prompt for re-auth.
        assertNull(reader.load("k"))
    }

    @Test
    fun `tampered ciphertext returns null instead of throwing`(@TempDir dir: File) {
        val file = File(dir, "tokens.enc")
        val sut = FallbackTokenStorage(file = file, machineSecret = secret)
        sut.save("k", "hello")

        // Corrupt the stored blob.
        val props = readRawProps(file)
        val original = props.getProperty("k")
        props.setProperty("k", original.reversed())
        file.outputStream().use { props.store(it, null) }

        assertNull(sut.load("k"))
    }

    @Test
    fun `creates parent directory if missing`(@TempDir dir: File) {
        val nested = File(dir, "a/b/c")
        val file = File(nested, "tokens.enc")
        assertFalse(nested.exists())

        val sut = FallbackTokenStorage(file = file, machineSecret = secret)
        sut.save("k", "v")

        assertTrue(nested.exists())
        assertEquals("v", sut.load("k"))
    }

    private fun readRawProps(file: File): Properties {
        val p = Properties()
        file.inputStream().use { p.load(it) }
        return p
    }
}

/*
 * Manual-test notes for the OS-specific backends (not runnable in CI):
 *
 * DpapiTokenStorage (Windows):
 *   1. Build an MSI: `./gradlew :desktopApp:packageMsi` (on a Windows runner).
 *   2. Install, launch as user A, trigger login -> tokens persist to
 *      %APPDATA%\FIVUCSAS\tokens.dat.
 *   3. Inspect tokens.dat with Notepad: values should be opaque base64 blobs
 *      (DPAPI wraps with CRYPTPROTECT_UI_FORBIDDEN).
 *   4. Switch to user B on the same machine and confirm `load()` returns
 *      null (ciphertext bound to user A's SID).
 *   5. Back as user A: call `clearAll()` and verify tokens.dat is deleted.
 *
 * LibsecretTokenStorage (Linux, GNOME or KDE):
 *   1. Ensure libsecret-tools is installed (`apt install libsecret-tools`).
 *   2. Run the desktop app, log in, confirm entries appear in `seahorse`
 *      under "Login" keyring with label "FIVUCSAS Token: <key>".
 *   3. `secret-tool lookup service fivucsas account access_token` should
 *      print the token on stdout.
 *   4. `secret-tool clear service fivucsas` removes every FIVUCSAS entry and
 *      the next `load()` returns null.
 *   5. Kill `gnome-keyring-daemon` mid-session and re-launch: the library
 *      should surface a StorageUnavailableException at factory time, and the
 *      factory should fall back to FallbackTokenStorage with a WARNING log.
 */
