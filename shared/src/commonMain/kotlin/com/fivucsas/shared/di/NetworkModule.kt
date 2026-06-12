package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.data.local.StepUpTokenManager
import com.fivucsas.shared.data.remote.api.AuthBiometricApi
import com.fivucsas.shared.data.remote.api.AuthBiometricApiImpl
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.api.AuthApiImpl
import com.fivucsas.shared.data.remote.api.AuthFlowApi
import com.fivucsas.shared.data.remote.api.AuthFlowApiImpl
import com.fivucsas.shared.data.remote.api.AuthSessionApi
import com.fivucsas.shared.data.remote.api.AuthSessionApiImpl
import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.api.BiometricApiImpl
import com.fivucsas.shared.data.remote.api.DataExportApi
import com.fivucsas.shared.data.remote.api.DataExportApiImpl
import com.fivucsas.shared.data.remote.api.DeviceApi
import com.fivucsas.shared.data.remote.api.DeviceApiImpl
import com.fivucsas.shared.data.remote.api.EnrollmentApi
import com.fivucsas.shared.data.remote.api.EnrollmentApiImpl
import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.api.IdentityApiImpl
import com.fivucsas.shared.data.remote.api.InviteApi
import com.fivucsas.shared.data.remote.api.InviteApiImpl
import com.fivucsas.shared.data.remote.api.QrLoginApi
import com.fivucsas.shared.data.remote.api.QrLoginApiImpl
import com.fivucsas.shared.data.remote.api.RootAdminApi
import com.fivucsas.shared.data.remote.api.RootAdminApiImpl
import com.fivucsas.shared.data.remote.api.SessionApi
import com.fivucsas.shared.data.remote.api.SessionApiImpl
import com.fivucsas.shared.data.remote.api.TenantSettingsApi
import com.fivucsas.shared.data.remote.api.TenantSettingsApiImpl
import com.fivucsas.shared.data.remote.config.ApiConfig
import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.OAuthTokenResponseDto
import com.fivucsas.shared.data.remote.dto.toModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Serializes concurrent 401-triggered token refreshes.
 *
 * Without this guard, N parallel requests that all receive a 401 would each
 * fire `POST /auth/refresh`. With refresh-token rotation enabled server-side,
 * only the first rotation is accepted; the rest present an already-rotated
 * (now-invalid) refresh token, get rejected, and clear the tokens — logging
 * the user out. The mutex ensures only one refresh runs at a time; callers
 * that arrive while a refresh is in flight await it and then reuse the freshly
 * stored access token on their transparent retry.
 */
private val refreshMutex = Mutex()

/**
 * OAuth public client_id for the native apps. Must match the redirect-allowlisted
 * client registered server-side and the value AppAuth uses on Android
 * (`HostedAuthManager.CLIENT_ID`). Sent on the `grant_type=refresh_token` call.
 */
private const val OAUTH_MOBILE_CLIENT_ID = "fivucsas-mobile"

/**
 * Auth endpoints that must NOT trigger a refresh-on-401 (avoids infinite loops);
 * includes the OAuth token endpoint so a 401 from the refresh_token grant itself
 * can't recurse into another refresh.
 */
private fun isAuthEndpoint(url: String): Boolean =
    url.contains("/auth/login") || url.contains("/auth/refresh") ||
        url.contains("/auth/logout") || url.contains("/oauth2/token")

/**
 * A definitive auth failure is one the server reports for the refresh-token grant
 * itself: HTTP 400/401 from `/oauth2/token` (or `/auth/refresh`) means the refresh
 * token is truly dead (expired, revoked, reuse-detection family-revoke → the OAuth
 * `invalid_grant` family). Only these justify wiping the session and routing to
 * login. EVERYTHING else — a socket timeout, a dropped keep-alive, a brief offline,
 * a Traefik HTTP/2 stale-connection RST, a 5xx/502/503, a 429 — is TRANSIENT: it
 * says nothing about whether the refresh token is still valid, so we must NOT clear
 * the persisted session over it; only the in-flight request fails and the next
 * attempt re-tries with the surviving tokens.
 */
