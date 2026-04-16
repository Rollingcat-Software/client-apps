# Client Apps - Implementation Plan for 100% Completion

**Version**: 2.0
**Date**: January 2026
**Target**: 100% Production-Ready
**Current Status**: ~60% Complete
**Estimated Effort**: 21 days

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [API Contracts](#api-contracts)
4. [Implementation Phases](#implementation-phases)
   - [Phase 1: Package Consolidation & Code Cleanup](#phase-1-package-consolidation--code-cleanup)
   - [Phase 2: Backend Integration](#phase-2-backend-integration)
   - [Phase 3: Camera Integration](#phase-3-camera-integration)
   - [Phase 4: Biometric Flow Implementation](#phase-4-biometric-flow-implementation)
   - [Phase 5: Platform-Specific Features](#phase-5-platform-specific-features)
   - [Phase 6: Test Coverage Expansion](#phase-6-test-coverage-expansion)
   - [Phase 7: Production Readiness](#phase-7-production-readiness)
5. [Integration Points](#integration-points)
6. [Testing Strategy](#testing-strategy)
7. [Deployment Checklist](#deployment-checklist)

---

## Executive Summary

This document provides a comprehensive implementation plan for completing the FIVUCSAS Client Apps to 100% production readiness. The client-apps module is a Kotlin Multiplatform project supporting Android, iOS, and Desktop platforms.

### Architecture

```
client-apps/
├── shared/                      # Shared Kotlin Multiplatform code
│   └── src/
│       ├── commonMain/          # Cross-platform code
│       │   └── kotlin/com/fivucsas/shared/
│       │       ├── config/      # Configuration constants
│       │       ├── data/        # Data layer (repositories, API)
│       │       ├── di/          # Koin dependency injection
│       │       ├── domain/      # Domain models and use cases
│       │       ├── platform/    # Platform abstractions
│       │       └── presentation/# ViewModels and UI state
│       ├── androidMain/         # Android-specific implementations
│       ├── iosMain/             # iOS-specific implementations
│       └── desktopMain/         # Desktop-specific implementations
├── androidApp/                  # Android application
├── desktopApp/                  # Desktop application (Compose Desktop)
└── iosApp/                      # iOS application (SwiftUI wrapper)
```

### Key Dependencies

- **Kotlin Multiplatform** - Cross-platform development
- **Compose Multiplatform** - UI framework
- **Koin** - Dependency injection
- **Ktor** - HTTP client
- **Kotlinx.serialization** - JSON serialization
- **Kotlinx.coroutines** - Async operations
- **CameraX** (Android) - Camera capture
- **AVFoundation** (iOS) - Camera capture
- **JavaCV** (Desktop) - Camera capture

---

## Current State Analysis

### Completed Features (60%)

| Feature | Status | Notes |
|---------|--------|-------|
| Project Structure | ✅ 100% | KMP setup complete |
| Koin DI | ✅ 100% | Dependency injection configured |
| Mock API Mode | ✅ 100% | Development without backend |
| Admin Dashboard UI | ✅ 90% | All tabs implemented |
| Kiosk Mode UI | ✅ 85% | Welcome, Enrollment, Verification |
| Desktop App | ✅ 80% | Functional but needs polish |
| Shared Components | ✅ 70% | Some extraction needed |

### Pending Features (40%)

| Feature | Status | Priority |
|---------|--------|----------|
| Real API Integration | 🔴 10% | HIGH |
| Camera Integration (Android) | 🔴 20% | HIGH |
| Camera Integration (Desktop) | 🟡 40% | HIGH |
| Camera Integration (iOS) | 🔴 0% | MEDIUM |
| Biometric Capture Flow | 🔴 20% | HIGH |
| Test Coverage | 🔴 10% | HIGH |
| Error Handling | 🟡 50% | HIGH |
| Offline Support | 🔴 0% | MEDIUM |
| Package Consolidation | 🔴 0% | HIGH |

---

## API Contracts

### Identity Core API (http://localhost:8080/api/v1)

All requests require the following headers:

```kotlin
// Common headers interface
interface CommonHeaders {
    val authorization: String  // "Bearer ${accessToken}"
    val tenantId: String       // X-Tenant-ID header
    val contentType: String    // "application/json"
}
```

### Authentication Endpoints

```kotlin
// POST /auth/login
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserDTO
)

// POST /auth/refresh
@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

// GET /auth/me
// Returns UserDTO

// POST /auth/logout
// Returns 204 No Content
```

### User Management Endpoints

```kotlin
// GET /users?page=0&size=20&sort=createdAt,desc
@Serializable
data class PaginatedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class UserDTO(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val status: UserStatus,
    val tenantId: Long,
    val permissions: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String? = null,
    val lastLoginIp: String? = null
)

@Serializable
enum class UserRole {
    SUPER_ADMIN, ADMIN, OPERATOR, VIEWER
}

@Serializable
enum class UserStatus {
    ACTIVE, INACTIVE, LOCKED
}

// POST /users
@Serializable
data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val role: UserRole
)

// PUT /users/{id}
@Serializable
data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val status: UserStatus
)

// DELETE /users/{id}
// Returns 204 No Content
```

### Biometric Processor API (http://localhost:8001/api/v1)

```kotlin
// POST /enrollments
@Serializable
data class EnrollmentRequest(
    val userId: Long,
    val image: String,  // Base64 encoded image
    val metadata: EnrollmentMetadata? = null
)

@Serializable
data class EnrollmentMetadata(
    val deviceType: String,
    val captureQuality: Float,
    val lightCondition: String
)

@Serializable
data class EnrollmentResponse(
    val enrollmentId: String,
    val status: EnrollmentStatus,
    val qualityScore: Float,
    val message: String
)

@Serializable
enum class EnrollmentStatus {
    PENDING, COMPLETED, FAILED
}

// POST /verify
@Serializable
data class VerificationRequest(
    val image: String,  // Base64 encoded image
    val userId: Long? = null,  // Optional for 1:N matching
    val threshold: Float = 0.85f
)

@Serializable
data class VerificationResponse(
    val verified: Boolean,
    val confidence: Float,
    val userId: Long? = null,
    val message: String,
    val processingTimeMs: Long
)

// POST /liveness
@Serializable
data class LivenessRequest(
    val image: String,  // Base64 encoded image
    val challenge: LivenessChallenge? = null
)

@Serializable
data class LivenessChallenge(
    val type: String,  // "BLINK", "SMILE", "TURN_HEAD"
    val direction: String? = null
)

@Serializable
data class LivenessResponse(
    val isLive: Boolean,
    val confidence: Float,
    val challengePassed: Boolean,
    val message: String
)

// GET /enrollments/{userId}
// Returns List<EnrollmentDTO>

@Serializable
data class EnrollmentDTO(
    val id: String,
    val userId: Long,
    val status: EnrollmentStatus,
    val qualityScore: Float,
    val createdAt: String,
    val expiresAt: String? = null
)
```

### Dashboard/Statistics Endpoints

```kotlin
// GET /dashboard/statistics
@Serializable
data class DashboardStatistics(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalEnrollments: Int,
    val totalVerifications: Int,
    val recentVerifications: List<VerificationActivity>,
    val enrollmentsByDay: List<DailyCount>,
    val verificationsByDay: List<DailyCount>
)

@Serializable
data class VerificationActivity(
    val id: Long,
    val userId: Long,
    val userName: String,
    val result: Boolean,
    val confidence: Float,
    val timestamp: String
)

@Serializable
data class DailyCount(
    val date: String,
    val count: Int
)
```

---

## Implementation Phases

### Phase 1: Package Consolidation & Code Cleanup

**Duration**: 2 days
**Priority**: HIGH
**Goal**: Clean up duplicate packages and establish clean architecture

#### 1.1 Merge Duplicate Packages

```kotlin
// Current structure has duplicates:
// com.fivucsas.mobile -> DELETE
// com.fivucsas.shared -> KEEP

// Step 1: Find all imports from mobile package
// grep -r "import com.fivucsas.mobile" shared/

// Step 2: Update imports to use shared package
// Find: import com.fivucsas.mobile
// Replace: import com.fivucsas.shared
```

#### 1.2 Create Configuration Objects

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/config/AppConfig.kt
package com.fivucsas.shared.config

object AppConfig {
    const val APP_NAME = "FIVUCSAS"
    const val APP_VERSION = "1.0.0"

    object Api {
        const val IDENTITY_BASE_URL = "http://localhost:8080/api/v1"
        const val BIOMETRIC_BASE_URL = "http://localhost:8001/api/v1"
        const val TIMEOUT_SECONDS = 30L
        const val MAX_RETRIES = 3
    }

    object Auth {
        const val TOKEN_REFRESH_THRESHOLD_SECONDS = 300L  // 5 minutes
        const val SESSION_TIMEOUT_MINUTES = 30
    }

    object Biometric {
        const val CONFIDENCE_THRESHOLD = 0.85f
        const val LIVENESS_THRESHOLD = 0.80f
        const val QUALITY_THRESHOLD = 0.75f
        const val MAX_ENROLLMENT_RETRIES = 3
    }

    object Cache {
        const val MAX_AGE_MINUTES = 15
        const val MAX_SIZE_MB = 50
    }
}
```

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/config/UIDimens.kt
package com.fivucsas.shared.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object UIDimens {
    // Spacing
    val SpacingXSmall: Dp = 4.dp
    val SpacingSmall: Dp = 8.dp
    val SpacingMedium: Dp = 16.dp
    val SpacingLarge: Dp = 24.dp
    val SpacingXLarge: Dp = 32.dp
    val SpacingXXLarge: Dp = 64.dp

    // Icons
    val IconSmall: Dp = 24.dp
    val IconMedium: Dp = 32.dp
    val IconLarge: Dp = 48.dp
    val IconXLarge: Dp = 64.dp
    val KioskIconSize: Dp = 120.dp

    // Buttons
    val ButtonHeight: Dp = 48.dp
    val ButtonHeightKiosk: Dp = 80.dp
    val ButtonWidthKiosk: Dp = 250.dp

    // Cards
    val CardRadius: Dp = 12.dp
    val CardElevation: Dp = 4.dp

    // Camera
    val CameraPreviewHeight: Dp = 400.dp
    val CameraPreviewWidth: Dp = 600.dp
}
```

#### 1.3 Establish Clean Package Structure

```
shared/src/commonMain/kotlin/com/fivucsas/shared/
├── config/
│   ├── AppConfig.kt
│   ├── UIDimens.kt
│   └── AnimationConfig.kt
├── data/
│   ├── api/
│   │   ├── IdentityApiClient.kt
│   │   └── BiometricApiClient.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── UserRepository.kt
│   │   └── BiometricRepository.kt
│   └── local/
│       └── TokenStorage.kt
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Enrollment.kt
│   │   └── Verification.kt
│   └── usecase/
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   └── LogoutUseCase.kt
│       └── biometric/
│           ├── EnrollUserUseCase.kt
│           └── VerifyUserUseCase.kt
├── di/
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── PlatformModule.kt
├── platform/
│   ├── camera/
│   │   └── ICameraService.kt
│   ├── storage/
│   │   └── ISecureStorage.kt
│   └── logger/
│       └── ILogger.kt
└── presentation/
    ├── viewmodel/
    │   ├── AdminViewModel.kt
    │   ├── KioskViewModel.kt
    │   └── AuthViewModel.kt
    └── state/
        ├── AdminUiState.kt
        ├── KioskUiState.kt
        └── AuthUiState.kt
```

**Acceptance Criteria**:
- [ ] com.fivucsas.mobile package deleted
- [ ] All imports updated to com.fivucsas.shared
- [ ] Configuration objects created
- [ ] Clean package structure established
- [ ] Build succeeds without errors

---

### Phase 2: Backend Integration

**Duration**: 4 days
**Priority**: HIGH
**Goal**: Connect to real Identity Core API and Biometric Processor

#### 2.1 Create Ktor HTTP Client

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/data/api/HttpClientFactory.kt
package com.fivucsas.shared.data.api

import com.fivucsas.shared.config.AppConfig
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val tokenProvider: TokenProvider
) {
    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }

        install(HttpTimeout) {
            requestTimeoutMillis = AppConfig.Api.TIMEOUT_SECONDS * 1000
            connectTimeoutMillis = AppConfig.Api.TIMEOUT_SECONDS * 1000
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = tokenProvider.getAccessToken()
                    val refreshToken = tokenProvider.getRefreshToken()
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(accessToken, refreshToken)
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val refreshToken = tokenProvider.getRefreshToken()
                    if (refreshToken != null) {
                        val newTokens = tokenProvider.refreshTokens(refreshToken)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    } else {
                        null
                    }
                }
            }
        }

        defaultRequest {
            url(AppConfig.Api.IDENTITY_BASE_URL)
        }
    }
}

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun refreshTokens(refreshToken: String): TokenPair
    suspend fun clearTokens()
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
```

#### 2.2 Create Identity API Client

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/data/api/IdentityApiClient.kt
package com.fivucsas.shared.data.api

import com.fivucsas.shared.data.api.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class IdentityApiClient(
    private val httpClient: HttpClient
) {
    // Authentication
    suspend fun login(request: LoginRequest): Result<LoginResponse> = runCatching {
        httpClient.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun refreshToken(request: RefreshRequest): Result<RefreshResponse> = runCatching {
        httpClient.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun logout(): Result<Unit> = runCatching {
        httpClient.post("/auth/logout")
    }

    suspend fun getCurrentUser(): Result<UserDTO> = runCatching {
        httpClient.get("/auth/me").body()
    }

    // Users
    suspend fun getUsers(
        page: Int = 0,
        size: Int = 20,
        sort: String? = null
    ): Result<PaginatedResponse<UserDTO>> = runCatching {
        httpClient.get("/users") {
            parameter("page", page)
            parameter("size", size)
            sort?.let { parameter("sort", it) }
        }.body()
    }

    suspend fun getUser(id: Long): Result<UserDTO> = runCatching {
        httpClient.get("/users/$id").body()
    }

    suspend fun createUser(request: CreateUserRequest): Result<UserDTO> = runCatching {
        httpClient.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateUser(id: Long, request: UpdateUserRequest): Result<UserDTO> = runCatching {
        httpClient.put("/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteUser(id: Long): Result<Unit> = runCatching {
        httpClient.delete("/users/$id")
    }

    // Dashboard
    suspend fun getDashboardStatistics(): Result<DashboardStatistics> = runCatching {
        httpClient.get("/dashboard/statistics").body()
    }
}
```

#### 2.3 Create Biometric API Client

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/data/api/BiometricApiClient.kt
package com.fivucsas.shared.data.api

import com.fivucsas.shared.config.AppConfig
import com.fivucsas.shared.data.api.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class BiometricApiClient(
    private val httpClient: HttpClient
) {
    private val baseUrl = AppConfig.Api.BIOMETRIC_BASE_URL

    suspend fun enroll(request: EnrollmentRequest): Result<EnrollmentResponse> = runCatching {
        httpClient.post("$baseUrl/enrollments") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun verify(request: VerificationRequest): Result<VerificationResponse> = runCatching {
        httpClient.post("$baseUrl/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun checkLiveness(request: LivenessRequest): Result<LivenessResponse> = runCatching {
        httpClient.post("$baseUrl/liveness") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getEnrollments(userId: Long): Result<List<EnrollmentDTO>> = runCatching {
        httpClient.get("$baseUrl/enrollments/$userId").body()
    }

    suspend fun deleteEnrollment(enrollmentId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/enrollments/$enrollmentId")
    }
}
```

#### 2.4 Create Auth Repository

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/data/repository/AuthRepository.kt
package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.api.IdentityApiClient
import com.fivucsas.shared.data.api.TokenProvider
import com.fivucsas.shared.data.api.TokenPair
import com.fivucsas.shared.data.api.dto.LoginRequest
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val apiClient: IdentityApiClient,
    private val tokenStorage: TokenStorage
) : TokenProvider {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    suspend fun login(email: String, password: String): Result<User> {
        val result = apiClient.login(LoginRequest(email, password))

        return result.fold(
            onSuccess = { response ->
                tokenStorage.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
                val user = response.user.toDomain()
                _currentUser.value = user
                _isAuthenticated.value = true
                Result.success(user)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun logout() {
        apiClient.logout()
        tokenStorage.clearTokens()
        _currentUser.value = null
        _isAuthenticated.value = false
    }

    suspend fun checkAuthState() {
        val token = tokenStorage.getAccessToken()
        if (token != null) {
            apiClient.getCurrentUser().fold(
                onSuccess = { userDto ->
                    _currentUser.value = userDto.toDomain()
                    _isAuthenticated.value = true
                },
                onFailure = {
                    tokenStorage.clearTokens()
                    _isAuthenticated.value = false
                }
            )
        }
    }

    // TokenProvider implementation
    override suspend fun getAccessToken(): String? = tokenStorage.getAccessToken()

    override suspend fun getRefreshToken(): String? = tokenStorage.getRefreshToken()

    override suspend fun refreshTokens(refreshToken: String): TokenPair {
        val response = apiClient.refreshToken(RefreshRequest(refreshToken))
            .getOrThrow()

        tokenStorage.saveTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken
        )

        return TokenPair(response.accessToken, response.refreshToken)
    }

    override suspend fun clearTokens() {
        tokenStorage.clearTokens()
    }
}
```

#### 2.5 Create Biometric Repository

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/data/repository/BiometricRepository.kt
package com.fivucsas.shared.data.repository

import com.fivucsas.shared.config.AppConfig
import com.fivucsas.shared.data.api.BiometricApiClient
import com.fivucsas.shared.data.api.dto.*
import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.model.VerificationResult
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class BiometricRepository(
    private val apiClient: BiometricApiClient
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun enrollUser(
        userId: Long,
        imageBytes: ByteArray,
        metadata: EnrollmentMetadata? = null
    ): Result<Enrollment> {
        val base64Image = Base64.encode(imageBytes)

        val request = EnrollmentRequest(
            userId = userId,
            image = base64Image,
            metadata = metadata
        )

        return apiClient.enroll(request).map { response ->
            Enrollment(
                id = response.enrollmentId,
                userId = userId,
                status = response.status,
                qualityScore = response.qualityScore,
                message = response.message
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun verifyUser(
        imageBytes: ByteArray,
        userId: Long? = null,
        threshold: Float = AppConfig.Biometric.CONFIDENCE_THRESHOLD
    ): Result<VerificationResult> {
        val base64Image = Base64.encode(imageBytes)

        val request = VerificationRequest(
            image = base64Image,
            userId = userId,
            threshold = threshold
        )

        return apiClient.verify(request).map { response ->
            VerificationResult(
                verified = response.verified,
                confidence = response.confidence,
                userId = response.userId,
                message = response.message,
                processingTimeMs = response.processingTimeMs
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun checkLiveness(
        imageBytes: ByteArray,
        challenge: LivenessChallenge? = null
    ): Result<LivenessResult> {
        val base64Image = Base64.encode(imageBytes)

        val request = LivenessRequest(
            image = base64Image,
            challenge = challenge
        )

        return apiClient.checkLiveness(request).map { response ->
            LivenessResult(
                isLive = response.isLive,
                confidence = response.confidence,
                challengePassed = response.challengePassed,
                message = response.message
            )
        }
    }

    suspend fun getEnrollments(userId: Long): Result<List<Enrollment>> {
        return apiClient.getEnrollments(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    suspend fun deleteEnrollment(enrollmentId: String): Result<Unit> {
        return apiClient.deleteEnrollment(enrollmentId)
    }
}

data class LivenessResult(
    val isLive: Boolean,
    val confidence: Float,
    val challengePassed: Boolean,
    val message: String
)
```

#### 2.6 Update Koin DI Module

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/di/AppModule.kt
package com.fivucsas.shared.di

import com.fivucsas.shared.data.api.*
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.data.repository.*
import com.fivucsas.shared.domain.usecase.auth.*
import com.fivucsas.shared.domain.usecase.biometric.*
import com.fivucsas.shared.presentation.viewmodel.*
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory(get()).create() }
    singleOf(::IdentityApiClient)
    singleOf(::BiometricApiClient)
}

val repositoryModule = module {
    singleOf(::TokenStorage)
    singleOf(::AuthRepository)
    singleOf(::UserRepository)
    singleOf(::BiometricRepository)
}

val useCaseModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::GetUsersUseCase)
    factoryOf(::EnrollUserUseCase)
    factoryOf(::VerifyUserUseCase)
    factoryOf(::GetStatisticsUseCase)
}

val viewModelModule = module {
    factoryOf(::AuthViewModel)
    factoryOf(::AdminViewModel)
    factoryOf(::KioskViewModel)
}

val appModules = listOf(
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    platformModule()  // Platform-specific bindings
)
```

**Acceptance Criteria**:
- [ ] Ktor HTTP client configured with auth
- [ ] Identity API client complete
- [ ] Biometric API client complete
- [ ] Repositories implement domain interfaces
- [ ] DI modules updated
- [ ] Build succeeds without errors

---

### Phase 3: Camera Integration

**Duration**: 4 days
**Priority**: HIGH
**Goal**: Implement camera capture across all platforms

#### 3.1 Define Camera Interface

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/platform/camera/ICameraService.kt
package com.fivucsas.shared.platform.camera

interface ICameraService {
    suspend fun initialize(config: CameraConfig): Result<Unit>
    suspend fun capturePhoto(): Result<ByteArray>
    suspend fun startPreview(onFrame: (ByteArray) -> Unit): Result<Unit>
    suspend fun stopPreview(): Result<Unit>
    suspend fun release(): Result<Unit>
    fun isAvailable(): Boolean
}

data class CameraConfig(
    val resolution: Resolution = Resolution.HD,
    val facing: CameraFacing = CameraFacing.FRONT,
    val enableFlash: Boolean = false
)

enum class Resolution(val width: Int, val height: Int) {
    VGA(640, 480),
    HD(1280, 720),
    FULL_HD(1920, 1080)
}

enum class CameraFacing {
    FRONT, BACK
}

sealed class CameraError : Exception() {
    object NotAvailable : CameraError()
    object PermissionDenied : CameraError()
    object InitializationFailed : CameraError()
    object CaptureFailed : CameraError()
    data class Unknown(override val message: String?) : CameraError()
}
```

#### 3.2 Android Camera Implementation (CameraX)

```kotlin
// shared/src/androidMain/kotlin/com/fivucsas/shared/platform/camera/AndroidCameraService.kt
package com.fivucsas.shared.platform.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidCameraService(
    private val context: Context
) : ICameraService {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val executor = Executors.newSingleThreadExecutor()

    override suspend fun initialize(config: CameraConfig): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val provider = suspendCancellableCoroutine<ProcessCameraProvider> { cont ->
                ProcessCameraProvider.getInstance(context).apply {
                    addListener({
                        cont.resume(get())
                    }, ContextCompat.getMainExecutor(context))
                }
            }

            cameraProvider = provider

            val cameraSelector = when (config.facing) {
                CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            }

            val targetResolution = android.util.Size(
                config.resolution.width,
                config.resolution.height
            )

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(targetResolution)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(targetResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CameraError.InitializationFailed)
        }
    }

    override suspend fun capturePhoto(): Result<ByteArray> = withContext(Dispatchers.IO) {
        val capture = imageCapture ?: return@withContext Result.failure(CameraError.NotAvailable)

        try {
            val bytes = suspendCancellableCoroutine<ByteArray> { cont ->
                capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        image.close()
                        cont.resume(bytes)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                })
            }
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(CameraError.CaptureFailed)
        }
    }

    override suspend fun startPreview(onFrame: (ByteArray) -> Unit): Result<Unit> {
        imageAnalysis?.setAnalyzer(executor) { imageProxy ->
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            onFrame(bytes)
            imageProxy.close()
        }
        return Result.success(Unit)
    }

    override suspend fun stopPreview(): Result<Unit> {
        imageAnalysis?.clearAnalyzer()
        return Result.success(Unit)
    }

    override suspend fun release(): Result<Unit> {
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        imageAnalysis = null
        return Result.success(Unit)
    }

    override fun isAvailable(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true
    }
}
```

#### 3.3 Desktop Camera Implementation (JavaCV)

```kotlin
// shared/src/desktopMain/kotlin/com/fivucsas/shared/platform/camera/DesktopCameraService.kt
package com.fivucsas.shared.platform.camera

import kotlinx.coroutines.*
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class DesktopCameraService : ICameraService {

    private var grabber: FrameGrabber? = null
    private var converter: Java2DFrameConverter? = null
    private var previewJob: Job? = null
    private var isInitialized = false

    override suspend fun initialize(config: CameraConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            grabber = OpenCVFrameGrabber(0).apply {
                imageWidth = config.resolution.width
                imageHeight = config.resolution.height
                start()
            }
            converter = Java2DFrameConverter()
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CameraError.InitializationFailed)
        }
    }

    override suspend fun capturePhoto(): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext Result.failure(CameraError.NotAvailable)
        }

        try {
            val frame = grabber?.grab()
                ?: return@withContext Result.failure(CameraError.CaptureFailed)

            val image = converter?.convert(frame)
                ?: return@withContext Result.failure(CameraError.CaptureFailed)

            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "jpg", outputStream)
            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            Result.failure(CameraError.CaptureFailed)
        }
    }

    override suspend fun startPreview(onFrame: (ByteArray) -> Unit): Result<Unit> {
        if (!isInitialized) {
            return Result.failure(CameraError.NotAvailable)
        }

        previewJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val frame = grabber?.grab() ?: continue
                    val image = converter?.convert(frame) ?: continue

                    val outputStream = ByteArrayOutputStream()
                    ImageIO.write(image, "jpg", outputStream)
                    onFrame(outputStream.toByteArray())

                    delay(33) // ~30 FPS
                } catch (e: Exception) {
                    // Log error and continue
                }
            }
        }

        return Result.success(Unit)
    }

    override suspend fun stopPreview(): Result<Unit> {
        previewJob?.cancel()
        previewJob = null
        return Result.success(Unit)
    }

    override suspend fun release(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stopPreview()
            grabber?.stop()
            grabber?.release()
            grabber = null
            converter = null
            isInitialized = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CameraError.Unknown(e.message))
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            val devices = OpenCVFrameGrabber.getDeviceDescriptions()
            devices.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
```

#### 3.4 iOS Camera Stub (expect/actual)

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/platform/camera/CameraServiceFactory.kt
package com.fivucsas.shared.platform.camera

expect fun createCameraService(): ICameraService

// shared/src/androidMain/kotlin/.../CameraServiceFactory.kt
actual fun createCameraService(): ICameraService {
    return AndroidCameraService(applicationContext)
}

// shared/src/desktopMain/kotlin/.../CameraServiceFactory.kt
actual fun createCameraService(): ICameraService {
    return DesktopCameraService()
}

// shared/src/iosMain/kotlin/.../CameraServiceFactory.kt
actual fun createCameraService(): ICameraService {
    return IosCameraService()
}
```

**Acceptance Criteria**:
- [ ] Camera interface defined
- [ ] Android CameraX implementation complete
- [ ] Desktop JavaCV implementation complete
- [ ] iOS stub created (full implementation optional)
- [ ] Camera service registered in DI
- [ ] Photo capture works on all platforms

---

### Phase 4: Biometric Flow Implementation

**Duration**: 4 days
**Priority**: HIGH
**Goal**: Complete enrollment and verification flows

#### 4.1 Update Kiosk ViewModel

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/presentation/viewmodel/KioskViewModel.kt
package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.config.AppConfig
import com.fivucsas.shared.data.repository.BiometricRepository
import com.fivucsas.shared.data.repository.UserRepository
import com.fivucsas.shared.platform.camera.CameraConfig
import com.fivucsas.shared.platform.camera.ICameraService
import com.fivucsas.shared.presentation.state.KioskUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KioskViewModel(
    private val biometricRepository: BiometricRepository,
    private val userRepository: UserRepository,
    private val cameraService: ICameraService
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(KioskUiState())
    val uiState: StateFlow<KioskUiState> = _uiState.asStateFlow()

    fun initializeCamera() {
        scope.launch {
            _uiState.update { it.copy(cameraState = CameraState.Initializing) }

            cameraService.initialize(CameraConfig(facing = CameraFacing.FRONT))
                .fold(
                    onSuccess = {
                        _uiState.update { it.copy(cameraState = CameraState.Ready) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                cameraState = CameraState.Error,
                                errorMessage = error.message
                            )
                        }
                    }
                )
        }
    }

    fun startEnrollment(
        firstName: String,
        lastName: String,
        email: String
    ) {
        scope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            // First create user
            val userResult = userRepository.createUser(
                firstName = firstName,
                lastName = lastName,
                email = email
            )

            userResult.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUserId = user.id,
                            enrollmentStep = EnrollmentStep.CAPTURE
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Failed to create user: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun captureAndEnroll() {
        scope.launch {
            val userId = _uiState.value.currentUserId
                ?: return@launch setError("No user selected")

            _uiState.update { it.copy(enrollmentStep = EnrollmentStep.CAPTURING) }

            // Step 1: Capture photo
            val captureResult = cameraService.capturePhoto()
            val imageBytes = captureResult.getOrElse {
                return@launch setError("Failed to capture photo")
            }

            // Step 2: Check liveness
            _uiState.update { it.copy(enrollmentStep = EnrollmentStep.LIVENESS_CHECK) }

            val livenessResult = biometricRepository.checkLiveness(imageBytes)
            val liveness = livenessResult.getOrElse {
                return@launch setError("Liveness check failed")
            }

            if (!liveness.isLive || liveness.confidence < AppConfig.Biometric.LIVENESS_THRESHOLD) {
                return@launch setError("Liveness check failed. Please try again.")
            }

            // Step 3: Enroll user
            _uiState.update { it.copy(enrollmentStep = EnrollmentStep.ENROLLING) }

            val enrollResult = biometricRepository.enrollUser(userId, imageBytes)

            enrollResult.fold(
                onSuccess = { enrollment ->
                    if (enrollment.qualityScore >= AppConfig.Biometric.QUALITY_THRESHOLD) {
                        _uiState.update {
                            it.copy(
                                enrollmentStep = EnrollmentStep.SUCCESS,
                                isProcessing = false,
                                successMessage = "Enrollment successful!"
                            )
                        }
                    } else {
                        setError("Image quality too low. Please try again with better lighting.")
                    }
                },
                onFailure = { error ->
                    setError("Enrollment failed: ${error.message}")
                }
            )
        }
    }

    fun startVerification() {
        scope.launch {
            _uiState.update {
                it.copy(
                    verificationStep = VerificationStep.READY,
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }

    fun captureAndVerify() {
        scope.launch {
            _uiState.update { it.copy(verificationStep = VerificationStep.CAPTURING) }

            // Step 1: Capture photo
            val captureResult = cameraService.capturePhoto()
            val imageBytes = captureResult.getOrElse {
                return@launch setVerificationError("Failed to capture photo")
            }

            // Step 2: Check liveness
            _uiState.update { it.copy(verificationStep = VerificationStep.LIVENESS_CHECK) }

            val livenessResult = biometricRepository.checkLiveness(imageBytes)
            val liveness = livenessResult.getOrElse {
                return@launch setVerificationError("Liveness check failed")
            }

            if (!liveness.isLive) {
                return@launch setVerificationError("Liveness check failed. Please try again.")
            }

            // Step 3: Verify
            _uiState.update { it.copy(verificationStep = VerificationStep.VERIFYING) }

            val verifyResult = biometricRepository.verifyUser(imageBytes)

            verifyResult.fold(
                onSuccess = { result ->
                    if (result.verified && result.confidence >= AppConfig.Biometric.CONFIDENCE_THRESHOLD) {
                        _uiState.update {
                            it.copy(
                                verificationStep = VerificationStep.SUCCESS,
                                verifiedUserId = result.userId,
                                verificationConfidence = result.confidence,
                                successMessage = "Verification successful!"
                            )
                        }
                    } else {
                        setVerificationError(
                            "Verification failed. Confidence: ${(result.confidence * 100).toInt()}%"
                        )
                    }
                },
                onFailure = { error ->
                    setVerificationError("Verification failed: ${error.message}")
                }
            )
        }
    }

    fun resetToWelcome() {
        _uiState.update { KioskUiState() }
    }

    fun releaseCamera() {
        scope.launch {
            cameraService.release()
        }
    }

    private fun setError(message: String) {
        _uiState.update {
            it.copy(
                isProcessing = false,
                errorMessage = message,
                enrollmentStep = EnrollmentStep.ERROR
            )
        }
    }

    private fun setVerificationError(message: String) {
        _uiState.update {
            it.copy(
                verificationStep = VerificationStep.FAILED,
                errorMessage = message
            )
        }
    }
}
```

#### 4.2 Define UI State

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/presentation/state/KioskUiState.kt
package com.fivucsas.shared.presentation.state

data class KioskUiState(
    val screen: KioskScreen = KioskScreen.WELCOME,
    val cameraState: CameraState = CameraState.NotInitialized,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Enrollment
    val currentUserId: Long? = null,
    val enrollmentStep: EnrollmentStep = EnrollmentStep.FORM,

    // Verification
    val verificationStep: VerificationStep = VerificationStep.READY,
    val verifiedUserId: Long? = null,
    val verificationConfidence: Float? = null
)

enum class KioskScreen {
    WELCOME,
    ENROLLMENT,
    VERIFICATION
}

enum class CameraState {
    NotInitialized,
    Initializing,
    Ready,
    Error
}

enum class EnrollmentStep {
    FORM,
    CAPTURE,
    CAPTURING,
    LIVENESS_CHECK,
    ENROLLING,
    SUCCESS,
    ERROR
}

enum class VerificationStep {
    READY,
    CAPTURING,
    LIVENESS_CHECK,
    VERIFYING,
    SUCCESS,
    FAILED
}
```

**Acceptance Criteria**:
- [ ] Enrollment flow complete with liveness check
- [ ] Verification flow complete with liveness check
- [ ] Camera integration working
- [ ] Error states properly handled
- [ ] Success/failure feedback displayed
- [ ] Retry logic implemented

---

### Phase 5: Platform-Specific Features

**Duration**: 3 days
**Priority**: MEDIUM
**Goal**: Implement platform-specific optimizations

#### 5.1 Android-Specific Features

```kotlin
// androidApp/src/main/kotlin/com/fivucsas/android/MainActivity.kt
package com.fivucsas.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.fivucsas.shared.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Camera permission granted
        } else {
            // Show permission denied message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkCameraPermission()

        setContent {
            AppTheme {
                App()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
```

#### 5.2 Secure Storage Implementation

```kotlin
// shared/src/androidMain/kotlin/.../storage/AndroidSecureStorage.kt
package com.fivucsas.shared.platform.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidSecureStorage(context: Context) : ISecureStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "fivucsas_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override suspend fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override suspend fun delete(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override suspend fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}

// shared/src/desktopMain/kotlin/.../storage/DesktopSecureStorage.kt
class DesktopSecureStorage : ISecureStorage {
    private val preferences = java.util.prefs.Preferences.userNodeForPackage(
        DesktopSecureStorage::class.java
    )

    override suspend fun saveString(key: String, value: String) {
        preferences.put(key, value)
    }

    override suspend fun getString(key: String): String? {
        return preferences.get(key, null)
    }

    override suspend fun delete(key: String) {
        preferences.remove(key)
    }

    override suspend fun clear() {
        preferences.clear()
    }
}
```

**Acceptance Criteria**:
- [ ] Android permissions handled properly
- [ ] Secure storage working on Android
- [ ] Secure storage working on Desktop
- [ ] Platform-specific DI bindings
- [ ] No platform-specific code in shared module

---

### Phase 6: Test Coverage Expansion

**Duration**: 3 days
**Priority**: HIGH
**Target**: 70% code coverage

#### 6.1 ViewModel Tests

```kotlin
// shared/src/commonTest/kotlin/.../viewmodel/KioskViewModelTest.kt
package com.fivucsas.shared.presentation.viewmodel

import app.cash.turbine.test
import com.fivucsas.shared.data.repository.BiometricRepository
import com.fivucsas.shared.data.repository.UserRepository
import com.fivucsas.shared.platform.camera.ICameraService
import com.fivucsas.shared.presentation.state.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class KioskViewModelTest {

    private lateinit var viewModel: KioskViewModel
    private lateinit var biometricRepository: BiometricRepository
    private lateinit var userRepository: UserRepository
    private lateinit var cameraService: ICameraService

    @BeforeTest
    fun setup() {
        biometricRepository = mockk()
        userRepository = mockk()
        cameraService = mockk()

        viewModel = KioskViewModel(
            biometricRepository = biometricRepository,
            userRepository = userRepository,
            cameraService = cameraService
        )
    }

    @Test
    fun `initializeCamera should update state to Ready on success`() = runTest {
        // Given
        coEvery { cameraService.initialize(any()) } returns Result.success(Unit)

        // When
        viewModel.initializeCamera()

        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(CameraState.NotInitialized, initialState.cameraState)

            val initializingState = awaitItem()
            assertEquals(CameraState.Initializing, initializingState.cameraState)

            val readyState = awaitItem()
            assertEquals(CameraState.Ready, readyState.cameraState)
        }
    }

    @Test
    fun `captureAndEnroll should fail if liveness check fails`() = runTest {
        // Given
        coEvery { cameraService.capturePhoto() } returns Result.success(byteArrayOf())
        coEvery { biometricRepository.checkLiveness(any()) } returns Result.success(
            LivenessResult(isLive = false, confidence = 0.3f, challengePassed = false, message = "Failed")
        )

        viewModel._uiState.value = viewModel.uiState.value.copy(currentUserId = 1L)

        // When
        viewModel.captureAndEnroll()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(EnrollmentStep.ERROR, state.enrollmentStep)
            assertNotNull(state.errorMessage)
        }
    }

    @Test
    fun `captureAndVerify should succeed with valid face`() = runTest {
        // Given
        coEvery { cameraService.capturePhoto() } returns Result.success(byteArrayOf())
        coEvery { biometricRepository.checkLiveness(any()) } returns Result.success(
            LivenessResult(isLive = true, confidence = 0.95f, challengePassed = true, message = "OK")
        )
        coEvery { biometricRepository.verifyUser(any()) } returns Result.success(
            VerificationResult(verified = true, confidence = 0.92f, userId = 1L, message = "Match", processingTimeMs = 150)
        )

        // When
        viewModel.captureAndVerify()

        // Then
        viewModel.uiState.test {
            skipItems(3) // Skip intermediate states
            val state = awaitItem()
            assertEquals(VerificationStep.SUCCESS, state.verificationStep)
            assertEquals(1L, state.verifiedUserId)
        }
    }

    // Add more tests...
}
```

#### 6.2 Repository Tests

```kotlin
// shared/src/commonTest/kotlin/.../repository/BiometricRepositoryTest.kt
package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.api.BiometricApiClient
import com.fivucsas.shared.data.api.dto.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BiometricRepositoryTest {

    private lateinit var repository: BiometricRepository
    private lateinit var apiClient: BiometricApiClient

    @BeforeTest
    fun setup() {
        apiClient = mockk()
        repository = BiometricRepository(apiClient)
    }

    @Test
    fun `enrollUser should return enrollment on success`() = runTest {
        // Given
        val userId = 1L
        val imageBytes = byteArrayOf(1, 2, 3)

        coEvery { apiClient.enroll(any()) } returns Result.success(
            EnrollmentResponse(
                enrollmentId = "enroll-123",
                status = EnrollmentStatus.COMPLETED,
                qualityScore = 0.92f,
                message = "Success"
            )
        )

        // When
        val result = repository.enrollUser(userId, imageBytes)

        // Then
        assertTrue(result.isSuccess)
        val enrollment = result.getOrThrow()
        assertEquals("enroll-123", enrollment.id)
        assertEquals(0.92f, enrollment.qualityScore)
    }

    @Test
    fun `verifyUser should return verification result`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)

        coEvery { apiClient.verify(any()) } returns Result.success(
            VerificationResponse(
                verified = true,
                confidence = 0.95f,
                userId = 1L,
                message = "Match found",
                processingTimeMs = 150
            )
        )

        // When
        val result = repository.verifyUser(imageBytes)

        // Then
        assertTrue(result.isSuccess)
        val verification = result.getOrThrow()
        assertTrue(verification.verified)
        assertEquals(0.95f, verification.confidence)
    }
}
```

**Acceptance Criteria**:
- [ ] ViewModel tests: 80% coverage
- [ ] Repository tests: 90% coverage
- [ ] Use case tests: 90% coverage
- [ ] All tests passing
- [ ] Coverage reports generated

---

### Phase 7: Production Readiness

**Duration**: 1 day
**Priority**: HIGH
**Goal**: Final polish and production configuration

#### 7.1 Environment Configuration

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/config/Environment.kt
package com.fivucsas.shared.config

enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

object EnvironmentConfig {
    var current: Environment = Environment.DEVELOPMENT
        private set

    fun configure(environment: Environment) {
        current = environment
    }

    val identityBaseUrl: String
        get() = when (current) {
            Environment.DEVELOPMENT -> "http://localhost:8080/api/v1"
            Environment.STAGING -> "https://staging-api.fivucsas.com/api/v1"
            Environment.PRODUCTION -> "https://api.fivucsas.com/api/v1"
        }

    val biometricBaseUrl: String
        get() = when (current) {
            Environment.DEVELOPMENT -> "http://localhost:8001/api/v1"
            Environment.STAGING -> "https://staging-biometric.fivucsas.com/api/v1"
            Environment.PRODUCTION -> "https://biometric.fivucsas.com/api/v1"
        }

    val enableLogging: Boolean
        get() = current != Environment.PRODUCTION

    val useMockApi: Boolean
        get() = false  // Set to false for production
}
```

#### 7.2 Error Handling

```kotlin
// shared/src/commonMain/kotlin/com/fivucsas/shared/domain/error/AppError.kt
package com.fivucsas.shared.domain.error

sealed class AppError : Exception() {
    data class Network(override val message: String?) : AppError()
    data class Authentication(override val message: String?) : AppError()
    data class Authorization(override val message: String?) : AppError()
    data class Validation(override val message: String?, val fields: Map<String, String>? = null) : AppError()
    data class NotFound(override val message: String?) : AppError()
    data class Server(override val message: String?) : AppError()
    data class Unknown(override val message: String?) : AppError()
}

fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        is java.net.UnknownHostException -> AppError.Network("No internet connection")
        is java.net.SocketTimeoutException -> AppError.Network("Connection timeout")
        else -> AppError.Unknown(message)
    }
}
```

**Acceptance Criteria**:
- [ ] Environment configuration complete
- [ ] Error handling comprehensive
- [ ] Logging configured per environment
- [ ] Release builds tested
- [ ] No hardcoded development URLs

---

## Integration Points

### With Identity Core API

```
Client Apps                   Identity Core API
     │                              │
     ├── POST /auth/login ─────────►│
     │◄──── JWT tokens ─────────────┤
     │                              │
     ├── GET /users ───────────────►│
     │   (Bearer token)             │
     │◄──── Paginated users ────────┤
     │                              │
     ├── POST /users ──────────────►│
     │◄──── Created user ───────────┤
```

### With Biometric Processor

```
Client Apps                 Biometric Processor
     │                              │
     ├── POST /enrollments ────────►│
     │   (Base64 image)             │
     │◄──── Enrollment result ──────┤
     │                              │
     ├── POST /verify ─────────────►│
     │   (Base64 image)             │
     │◄──── Verification result ────┤
     │                              │
     ├── POST /liveness ───────────►│
     │   (Base64 image)             │
     │◄──── Liveness result ────────┤
```

---

## Testing Strategy

### Test Types

| Type | Coverage Target | Tools |
|------|-----------------|-------|
| Unit Tests | ≥80% | kotlin.test + MockK |
| Integration Tests | Critical paths | Ktor MockEngine |
| UI Tests | Happy paths | Compose Testing |

### Test Commands

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew koverReport

# Run desktop app
./gradlew desktopApp:run

# Run Android app (requires emulator/device)
./gradlew androidApp:installDebug
```

---

## Deployment Checklist

### Pre-Release

- [ ] All tests pass
- [ ] Build succeeds on all platforms
- [ ] Mock API disabled
- [ ] Environment configured for production
- [ ] Camera permissions handled
- [ ] Error handling tested

### Build Commands

```bash
# Desktop
./gradlew desktopApp:packageDistributionForCurrentOS

# Android
./gradlew androidApp:assembleRelease

# iOS (requires macOS)
./gradlew iosApp:linkReleaseFrameworkIosArm64
```

---

## Timeline Summary

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 1: Package Cleanup | 2 days | None |
| Phase 2: Backend Integration | 4 days | Identity API, Biometric API |
| Phase 3: Camera Integration | 4 days | Platform SDKs |
| Phase 4: Biometric Flow | 4 days | Phases 2, 3 |
| Phase 5: Platform Features | 3 days | Phases 1-4 |
| Phase 6: Test Coverage | 3 days | All features |
| Phase 7: Production Ready | 1 day | All phases |
| **Total** | **21 days** | |

---

## Success Criteria

| Metric | Target |
|--------|--------|
| Feature Completion | 100% |
| Test Coverage | ≥70% |
| Build Time | <5 min |
| Android APK Size | <50MB |
| Desktop App Size | <100MB |

---

**Document Status**: Ready for Implementation
**Last Updated**: January 2026
**Next Review**: After Phase 7 completion
