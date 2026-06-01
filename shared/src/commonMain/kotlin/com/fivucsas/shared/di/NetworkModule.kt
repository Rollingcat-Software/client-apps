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
import com.fivucsas.shared.data.remote.dto.toModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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

            // Automatic token refresh on 401 responses
            HttpResponseValidator {
                validateResponse { response: HttpResponse ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        val url = response.call.request.url.toString()
                        // Don't attempt refresh on auth endpoints to avoid infinite loops
                        if (url.contains("/auth/login") || url.contains("/auth/refresh") || url.contains("/auth/logout")) {
                            return@validateResponse
                        }

                        // Snapshot the access token that this request used. If
                        // it differs from the current one by the time we hold
                        // the lock, another coroutine already refreshed and we
                        // must NOT refresh again (that would burn the just-issued
                        // rotated refresh token and log the user out).
                        val staleAccessToken = response.call.request.headers[HttpHeaders.Authorization]

                        refreshMutex.withLock {
                            // A refresh already happened while we waited for the lock.
                            val currentAccessToken = tokenManager.getAccessToken()
                            if (currentAccessToken != null &&
                                staleAccessToken != "Bearer $currentAccessToken") {
                                // Tokens were rotated by a concurrent 401 handler;
                                // reuse them on the transparent retry below.
                                return@withLock
                            }

                            val refreshToken = tokenManager.getRefreshToken() ?: return@withLock

                            try {
                                // Reuse the call's client; the recursion guard above
                                // prevents this /auth/refresh POST from re-entering.
                                val refreshResponse = response.call.client.post(ApiConfig.identityBaseUrl + "/auth/refresh") {
                                    contentType(ContentType.Application.Json)
                                    setBody(mapOf("refreshToken" to refreshToken))
                                }
                                if (refreshResponse.status == HttpStatusCode.OK) {
                                    val authResponse = refreshResponse.body<AuthResponseDto>()
                                    tokenManager.updateTokens(authResponse.toModel())
                                } else {
                                    // Refresh failed — clear tokens to force re-login
                                    tokenManager.clearTokens()
                                }
                            } catch (_: Exception) {
                                tokenManager.clearTokens()
                            }
                        }
                        // TODO: transparently retry the original request with the
                        // refreshed access token. Currently the caller receives the
                        // 401 and must retry; the next request picks up the new token
                        // via defaultRequest. The mutex is the load-bearing fix here.
                    }
                }
            }
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

    // NFC Approval API (push-approval decide endpoint)
    single<com.fivucsas.shared.data.remote.api.NfcApprovalApi> {
        com.fivucsas.shared.data.remote.api.NfcApprovalApiImpl(get(named("identityClient")))
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
}
