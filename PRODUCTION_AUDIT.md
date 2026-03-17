# FIVUCSAS Client-Apps Production Readiness Audit

**Date**: 2026-03-17
**Auditor**: Claude Opus 4.6
**Scope**: Kotlin Multiplatform client-apps (Android, Desktop, shared module)
**Verdict**: **NOT production-ready** -- significant issues in build configuration, mock data in production code, missing i18n, and incomplete feature parity with web-app.

---

## 1. Build Configuration Issues

### AGP Version Conflict (P0)
The root `build.gradle.kts` declares two conflicting Android Gradle Plugin versions:
- `classpath("com.android.tools.build:gradle:8.9.2")` (buildscript)
- `id("com.android.application").version("8.2.2")` (plugins block)

This will cause build failures or unpredictable behavior depending on Gradle resolution order.

### CameraX Version Mismatch (P1)
- `shared/build.gradle.kts` androidMain: CameraX **1.3.1**
- `androidApp/build.gradle.kts`: CameraX **1.4.1**

Runtime classpath conflicts are likely. All CameraX artifacts must use the same version.

### Koin Version Mismatch (P1)
- `shared/build.gradle.kts`: `koin-core:3.5.0`
- `androidApp/build.gradle.kts`: `koin-android:3.5.3`, `koin-androidx-compose:3.5.3`

Minor but can cause subtle DI issues.

### ProGuard/R8 Disabled (P2)
`isMinifyEnabled = false` in release build. For production APK, this means:
- Larger APK size
- No code obfuscation
- Ktor serialization may work fine without R8 rules, but the APK ships all unused code

### Cleartext Traffic Enabled (P1)
`android:usesCleartextTraffic="true"` in AndroidManifest.xml. This is a security risk for production -- allows HTTP traffic and will fail Google Play review.

### Camera Required (P2)
`android:required="true"` for camera hardware feature. This prevents installation on devices without cameras. Should be `false` with runtime checks.

---

## 2. Feature Comparison: Web-App vs Client-Apps

| Feature | Web-App | Android | Desktop | Notes |
|---------|---------|---------|---------|-------|
| **Login** | Yes | Yes | Yes | Shared LoginScreen |
| **Register** | Yes | Yes | Yes | Shared RegisterScreen |
| **Forgot Password** | Yes | Yes (UI only) | Yes (UI only) | No reset-password API call |
| **User List (CRUD)** | Yes | Yes | Yes | Admin screens |
| **User Create** | Yes | Yes | Yes | AddUserDialog |
| **User Edit** | Yes | Yes | Yes | EditUserDialog |
| **User Delete** | Yes | Yes | Yes | DeleteUserDialog |
| **User Detail Page** | Yes | Partial | Partial | No dedicated detail view |
| **Tenant Management** | Yes | Yes | Yes | Root console screens |
| **Tenant Create/Edit** | Yes | Yes | Yes | Via root console |
| **Auth Flow Config** | Yes | No | No | **Missing entirely** |
| **Auth Sessions** | Yes | No | No | **Missing entirely** |
| **Multi-Step Auth** | Yes | No | No | **Missing entirely** |
| **Biometric Enrollment** | Yes | Yes | Yes | Camera-based |
| **Biometric Verification** | Yes | Yes | Yes | Camera-based |
| **Face Detection (browser)** | Yes (MediaPipe) | Yes (ML Kit) | Yes (JavaCV) | Platform-specific |
| **Liveness Detection** | Yes | Yes | Partial | |
| **Device Management** | Yes | No | No | **Missing entirely** |
| **Audit Logs** | Yes | Yes (mock) | Yes (mock) | **Uses MockRootAdminRepository** |
| **Analytics/Dashboard** | Yes (recharts) | Partial | Partial | Static stat cards, no charts |
| **Settings** | Yes | Yes | Yes | |
| **Roles/Permissions** | Yes | Yes (mock) | Yes (mock) | **Uses MockRootAdminRepository** |
| **NFC Reading** | No | Yes | No | Android-only, well-implemented |
| **Fingerprint Step-Up** | No | Yes | No | Android BiometricPrompt |
| **QR Login** | Yes | Yes | Yes | Scan and display |
| **Invite System** | No | Yes | No | Create, accept, manage |
| **i18n (TR/EN)** | Yes | No | No | **Missing entirely** |
| **Dark Mode** | No | Defined but unused | Yes (default) | Android has theme but `darkTheme=false` hardcoded |
| **Enrollments List** | Yes | No | No | **Missing page** |
| **Guests Page** | Yes | Partial | No | GuestFaceCheck exists |
| **Kiosk Mode** | No | No | Yes | Desktop-only feature |
| **Onboarding** | No | Yes | No | First-launch tutorial |
| **Notifications** | Yes (polling) | Yes (UI) | No | |
| **TOTP Enrollment** | Yes | No | No | **Missing** |
| **WebAuthn Enrollment** | Yes | No | No | **Missing** |
| **Password Reset (email)** | Yes | No | No | **Missing** |