internal fun isDefinitiveAuthFailure(status: HttpStatusCode): Boolean =
    status == HttpStatusCode.BadRequest || status == HttpStatusCode.Unauthorized

/**
 * Perform a single silent token refresh under [refreshMutex].
 *
 * Returns true if, after this call, a usable access token exists (either because
 * we refreshed it, or because a concurrent 401 handler already did). Returns false
 * if the refresh did not produce a usable token.
 *
 * Token-clearing policy (the F8 fix): the ENTIRE persisted session is wiped ONLY on
 * a DEFINITIVE auth failure ([isDefinitiveAuthFailure]) — the server explicitly
 * rejecting the refresh token. On a TRANSIENT failure (an IOException/timeout/RST
 * caught below, or a non-auth non-200 such as 5xx/429) we return false WITHOUT
 * clearing tokens, so the session survives for the next attempt. The previous
 * blanket `catch (_) { clearTokens() }` + clear-on-any-non-200 wiped the whole
 * session on any flaky network event, which surfaced as the intermittent
 * "sometimes asks for login, sometimes doesn't" forced re-login.
 *
 * [staleAccessToken] is the `Authorization` header the failed request carried; if
 * the stored access token already differs we skip the refresh (a concurrent
 * handler rotated the tokens) and report success so the caller retries with the
 * fresh token. This is the logic that USED to live in the HttpResponseValidator;
 * it is now reusable from the HttpSend interceptor so the original request can be
 * transparently re-fired (closing the old "caller receives the 401" TODO).
 */
internal suspend fun refreshAccessToken(
    client: HttpClient,
    tokenManager: TokenManager,
    staleAccessToken: String?,
): Boolean = refreshMutex.withLock {
    // A refresh already happened while we waited for the lock.
    val currentAccessToken = tokenManager.getAccessToken()
    if (currentAccessToken != null && staleAccessToken != "Bearer $currentAccessToken") {
        // Tokens were rotated by a concurrent 401 handler; reuse them.
        return@withLock true
    }

    val refreshToken = tokenManager.getRefreshToken() ?: return@withLock false

    try {
        if (tokenManager.isOAuthSession()) {
            // Hosted-first OAuth session — renew via the OAuth refresh-token
            // grant (RFC 6749 §6) at the OAuth token endpoint, form-url-encoded.
            val refreshResponse = client.submitForm(
                url = ApiConfig.identityBaseUrl + "/oauth2/token",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                    append("client_id", OAUTH_MOBILE_CLIENT_ID)
                },
            )
            if (refreshResponse.status == HttpStatusCode.OK) {
                val oauth = refreshResponse.body<OAuthTokenResponseDto>().toModel()
                // The OAuth refresh response carries no profile fields — re-apply
                // the cached identity so role/tenant context survives the rotation.
                // When the server omits `refresh_token` (rotation disabled), the DTO
                // maps it to "" — KEEP the existing refresh token instead of wiping
                // it to blank, or the very next refresh would have no token and force
                // a re-login.
                tokenManager.updateTokens(
                    oauth.copy(
                        refreshToken = oauth.refreshToken.ifBlank { refreshToken },
                        role = tokenManager.getRole() ?: oauth.role,
                        userName = tokenManager.getUserName() ?: "",
                        userEmail = tokenManager.getUserEmail() ?: "",
                        userId = tokenManager.getUserId() ?: "",
                        tenantId = tokenManager.getTenantId() ?: "",
                    ),
                )
                true
            } else {
                // Definitive (400/401 invalid_grant) → token is dead, wipe + relogin.
                // Transient (5xx/429/…) → keep the session, just fail this request.
                if (isDefinitiveAuthFailure(refreshResponse.status)) tokenManager.clearTokens()
                false
            }
        } else {
            // Legacy password/MFA session — the original /auth/refresh JSON path.
            val refreshResponse = client.post(ApiConfig.identityBaseUrl + "/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to refreshToken))
            }
            if (refreshResponse.status == HttpStatusCode.OK) {
                val refreshed = refreshResponse.body<AuthResponseDto>().toModel()
                // Same blank-refresh-token guard as the OAuth branch: if the server
                // didn't rotate (omitted refreshToken → ""), preserve the current one.
                tokenManager.updateTokens(
                    refreshed.copy(refreshToken = refreshed.refreshToken.ifBlank { refreshToken }),
                )
                true
            } else {
                // Same split as the OAuth branch: only a definitive auth rejection
                // clears the session; a transient server/network error preserves it.
                if (isDefinitiveAuthFailure(refreshResponse.status)) tokenManager.clearTokens()
                false
            }
        }
    } catch (_: Exception) {
        // Transport/IO failure (timeout, dropped keep-alive, Traefik RST, offline).
        // This says NOTHING about refresh-token validity — keep the session intact
        // and let the next request retry; do NOT clear tokens here.
        false
    }
}

