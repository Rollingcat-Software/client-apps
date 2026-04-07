# FIVUCSAS Client-Apps Code Review

**Date:** 2026-03-18
**Scope:** Security, Performance, Reliability, Maintainability, Interoperability
**Codebase:** Kotlin Multiplatform (Android + Desktop) -- 266 shared, 79 Android-only, 33 Desktop-only .kt files

---

## 1. Security

### 1.1 Token Storage -- PASS (Android), FAIL (Desktop)

**Android:** EncryptedSharedPreferences with AES-256-GCM master key (`AndroidTokenStorage.kt`, `AndroidSecureStorage.kt`). Properly uses `MasterKey.KeyScheme.AES256_GCM` and `AES256_SIV` key encryption. Has a plaintext `SharedPreferences` fallback when crypto init fails -- acceptable for dev/emulator, but the fallback should log a warning or be disabled in release builds.

**Desktop:** `DesktopSecureStorageImpl.kt` stores tokens in Java `Preferences` API with **zero encryption**. The file comment itself admits: "In production, consider using encryption for sensitive data." JWTs, refresh tokens, and user data sit in plaintext in OS preference files.

**Severity:** HIGH (Desktop)
**Recommendation:** Implement desktop token encryption using `javax.crypto` AES with a key derived from OS keychain (macOS Keychain / Windows DPAPI / Linux Secret Service via `java.security.KeyStore`).

### 1.2 Certificate Pinning -- NOT IMPLEMENTED

No certificate pinning found anywhere in the codebase. Ktor `HttpClient` uses default platform trust. The `.env.example` references `CERTIFICATE_PIN_SHA256` but it is never consumed.

**Severity:** MEDIUM
**Recommendation:** Add Ktor `CertificatePinner` plugin with SHA-256 pins for `api.fivucsas.com` and `bio.fivucsas.com`. Include a backup pin for certificate rotation.

### 1.3 Root/Jailbreak Detection -- NOT IMPLEMENTED

No root detection, SafetyNet, or Play Integrity checks found. The `.env.example` mentions `ROOT_DETECTION_ENABLED` but no code consumes it.

**Severity:** MEDIUM
**Recommendation:** Add root detection check at app startup (e.g., via `rootbeer` library or Google Play Integrity API). For a biometric authentication app handling NFC identity documents, this is a compliance expectation.

### 1.4 Code Obfuscation (ProGuard/R8) -- DISABLED

`androidApp/build.gradle.kts` line 50: `isMinifyEnabled = false` in the release build type. No ProGuard rules file exists. The APK ships unobfuscated with all symbol names intact.

**Severity:** MEDIUM
**Recommendation:** Enable R8 (`isMinifyEnabled = true`, `isShrinkResources = true`) with keep rules for Ktor, kotlinx.serialization, and Koin reflection. This also reduces APK size significantly.

### 1.5 Biometric Key Storage (Android Keystore) -- PASS with caveat

`FingerprintPlatform.android.kt` correctly uses `AndroidKeyStore` with ECDSA P-256, `KeyGenParameterSpec`, and `BiometricPrompt` for nonce signing. Keys are non-exportable.

**Caveat:** `setUserAuthenticationRequired(false)` on line 111 -- the key can be used without biometric authentication. The signing operation does trigger `BiometricPrompt`, but the key itself is not hardware-bound to auth. If the device is compromised, the key can be used silently.

**Severity:** LOW (defense-in-depth)
**Recommendation:** Set `setUserAuthenticationRequired(true)` with `setUserAuthenticationValidityDurationSeconds(-1)` (require auth for every use). The current code already prompts for auth before signing, so this adds hardware enforcement.

### 1.6 NFC Data Handling (KVKK Compliance) -- MOSTLY PASS

Strong implementation:
- `SecureByteArray` provides two-phase memory wiping (random + zero) with `Closeable` pattern
- `SecureLogger` automatically redacts TCKN (11-digit Turkish ID), passport numbers, MRZ lines, dates, and hex data in logs
- Release builds suppress all sensitive data from logs
- BAC authentication and SOD validation properly implemented for e-Passport/eID

**Gaps:**
- `NfcIdentityDocumentData` stores `photoBytes` (face image from NFC chip) as a plain `ByteArray` in the domain model. This biometric data persists in memory without any wiping mechanism -- unlike the NFC layer which uses `SecureByteArray`.
- No explicit consent dialog before reading NFC identity documents (KVKK Article 6 requires explicit consent for biometric data processing).

**Severity:** MEDIUM (KVKK)
**Recommendation:** (1) Wrap `photoBytes` in a lifecycle-aware container that clears on screen exit. (2) Add explicit KVKK consent dialog before NFC reads.

