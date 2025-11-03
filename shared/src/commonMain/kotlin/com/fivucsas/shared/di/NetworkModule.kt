package com.fivucsas.shared.di

import com.fivucsas.shared.data.remote.api.*
import com.fivucsas.shared.data.remote.config.ApiConfig
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
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
            }
        }
    }
    
    // API Implementations (singletons)
    singleOf(::AuthApiImpl) { bind<AuthApi>() }
    singleOf(::BiometricApiImpl) { bind<BiometricApi>() }
    singleOf(::IdentityApiImpl) { bind<IdentityApi>() }
}
