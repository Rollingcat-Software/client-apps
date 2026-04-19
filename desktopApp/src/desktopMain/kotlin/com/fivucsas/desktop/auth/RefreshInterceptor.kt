package com.fivucsas.desktop.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * OAuth refresh-on-401 helper, modelled after the web-app's `api/client.ts`.
 *
 * We deliberately avoid `io.ktor:ktor-client-auth` because the shared build
 * file does not include it and the task brief forbids new dependencies.
 *
 * Usage:
 * ```
 * val client = RefreshInterceptor.buildClient()
 * val response = RefreshInterceptor.authenticatedRequest(client, authState) {
 *     url("https://api.fivucsas.com/me")
 *     method = HttpMethod.Get
 * }
 * ```
 *
 * [authenticatedRequest]:
 *   1. Stamps `Authorization: Bearer <access>` from [authState].
 *   2. If the response is 401, acquires [refreshLock], POSTs
 *      `grant_type=refresh_token` to TOKEN_URL, updates [authState], and
 *      retries the original request exactly once with the new access token.
 *   3. If refresh fails, calls [AuthStateManager.logout] and returns the 401.
 */
object RefreshInterceptor {

    private val refreshLock = Mutex()

    /** Build a vanilla Ktor client with snake_case JSON decoding. Reusable across calls. */
    fun buildClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
        expectSuccess = false
    }

    /**
     * Fire an authenticated request through [client], transparently refreshing
     * the access token and retrying exactly once if the server returns 401.
     */
    suspend fun authenticatedRequest(
        client: HttpClient,
        authState: AuthStateManager,
        tokenUrl: String = OAuthConfig.TOKEN_URL,
        clientId: String = OAuthConfig.CLIENT_ID,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val first = client.request {
            block()
            authState.tokens.value?.accessToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
        if (first.status != HttpStatusCode.Unauthorized) return first

        val refreshed = refreshLock.withLock {
            val current = authState.tokens.value
            val refreshToken = current?.refreshToken
                ?: return@withLock null.also { authState.logout() }

            runCatching { performRefresh(refreshToken, tokenUrl, clientId) }
                .onSuccess { authState.onTokensRefreshed(it) }
                .onFailure { authState.logout() }
                .getOrNull()
        } ?: return first

        // Retry once with the new access token.
        return client.request {
            block()
            header(HttpHeaders.Authorization, "Bearer ${refreshed.accessToken}")
        }
    }

    /** POST `grant_type=refresh_token` to [tokenUrl] and parse the returned token bundle. */
    internal suspend fun performRefresh(
        refreshToken: String,
        tokenUrl: String,
        clientId: String,
    ): AccessTokens {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    namingStrategy = JsonNamingStrategy.SnakeCase
                })
            }
        }.use { client ->
            val response: HttpResponse = client.submitForm(
                url = tokenUrl,
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                    append("client_id", clientId)
                },
            )
            if (response.status != HttpStatusCode.OK) {
                val body = runCatching { response.bodyAsText() }.getOrDefault("")
                throw OAuthException("Refresh failed: HTTP ${response.status.value} — $body")
            }
            val dto: RefreshDto = response.body()
            val expiresAt = System.currentTimeMillis() + (dto.expiresIn ?: 3600L) * 1000L
            return AccessTokens(
                accessToken = dto.accessToken,
                idToken = dto.idToken,
                refreshToken = dto.refreshToken ?: refreshToken, // reuse if rotation disabled
                expiresAt = expiresAt,
            )
        }
    }

    @Serializable
    internal data class RefreshDto(
        val accessToken: String,
        val idToken: String? = null,
        val refreshToken: String? = null,
        val tokenType: String? = null,
        val expiresIn: Long? = null,
        val scope: String? = null,
    )
}
