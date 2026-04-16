# Hexagonal Architecture Guide - FIVUCSAS Client Apps

**Version:** 1.0.0
**Date:** 2026-01-19
**Architecture Style:** Hexagonal (Ports and Adapters) + Clean Architecture

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Principles](#architecture-principles)
3. [Core Concepts](#core-concepts)
4. [Ports](#ports)
5. [Adapters](#adapters)
6. [Factory Pattern](#factory-pattern)
7. [Layer Interactions](#layer-interactions)
8. [Implementation Examples](#implementation-examples)
9. [Testing Strategy](#testing-strategy)
10. [Best Practices](#best-practices)

---

## Overview

### What is Hexagonal Architecture?

Hexagonal Architecture (also known as Ports and Adapters) is an architectural pattern that:

- **Isolates business logic** from external concerns
- **Defines clear boundaries** between core logic and infrastructure
- **Enables testability** through dependency inversion
- **Supports multiple interfaces** (UI, API, CLI, etc.)
- **Makes the system adaptable** to changing requirements

### Why Hexagonal Architecture?

✅ **Platform Independence** - Core logic works on Android, iOS, Desktop, Web
✅ **Testability** - Easy to test with mock implementations
✅ **Maintainability** - Clear separation of concerns
✅ **Flexibility** - Swap implementations without changing core logic
✅ **SOLID Compliance** - Enforces Dependency Inversion Principle

### Architecture Diagram

```
┌────────────────────────────────────────────────────────────────┐
│                     PRIMARY ADAPTERS                            │
│                  (UI, REST API, CLI)                           │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   Desktop    │  │    Android   │  │     iOS      │       │
│  │      UI      │  │      UI      │  │     UI       │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                  │                  │                │
└─────────┼──────────────────┼──────────────────┼────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌────────────────────────────────────────────────────────────────┐
│                     PRIMARY PORTS (Driving)                     │
│                                                                 │
│  INavigationService │ IDialogService │ INotificationService    │
│                                                                 │
└─────────┬──────────────────┬──────────────────┬────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌────────────────────────────────────────────────────────────────┐
│                    HEXAGONAL CORE                               │
│                  (Business Logic)                               │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │                  DOMAIN LAYER                           │   │
│  │                                                         │   │
│  │  ┌──────────────────────────────────────────────┐     │   │
│  │  │  USE CASES (Application Services)            │     │   │
│  │  │  • GetUsersUseCase                           │     │   │
│  │  │  • EnrollFaceUseCase                         │     │   │
│  │  │  • VerifyFaceUseCase                         │     │   │
│  │  │  • ValidateUserUseCase                       │     │   │
│  │  └──────────────────────────────────────────────┘     │   │
│  │                                                         │   │
│  │  ┌──────────────────────────────────────────────┐     │   │
│  │  │  DOMAIN MODELS (Entities)                    │     │   │
│  │  │  • User                                       │     │   │
│  │  │  • BiometricData                             │     │   │
│  │  │  • EnrollmentData                            │     │   │
│  │  │  • VerificationResult                        │     │   │
│  │  └──────────────────────────────────────────────┘     │   │
│  │                                                         │   │
│  │  ┌──────────────────────────────────────────────┐     │   │
│  │  │  BUSINESS RULES                              │     │   │
│  │  │  • ValidationRules                           │     │   │
│  │  │  • AppExceptions                             │     │   │
│  │  │  • BiometricConfig                           │     │   │
│  │  └──────────────────────────────────────────────┘     │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │            PRESENTATION LAYER                           │   │
│  │  • ViewModels (AdminViewModel, KioskViewModel)         │   │
│  │  • UI State (AdminUiState, KioskUiState)               │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────┬──────────────────┬──────────────────┬────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌────────────────────────────────────────────────────────────────┐
│                   SECONDARY PORTS (Driven)                      │
│                                                                 │
│  UserRepository │ ICameraService │ IConfigurationProvider      │
│  AuthRepository │ ILogger        │ ISecureStorage              │
│  BiometricRepo  │                                               │
│                                                                 │
└─────────┬──────────────────┬──────────────────┬────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌────────────────────────────────────────────────────────────────┐
│                   SECONDARY ADAPTERS                            │
│            (Database, External APIs, File System)              │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │ UserRepo     │  │ DesktopCamera│  │    Default   │        │
│  │ Impl         │  │ ServiceImpl  │  │    Config    │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │  Auth API    │  │ Desktop      │  │  Desktop     │        │
│  │  Impl        │  │ Logger       │  │  Secure      │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└────────────────────────────────────────────────────────────────┘
```

---

## Architecture Principles

### 1. Dependency Rule

**"Dependencies point inward, toward the core"**

```
External World → Adapters → Ports → Core Business Logic
     (UI)       (Impl)     (Interface)   (Use Cases)
```

- **Core** depends on nothing
- **Ports** define interfaces
- **Adapters** implement ports and depend on external systems
- **External systems** connect through adapters

### 2. Business Logic Independence

The hexagonal core (business logic) is independent of:

- ❌ UI frameworks (Compose, SwiftUI, React)
- ❌ Databases (PostgreSQL, MongoDB)
- ❌ External APIs (REST, GraphQL)
- ❌ Operating systems (Android, iOS, Desktop)
- ❌ Libraries and frameworks

### 3. Ports Define Contracts

Ports are **interfaces** that define:

- What the core **needs** from the outside world (Secondary Ports)
- What the core **provides** to the outside world (Primary Ports)

### 4. Adapters Implement Contracts

Adapters are **implementations** that:

- Connect external systems to ports
- Translate between external formats and domain models
- Handle platform-specific details

---

## Core Concepts

### The Hexagon

The "hexagon" represents the **core business logic** of the application:

```
         ┌─────────┐
        ╱           ╲
       ╱   BUSINESS  ╲
      │     LOGIC     │
      │   (CORE)      │
       ╲             ╱
        ╲           ╱
         └─────────┘
```

**Contains:**
- Domain models (User, BiometricData)
- Use cases (EnrollFace, VerifyFace)
- Business rules (Validation, Thresholds)
- Exceptions (AppException, ValidationException)

**Does NOT contain:**
- UI code
- Database code
- API client code
- Platform-specific code

### Primary Ports (Driving/Inbound)

**"What the application offers to the outside world"**

These ports allow external actors to **use** the application.

**Examples:**
- `INavigationService` - Allows core to trigger navigation
- `IDialogService` - Allows core to show dialogs
- `INotificationService` - Allows core to show notifications

**Direction:** Outside World → Port → Core

```kotlin
// Primary Port (Interface)
interface INavigationService {
    fun navigateTo(route: String)
}

// Core uses the port
class LoginUseCase(
    private val navigationService: INavigationService
) {
    suspend fun execute(credentials: Credentials) {
        // Business logic
        if (isValid) {
            navigationService.navigateTo("dashboard")
        }
    }
}
```

### Secondary Ports (Driven/Outbound)

**"What the application needs from the outside world"**

These ports allow the application to **interact with** external systems.

**Examples:**
- `UserRepository` - Data access port
- `ICameraService` - Camera access port
- `ILogger` - Logging port
- `IConfigurationProvider` - Configuration access port

**Direction:** Core → Port → Outside World

```kotlin
// Secondary Port (Interface)
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
}

// Core uses the port
class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<List<User>> {
        return userRepository.getUsers()
    }
}
```

### Primary Adapters (Driving/Inbound)

**"Implementations that call the core"**

These adapters **drive** the application.

**Examples:**
- Desktop UI (Compose Desktop)
- Android UI (Jetpack Compose)
- iOS UI (SwiftUI)
- REST API endpoints

```kotlin
// Primary Adapter (Desktop UI)
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    // UI calls ViewModel (which calls use cases in core)
    Button(onClick = { viewModel.login(email, password) }) {
        Text("Login")
    }
}
```

### Secondary Adapters (Driven/Outbound)

**"Implementations that are called by the core"**

These adapters are **driven** by the application.

**Examples:**
- `UserRepositoryImpl` - Database adapter
- `DesktopCameraServiceImpl` - Camera adapter
- `DesktopLoggerImpl` - Logging adapter
- `DefaultConfigurationProvider` - Config adapter

```kotlin
// Secondary Adapter (Repository Implementation)
class UserRepositoryImpl(
    private val apiClient: IdentityApi
) : UserRepository {
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiClient.getUsers()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Ports

### Primary Ports (Driving)

#### 1. INavigationService

**Purpose:** Allows core logic to trigger navigation without depending on UI framework.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/INavigationService.kt`

```kotlin
interface INavigationService {
    fun navigateTo(route: String, params: Map<String, Any> = emptyMap())
    fun navigateBack(): Boolean
    fun navigateAndClearStack(route: String)
    fun popUpTo(route: String, inclusive: Boolean = false)
}
```

**Usage Example:**
```kotlin
class LoginUseCase(
    private val authRepository: AuthRepository,
    private val navigationService: INavigationService
) {
    suspend fun execute(email: String, password: String) {
        val result = authRepository.login(email, password)
        if (result.isSuccess) {
            navigationService.navigateTo(NavigationRoutes.DASHBOARD)
        }
    }
}
```

#### 2. IDialogService

**Purpose:** Allows core logic to show dialogs without depending on UI framework.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/IDialogService.kt`

```kotlin
interface IDialogService {
    suspend fun showInfo(title: String, message: String, onDismiss: (() -> Unit)? = null)
    suspend fun showConfirmation(title: String, message: String, confirmText: String, cancelText: String): Boolean
    suspend fun showError(title: String, message: String, onDismiss: (() -> Unit)? = null)
    suspend fun showLoading(message: String): DialogHandle
    fun dismiss(handle: DialogHandle)
    fun dismissAll()
}
```

**Usage Example:**
```kotlin
class DeleteUserUseCase(
    private val userRepository: UserRepository,
    private val dialogService: IDialogService
) {
    suspend fun execute(userId: String) {
        val confirmed = dialogService.showConfirmation(
            title = "Delete User",
            message = "Are you sure you want to delete this user?",
            confirmText = "Delete",
            cancelText = "Cancel"
        )

        if (confirmed) {
            val loadingHandle = dialogService.showLoading("Deleting user...")
            userRepository.deleteUser(userId)
            dialogService.dismiss(loadingHandle)
        }
    }
}
```

#### 3. INotificationService

**Purpose:** Allows core logic to show notifications without depending on UI framework.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/INotificationService.kt`

```kotlin
interface INotificationService {
    fun showSuccess(message: String, duration: Long? = null)
    fun showError(message: String, duration: Long? = null)
    fun showWarning(message: String, duration: Long? = null)
    fun showInfo(message: String, duration: Long? = null)
    fun clearAll()
}
```

**Usage Example:**
```kotlin
class EnrollFaceUseCase(
    private val biometricRepository: BiometricRepository,
    private val notificationService: INotificationService
) {
    suspend fun execute(userId: String, imageBytes: ByteArray) {
        val result = biometricRepository.enrollFace(userId, imageBytes)

        if (result.isSuccess) {
            notificationService.showSuccess("Face enrolled successfully!")
        } else {
            notificationService.showError("Enrollment failed: ${result.exceptionOrNull()?.message}")
        }
    }
}
```

### Secondary Ports (Driven)

#### 1. UserRepository

**Purpose:** Data access abstraction for user operations.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/domain/repository/UserRepository.kt`

```kotlin
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: String): Result<User>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(id: String, user: User): Result<User>
    suspend fun deleteUser(id: String): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun getStatistics(): Result<Statistics>
}
```

#### 2. ICameraService

**Purpose:** Camera operations abstraction for cross-platform support.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/ICameraService.kt`

```kotlin
interface ICameraService {
    val cameraState: StateFlow<CameraState>
    suspend fun initialize(lensFacing: LensFacing = FRONT): Result<Unit>
    suspend fun startPreview(): Result<Unit>
    suspend fun stopPreview(): Result<Unit>
    suspend fun captureImage(): Result<ByteArray>
    suspend fun captureFrame(): Result<ByteArray>
    fun isAvailable(): Boolean
    fun hasCamera(lensFacing: LensFacing): Boolean
    suspend fun release()
    fun getPreviewDimensions(): Pair<Int, Int>
    fun getSupportedResolutions(): List<Pair<Int, Int>>
}
```

#### 3. IConfigurationProvider

**Purpose:** Configuration access abstraction.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/IConfigurationProvider.kt`

```kotlin
interface IConfigurationProvider {
    val appName: String
    val apiBaseUrl: String
    val cacheEnabled: Boolean
    // ... 20+ configuration properties

    fun <T> get(key: String, defaultValue: T): T
    fun <T> set(key: String, value: T)
    suspend fun reload()
}
```

#### 4. ILogger

**Purpose:** Logging abstraction for cross-platform support.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/ILogger.kt`

```kotlin
interface ILogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
    fun verbose(tag: String, message: String)
}
```

#### 5. ISecureStorage

**Purpose:** Secure storage abstraction for cross-platform support.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/ISecureStorage.kt`

```kotlin
interface ISecureStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clear()
}
```

---

## Adapters

### Primary Adapters (Driving)

#### Desktop UI Adapter

**Location:** `/desktopApp/src/desktopMain/kotlin/`

```kotlin
// Desktop app drives the core through ViewModels
@Composable
fun App() {
    val viewModel: AdminViewModel = koinInject()

    AdminDashboard(
        state = viewModel.state.collectAsState().value,
        onAction = viewModel::handleAction // Calls use cases in core
    )
}
```

**Flow:**
```
User Click → Compose UI → ViewModel → Use Case → Repository Port → Repository Adapter → API/Database
```

### Secondary Adapters (Driven)

#### 1. UserRepositoryImpl (Data Adapter)

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/data/repository/UserRepositoryImpl.kt`

```kotlin
class UserRepositoryImpl(
    private val identityApi: IdentityApi,
    private val logger: ILogger
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            logger.info("UserRepository", "Fetching users from API")
            val response = identityApi.getUsers()
            val users = response.map { it.toDomain() }
            Result.success(users)
        } catch (e: Exception) {
            logger.error("UserRepository", "Failed to fetch users", e)
            Result.failure(e.toAppException())
        }
    }

    // ... other methods
}
```

**Adapter Responsibilities:**
- Calls external API
- Converts DTOs to domain models
- Handles errors
- Logs operations

#### 2. DesktopCameraServiceImpl (Platform Adapter)

**Location:** `/desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/platform/DesktopCameraServiceImpl.kt`

```kotlin
class DesktopCameraServiceImpl(
    private val logger: ILogger
) : ICameraService {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> {
        return try {
            // JavaCV specific implementation
            grabber = VideoInputFrameGrabber(0)
            grabber.start()
            _cameraState.value = CameraState.Ready
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("DesktopCamera", "Failed to initialize", e)
            Result.failure(e)
        }
    }

    // ... other methods
}
```

**Adapter Responsibilities:**
- Uses platform-specific library (JavaCV)
- Implements port interface
- Manages camera lifecycle
- Handles platform-specific errors

#### 3. DefaultConfigurationProvider (Config Adapter)

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/config/DefaultConfigurationProvider.kt`

```kotlin
class DefaultConfigurationProvider : IConfigurationProvider {

    override val appName: String get() = AppConfig.APP_NAME
    override val apiBaseUrl: String get() = AppConfig.Api.BASE_URL
    override val cacheEnabled: Boolean get() = AppConfig.Cache.ENABLE_CACHE

    private val customConfig = mutableMapOf<String, Any>()

    override fun <T> get(key: String, defaultValue: T): T {
        return customConfig[key] as? T ?: defaultValue
    }

    override fun <T> set(key: String, value: T) {
        customConfig[key] = value as Any
    }

    override suspend fun reload() {
        // Reload from file/remote source
    }
}
```

**Adapter Responsibilities:**
- Adapts existing configuration objects
- Implements port interface
- Provides runtime configuration override
- Supports configuration reload

---

## Factory Pattern

### PlatformServiceFactory

**Purpose:** Centralized creation of platform-specific adapters.

**Location:** `/shared/src/commonMain/kotlin/com/fivucsas/shared/platform/factory/PlatformServiceFactory.kt`

```kotlin
interface PlatformServiceFactory {
    fun createCameraService(): ICameraService
    fun createLogger(): ILogger
    fun createSecureStorage(): ISecureStorage
    fun createNavigationService(): INavigationService
    fun createDialogService(): IDialogService
    fun createNotificationService(): INotificationService
    fun createConfigurationProvider(): IConfigurationProvider
}

expect fun getCurrentPlatform(): PlatformType
expect fun createPlatformServiceFactory(): PlatformServiceFactory
```

### Platform-Specific Factory (Desktop)

**Location:** `/shared/src/desktopMain/kotlin/com/fivucsas/shared/platform/factory/DesktopPlatformFactory.kt`

```kotlin
actual fun getCurrentPlatform(): PlatformType = PlatformType.DESKTOP

actual fun createPlatformServiceFactory(): PlatformServiceFactory {
    return DesktopPlatformServiceFactory()
}

class DesktopPlatformServiceFactory : PlatformServiceFactory {
    override fun createNavigationService() = DesktopNavigationService()
    override fun createDialogService() = DesktopDialogService()
    override fun createNotificationService() = DesktopNotificationService()
    override fun createConfigurationProvider() = DefaultConfigurationProvider()
    // ...
}
```

### Factory Benefits

✅ **Centralized Creation** - Single place to create adapters
✅ **Platform Abstraction** - Easy to add new platforms (Android, iOS, Web)
✅ **Testability** - Easy to create mock factories for testing
✅ **Open/Closed Principle** - Open for extension, closed for modification

### Factory Usage

```kotlin
// In DI module
val platformModule = module {
    single { createPlatformServiceFactory() }
    single { get<PlatformServiceFactory>().createNavigationService() }
    single { get<PlatformServiceFactory>().createDialogService() }
    single { get<PlatformServiceFactory>().createNotificationService() }
    single { get<PlatformServiceFactory>().createConfigurationProvider() }
}
```

---

## Layer Interactions

### Data Flow: User Action to UI Update

```
┌─────────────────────────────────────────────────────────────┐
│ 1. USER ACTION                                              │
│    User clicks "Load Users" button                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. PRIMARY ADAPTER (Desktop UI)                             │
│    Button(onClick = { viewModel.loadUsers() })              │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. PRESENTATION LAYER (ViewModel)                           │
│    fun loadUsers() {                                        │
│        viewModelScope.launch {                              │
│            getUsersUseCase.execute()                        │
│        }                                                    │
│    }                                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. DOMAIN LAYER (Use Case)                                  │
│    class GetUsersUseCase(                                   │
│        private val userRepository: UserRepository           │
│    ) {                                                      │
│        suspend fun execute(): Result<List<User>> {          │
│            return userRepository.getUsers()                 │
│        }                                                    │
│    }                                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. SECONDARY PORT (Repository Interface)                    │
│    interface UserRepository {                               │
│        suspend fun getUsers(): Result<List<User>>           │
│    }                                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. SECONDARY ADAPTER (Repository Implementation)            │
│    class UserRepositoryImpl(...) : UserRepository {         │
│        override suspend fun getUsers(): Result<List<User>> {│
│            val response = identityApi.getUsers()            │
│            return Result.success(response.map { it.toDomain() })│
│        }                                                    │
│    }                                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. EXTERNAL SYSTEM (API)                                    │
│    GET https://api.fivucsas.com/v1/users                    │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼ (Response flows back up)
┌─────────────────────────────────────────────────────────────┐
│ 8. UI UPDATE                                                │
│    state.value = state.value.copy(users = result)           │
│    (UI automatically updates via StateFlow)                 │
└─────────────────────────────────────────────────────────────┘
```

### Dependency Direction

```
UI Adapter → ViewModel → Use Case → Repository Interface ← Repository Impl → External API
  (View)       (VM)      (Domain)      (Port)              (Adapter)         (DB/API)

Direction of Dependencies:
←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
(All dependencies point INWARD toward the core)
```

**Key Points:**
- Core (Use Cases, Domain Models) has NO dependencies on outer layers
- Outer layers depend on core through interfaces (ports)
- Dependency Inversion Principle in action

---

## Implementation Examples

### Example 1: Adding a New Feature

**Scenario:** Add "Export Users to CSV" feature

**Step 1: Define Use Case (Core)**
```kotlin
// In /shared/src/commonMain/kotlin/com/fivucsas/shared/domain/usecase/admin/
class ExportUsersUseCase(
    private val userRepository: UserRepository,
    private val fileService: IFileService, // New port
    private val notificationService: INotificationService
) {
    suspend fun execute(format: ExportFormat): Result<Unit> {
        return try {
            val users = userRepository.getUsers().getOrThrow()
            val csvData = convertToCSV(users)
            fileService.saveFile("users.csv", csvData)
            notificationService.showSuccess("Users exported successfully!")
            Result.success(Unit)
        } catch (e: Exception) {
            notificationService.showError("Export failed: ${e.message}")
            Result.failure(e)
        }
    }

    private fun convertToCSV(users: List<User>): String {
        // Business logic for CSV conversion
    }
}
```

**Step 2: Define Port (if needed)**
```kotlin
// In /shared/src/commonMain/kotlin/com/fivucsas/shared/platform/
interface IFileService {
    suspend fun saveFile(filename: String, content: String): Result<Unit>
    suspend fun readFile(filename: String): Result<String>
    suspend fun deleteFile(filename: String): Result<Unit>
}
```

**Step 3: Create Adapters**
```kotlin
// Desktop implementation
class DesktopFileService : IFileService {
    override suspend fun saveFile(filename: String, content: String): Result<Unit> {
        return try {
            File(filename).writeText(content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Android implementation
class AndroidFileService(private val context: Context) : IFileService {
    override suspend fun saveFile(filename: String, content: String): Result<Unit> {
        // Android-specific implementation
    }
}
```

**Step 4: Register in DI**
```kotlin
val useCaseModule = module {
    // ...
    factoryOf(::ExportUsersUseCase)
}

val platformModule = module {
    // ...
    single<IFileService> {
        get<PlatformServiceFactory>().createFileService()
    }
}
```

**Step 5: Use in ViewModel**
```kotlin
class AdminViewModel(
    // ...
    private val exportUsersUseCase: ExportUsersUseCase
) : ViewModel() {

    fun exportUsers() {
        viewModelScope.launch {
            exportUsersUseCase.execute(ExportFormat.CSV)
        }
    }
}
```

**Step 6: Trigger from UI**
```kotlin
@Composable
fun AdminDashboard(viewModel: AdminViewModel) {
    Button(onClick = { viewModel.exportUsers() }) {
        Text("Export to CSV")
    }
}
```

**Benefits:**
- ✅ Business logic (CSV conversion) in core
- ✅ Platform-specific file I/O in adapters
- ✅ Easy to test with mock IFileService
- ✅ Works on all platforms

### Example 2: Testing with Hexagonal Architecture

**Mock Port Implementation:**
```kotlin
class MockNavigationService : INavigationService {
    val navigationHistory = mutableListOf<String>()

    override fun navigateTo(route: String, params: Map<String, Any>) {
        navigationHistory.add(route)
    }

    override fun navigateBack(): Boolean {
        if (navigationHistory.isNotEmpty()) {
            navigationHistory.removeLast()
            return true
        }
        return false
    }

    override fun navigateAndClearStack(route: String) {
        navigationHistory.clear()
        navigationHistory.add(route)
    }

    override fun popUpTo(route: String, inclusive: Boolean) {
        // Mock implementation
    }
}
```

**Test Use Case:**
```kotlin
@Test
fun `should navigate to dashboard on successful login`() = runTest {
    // Arrange
    val mockAuthRepo = MockAuthRepository().apply {
        shouldSucceed = true
    }
    val mockNavigation = MockNavigationService()

    val useCase = LoginUseCase(mockAuthRepo, mockNavigation)

    // Act
    useCase.execute("test@test.com", "password")

    // Assert
    assertTrue(mockNavigation.navigationHistory.contains("dashboard"))
}
```

**Benefits:**
- ✅ No need for UI framework
- ✅ Fast test execution
- ✅ Easy to control behavior
- ✅ Test business logic in isolation

---

## Testing Strategy

### Test Pyramid with Hexagonal Architecture

```
         ┌─────────────────┐
         │   E2E Tests     │  5%  (Full app, real adapters)
         └─────────────────┘
        ┌───────────────────┐
        │ Integration Tests │  20% (Use cases + real adapters)
        └───────────────────┘
       ┌─────────────────────┐
       │  Use Case Tests     │  30% (Use cases + mock ports)
       └─────────────────────┘
      ┌───────────────────────┐
      │   Domain Tests        │  45% (Models, validation, rules)
      └───────────────────────┘
```

### Testing Layers

#### 1. Domain Layer Tests (45%)

**What to Test:**
- Domain models
- Validation rules
- Business rules
- Exception handling

**Example:**
```kotlin
@Test
fun `should validate Turkish national ID correctly`() {
    val result = ValidationRules.validateTurkishNationalId("12345678901")
    assertTrue(result is ValidationResult.Error)

    val validResult = ValidationRules.validateTurkishNationalId("12345678900")
    assertTrue(validResult is ValidationResult.Success)
}
```

#### 2. Use Case Tests (30%)

**What to Test:**
- Use case logic
- Port interactions
- Error handling

**Example:**
```kotlin
@Test
fun `should enroll face and show success notification`() = runTest {
    // Arrange
    val mockBiometricRepo = MockBiometricRepository().apply {
        shouldSucceed = true
    }
    val mockNotification = MockNotificationService()

    val useCase = EnrollFaceUseCase(mockBiometricRepo, mockNotification)

    // Act
    useCase.execute("user123", byteArrayOf())

    // Assert
    assertEquals(1, mockNotification.successMessages.size)
    assertEquals("Face enrolled successfully!", mockNotification.successMessages.first())
}
```

#### 3. Integration Tests (20%)

**What to Test:**
- Use cases with real adapters
- Data flow through layers
- Error propagation

**Example:**
```kotlin
@Test
fun `should retrieve users from API and update state`() = runTest {
    // Arrange
    val realApiClient = createTestApiClient()
    val userRepository = UserRepositoryImpl(realApiClient, mockLogger)
    val useCase = GetUsersUseCase(userRepository)

    // Act
    val result = useCase.execute()

    // Assert
    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()!!.isNotEmpty())
}
```

#### 4. E2E Tests (5%)

**What to Test:**
- Complete user flows
- Real UI interactions
- All layers integrated

**Example:**
```kotlin
@Test
fun `should complete enrollment flow`() {
    // Arrange
    composeTestRule.setContent { App() }

    // Act
    composeTestRule.onNodeWithText("Enroll").performClick()
    composeTestRule.onNodeWithTag("camera_preview").assertIsDisplayed()
    composeTestRule.onNodeWithText("Capture").performClick()

    // Assert
    composeTestRule.onNodeWithText("Enrollment successful").assertIsDisplayed()
}
```

### Mock Strategy

**✅ DO:**
- Mock ports (interfaces)
- Use fakes for complex behavior
- Verify interactions with mocks

**❌ DON'T:**
- Mock domain models
- Mock value objects
- Mock implementation details

---

## Best Practices

### 1. Keep Core Pure

**✅ DO:**
```kotlin
// Pure business logic in core
class ValidateUserUseCase {
    fun execute(user: User): ValidationResult {
        if (user.email.isEmpty()) {
            return ValidationResult.Error("Email is required")
        }
        return ValidationResult.Success
    }
}
```

**❌ DON'T:**
```kotlin
// Core depending on Android framework
class ValidateUserUseCase(
    private val context: Context // ❌ Android dependency
) {
    fun execute(user: User): ValidationResult {
        val errorMessage = context.getString(R.string.email_required) // ❌
        // ...
    }
}
```

### 2. Use Ports for External Interactions

**✅ DO:**
```kotlin
class EnrollFaceUseCase(
    private val cameraService: ICameraService, // ✅ Port
    private val biometricRepo: BiometricRepository // ✅ Port
) {
    suspend fun execute() {
        val imageBytes = cameraService.captureImage().getOrThrow()
        biometricRepo.enrollFace("user123", imageBytes)
    }
}
```

**❌ DON'T:**
```kotlin
class EnrollFaceUseCase(
    private val cameraX: CameraXImpl, // ❌ Concrete Android class
    private val database: RoomDatabase // ❌ Concrete database
) {
    // ...
}
```

### 3. Keep Adapters Thin

**✅ DO:**
```kotlin
class UserRepositoryImpl(
    private val apiClient: IdentityApi
) : UserRepository {
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val dtos = apiClient.getUsers()
            Result.success(dtos.map { it.toDomain() }) // ✅ Simple transformation
        } catch (e: Exception) {
            Result.failure(e.toAppException())
        }
    }
}
```

**❌ DON'T:**
```kotlin
class UserRepositoryImpl(
    private val apiClient: IdentityApi
) : UserRepository {
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val dtos = apiClient.getUsers()

            // ❌ Business logic in adapter
            val filteredUsers = dtos.filter { it.isActive }
            val sortedUsers = filteredUsers.sortedBy { it.name }

            Result.success(sortedUsers.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e.toAppException())
        }
    }
}
```

**Instead, put business logic in use case:**
```kotlin
class GetActiveUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend fun execute(): Result<List<User>> {
        return userRepository.getUsers()
            .map { users ->
                users.filter { it.isActive } // ✅ Business logic in core
                     .sortedBy { it.name }
            }
    }
}
```

### 4. Use Factory Pattern for Adapters

**✅ DO:**
```kotlin
val platformModule = module {
    single { createPlatformServiceFactory() }
    single { get<PlatformServiceFactory>().createCameraService() }
}
```

**❌ DON'T:**
```kotlin
val platformModule = module {
    single<ICameraService> { DesktopCameraServiceImpl() } // ❌ Platform-specific
}
```

### 5. Design Ports Before Implementation

**Process:**
1. Identify external interaction
2. Define port interface
3. Implement adapter
4. Test with mock

**Example:**
```kotlin
// 1. Identify: Need to send push notifications

// 2. Define port
interface IPushNotificationService {
    suspend fun sendNotification(userId: String, message: String): Result<Unit>
}

// 3. Implement adapter
class FirebasePushNotificationService : IPushNotificationService {
    override suspend fun sendNotification(userId: String, message: String): Result<Unit> {
        // Firebase implementation
    }
}

// 4. Test with mock
class MockPushNotificationService : IPushNotificationService {
    val sentNotifications = mutableListOf<Pair<String, String>>()

    override suspend fun sendNotification(userId: String, message: String): Result<Unit> {
        sentNotifications.add(userId to message)
        return Result.success(Unit)
    }
}
```

---

## Conclusion

### Benefits Achieved

✅ **Platform Independence** - Core logic works everywhere
✅ **Testability** - Easy to test with mocks
✅ **Flexibility** - Swap implementations easily
✅ **Maintainability** - Clear separation of concerns
✅ **SOLID Compliance** - All principles followed
✅ **Extensibility** - Easy to add new features

### Architecture Grade

**Before Hexagonal Ports:** B+
**After Hexagonal Ports:** A++

### Next Steps

1. ✅ Implement remaining platform factories (Android, iOS)
2. ⏳ Increase test coverage using mock ports
3. ⏳ Add more integration tests
4. ⏳ Document adapter implementation guidelines

---

**Document Version:** 1.0.0
**Last Updated:** 2026-01-19
**Next Review:** After platform adapter implementations
