package com.fivucsas.desktop.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale

/**
 * RFC 8252 OAuth 2.0 Native App loopback flow.
 *
 * Flow:
 *   1. Bind an ephemeral `ServerSocket` on 127.0.0.1:<os-assigned>
 *   2. Build authorize URL with PKCE S256 + random `state`
 *   3. Open URL in the user's default browser
 *   4. Wait for ONE GET request on the loopback socket
 *   5. Validate `state`, extract `code`
 *   6. Respond with a branded "you can close this window" page
 *   7. Exchange `code` at TOKEN_URL using the PKCE verifier
 *   8. Return [AccessTokens]
 *
 * Same pattern as `gh auth login`, `gcloud auth login`, `az login`, `aws sso login`.
 */
class OAuthLoopbackClient(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val browserLauncher: BrowserLauncher = SystemBrowserLauncher(),
    private val random: SecureRandom = SecureRandom(),
    private val socketTimeoutMs: Int = 5 * 60 * 1000, // 5 minute window for the user to finish login
) {

    /** Run the end-to-end loopback login. Blocks until tokens are returned or an error is thrown. */
    suspend fun login(): AccessTokens = withContext(Dispatchers.IO) {
        val pkce = PkcePair.generate(random)
        val state = randomState()

        ServerSocket(0, 1, java.net.InetAddress.getByName(OAuthConfig.LOOPBACK_HOST)).use { server ->
            server.soTimeout = socketTimeoutMs
            val port = server.localPort
            val redirectUri = "http://${OAuthConfig.LOOPBACK_HOST}:$port${OAuthConfig.REDIRECT_CALLBACK_PATH}"

            val authUrl = buildAuthorizeUrl(redirectUri, state, pkce.challenge)
            browserLauncher.open(authUrl)

            val code = awaitCallback(server, expectedState = state)
            exchangeCode(code = code, redirectUri = redirectUri, verifier = pkce.verifier)
        }
    }

    /** Build the authorize URL per RFC 6749 §4.1.1 + RFC 7636 §4.3. */
    internal fun buildAuthorizeUrl(redirectUri: String, state: String, codeChallenge: String): String {
        val url = URLBuilder(OAuthConfig.AUTH_URL)
        url.parameters.append("client_id", OAuthConfig.CLIENT_ID)
        url.parameters.append("redirect_uri", redirectUri)
        url.parameters.append("response_type", "code")
        url.parameters.append("scope", OAuthConfig.SCOPES.joinToString(" "))
        url.parameters.append("state", state)
        url.parameters.append("code_challenge", codeChallenge)
        url.parameters.append("code_challenge_method", "S256")
        return url.buildString()
    }

    /** Block on the loopback socket for one inbound HTTP GET, then return the validated `code`. */
    @Throws(OAuthException::class)
    internal fun awaitCallback(server: ServerSocket, expectedState: String): String {
        val socket: Socket = try {
            server.accept()
        } catch (e: IOException) {
            throw OAuthException("Timed out waiting for browser redirect (${socketTimeoutMs}ms)", e)
        }
        socket.use {
            val reader = BufferedReader(InputStreamReader(it.getInputStream(), StandardCharsets.UTF_8))
            val requestLine = reader.readLine()
                ?: throw OAuthException("Empty HTTP request from browser")

            val params = parseQueryFromRequestLine(requestLine)

            // Error path: provider sent back ?error=access_denied&error_description=...
            params["error"]?.let { err ->
                writeResponse(it, errorHtml(err, params["error_description"]))
                throw OAuthException("Authorization server returned error: $err — ${params["error_description"].orEmpty()}")
            }

            val actualState = params["state"]
                ?: run {
                    writeResponse(it, errorHtml("missing_state", "Response lacks the state parameter."))
                    throw OAuthException("Callback is missing 'state' parameter")
                }
            if (actualState != expectedState) {
                writeResponse(it, errorHtml("state_mismatch", "State parameter did not match."))
                throw OAuthException("State mismatch: CSRF protection tripped")
            }

            val code = params["code"]
                ?: run {
                    writeResponse(it, errorHtml("missing_code", "Response lacks the authorization code."))
                    throw OAuthException("Callback is missing 'code' parameter")
                }

            writeResponse(it, successHtml())
            return code
        }
    }

    /** Exchange an authorization code for tokens at TOKEN_URL. Uses PKCE verifier, no client secret. */
    private suspend fun exchangeCode(code: String, redirectUri: String, verifier: String): AccessTokens {
        val response: HttpResponse = httpClient.submitForm(
            url = OAuthConfig.TOKEN_URL,
            formParameters = Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", redirectUri)
                append("client_id", OAuthConfig.CLIENT_ID)
                append("code_verifier", verifier)
            },
        )

        if (response.status != HttpStatusCode.OK) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            throw OAuthException("Token exchange failed: HTTP ${response.status.value} — $body")
        }

        val dto: TokenResponseDto = response.body()
        val nowMs = System.currentTimeMillis()
        val expiresAt = nowMs + (dto.expiresIn ?: 3600L) * 1000L
        return AccessTokens(
            accessToken = dto.accessToken,
            idToken = dto.idToken,
            refreshToken = dto.refreshToken,
            expiresAt = expiresAt,
        )
    }

    private fun randomState(): String {
        val bytes = ByteArray(24).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /** Write a minimal HTTP/1.1 200 response with the given HTML body. */
    private fun writeResponse(socket: Socket, html: String) {
        val writer = OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
        writer.write("HTTP/1.1 200 OK\r\n")
        writer.write("Content-Type: text/html; charset=utf-8\r\n")
        writer.write("Content-Length: ${html.toByteArray(StandardCharsets.UTF_8).size}\r\n")
        writer.write("Connection: close\r\n")
        writer.write("\r\n")
        writer.write(html)
        writer.flush()
    }

    @Serializable
    private data class TokenResponseDto(
        val accessToken: String,
        val idToken: String? = null,
        val refreshToken: String? = null,
        val tokenType: String? = null,
        val expiresIn: Long? = null,
        val scope: String? = null,
    ) {
        companion object {
            // kotlinx.serialization strategy-agnostic — the Ktor Json config (below)
            // uses snake_case, so field names map to `access_token`, `id_token`, etc.
        }
    }

    companion object {
        /** Default Ktor client with snake_case JSON + lenient parsing. */
        fun defaultHttpClient(): HttpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    namingStrategy = kotlinx.serialization.json.JsonNamingStrategy.SnakeCase
                })
            }
        }

        /**
         * Parse the query params from an HTTP request line like:
         *     GET /callback?code=abc&state=xyz HTTP/1.1
         */
        internal fun parseQueryFromRequestLine(requestLine: String): Map<String, String> {
            val parts = requestLine.split(' ')
            if (parts.size < 2 || !parts[0].equals("GET", ignoreCase = true)) return emptyMap()
            val path = parts[1]
            val qIdx = path.indexOf('?')
            if (qIdx < 0) return emptyMap()
            val query = path.substring(qIdx + 1)
            if (query.isEmpty()) return emptyMap()
            return query.split('&').mapNotNull { kv ->
                val eqIdx = kv.indexOf('=')
                if (eqIdx < 0) {
                    val key = URLDecoder.decode(kv, StandardCharsets.UTF_8)
                    key to ""
                } else {
                    val k = URLDecoder.decode(kv.substring(0, eqIdx), StandardCharsets.UTF_8)
                    val v = URLDecoder.decode(kv.substring(eqIdx + 1), StandardCharsets.UTF_8)
                    k to v
                }
            }.toMap()
        }

        internal fun successHtml(): String = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <title>FIVUCSAS — Signed in</title>
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <style>
                body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                       background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%); color: #fff;
                       min-height: 100vh; display: flex; align-items: center; justify-content: center; }
                .card { background: rgba(255,255,255,0.1); backdrop-filter: blur(10px);
                        border-radius: 16px; padding: 48px 64px; text-align: center; max-width: 480px; }
                h1 { margin: 0 0 12px; font-size: 28px; font-weight: 600; }
                p { margin: 0; opacity: 0.9; line-height: 1.6; }
                .check { font-size: 48px; margin-bottom: 16px; }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="check">&#10003;</div>
                <h1>You're signed in to FIVUCSAS</h1>
                <p>You can close this window and return to the desktop app.</p>
              </div>
            </body>
            </html>
        """.trimIndent()

        internal fun errorHtml(errorCode: String, description: String?): String {
            val safeCode = errorCode.take(80).replace("<", "&lt;").replace(">", "&gt;")
            val safeDesc = (description ?: "").take(240).replace("<", "&lt;").replace(">", "&gt;")
            return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>FIVUCSAS — Sign-in failed</title>
                  <style>
                    body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                           background: #1F2937; color: #F9FAFB;
                           min-height: 100vh; display: flex; align-items: center; justify-content: center; }
                    .card { background: #374151; border-radius: 16px; padding: 48px 64px; text-align: center; max-width: 480px; }
                    h1 { margin: 0 0 12px; font-size: 24px; color: #FCA5A5; }
                    p { margin: 8px 0; opacity: 0.9; line-height: 1.6; }
                    code { background: rgba(255,255,255,0.1); padding: 2px 6px; border-radius: 4px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>Sign-in failed</h1>
                    <p>Error: <code>$safeCode</code></p>
                    <p>$safeDesc</p>
                    <p>You can close this window and try again in the desktop app.</p>
                  </div>
                </body>
                </html>
            """.trimIndent()
        }
    }
}

