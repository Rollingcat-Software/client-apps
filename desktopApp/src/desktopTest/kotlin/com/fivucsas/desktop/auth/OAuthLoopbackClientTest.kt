package com.fivucsas.desktop.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Unit tests for the hosted-first OAuth 2.0 loopback client (RFC 8252).
 *
 * These cover:
 *   - PKCE verifier+challenge generation (RFC 7636 §4.1-4.2)
 *   - State validation (RFC 6749 §10.12 CSRF mitigation)
 *   - Query-string extraction from a raw HTTP GET request line
 *
 * The full end-to-end HTTP round-trip against `verify.fivucsas.com` lives in
 * the integration suite (not in scope here).
 */
class OAuthLoopbackClientTest {

    // ---------- PKCE ----------

    @Test
    fun `pkce verifier is 43 chars of url-safe base64`() {
        val pair = PkcePair.generate()
        assertTrue(pair.verifier.length in 43..128,
            "verifier length must be in RFC 7636 range (got ${pair.verifier.length})")
        // URL-safe base64 alphabet: A-Z a-z 0-9 - _ (no padding)
        val urlSafe = Regex("^[A-Za-z0-9_\\-]+$")
        assertTrue(urlSafe.matches(pair.verifier),
            "verifier must use URL-safe base64 alphabet (no padding)")
    }

    @Test
    fun `pkce challenge is SHA256 of verifier base64url encoded`() {
        val pair = PkcePair.generate()
        val expected = base64UrlNoPad(
            MessageDigest.getInstance("SHA-256")
                .digest(pair.verifier.toByteArray(StandardCharsets.US_ASCII))
        )
        assertEquals(expected, pair.challenge)
    }

    @Test
    fun `pkce challengeFor is stable for a fixed verifier`() {
        // Example from RFC 7636 Appendix B
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val expected = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
        assertEquals(expected, PkcePair.challengeFor(verifier))
    }

    @Test
    fun `pkce pairs are different across generations`() {
        val a = PkcePair.generate()
        val b = PkcePair.generate()
        assertNotEquals(a.verifier, b.verifier)
        assertNotEquals(a.challenge, b.challenge)
    }

    @Test
    fun `pkce generate is deterministic under a seeded random`() {
        val fixedSeed = ByteArray(32) { it.toByte() } // 0x00..0x1F
        val random1 = DeterministicSecureRandom(fixedSeed)
        val random2 = DeterministicSecureRandom(fixedSeed)
        val a = PkcePair.generate(random1)
        val b = PkcePair.generate(random2)
        assertEquals(a.verifier, b.verifier)
        assertEquals(a.challenge, b.challenge)
    }

    // ---------- Query-string extraction ----------

    @Test
    fun `parseQuery extracts code and state from a GET request line`() {
        val line = "GET /callback?code=abc123&state=xyz HTTP/1.1"
        val params = OAuthLoopbackClient.parseQueryFromRequestLine(line)
        assertEquals("abc123", params["code"])
        assertEquals("xyz", params["state"])
    }

    @Test
    fun `parseQuery returns empty map when there is no query string`() {
        val line = "GET /callback HTTP/1.1"
        val params = OAuthLoopbackClient.parseQueryFromRequestLine(line)
        assertTrue(params.isEmpty())
    }

    @Test
    fun `parseQuery returns empty map for non-GET methods`() {
        val line = "POST /callback?code=abc HTTP/1.1"
        val params = OAuthLoopbackClient.parseQueryFromRequestLine(line)
        assertTrue(params.isEmpty())
    }

    @Test
    fun `parseQuery URL-decodes percent-encoded values`() {
        val line = "GET /callback?code=abc%2B123&state=a%20b HTTP/1.1"
        val params = OAuthLoopbackClient.parseQueryFromRequestLine(line)
        assertEquals("abc+123", params["code"])
        assertEquals("a b", params["state"])
    }

    @Test
    fun `parseQuery surfaces error params from the provider`() {
        val line = "GET /callback?error=access_denied&error_description=User%20cancelled HTTP/1.1"
        val params = OAuthLoopbackClient.parseQueryFromRequestLine(line)
        assertEquals("access_denied", params["error"])
        assertEquals("User cancelled", params["error_description"])
    }

    @Test
    fun `parseQuery handles key-only parameters gracefully`() {
        val line = "GET /callback?flag&code=x HTTP/1.1"
        val params = OAuthLoopbackClient.parseQueryFromRequestLine(line)
        assertEquals("", params["flag"])
        assertEquals("x", params["code"])
    }