/**
 * Install the shared transparent refresh-on-401 + single-retry interceptor on
 * [httpClient].
 *
 * Both the identity and the biometric clients use THIS one function so every 401
 * is funnelled through the SAME [refreshAccessToken] (and therefore the SAME
 * module-level [refreshMutex]). That cross-client serialization is the F8 fix for
 * refresh-token reuse-detection family-revoke: previously the biometric client had
 * NO refresh interceptor and was outside the mutex, so a biometric 401 racing an
 * identity refresh (or presenting a token rotated out from under it) could
 * re-present an already-rotated refresh token → the server revokes the whole
 * rotation family → all sessions logged out non-deterministically. Routing both
 * through one mutexed refresh removes that race.
 *
 * The refresh itself always targets the IDENTITY token endpoint (absolute URLs in
 * [refreshAccessToken]), so it is correct to drive it from the biometric client
 * too — the absolute URL overrides that client's biometric `defaultRequest` base.
 */
private fun installRefreshOn401(httpClient: HttpClient, tokenManager: TokenManager) {
    // Transparent refresh-on-401 + RETRY of the original request.
    //
    // We use the HttpSend plugin (part of ktor-client-core — no extra
    // dependency; the project deliberately avoids io.ktor:ktor-client-auth,
    // see desktopApp RefreshInterceptor) because, unlike HttpResponseValidator,
    // its interceptor CAN re-execute the request. This closes the old TODO
    // where the caller received the raw 401 and the session appeared to
    // die after the ~15-min access-token lifetime: now the access token is
    // refreshed under a mutex and the SAME request is re-fired once with the
    // fresh bearer (defaultRequest re-reads the stored token), so the call
    // succeeds transparently instead of surfacing a logout.
    httpClient.plugin(HttpSend).intercept { request: HttpRequestBuilder ->
        val originalCall = execute(request)
        val url = originalCall.request.url.toString()
        if (originalCall.response.status != HttpStatusCode.Unauthorized || isAuthEndpoint(url)) {
            return@intercept originalCall
        }

        // Snapshot the access token this request carried so the refresh
        // helper can detect a concurrent rotation and avoid double-refresh.
        val staleAccessToken = originalCall.request.headers[HttpHeaders.Authorization]
        val refreshed = refreshAccessToken(httpClient, tokenManager, staleAccessToken)
        if (!refreshed) {
            return@intercept originalCall
        }

        // Re-fire the original request once. defaultRequest stamps the
        // freshly-stored access token onto the retry.
        execute(request)
    }
}

/**
 * Network module - Provides HTTP clients and API clients
 *
 * Provides two separate HTTP clients:
 * - identityClient: For Identity Core API (auth, users, RBAC)
 * - biometricClient: For Biometric Processor API (face detection, verification)
 */
