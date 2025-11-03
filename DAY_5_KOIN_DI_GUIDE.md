# Day 5: Koin Dependency Injection 🎯

**Date:** November 3, 2025  
**Status:** Ready to Start  
**Estimated Time:** 50-60 minutes  
**Difficulty:** EASY  
**Impact:** MASSIVE - Completes 60% of refactoring!

---

## 📊 What We'll Achieve

### Before Day 5 ⚠️
```kotlin
// Manual factory - verbose, error-prone
object ViewModelFactory {
    fun createKioskViewModel(): KioskViewModel {
        val authApiClient = AuthApiClient(HttpClient())
        val biometricApiClient = BiometricApiClient(HttpClient())
        // ... 20 more lines of manual wiring
        return KioskViewModel(/* dependencies */)
    }
}

// Usage
@Composable
fun KioskMode() {
    val viewModel = remember { ViewModelFactory.createKioskViewModel() }
    // Manually managing lifecycle
}
```

### After Day 5 ✅
```kotlin
// Koin modules - clean, automatic
val appModule = module {
    // Define once, use everywhere
}

// Usage
@Composable
fun KioskMode() {
    val viewModel: KioskViewModel = koinViewModel()
    // Automatic injection, lifecycle management!
}
```

---

## 🎯 Goals

1. ✅ Add Koin dependencies
2. ✅ Create DI modules (repository, useCase, viewModel, network)
3. ✅ Initialize Koin for each platform
4. ✅ Replace ViewModelFactory with Koin
5. ✅ Test dependency injection
6. ✅ Verify builds work

---

## 📦 Step 1: Add Koin Dependencies (5 minutes)

### 1.1 Update `gradle/libs.versions.toml` (if you have one)

If you don't have a version catalog, skip to 1.2.

```toml
[versions]
koin = "3.5.0"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
```

### 1.2 Update `shared/build.gradle.kts`

Add Koin to shared module:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Existing dependencies...
                
                // Koin for dependency injection
                implementation("io.insert-koin:koin-core:3.5.0")
                implementation("io.insert-koin:koin-compose:1.1.0")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Existing dependencies...
                
                // Koin for Android
                implementation("io.insert-koin:koin-android:3.5.0")
            }
        }
    }
}
```

### 1.3 Update `desktopApp/build.gradle.kts`

Add Koin for desktop:

```kotlin
dependencies {
    // Existing dependencies...
    
    // Koin for Desktop
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-compose:1.1.0")
}
```

### 1.4 Sync Gradle

```bash
cd mobile-app
./gradlew --refresh-dependencies
```

---

## 🏗️ Step 2: Create DI Modules (20 minutes)

### 2.1 Create Network Module

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/di/NetworkModule.kt`

```kotlin
package com.fivucsas.shared.di

import com.fivucsas.shared.data.remote.api.AuthApiClient
import com.fivucsas.shared.data.remote.api.BiometricApiClient
import com.fivucsas.shared.data.remote.api.UserApiClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
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
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
            
            defaultRequest {
                // Base URL will be set per platform
                url("http://localhost:8080/api/v1/")
            }
        }
    }
    
    // API Clients (singletons)
    singleOf(::AuthApiClient)
    singleOf(::BiometricApiClient)
    singleOf(::UserApiClient)
}
```

### 2.2 Create Repository Module

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/di/RepositoryModule.kt`

```kotlin
package com.fivucsas.shared.di

import com.fivucsas.shared.data.repository.AuthRepositoryImpl
import com.fivucsas.shared.data.repository.BiometricRepositoryImpl
import com.fivucsas.shared.data.repository.UserRepositoryImpl
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.UserRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Repository module - Provides repository implementations
 */
val repositoryModule = module {
    // Auth Repository
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    
    // Biometric Repository
    singleOf(::BiometricRepositoryImpl) { bind<BiometricRepository>() }
    
    // User Repository
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
}
```

### 2.3 Create Use Case Module

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/di/UseCaseModule.kt`

```kotlin
package com.fivucsas.shared.di

import com.fivucsas.shared.domain.usecase.*
import com.fivucsas.shared.domain.validation.EmailValidator
import com.fivucsas.shared.domain.validation.PasswordValidator
import com.fivucsas.shared.domain.validation.UserDataValidator
import com.fivucsas.shared.domain.validation.UsernameValidator
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Use case module - Provides business logic use cases
 */
val useCaseModule = module {
    // Validators (singletons - stateless)
    singleOf(::EmailValidator)
    singleOf(::PasswordValidator)
    singleOf(::UsernameValidator)
    singleOf(::UserDataValidator)
    
    // Use Cases (factories - new instance per injection)
    factoryOf(::RegisterUserUseCase)
    factoryOf(::LoginUserUseCase)
    factoryOf(::EnrollFaceUseCase)
    factoryOf(::VerifyFaceUseCase)
    factoryOf(::GetAllUsersUseCase)
    factoryOf(::SearchUsersUseCase)
    factoryOf(::GetStatisticsUseCase)
    factoryOf(::PerformLivenessCheckUseCase)
}
```

