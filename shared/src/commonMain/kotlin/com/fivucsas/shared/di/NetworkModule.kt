package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.api.AuthApiImpl
import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.api.BiometricApiImpl
import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.api.IdentityApiImpl
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
import org.koin.dsl.module

/**
 * Network module - Provides HTTP client and API clients
 */
val networkModule = module {
    // HTTP Client (singleton)
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
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
                url(ApiConfig.baseUrl + "/")

                // Add JWT token to all requests (except auth endpoints)
                val tokenManager = get<TokenManager>()
                val accessToken = tokenManager.getAccessToken()

                if (accessToken != null &&
                    !url.toString().contains("/auth/login") &&
                    !url.toString().contains("/auth/register")) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }
        }
    }

    // API Implementations (singletons)
    singleOf(::AuthApiImpl) { bind<AuthApi>() }
    singleOf(::BiometricApiImpl) { bind<BiometricApi>() }
    singleOf(::IdentityApiImpl) { bind<IdentityApi>() }
}