/** Thrown for any OAuth loopback / token-exchange failure. */
class OAuthException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/** Strategy for opening a URL in the system browser. Injectable for tests. */
interface BrowserLauncher {
    fun open(url: String)
}

/**
 * Default browser launcher. Prefers `java.awt.Desktop.browse()` when supported;
 * falls back to `xdg-open` on Linux / `rundll32` on Windows.
 */
class SystemBrowserLauncher : BrowserLauncher {
    override fun open(url: String) {
        val uri = URI.create(url)

        // Preferred path: AWT Desktop (cross-platform, respects user default browser).
        if (Desktop.isDesktopSupported()) {
            val d = Desktop.getDesktop()
            if (d.isSupported(Desktop.Action.BROWSE)) {
                try {
                    d.browse(uri)
                    return
                } catch (_: Exception) {
                    // fall through to platform-specific fallbacks
                }
            }
        }

        val os = (System.getProperty("os.name") ?: "").lowercase(Locale.ROOT)
        val cmd: Array<String> = when {
            os.contains("win") -> arrayOf(
                "rundll32", "url.dll,FileProtocolHandler", url,
            )
            os.contains("nux") || os.contains("nix") || os.contains("bsd") -> arrayOf(
                "xdg-open", url,
            )
            else -> throw OAuthException(
                "No browser launcher available on unsupported OS '$os'; " +
                    "open this URL manually: $url"
            )
        }

        try {
            ProcessBuilder(*cmd).inheritIO().start()
        } catch (e: Exception) {
            throw OAuthException("Failed to launch browser via '${cmd.first()}'. " +
                "Open this URL manually: $url", e)
        }
    }

    @Suppress("unused")
    private fun urlEncode(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)
}