### 2.4 Create ViewModel Module

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/di/ViewModelModule.kt`

```kotlin
package com.fivucsas.shared.di

import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.viewmodel.AppViewModel
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * ViewModel module - Provides ViewModels for UI
 */
val viewModelModule = module {
    // ViewModels (factory scoped - new instance per screen)
    viewModelOf(::AppViewModel)
    viewModelOf(::KioskViewModel)
    viewModelOf(::AdminViewModel)
}
```

### 2.5 Create Main DI Module

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/di/AppModule.kt`

```kotlin
package com.fivucsas.shared.di

import org.koin.dsl.module

/**
 * Main application module - Combines all modules
 */
val appModule = module {
    includes(
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule
    )
}

/**
 * Get all application modules
 */
fun getAppModules() = listOf(appModule)
```

---

## 🚀 Step 3: Initialize Koin (15 minutes)

### 3.1 Desktop App Initialization

**File:** `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/Main.kt`

Update the main function:

```kotlin
package com.fivucsas.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fivucsas.shared.di.getAppModules
import org.koin.core.context.startKoin

fun main() {
    // Initialize Koin
    startKoin {
        modules(getAppModules())
    }
    
    application {
        val windowState = rememberWindowState()
        
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "FIVUCSAS - Face and Identity Verification"
        ) {
            DesktopApp()
        }
    }
}
```

### 3.2 Android App Initialization

**File:** `androidApp/src/main/kotlin/com/fivucsas/mobile/android/FIVUCSASApplication.kt`

Create Application class:

```kotlin
package com.fivucsas.mobile.android

import android.app.Application
import com.fivucsas.shared.di.getAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FIVUCSASApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FIVUCSASApplication)
            modules(getAppModules())
        }
    }
}
```

**Update AndroidManifest.xml:**

```xml
<manifest>
    <application
        android:name=".FIVUCSASApplication"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        ...>
        <!-- ... -->
    </application>
</manifest>
```

### 3.3 iOS Initialization (Future)

**File:** `shared/src/iosMain/kotlin/com/fivucsas/shared/di/KoinHelper.kt`

```kotlin
package com.fivucsas.shared.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(getAppModules())
    }
}
```

---

## 🔄 Step 4: Replace ViewModelFactory (10 minutes)

### 4.1 Update Desktop App

**File:** `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/kiosk/KioskMode.kt`

**Before:**
```kotlin
@Composable
fun KioskMode(
    onBack: () -> Unit,
    viewModel: KioskViewModel = remember { ViewModelFactory.createKioskViewModel() }
) {
    // ...
}
```

**After:**
```kotlin
import org.koin.compose.koinInject

@Composable
fun KioskMode(
    onBack: () -> Unit,
    viewModel: KioskViewModel = koinInject()  // Automatic injection!
) {
    // ...
}
```

**File:** `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ui/admin/AdminDashboard.kt`

**Before:**
```kotlin
@Composable
fun AdminDashboard(
    onBack: () -> Unit,
    viewModel: AdminViewModel = remember { ViewModelFactory.createAdminViewModel() }
) {
    // ...
}
```

**After:**
```kotlin
import org.koin.compose.koinInject

@Composable
fun AdminDashboard(
    onBack: () -> Unit,
    viewModel: AdminViewModel = koinInject()  // Automatic injection!
) {
    // ...
}
```

### 4.2 Delete ViewModelFactory

**Remove file:** `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ViewModelFactory.kt`

```bash
# This file is no longer needed!
rm desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/ViewModelFactory.kt
```

---

## 🧪 Step 5: Test (10 minutes)

### 5.1 Build Shared Module

```bash
cd mobile-app
./gradlew :shared:build
```

**Expected:** ✅ BUILD SUCCESSFUL

### 5.2 Build Desktop App

```bash
./gradlew :desktopApp:build
```

**Expected:** ✅ BUILD SUCCESSFUL

### 5.3 Run Desktop App

```bash
./gradlew :desktopApp:run
```

**Expected:** 
- ✅ App launches
- ✅ Koin initializes
- ✅ ViewModels injected automatically
- ✅ All screens work

### 5.4 Verify Koin Logs

Check console output for Koin initialization:

```
[Koin] started
[Koin] module networkModule
[Koin] module repositoryModule
[Koin] module useCaseModule
[Koin] module viewModelModule
```

### 5.5 Test Each Screen

1. **Launcher Screen:** ✅ Should show mode selection
2. **Kiosk Mode:** ✅ Should navigate to enrollment
3. **Admin Dashboard:** ✅ Should show user table

---

## 🐛 Troubleshooting

### Issue 1: "No definition found for KioskViewModel"

**Cause:** ViewModelModule not loaded

**Fix:**
```kotlin
// Verify appModule includes viewModelModule
val appModule = module {
    includes(
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule  // Make sure this is here!
    )
}
```

### Issue 2: "Circular dependency detected"