val networkModule = module {
    // Token Manager (singleton) - must be created before HttpClient
    single { TokenManager(get<TokenStorage>()) }
    // Pass the platform ISecureStorage so step-up tokens persist across app
    // backgrounding (the manager keeps an in-memory cache with secure-storage
    // fallback). The ISecureStorage single is registered in each platform module.
    single { StepUpTokenManager(secureStorage = get()) }

    // Shared JSON configuration
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            // Coerce an explicit null on a field that has a default to its
            // default (e.g. `content: List = emptyList()`), instead of throwing.
            coerceInputValues = true
        }
    }

    // Identity Core API HTTP Client (port 8080)
    single(named("identityClient")) {
        val tokenManager = get<TokenManager>()
        val stepUpTokenManager = get<StepUpTokenManager>()
        val json = get<Json>()

        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = if (ApiConfig.isLoggingEnabled) LogLevel.INFO else LogLevel.NONE
            }

            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
                socketTimeoutMillis = ApiConfig.SOCKET_TIMEOUT_MS
            }

            // Resilience against stale-connection aborts (e.g. OkHttp reusing a
            // half-closed HTTP/2 keep-alive connection that Traefik dropped while
            // the app was idle → the POST body is RST mid-send → the server logs
            // "Malformed request body: I/O error while reading input message").
            // This is bandwidth-independent and was misdiagnosed as a slow-uplink
            // truncation. Retry ONLY on transport/IO exceptions (NOT on 4xx/5xx),
            // so a consumed MFA code is never resubmitted; the body is a serialized
            // object, so it is fully replayable on retry.
            install(HttpRequestRetry) {
                retryOnExceptionIf(maxRetries = 2) { _, cause ->
                    val name = cause::class.simpleName ?: ""
                    name.contains("IOException") ||
                        name.contains("SocketTimeout") ||
                        name.contains("ConnectTimeout") ||
                        name == "ClosedReceiveChannelException"
                }
                exponentialDelay()
            }

            defaultRequest {
                url(ApiConfig.identityBaseUrl + "/")

                // Add JWT token to all requests (except auth endpoints)
                val accessToken = tokenManager.getAccessToken()

                if (accessToken != null &&
                    !url.toString().contains("/auth/login") &&
                    !url.toString().contains("/auth/register") &&
                    !url.toString().contains("/auth/mfa/")) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }

                // Include tenant context for multi-tenant API calls
                tokenManager.getTenantId()?.let { header("X-Tenant-Id", it) }

                stepUpTokenManager.getToken()?.let { header("X-Step-Up-Token", it) }
            }

        }.also { httpClient ->
            installRefreshOn401(httpClient, tokenManager)
        }
    }

    // Biometric Processor API HTTP Client (port 8001)
    single(named("biometricClient")) {
        val tokenManager = get<TokenManager>()
        val json = get<Json>()

        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = if (ApiConfig.isLoggingEnabled) LogLevel.INFO else LogLevel.NONE
            }

            install(HttpTimeout) {
                // Biometric operations may take longer
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS * 2
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
                socketTimeoutMillis = ApiConfig.SOCKET_TIMEOUT_MS
            }

            defaultRequest {
                url(ApiConfig.biometricBaseUrl + "/")

                // Add JWT token for authenticated biometric operations
                val accessToken = tokenManager.getAccessToken()
                if (accessToken != null) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }
        }.also { httpClient ->
            // F8: the biometric client previously had NO refresh interceptor and
            // sat outside [refreshMutex], so a biometric 401 could refresh out of
            // band / re-present an already-rotated refresh token → reuse-detection
            // family-revoke → all sessions logged out. Use the SAME shared,
            // mutex-serialized refresh as the identity client. The refresh targets
            // the identity token endpoint via absolute URLs, so it is correct here.
            installRefreshOn401(httpClient, tokenManager)
        }
    }

    // API Implementations with specific HTTP clients
    single<AuthApi> { AuthApiImpl(get(named("identityClient"))) }
    single<AuthBiometricApi> { AuthBiometricApiImpl(get(named("identityClient"))) }
    single<IdentityApi> { IdentityApiImpl(get(named("identityClient"))) }
    single<InviteApi> { InviteApiImpl(get(named("identityClient"))) }
    single<QrLoginApi> { QrLoginApiImpl(get(named("identityClient"))) }
    single<TenantSettingsApi> { TenantSettingsApiImpl(get(named("identityClient"))) }
    single<BiometricApi> { BiometricApiImpl(get(named("biometricClient"))) }
    single<RootAdminApi> { RootAdminApiImpl(get(named("identityClient"))) }
    single<AuthFlowApi> { AuthFlowApiImpl(get(named("identityClient"))) }
    single<SessionApi> { SessionApiImpl(get(named("identityClient"))) }
    single<AuthSessionApi> { AuthSessionApiImpl(get(named("identityClient"))) }
    single<DeviceApi> { DeviceApiImpl(get(named("identityClient"))) }
    single<EnrollmentApi> { EnrollmentApiImpl(get(named("identityClient"))) }
    single<DataExportApi> { DataExportApiImpl(get(named("identityClient"))) }

    // WebAuthn API for FIDO2 credential registration/verification
    single<com.fivucsas.shared.data.remote.api.WebAuthnApi> {
        com.fivucsas.shared.data.remote.api.WebAuthnApiImpl(get(named("identityClient")))
    }

    // New API clients for voice, OTP, TOTP, dashboard
    single<com.fivucsas.shared.data.remote.api.VoiceApi> {
        com.fivucsas.shared.data.remote.api.VoiceApiImpl(get(named("identityClient")))
    }
    single<com.fivucsas.shared.data.remote.api.OtpApi> {
        com.fivucsas.shared.data.remote.api.OtpApiImpl(get(named("identityClient")))
    }
    single<com.fivucsas.shared.data.remote.api.TotpApi> {
        com.fivucsas.shared.data.remote.api.TotpApiImpl(get(named("identityClient")))
    }
    single<com.fivucsas.shared.data.remote.api.DashboardApi> {
        com.fivucsas.shared.data.remote.api.DashboardApiImpl(get(named("identityClient")))
    }

    // Verification Pipeline API
    single<com.fivucsas.shared.data.remote.api.VerificationApi> {
        com.fivucsas.shared.data.remote.api.VerificationApiImpl(get(named("identityClient")))
    }

    // OAuth2 Client API (Developer Portal)
    single<com.fivucsas.shared.data.remote.api.OAuth2ClientApi> {
        com.fivucsas.shared.data.remote.api.OAuth2ClientApiImpl(get(named("identityClient")))
    }

    // Approve-login API (number-matching approver side: list pending + decide)
    single<com.fivucsas.shared.data.remote.api.ApproveLoginApi> {
        com.fivucsas.shared.data.remote.api.ApproveLoginApiImpl(get(named("identityClient")))
    }

    // NFC document enrollment / verification API
    single<com.fivucsas.shared.data.remote.api.NfcEnrollmentApi> {
        com.fivucsas.shared.data.remote.api.NfcEnrollmentApiImpl(get(named("identityClient")))
    }

    // Account-linking + workspace-switcher API
    single<com.fivucsas.shared.data.remote.api.AccountLinkingApi> {
        com.fivucsas.shared.data.remote.api.AccountLinkingApiImpl(get(named("identityClient")))
    }

    // NFC passive-authentication API (verify-authenticity)
    single<com.fivucsas.shared.data.remote.api.NfcAuthenticityApi> {
        com.fivucsas.shared.data.remote.api.NfcAuthenticityApiImpl(get(named("identityClient")))
    }

    // Audit-log / "my activity" API — #9: was NEVER registered, so ActivityHistoryViewModel
    // and AuditLogDashboardViewModel could not be created → Koin NoDefinitionFoundException
    // (FATAL crash on opening Activity History / the audit-log dashboard). Added in #83 but
    // its DI binding was omitted; this restores it.
    single<com.fivucsas.shared.data.remote.api.AuditLogApi> {
        com.fivucsas.shared.data.remote.api.AuditLogApiImpl(get(named("identityClient")))
    }
}