---

## 3. Issues by Priority

### P0 - Blocker (Must fix before any release)

| # | Issue | Location |
|---|-------|----------|
| 1 | AGP version conflict (8.9.2 vs 8.2.2) will cause build failures | `build.gradle.kts` L8 vs L17-18 |
| 2 | Root console screens use `MockRootAdminRepository` with hardcoded fake data -- no real API calls | 17 instances across `RootScreens.kt` and `RootDesktopScreens.kt` |
| 3 | `RootAdminApi` interface exists but has **no implementation class** -- only a mock repository | `shared/.../api/RootAdminApi.kt` |
| 4 | `android:usesCleartextTraffic="true"` allows insecure HTTP in production | `AndroidManifest.xml` L29 |

### P1 - High Priority (Should fix before release)

| # | Issue | Location |
|---|-------|----------|
| 5 | CameraX version mismatch (1.3.1 in shared vs 1.4.1 in androidApp) | `shared/build.gradle.kts` vs `androidApp/build.gradle.kts` |
| 6 | No i18n/localization -- all strings are hardcoded in English | Entire codebase |
| 7 | Auth flow configuration UI missing (web-app has full builder) | No equivalent screens |
| 8 | Device management page missing | No equivalent screens |
| 9 | Auth sessions page missing | No equivalent screens |
| 10 | Multi-step authentication flow missing | No equivalent in client-apps |
| 11 | Default environment is `DEVELOPMENT` (`ApiConfig.currentEnvironment`) -- must be set to PRODUCTION for release builds | `ApiConfig.kt` L21 |
| 12 | `localhost` URLs in DEV config and API doc comments | `ApiConfig.kt`, `IdentityApi.kt`, `AuthApi.kt` |
| 13 | No build flavor or build type configuration to auto-select environment | `androidApp/build.gradle.kts` |
| 14 | Token refresh uses same client instance (potential recursion despite guard) | `NetworkModule.kt` L108-144 |

### P2 - Medium Priority (Should fix for polish)

| # | Issue | Location |
|---|-------|----------|
| 15 | ProGuard/R8 disabled for release builds | `androidApp/build.gradle.kts` L42 |
| 16 | Camera hardware required=true prevents install on some devices | `AndroidManifest.xml` L12 |
| 17 | Dark mode defined but hardcoded to `false` on Android | `Theme.kt` L29 |
| 18 | No TOTP enrollment UI | Missing feature |
| 19 | No WebAuthn enrollment UI | Missing feature |
| 20 | No password reset via email flow | Missing feature |
| 21 | Settings persistence TODO: `/* TODO persist system settings */` | `SettingsScreen.kt` L227 |
| 22 | Export TODO: `/* TODO: implement export */` | `AppNavigation.kt` L637 |
| 23 | Delete enrollment TODO: `/* TODO: delete enrollment */` | `desktopApp/Main.kt` L303 |
| 24 | Filter TODO in desktop: `/* TODO: Implement filter */` | `UsersTab.kt` L87 |
| 25 | Koin version mismatch (3.5.0 vs 3.5.3) | `shared/build.gradle.kts` vs `androidApp/build.gradle.kts` |
| 26 | Duplicate `AppConfig.Api.BASE_URL` constant alongside `ApiConfig` -- two sources of truth | `AppConfig.kt` L18 vs `ApiConfig.kt` |
| 27 | Analytics page has static stat cards, no actual charts (web-app has recharts pie/bar/area) | `AdminDashboardScreen.kt` |
| 28 | `versionCode = 1` and `versionName = "1.0.0-MVP"` -- needs versioning strategy | `androidApp/build.gradle.kts` L14-15 |