    // ---------- State validation over a real loopback socket ----------

    @Test
    fun `awaitCallback returns the code when state matches`() {
        val client = newClientWithFastSocket()
        val server = ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))
        server.soTimeout = 2_000
        val port = server.localPort
        val expectedState = "my-state-123"

        // Simulate the browser sending the redirect after the user completes login.
        val browser = thread(name = "fake-browser", start = true) {
            // small delay so the server accept() is already armed
            Thread.sleep(50)
            Socket("127.0.0.1", port).use { s ->
                val w = OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)
                w.write("GET /callback?code=THE_CODE&state=my-state-123 HTTP/1.1\r\n")
                w.write("Host: 127.0.0.1:$port\r\n\r\n")
                w.flush()
                // Drain the response so the server's socket close is clean
                BufferedReader(InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))
                    .use { it.readText() }
            }
        }

        val code = client.awaitCallback(server, expectedState = expectedState)
        browser.join(5_000)
        server.close()
        assertEquals("THE_CODE", code)
    }

    @Test
    fun `awaitCallback throws on state mismatch`() {
        val client = newClientWithFastSocket()
        val server = ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))
        server.soTimeout = 2_000
        val port = server.localPort

        val browser = thread(name = "fake-browser", start = true) {
            Thread.sleep(50)
            Socket("127.0.0.1", port).use { s ->
                val w = OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)
                w.write("GET /callback?code=X&state=FORGED HTTP/1.1\r\n\r\n")
                w.flush()
                BufferedReader(InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))
                    .use { it.readText() }
            }
        }

        val error = assertThrows(OAuthException::class.java) {
            client.awaitCallback(server, expectedState = "expected-state")
        }
        assertTrue(error.message!!.contains("State mismatch"))

        browser.join(5_000)
        server.close()
    }

    @Test
    fun `awaitCallback throws when provider returns an error parameter`() {
        val client = newClientWithFastSocket()
        val server = ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))
        server.soTimeout = 2_000
        val port = server.localPort

        val browser = thread(name = "fake-browser", start = true) {
            Thread.sleep(50)
            Socket("127.0.0.1", port).use { s ->
                val w = OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)
                w.write("GET /callback?error=access_denied&error_description=User%20denied HTTP/1.1\r\n\r\n")
                w.flush()
                BufferedReader(InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))
                    .use { it.readText() }
            }
        }

        val error = assertThrows(OAuthException::class.java) {
            client.awaitCallback(server, expectedState = "whatever")
        }
        assertTrue(error.message!!.contains("access_denied"))

        browser.join(5_000)
        server.close()
    }

    // ---------- Authorize URL ----------

    @Test
    fun `buildAuthorizeUrl includes all OIDC + PKCE parameters`() {
        val client = OAuthLoopbackClient(browserLauncher = NoopBrowserLauncher())
        val url = client.buildAuthorizeUrl(
            redirectUri = "http://127.0.0.1:54321/callback",
            state = "the-state",
            codeChallenge = "the-challenge",
        )
        assertTrue(url.startsWith(OAuthConfig.AUTH_URL + "?"))
        assertTrue(url.contains("client_id=${OAuthConfig.CLIENT_ID}"))
        assertTrue(url.contains("response_type=code"))
        assertTrue(url.contains("code_challenge_method=S256"))
        assertTrue(url.contains("code_challenge=the-challenge"))
        assertTrue(url.contains("state=the-state"))
        // scopes are space-separated, URL-encoded -> "openid+profile+email" or "openid%20profile%20email"
        assertTrue(url.contains("openid") && url.contains("profile") && url.contains("email"))
    }

    // ---------- helpers ----------

    private fun newClientWithFastSocket() = OAuthLoopbackClient(
        browserLauncher = NoopBrowserLauncher(),
        socketTimeoutMs = 2_000,
    )

    private fun base64UrlNoPad(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private class NoopBrowserLauncher : BrowserLauncher {
        override fun open(url: String) { /* intentionally blank */ }
    }

    /** SecureRandom that always emits the same bytes. Used to assert determinism. */
    private class DeterministicSecureRandom(private val bytes: ByteArray) : SecureRandom() {
        override fun nextBytes(out: ByteArray) {
            for (i in out.indices) out[i] = bytes[i % bytes.size]
        }
    }
}
