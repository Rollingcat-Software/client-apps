# Day 6: API Integration 🌐

**Date:** November 3, 2025  
**Status:** In Progress  
**Estimated Time:** 60-90 minutes  
**Difficulty:** MEDIUM  
**Impact:** HIGH - Real data flow!

---

## 📊 What We'll Achieve

### Before Day 6 ⚠️
```kotlin
// Mock data everywhere
class UserRepositoryImpl : UserRepository {
    override suspend fun getUsers(): List<User> {
        // Hardcoded mock data
        return listOf(
            User(id = "1", name = "John Doe", ...),
            User(id = "2", name = "Jane Smith", ...)
        )
    }
}
```

### After Day 6 ✅
```kotlin
// Real API integration
class UserRepositoryImpl(
    private val userApi: UserApi
) : UserRepository {
    override suspend fun getUsers(): List<User> {
        return try {
            val response = userApi.getUsers()
            response.map { it.toDomain() }
        } catch (e: Exception) {
            throw NetworkException(e)
        }
    }
}
```

---

## 🎯 Goals

1. ✅ Implement API client classes
2. ✅ Add environment configuration
3. ✅ Implement network error handling
4. ✅ Add network state management
5. ✅ Update repositories to use real APIs
6. ✅ Keep mock data as fallback
7. ✅ Test API integration

---

## 📦 Step 1: Create API Implementations (20 minutes)

### 1.1 Create AuthApiImpl

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/remote/api/AuthApiImpl.kt`

```kotlin
package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Auth API implementation
 * Handles authentication endpoints
 */
class AuthApiImpl(
    private val client: HttpClient
) : AuthApi {
    
    companion object {
        private const val BASE_PATH = "auth"
    }
    
    override suspend fun login(request: LoginRequestDto): AuthResponseDto {
        return client.post("$BASE_PATH/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    override suspend fun logout(token: String) {
        client.post("$BASE_PATH/logout") {
            bearerAuth(token)
        }
    }
    
    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        return client.post("$BASE_PATH/refresh") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refreshToken" to refreshToken))
        }.body()
    }
}
```

### 1.2 Create BiometricApiImpl

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/remote/api/BiometricApiImpl.kt`

```kotlin
package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricDataDto
import com.fivucsas.shared.data.remote.dto.EnrollmentResponseDto
import com.fivucsas.shared.data.remote.dto.LivenessCheckRequestDto
import com.fivucsas.shared.data.remote.dto.LivenessCheckResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationRequestDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Biometric API implementation
 * Handles face enrollment, verification, and liveness detection
 */
class BiometricApiImpl(
    private val client: HttpClient
) : BiometricApi {
    
    companion object {
        private const val BASE_PATH = "biometric"
    }
    
    override suspend fun enrollFace(
        userId: String,
        biometricData: BiometricDataDto
    ): EnrollmentResponseDto {
        return client.post("$BASE_PATH/enroll") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "userId" to userId,
                "biometricData" to biometricData
            ))
        }.body()
    }
    
    override suspend fun verifyFace(
        request: VerificationRequestDto
    ): VerificationResponseDto {
        return client.post("$BASE_PATH/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    override suspend fun checkLiveness(
        request: LivenessCheckRequestDto
    ): LivenessCheckResponseDto {
        return client.post("$BASE_PATH/liveness") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    override suspend fun deleteBiometricData(userId: String) {
        client.delete("$BASE_PATH/$userId")
    }
}
```

### 1.3 Create IdentityApiImpl

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/remote/api/IdentityApiImpl.kt`

```kotlin
package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.StatisticsDto
import com.fivucsas.shared.data.remote.dto.UserDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Identity API implementation
 * Handles user management and statistics
 */