---

## 4. Code Quality Assessment

### Positives
- **Clean architecture**: Hexagonal architecture well-followed (domain/data/presentation layers)
- **Dependency injection**: Koin properly configured with modules
- **Error handling**: Comprehensive `AppError` sealed class with user-friendly messages
- **Loading states**: `AppLoadingIndicator` and `LoadingBox` components exist and are used across ViewModels
- **Empty states**: Dedicated `EmptyState` composable with icon/title/message/action
- **API configuration**: Environment-aware `ApiConfig` with DEV/STAGING/PROD URLs
- **Token management**: Automatic JWT refresh with 401 interceptor
- **Validation**: `ValidationRules` with proper email/password/phone validation
- **NFC implementation**: Thorough Android NFC with BAC, secure messaging, SOD validation, Turkish eID support
- **Test coverage**: 7 test files with fakes/mocks (LoginViewModelTest, AdminViewModelTest, KioskViewModelTest, etc.)
- **Navigation**: Well-structured route system with `AppRoute` sealed class and `AppNavigator`
- **Component library**: Atomic design (atoms/molecules/organisms) with reusable composables

### Negatives
- **Mock data in production code**: `MockRootAdminRepository` is directly instantiated in UI composables, not injected via DI
- **No i18n**: Every user-facing string is hardcoded English (web-app supports TR/EN)
- **Dual URL config**: `AppConfig.Api.BASE_URL` and `ApiConfig.identityBaseUrl` create confusion
- **No build variants**: No debug/release flavor to auto-switch API environments
- **Security**: cleartext traffic allowed, no certificate pinning, no network security config

---

## 5. Missing Features vs Web-App

Features present in web-app but completely absent from client-apps:

1. **Auth Flow Configuration** -- The web-app has `AuthFlowBuilder` and `AuthFlowsPage` for configuring multi-step authentication flows per operation type. Client-apps has nothing equivalent.
2. **Auth Sessions Page** -- Web-app shows active auth sessions. Not in client-apps.
3. **Multi-Step Auth UI** -- Web-app has 10 step components (Password, Face, Email OTP, SMS OTP, TOTP, QR, Fingerprint, Voice, Hardware Key, NFC). Client-apps only has password + face + fingerprint.
4. **Device Management** -- Web-app has `DevicesPage`. Client-apps has no device listing.
5. **Enrollments List** -- Web-app has `EnrollmentsListPage`. Client-apps has no equivalent.
6. **TOTP Enrollment** -- Web-app has `TotpEnrollment` dialog with QR code. Missing from client-apps.
7. **WebAuthn Enrollment** -- Web-app has `WebAuthnEnrollment`. Missing from client-apps.
8. **Roles CRUD** -- Web-app has `RolesListPage` and `RoleFormPage`. Client-apps shows roles as read-only mock data.
9. **Password Reset (email)** -- Web-app has `ForgotPasswordPage` and `ResetPasswordPage` with full flow. Client-apps has `ForgotPasswordScreen` UI but no API integration.
10. **i18n (Turkish/English)** -- Web-app has full bilingual support. Client-apps has none.
11. **Step-Up Device Registration** -- Web-app has `StepUpDeviceRegistration`. Client-apps handles fingerprint but not device registration flow.

