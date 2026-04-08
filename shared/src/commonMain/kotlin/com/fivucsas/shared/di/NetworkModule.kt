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
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

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
    single { StepUpTokenManager() }

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

                        val refreshToken = tokenManager.getRefreshToken() ?: return@validateResponse

                        try {
                            // Create a temporary client to avoid interceptor recursion
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
}