class IdentityApiImpl(
    private val client: HttpClient
) : IdentityApi {
    
    companion object {
        private const val BASE_PATH = "users"
        private const val STATS_PATH = "statistics"
    }
    
    override suspend fun getUsers(): List<UserDto> {
        return client.get(BASE_PATH).body()
    }
    
    override suspend fun getUserById(id: String): UserDto {
        return client.get("$BASE_PATH/$id").body()
    }
    
    override suspend fun createUser(user: UserDto): UserDto {
        return client.post(BASE_PATH) {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }
    
    override suspend fun updateUser(id: String, user: UserDto): UserDto {
        return client.put("$BASE_PATH/$id") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }
    
    override suspend fun deleteUser(id: String) {
        client.delete("$BASE_PATH/$id")
    }
    
    override suspend fun searchUsers(query: String): List<UserDto> {
        return client.get("$BASE_PATH/search") {
            parameter("q", query)
        }.body()
    }
    
    override suspend fun getStatistics(): StatisticsDto {
        return client.get(STATS_PATH).body()
    }
}
```

---

## 🔧 Step 2: Environment Configuration (10 minutes)

### 2.1 Create ApiConfig

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/remote/config/ApiConfig.kt`

```kotlin
package com.fivucsas.shared.data.remote.config

/**
 * API Configuration
 * Centralized configuration for API endpoints
 */
object ApiConfig {
    
    // Environment
    enum class Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }
    
    // Current environment (can be changed at runtime)
    var currentEnvironment: Environment = Environment.DEVELOPMENT
    
    // Base URLs per environment
    private const val DEV_BASE_URL = "http://localhost:8080/api/v1"
    private const val STAGING_BASE_URL = "https://staging.fivucsas.com/api/v1"
    private const val PROD_BASE_URL = "https://api.fivucsas.com/api/v1"
    
    // Get base URL for current environment
    val baseUrl: String
        get() = when (currentEnvironment) {
            Environment.DEVELOPMENT -> DEV_BASE_URL
            Environment.STAGING -> STAGING_BASE_URL
            Environment.PRODUCTION -> PROD_BASE_URL
        }
    
    // Timeout configuration
    const val CONNECT_TIMEOUT_MS = 30_000L
    const val REQUEST_TIMEOUT_MS = 60_000L
    const val SOCKET_TIMEOUT_MS = 30_000L
    
    // Retry configuration
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
    
    // Logging
    val isLoggingEnabled: Boolean
        get() = currentEnvironment != Environment.PRODUCTION
}
```

### 2.2 Create NetworkResult sealed class

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/domain/model/NetworkResult.kt`

```kotlin
package com.fivucsas.shared.domain.model

/**
 * Network result wrapper
 * Represents the result of a network operation
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

/**
 * Network exception types
 */
sealed class NetworkException(message: String) : Exception(message) {
    class NetworkError(message: String = "Network error occurred") : NetworkException(message)
    class ServerError(val code: Int, message: String = "Server error: $code") : NetworkException(message)
    class Unauthorized(message: String = "Unauthorized") : NetworkException(message)
    class NotFound(message: String = "Resource not found") : NetworkException(message)
    class Timeout(message: String = "Request timeout") : NetworkException(message)
    class Unknown(message: String = "Unknown error") : NetworkException(message)
}

/**
 * Extension functions for NetworkResult
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (NetworkException) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(exception)
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action()
    return this
}
```

---

## 🏗️ Step 3: Update DI Modules (10 minutes)

### 3.1 Update NetworkModule

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/di/NetworkModule.kt`

```kotlin
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
```

---

## 🔄 Step 4: Update Repositories (15 minutes)

### 4.1 Update UserRepositoryImpl

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/repository/UserRepositoryImpl.kt`

Add API integration while keeping mock data as fallback:

```kotlin
package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.domain.model.NetworkException
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

class UserRepositoryImpl(
    private val identityApi: IdentityApi
) : UserRepository {
    
    // Feature flag for API integration
    private val useApi = false // Set to true when backend is ready
    
    override suspend fun getUsers(): List<User> {
        return if (useApi) {
            try {
                identityApi.getUsers().map { it.toDomain() }
            } catch (e: Exception) {
                throw NetworkException.NetworkError(e.message ?: "Failed to fetch users")
            }
        } else {
            // Mock data fallback
            getMockUsers()
        }
    }
    
    override suspend fun getUserById(id: String): User? {
        return if (useApi) {
            try {
                identityApi.getUserById(id).toDomain()
            } catch (e: Exception) {
                null
            }
        } else {
            getMockUsers().find { it.id == id }
        }
    }
    
    override suspend fun searchUsers(query: String): List<User> {
        return if (useApi) {
            try {
                identityApi.searchUsers(query).map { it.toDomain() }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            getMockUsers().filter { 
                it.firstName.contains(query, ignoreCase = true) ||
                it.lastName.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true)
            }
        }
    }
    
    // Keep existing mock data methods
    private fun getMockUsers(): List<User> {
        // ... existing mock implementation
    }
}
```

---

## 🧪 Step 5: Testing (15 minutes)

### 5.1 Test API Configuration

Create a simple test to verify API setup:

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/remote/test/ApiTest.kt`

```kotlin
package com.fivucsas.shared.data.remote.test

import com.fivucsas.shared.data.remote.config.ApiConfig

/**
 * API configuration test utilities
 */
object ApiTest {
    
    fun testEnvironmentConfiguration() {
        println("🔧 Testing API Configuration...")
        
        // Test development environment
        ApiConfig.currentEnvironment = ApiConfig.Environment.DEVELOPMENT
        println("✅ DEV URL: ${ApiConfig.baseUrl}")
        
        // Test staging environment
        ApiConfig.currentEnvironment = ApiConfig.Environment.STAGING
        println("✅ STAGING URL: ${ApiConfig.baseUrl}")
        
        // Test production environment
        ApiConfig.currentEnvironment = ApiConfig.Environment.PRODUCTION
        println("✅ PROD URL: ${ApiConfig.baseUrl}")
        
        // Reset to development
        ApiConfig.currentEnvironment = ApiConfig.Environment.DEVELOPMENT
        
        println("✅ API Configuration Tests Passed!")
    }
    
    fun printCurrentConfig() {
        println("""
            📡 Current API Configuration:
            Environment: ${ApiConfig.currentEnvironment}
            Base URL: ${ApiConfig.baseUrl}
            Connect Timeout: ${ApiConfig.CONNECT_TIMEOUT_MS}ms
            Request Timeout: ${ApiConfig.REQUEST_TIMEOUT_MS}ms
            Logging: ${if (ApiConfig.isLoggingEnabled) "Enabled" else "Disabled"}
        """.trimIndent())
    }
}
```

---

## 📊 Progress Tracking

```
Day 1: Shared Module Structure    ✅ (10%)
Day 2: Data Layer                  ✅ (20%)
Day 3: Use Cases & Validation      ✅ (30%)
Day 4: ViewModels to Shared        ✅ (50%)
Day 5: Dependency Injection        ✅ (60%)
Day 6: API Integration             ⏳ (70%) ⭐ IN PROGRESS!
----------------------------------------------
Day 7: Testing Infrastructure      ⬜ (80%)
Day 8: Error Handling              ⬜ (90%)
Day 9: Performance & Polish        ⬜ (95%)
Day 10: Final Integration          ⬜ (100%)
```

---

## 🎯 Success Criteria

- [ ] API implementation classes created
- [ ] Environment configuration setup
- [ ] NetworkResult wrapper created
- [ ] DI modules updated
- [ ] Repositories support both API and mock data
- [ ] Feature flags for gradual rollout
- [ ] Shared module builds successfully
- [ ] Desktop app runs without errors
- [ ] Configuration tests pass

---

## 🐛 Troubleshooting

### Issue 1: "Serialization not found"
**Solution:** Ensure DTOs have `@Serializable` annotation

### Issue 2: "Connection refused"
**Solution:** Backend not running - use mock data fallback

### Issue 3: "Timeout errors"
**Solution:** Increase timeout values in ApiConfig

---

**Ready to implement? Let's go! 🚀**