---

## 6. Stubs and Placeholders in Client-Apps

| Location | Stub |
|----------|------|
| `SettingsScreen.kt:227` | `/* TODO persist system settings */` |
| `AppNavigation.kt:637` | `onExport = { /* TODO: implement export */ }` |
| `desktopApp/Main.kt:303` | `onDeleteEnrollment = { /* TODO: delete enrollment */ }` |
| `UsersTab.kt:87-88` | `onFilter = { /* TODO */ }`, `onExport = { /* TODO */ }` |
| `IdentityApi.kt:10` | `TODO: Implement with Ktor client (Week 2, Day 6)` (stale comment) |
| `RootScreens.kt` (9 instances) | All root screens hardcode `MockRootAdminRepository()` instead of using DI |
| `RootDesktopScreens.kt` (8 instances) | Same mock repository pattern |
| `LoginScreen` | `onNavigateToGuestFaceCheck = { }` -- empty lambda |
| `ForgotPasswordScreen` | UI only -- no API call to trigger password reset email |

---

## 7. Recommendations

### Before Any Release
1. **Fix AGP version conflict** -- use a single version (8.2.2 or upgrade both to match)
2. **Implement `RootAdminApiImpl`** -- replace all `MockRootAdminRepository()` usages with DI-injected real API calls
3. **Remove `usesCleartextTraffic="true"`** -- add a `network_security_config.xml` that only allows cleartext for `10.0.2.2` (emulator) in debug builds
4. **Set production environment by default** or use build flavors

### High Priority
5. **Align dependency versions** -- CameraX, Koin across modules
6. **Add i18n** -- at minimum Turkish and English, matching web-app
7. **Add build flavors** (debug/release) that auto-select API environment
8. **Implement missing admin pages** -- auth flows, devices, auth sessions
9. **Remove duplicate URL config** -- keep only `ApiConfig`, delete `AppConfig.Api`

### Polish
10. **Enable R8/ProGuard** with proper keep rules for Ktor and kotlinx.serialization
11. **Wire up dark mode** on Android (currently hardcoded to light)
12. **Add analytics charts** (compose charts library) to match web-app recharts
13. **Implement remaining TODOs** (export, filter, delete enrollment, settings persistence)
14. **Set up versioning** -- use versionCode from CI build number
15. **Add certificate pinning** for production API endpoints

---

## 8. Test Coverage Summary

| Test File | Tests |
|-----------|-------|
| `LoginViewModelTest.kt` | Login flow states |
| `AdminViewModelTest.kt` | Admin user management |
| `KioskViewModelTest.kt` | Kiosk mode flow |
| `GetStatisticsUseCaseTest.kt` | Statistics use case |
| `GetUsersUseCaseTest.kt` | User listing use case |
| `SearchUsersUseCaseTest.kt` | User search use case |
| `UserRepositoryImplTest.kt` | Repository layer |
| `RolePermissionsTest.kt` | RBAC model |

Test infrastructure includes fakes (`FakeAuthRepository`, `FakeBiometricRepository`, `FakeIdentityApi`, `FakeUserRepository`) and mocks (`AdminMocks`, `KioskMocks`). Tests cannot currently run without Android SDK on this server.

---

## 9. Architecture Quality Score

| Dimension | Score | Notes |
|-----------|-------|-------|
| Code Structure | 8/10 | Clean hexagonal architecture, good separation |
| API Integration | 6/10 | Auth, users, biometric work; root admin is mocked |
| Error Handling | 8/10 | Comprehensive AppError sealed class |
| UI Components | 7/10 | Good atomic design system |
| Testing | 5/10 | Foundations exist but coverage is thin |
| Security | 3/10 | Cleartext allowed, no pinning, no obfuscation |
| Build Config | 3/10 | Version conflicts, no flavors |
| Feature Parity | 5/10 | Core features present, admin features incomplete |
| **Overall** | **5.6/10** | Solid foundation but not production-ready |