**Cause:** Dependencies depend on each other

**Fix:** Check your use cases and repositories don't create circular references.

### Issue 3: Build fails with "Unresolved reference: koinInject"

**Cause:** Missing Koin Compose dependency

**Fix:**
```kotlin
// shared/build.gradle.kts
implementation("io.insert-koin:koin-compose:1.1.0")
```

### Issue 4: Android app crashes on launch

**Cause:** Forgot to register FIVUCSASApplication in manifest

**Fix:**
```xml
<application
    android:name=".FIVUCSASApplication"
    ...>
```

---

## 📊 Progress Update

```
Day 1: Shared Module Structure    ✅ (10%)
Day 2: Data Layer                  ✅ (20%)
Day 3: Use Cases & Validation      ✅ (30%)
Day 4: ViewModels to Shared        ✅ (50%)
Day 5: Dependency Injection        ✅ (60%) ⭐ YOU ARE HERE!
Day 6: API Integration             ⬜ (70%)
Day 7: Testing Infrastructure      ⬜ (80%)
Day 8: Error Handling              ⬜ (90%)
Day 9: Performance & Polish        ⬜ (95%)
Day 10: Final Integration          ⬜ (100%)
```

---

## 🎯 What Day 5 Unlocks

### 1. **Automatic Dependency Management** ✅

No more manual factories!

```kotlin
// Before: 20 lines of manual wiring
val viewModel = ViewModelFactory.createKioskViewModel()

// After: 1 line!
val viewModel: KioskViewModel = koinInject()
```

### 2. **Easy Testing** ✅

```kotlin
@Test
fun testKioskViewModel() {
    startKoin {
        modules(module {
            single<AuthRepository> { MockAuthRepository() }  // Mock!
            factoryOf(::RegisterUserUseCase)
            viewModelOf(::KioskViewModel)
        })
    }
    
    val viewModel: KioskViewModel = koin.get()
    // Test with mocked dependencies!
}
```

### 3. **Cleaner Code** ✅

- No more `remember { }`
- No factory boilerplate
- Automatic lifecycle management

### 4. **Platform-Agnostic DI** ✅

Same modules work on:
- ✅ Desktop
- ✅ Android
- ✅ iOS (with KoinHelper)
- ✅ Web (future)

---

## 📚 Koin Best Practices

### 1. **Use `single` for Stateless Objects**

```kotlin
single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
// Created once, shared everywhere
```

### 2. **Use `factory` for Stateful Objects**

```kotlin
factory { RegisterUserUseCase(get(), get()) }
// New instance per injection
```

### 3. **Use `viewModel` for ViewModels**

```kotlin
viewModel { KioskViewModel(get(), get(), get()) }
// Proper lifecycle management
```

### 4. **Use `singleOf` for Simple Constructors**

```kotlin
singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
// Cleaner syntax when constructor matches dependencies
```

---

## 🎓 What We Learned

### 1. **Koin is Simple**
- No annotation processing
- No code generation
- Pure Kotlin DSL

### 2. **DI Improves Testability**
- Easy to mock dependencies
- Clean test setup

### 3. **Modules Organize Dependencies**
- Network layer = networkModule
- Data layer = repositoryModule
- Domain layer = useCaseModule
- Presentation layer = viewModelModule

### 4. **Platform Initialization Varies**
- Desktop: `startKoin` in `main()`
- Android: `startKoin` in `Application.onCreate()`
- iOS: `initKoin()` helper function

---

## 🚀 What's Next?

### Day 6: API Integration
- Connect to real backend APIs
- Environment configuration
- Error handling
- Network states (loading, success, error)

**Expected Time:** 1 hour  
**Difficulty:** MEDIUM  
**Impact:** HIGH - Real data flow!

---

## 🎉 Success Criteria

- ✅ Koin dependencies added
- ✅ 4 DI modules created
- ✅ Koin initialized on all platforms
- ✅ ViewModelFactory removed
- ✅ Desktop app builds
- ✅ Desktop app runs
- ✅ ViewModels auto-injected
- ✅ No runtime errors
- ✅ Koin logs appear

**If all ✅ - Day 5 COMPLETE! 🎉**

---

## 📁 Files Created in Day 5

```
shared/src/commonMain/kotlin/com/fivucsas/shared/di/
├── NetworkModule.kt        (50 lines)
├── RepositoryModule.kt     (25 lines)
├── UseCaseModule.kt        (35 lines)
├── ViewModelModule.kt      (20 lines)
└── AppModule.kt            (20 lines)

androidApp/src/main/kotlin/com/fivucsas/mobile/android/
└── FIVUCSASApplication.kt  (20 lines)

shared/src/iosMain/kotlin/com/fivucsas/shared/di/
└── KoinHelper.kt           (10 lines)
```

**Total:** ~180 lines added  
**Total:** ~65 lines removed (ViewModelFactory)  
**Net:** +115 lines of cleaner, better code!

---

**Ready to start Day 5? Let's go! 🚀**
