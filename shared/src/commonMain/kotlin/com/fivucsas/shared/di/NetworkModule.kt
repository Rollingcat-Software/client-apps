package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.data.local.StepUpTokenManager
import com.fivucsas.shared.data.remote.api.AuthBiometricApi
import com.fivucsas.shared.data.remote.api.AuthBiometricApiImpl
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.api.AuthApiImpl
import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.api.BiometricApiImpl
import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.api.IdentityApiImpl
import com.fivucsas.shared.data.remote.api.InviteApi
import com.fivucsas.shared.data.remote.api.InviteApiImpl
import com.fivucsas.shared.data.remote.api.QrLoginApi
import com.fivucsas.shared.data.remote.api.QrLoginApiImpl
import com.fivucsas.shared.data.remote.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
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
                    !url.toString().contains("/auth/register")) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }

                stepUpTokenManager.getToken()?.let { header("X-Step-Up-Token", it) }
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
    single<BiometricApi> { BiometricApiImpl(get(named("biometricClient"))) }
}