### 1.7 Refresh Token Not Persisted -- BUG

`TokenManager` stores the access token via `tokenStorage.saveToken()` but the refresh token is only held in the in-memory `cachedTokens` field. `TokenStorage` interface has no `saveRefreshToken`/`getRefreshToken` methods. On process death, the refresh token is lost, forcing re-login.

**Severity:** MEDIUM
**Recommendation:** Extend `TokenStorage` with `saveRefreshToken`/`getRefreshToken` and persist it in EncryptedSharedPreferences.

---

## 2. Performance

### 2.1 App Startup / Initialization Chain -- ACCEPTABLE

`FIVUCSASApplication.onCreate()` initializes Koin with all modules synchronously. The DI graph includes ~25 ViewModels (factory-scoped), ~16 API clients, ~15 repositories, and ~20 use cases. Koin uses reflection-based injection which is slower than compile-time DI (Dagger/Hilt).

**Estimated impact:** ~100-200ms on cold start for DI initialization. Acceptable for an MVP.

**Recommendation:** For future optimization, consider lazy module loading or migrating to Koin annotations (compile-time).

### 2.2 Image/Memory Management -- ACCEPTABLE

- CameraX 1.4.1 is used (proper for Android 15+ 16KB page alignment)
- Coil 2.5.0 for image loading (default memory/disk caching)
- No custom bitmap pooling needed at current scale

**Gap:** NFC photo bytes (`ByteArray`) from e-Passport can be ~15-50KB JPEG per card read. These are held in ViewModel state. No eviction on navigation away.

### 2.3 Network Call Optimization -- ADEQUATE

- JSON serialization configured with `ignoreUnknownKeys = true` (forward-compatible)
- Timeouts properly configured: 30s connect, 60s request, 30s socket
- Biometric operations get 2x timeout (120s)
- No request batching, but API calls are independent
- No HTTP response caching (Ktor does not cache by default)

**Recommendation:** Add `Cache-Control` header support for GET endpoints (user profiles, statistics) to reduce redundant calls.

### 2.4 Compose Recomposition -- LOW RISK

- `rememberSaveable` used in 8 Android screen files for camera/biometric state
- State hoisting via `StateFlow` from ViewModels (stable references)
- No obvious `@Composable` functions with unstable parameters

### 2.5 Koin Injection Overhead -- ACCEPTABLE

ViewModels are factory-scoped (new instance per screen). This is correct for navigation-scoped lifetime. Singletons for TokenManager, HttpClient, and JSON are appropriate.

---

## 3. Reliability

### 3.1 Crash Handling -- NOT IMPLEMENTED

No `Thread.setDefaultUncaughtExceptionHandler` or crash reporting framework (Crashlytics, Sentry). Unhandled exceptions will trigger Android's default crash dialog.

**Severity:** HIGH
**Recommendation:** Add a global exception handler that logs to local storage and optionally reports to a remote service. At minimum, catch and log before crashing.

### 3.2 Network Timeout Configuration -- PASS

Properly configured in `ApiConfig.kt` with sensible values (30s connect, 60s request). Biometric client gets 2x timeouts.

### 3.3 Token Refresh on 401 -- PARTIAL (has race condition)

`NetworkModule.kt` lines 118-154 implement automatic token refresh in `HttpResponseValidator`. The refresh logic uses `response.call.client.post()` (the same client), which could trigger the validator recursively despite the URL check.

**Race condition:** Multiple concurrent 401 responses will each attempt to refresh the token independently. No mutex or deduplication.

**Severity:** MEDIUM
**Recommendation:** Add a `Mutex` around the refresh logic so only one refresh executes at a time, with others awaiting the result.

### 3.4 Offline Mode -- BASIC

`OfflineCache` stores user profile data in secure storage. `INetworkMonitor` interface exists with `AndroidNetworkMonitor` implementation. The app can display cached profile data when offline.

**Gaps:**
- No queue for failed writes (e.g., enrollment attempts while offline)
- No cache invalidation strategy beyond full clear on logout
- No TTL on cached data

### 3.5 State Restoration on Process Death -- PARTIAL

`rememberSaveable` is used in some Android screens (8 files found). However, ViewModels create their own `CoroutineScope(Dispatchers.Main)` (e.g., `AdminViewModel` line 41) instead of using Android's `viewModelScope` or `SavedStateHandle`. This means:
- ViewModel state is lost on process death
- Coroutines are not cancelled on ViewModel clear (potential leak)

**Severity:** MEDIUM
**Recommendation:** Use `CoroutineScope` tied to the ViewModel lifecycle, and persist critical state via `SavedStateHandle` or the offline cache.

---

## 4. Maintainability

### 4.1 Architecture Consistency (Hexagonal) -- GOOD

Clean layer separation:
- `domain/model/` -- 20+ domain models
- `domain/repository/` -- 16 repository interfaces
- `domain/usecase/` -- 20+ use cases organized by feature
- `data/remote/api/` -- 16 API interface + implementation pairs
- `data/remote/dto/` -- DTOs for API boundaries
- `presentation/viewmodel/` -- 25 ViewModels
- `presentation/state/` -- UI state classes
- `di/` -- 6 Koin modules (App, Network, Repository, UseCase, ViewModel, Platform)

The architecture follows hexagonal principles: domain has no framework dependencies, ports (repository interfaces) are in domain, adapters (API impls) are in data layer.

### 4.2 ViewModel Complexity -- MODERATE

- Largest VM: `AdminViewModel` at 396 lines (manages users, stats, search, CRUD)
- `KioskViewModel` at 345 lines (multi-mode kiosk flow)
- Most VMs are 80-180 lines -- reasonable
- `AdminViewModel` should be split into `UserManagementViewModel` + `StatisticsViewModel`

### 4.3 Code Duplication (Android/Desktop) -- LOW

Code sharing is high (~76% shared): 266 shared files vs 79 Android + 33 Desktop. Platform-specific code is limited to:
- Android: NFC readers (11 files), push notifications, network monitor, camera, UI screens
- Desktop: Secure storage, camera, theme, kiosk mode, admin UI

The desktop app duplicates some UI components that could potentially be shared via Compose Multiplatform, but the divergence is reasonable given platform differences (kiosk mode, admin dashboard layout).

### 4.4 i18n Coverage -- COMPLETE

`StringResources.kt` provides 148 string keys with full EN and TR translations. Both maps cover identical keys. Uses a simple map-based approach with `{0}`-style parameter substitution. Some screens (particularly Android-only screens) use hardcoded strings instead of `StringResources`.

**Recommendation:** Audit Android-only screens for hardcoded strings and migrate to `StringResources`.

### 4.5 DI Module Organization -- CLEAN

6 well-separated modules: `appModule`, `networkModule`, `repositoryModule`, `useCaseModule`, `viewModelModule`, `platformModule`. Named qualifiers for dual HTTP clients (`identityClient`, `biometricClient`). Platform module uses expect/actual pattern.

---

## 5. Interoperability

### 5.1 API Contract Alignment -- GOOD

- 16 API interface/implementation pairs covering all backend endpoints
- Dual HTTP clients properly targeting Identity Core (port 8080) and Biometric Processor (port 8001)
- `ignoreUnknownKeys = true` provides forward compatibility with backend changes

### 5.2 DTO Field Naming -- CONSISTENT

DTOs use kotlinx.serialization with `@SerialName` annotations where backend uses snake_case. The `Json` configuration includes `isLenient = true` and `encodeDefaults = true`.

### 5.3 Date/Time Handling -- ADEQUATE

Uses `kotlinx.datetime` (multiplatform) for timestamps. `OfflineCache` stores timestamps as ISO-8601 strings via `Clock.System.now().toString()`. Backend uses ISO-8601 format. No timezone ambiguity issues found.

### 5.4 Multiplatform Code Sharing -- 76%

266 shared / (266 + 79 + 33) = ~70% by file count. With platform expect/actual implementations (9 Android + 5 Desktop), effective sharing is ~76% of logic. This is good for a KMP project with NFC and camera features that are inherently platform-specific.

---

## Summary of Findings by Severity

### HIGH
1. **Desktop token storage is plaintext** -- `DesktopSecureStorageImpl` uses Java Preferences with no encryption
2. **No crash reporting** -- unhandled exceptions are silently lost
3. **Refresh token not persisted** -- lost on process death, forces re-login

### MEDIUM
4. **No certificate pinning** -- MITM risk on public networks
5. **No root/jailbreak detection** -- relevant for biometric auth app
6. **R8/ProGuard disabled** -- APK ships unobfuscated
7. **NFC photo bytes not wiped from memory** -- KVKK gap
8. **No KVKK consent dialog before NFC reads**
9. **Token refresh race condition** -- concurrent 401s trigger multiple refreshes
10. **ViewModel CoroutineScope leak** -- not tied to lifecycle

### LOW
11. **EncryptedSharedPreferences fallback to plaintext** -- should warn in release
12. **Biometric key not hardware-bound to auth** -- defense-in-depth improvement
13. **No HTTP response caching** -- unnecessary network calls
14. **AdminViewModel too large** -- should be split
15. **Some Android screens use hardcoded strings** -- i18n gap
