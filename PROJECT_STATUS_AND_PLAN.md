# FIVUCSAS Client Apps - Project Status & Implementation Plan

**Date:** January 31, 2026
**Branch:** `dev`
**Prepared By:** Development Team
**Project Type:** Kotlin Multiplatform (Android, Desktop, iOS)
**Overall Completion:** ~55-60%

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Documentation Accuracy Assessment](#documentation-accuracy-assessment)
3. [Completed Features (Verified)](#completed-features-verified)
4. [Pending Features](#pending-features)
5. [Platform Completion Summary](#platform-completion-summary)
6. [2-Member Implementation Plan](#2-member-implementation-plan)
7. [Member A - Task Breakdown](#member-a---backend-integration--desktop-completion)
8. [Member B - Task Breakdown](#member-b---android-completion--testing--production)
9. [Shared/Collaborative Tasks](#sharedcollaborative-tasks)
10. [Timeline & Milestones](#timeline--milestones)
11. [Risk Register](#risk-register)
12. [Success Criteria](#success-criteria)

---

## Executive Summary

The FIVUCSAS Client Apps project is a Kotlin Multiplatform application targeting Android, Desktop, and iOS. The project has a solid architectural foundation with Clean Architecture, Koin DI, shared UI components, and working screen layouts. However, **critical gaps exist** between what documentation claims and what code actually delivers:

- The `100_PERCENT_COMPLETE.md` (Nov 2025) claims **100% completion** - this is **inaccurate**
- The `PROJECT_PROGRESS_VERIFIED.md` (Dec 2025) corrected this to **~55-60%**
- The `IMPLEMENTATION_PLAN.md` (Jan 2026) confirms **~60% complete** with a 21-day roadmap

The remaining 40% consists of: real backend integration, Security/Settings tabs, iOS app, Android backend connection, test coverage expansion, and production readiness.

---

## Documentation Accuracy Assessment

| Document | Claims | Reality | Reliability |
|----------|--------|---------|-------------|
| `100_PERCENT_COMPLETE.md` | 100% done, production-ready | ~55-60% done, mock data only | UNRELIABLE |
| `PROJECT_PROGRESS_VERIFIED.md` | ~55-60% done with discrepancies listed | Matches code inspection | ACCURATE |
| `IMPLEMENTATION_PLAN.md` | ~60% done, 21-day plan to finish | Matches verified status | ACCURATE |
| `client-apps-MODULE_PLAN.md` | Settings 100%, Security 80% | Both are 0% (placeholders) | UNRELIABLE on these items |
| `ARCHITECTURE_REVIEW.md` | Professional architecture review | Architecture is solid | ACCURATE |

---

## Completed Features (Verified)

### Architecture & Infrastructure - 100%

| Component | File(s) | Status |
|-----------|---------|--------|
| Clean Architecture layers | `shared/src/commonMain/` | domain/data/presentation separation verified |
| KMP module setup | `shared/`, `androidApp/`, `desktopApp/` | 3 modules, `settings.gradle.kts` configured |
| MVVM + StateFlow | ViewModels + State classes | Reactive state management working |
| Gradle build system | 4 `build.gradle.kts` files | Kotlin 1.9.22, Compose 1.6.0, AGP 8.13.1 |

### Dependency Injection (Koin) - 100%

| File | Purpose |
|------|---------|
| `shared/.../di/NetworkModule.kt` | HTTP client and API bindings |
| `shared/.../di/RepositoryModule.kt` | Repository bindings |
| `shared/.../di/UseCaseModule.kt` | Use case factories |
| `shared/.../di/ViewModelModule.kt` | ViewModel factories |
| `shared/.../di/AppModule.kt` | Root module aggregation |
| `shared/.../di/KoinHelper.kt` | Koin initialization helper |
| `shared/.../di/PlatformModule.kt` | expect/actual platform bindings |

### Configuration System - 100%

| File | Content |
|------|---------|
| `shared/.../config/AppConfig.kt` | API URLs, cache, session, logging settings |
| `shared/.../config/UIDimens.kt` | Spacing, icon sizes, button dimensions |
| `shared/.../config/AnimationConfig.kt` | Animation durations and delays |
| `shared/.../config/BiometricConfig.kt` | Confidence thresholds, timeouts |

### Shared UI Component Library (Atomic Design) - 100%

| Layer | Files | Components |
|-------|-------|------------|
| **Atoms** | `Buttons.kt`, `Text.kt`, `TextFields.kt`, `Spacers.kt`, `LoadingIndicator.kt` | Primary/Secondary buttons, styled text, input fields, spacing, spinners |
| **Molecules** | `Cards.kt`, `Dialogs.kt`, `Messages.kt` | Info/stat cards, confirmation dialogs, toast messages |
| **Organisms** | `AppBars.kt`, `Layouts.kt`, `EmptyState.kt` | Top/bottom bars, screen layouts, empty state placeholder |

### Theme System - 100%

| File | Content |
|------|---------|
| `shared/.../ui/theme/AppColors.kt` | Full color palette |
| `shared/.../ui/theme/AppTypography.kt` | Typography scale |
| `shared/.../ui/theme/AppShapes.kt` | Corner radius, shape system |

### Domain Layer - 100%

| Category | Use Cases |
|----------|-----------|
| **Admin** | `GetUsersUseCase`, `DeleteUserUseCase`, `UpdateUserUseCase`, `GetStatisticsUseCase`, `SearchUsersUseCase` |
| **Auth** | `LoginUseCase`, `RegisterUseCase` |
| **Enrollment** | `EnrollUserUseCase` |
| **Verification** | `VerifyUserUseCase`, `CheckLivenessUseCase` |
| **Models** | User, Enrollment, Verification entities |
| **Exceptions** | Custom error types defined |
| **Validation** | `ValidationRules.kt` for input validation |

### Platform Abstractions - 100%

| Interface | Desktop Implementation | Android Implementation | iOS |
|-----------|----------------------|----------------------|-----|
| `ICameraService.kt` | `DesktopCameraServiceImpl.kt` (JavaCV) | `AndroidCameraService.kt` (CameraX) | Stub only |
| `ILogger.kt` | `DesktopLoggerImpl.kt` | Platform default | `IosLogger.kt` stub |
| `ISecureStorage.kt` | `DesktopSecureStorageImpl.kt` | `AndroidTokenStorage.kt` | None |

### Desktop App - Kiosk Mode - 100%

| Screen | File | Features |
|--------|------|----------|
| Welcome | `WelcomeScreen.kt` | Enrollment/Verification buttons, gradient background |
| Enroll | `EnrollScreen.kt` | User form, camera preview placeholder, validation |
| Verify | `VerifyScreen.kt` | Camera capture, success/failure states, progress indicator |
| Shell | `KioskMode.kt` (117 lines) | Screen navigation and state management |

### Desktop App - Admin Dashboard - Partial

| Tab | File | Lines | Status | Notes |
|-----|------|-------|--------|-------|
| Dashboard Shell | `AdminDashboard.kt` | 160 | 100% | Tab navigation working |
| Navigation | `AdminNavigationRail.kt` | - | 100% | Side rail with icons |
| Users | `UsersTab.kt` | 482 | 95% | Full CRUD, search, stats cards. **Missing:** filter + export |
| Analytics | `AnalyticsTab.kt` | 303 | 90% | Stats cards, bar chart. **Limitation:** hardcoded sample data |
| Security | `SecurityTab.kt` | 50 | **0%** | `EmptyState("Security Features Coming Soon")` |
| Settings | `SettingsTab.kt` | 50 | **0%** | `EmptyState("Settings Coming Soon")` |
| Dialogs | `AddUserDialog.kt`, `EditUserDialog.kt`, `DeleteUserDialog.kt` | - | 100% | Full form validation |

### Android App - UI Only - 60%

| File | Lines | Status | Notes |
|------|-------|--------|-------|
| `LoginScreen.kt` | 130+ | 100% UI | Full login form |
| `RegisterScreen.kt` | - | 100% UI | Registration form |
| `HomeScreen.kt` | - | 100% UI | Navigation hub |
| `BiometricEnrollScreen.kt` | 240+ | 100% UI | Real CameraX integration |
| `BiometricVerifyScreen.kt` | - | 100% UI | Camera capture working |
| `AppNavigation.kt` | - | 100% | Jetpack Navigation setup |
| `MainActivity.kt` | - | 100% | Entry point |
| `FIVUCSASApplication.kt` | - | 100% | App class with Koin init |
| `AppDependencies.kt` | - | 100% | DI setup |
| `Theme.kt` | - | 100% | Material theme |

**Key finding:** All Android UI screens are done, but **zero backend connection** - all data is mock.

### Unit Tests - ~50%

| Test File | Coverage Area | Tests |
|-----------|--------------|-------|
| `AdminViewModelTest.kt` | Tab navigation, CRUD, search, dialogs | 25+ |
| `KioskViewModelTest.kt` | Enrollment, verification flows | Yes |
| `LoginViewModelTest.kt` | Login flow | Yes |
| `GetUsersUseCaseTest.kt` | Use case logic | Yes |
| `GetStatisticsUseCaseTest.kt` | Statistics retrieval | Yes |
| `SearchUsersUseCaseTest.kt` | Search filtering | Yes |
| `UserRepositoryImplTest.kt` | Repository layer | Yes |
| `FakeUserRepository.kt` | Test double | Mock |
| `AdminMocks.kt` | Mock objects | Mock |
| `KioskMocks.kt` | Mock objects | Mock |
| `TestData.kt` | Test data factory | Helper |

**Total:** 10 test files, 50+ unit tests. **Target:** 80% coverage (currently ~50%).

### Data Layer (Mock Mode) - 80%

| Component | Status | Notes |
|-----------|--------|-------|
| `AuthApi.kt` + `AuthApiImpl.kt` | Exists | Returns mock data |
| `BiometricApi.kt` + `BiometricApiImpl.kt` | Exists | TODO: implement with Ktor |
| `IdentityApi.kt` + `IdentityApiImpl.kt` | Exists | TODO: implement with Ktor |
| `AuthRepositoryImpl.kt` | Exists | Mock mode |
| `BiometricRepositoryImpl.kt` | Exists | Mock mode |
| `UserRepositoryImpl.kt` | Exists | Mock mode |
| `TokenStorage.kt` / `TokenStore.kt` | Exists | Local token management |
| DTOs: `AuthDto.kt`, `BiometricDto.kt`, `UserDto.kt` | Exists | Serialization models ready |

**Critical:** `ApiConfig.kt` has `useRealApi = false`. No real API calls are made anywhere.

---

## Pending Features

### CRITICAL Priority (Blocks production release)

| # | Feature | Current | Target | Impact |
|---|---------|---------|--------|--------|
| C1 | **Real API Integration** | 0% (mock only) | 100% | Cannot operate without backend |
| C2 | **Security Tab** | 0% (placeholder) | 100% | Core admin functionality missing |
| C3 | **Settings Tab** | 0% (placeholder) | 100% | Core admin functionality missing |
| C4 | **Package Consolidation** | 0% | 100% | Duplicate packages cause confusion |

### HIGH Priority (Required for v1.0)

| # | Feature | Current | Target | Impact |
|---|---------|---------|--------|--------|
| H1 | Backend Auth Flow (JWT) | 0% | 100% | Login/session management |
| H2 | Biometric API (real Ktor calls) | 0% | 100% | Enrollment/verification |
| H3 | Users Tab - Filter/Export | 0% | 100% | Admin usability |
| H4 | Analytics - Live Data | 0% | 100% | Dashboard accuracy |
| H5 | AdminViewModel backend TODOs | 0% | 100% | 5 TODO items in ViewModel |
| H6 | Test Coverage to 80% | ~50% | 80% | Quality assurance |
| H7 | Android backend connection | 0% | 100% | Android app functionality |

### MEDIUM Priority (v1.1 or stretch goal)

| # | Feature | Current | Target | Impact |
|---|---------|---------|--------|--------|
| M1 | iOS Application | 0% | Basic | Third platform |
| M2 | Offline Support | 0% | Basic | Network resilience |
| M3 | Environment Config (prod URLs) | 0% | 100% | Deployment |

### LOW Priority (Nice to have)

| # | Feature | Current | Notes |
|---|---------|---------|-------|
| L1 | Desktop system tray icon | 0% | TODO in `Main.kt` |
| L2 | Integration/E2E tests | 0% | Beyond unit tests |
| L3 | UI/Compose tests | 0% | Visual regression |

---

## Platform Completion Summary

| Platform | Current | After Plan | Key Gaps |
|----------|---------|------------|----------|
| **Shared Module** | 85% | 100% | Real API, package cleanup |
| **Desktop App** | 75% | 100% | Security tab, Settings tab, live data |
| **Android App** | 60% | 95% | Backend connection, error polish |
| **iOS App** | 0% | 0% | Deferred to v1.1 |
| **Backend Integration** | 10% | 100% | All API connections |
| **Tests** | 50% | 80% | More unit + integration tests |
| **OVERALL** | **~58%** | **~95%** | iOS deferred |

---

## 2-Member Implementation Plan

### Team Structure

| Role | Focus Area | Key Skills Needed |
|------|-----------|-------------------|
| **Member A** | Backend Integration + Desktop Completion | Ktor, API design, Compose Desktop |
| **Member B** | Android Completion + Testing + Production | Android/CameraX, Testing, CI/CD |

### Work Distribution Rationale

- **Member A** handles the **data/API layer and Desktop UI** because these are tightly coupled (Security tab needs API data, Settings tab needs API config, Analytics needs live data)
- **Member B** handles the **Android platform, testing, and production readiness** because Android needs its own platform-specific work and testing cuts across all features

### Dependency Graph

```
Phase 1: Package Cleanup -----> (both members, collaborative)
    |
    v
Phase 2A: Backend API Client (Member A)
Phase 2B: Android Platform Polish (Member B)
    |                    |
    v                    v
Phase 3A: Desktop Security+Settings (Member A) <-- needs API
Phase 3B: Android Backend Connection (Member B) <-- needs API from A
    |                    |
    v                    v
Phase 4A: Desktop Live Data + Polish (Member A)
Phase 4B: Test Coverage Expansion (Member B)
    |                    |
    v                    v
Phase 5: Production Readiness (both members, collaborative)
```

---

## Member A - Backend Integration & Desktop Completion

### A-Phase 1: Package Consolidation & Ktor HTTP Client (Days 1-3)

#### Task A1.1: Clean Up Duplicate Packages
**Priority:** CRITICAL
**Files to modify:**
- All files under `shared/src/commonMain/` with `com.fivucsas.mobile` imports
- Update to `com.fivucsas.shared` consistently

**Steps:**
1. Search all files for `import com.fivucsas.mobile` across the entire project
2. Replace all occurrences with `import com.fivucsas.shared`
3. Delete any leftover `com.fivucsas.mobile` package directories
4. Verify build succeeds: `./gradlew build`

**Acceptance Criteria:**
- [ ] No references to `com.fivucsas.mobile` remain anywhere
- [ ] All imports use `com.fivucsas.shared`
- [ ] Build succeeds without errors

#### Task A1.2: Implement Ktor HTTP Client Factory
**Priority:** CRITICAL
**New file:** `shared/src/commonMain/kotlin/com/fivucsas/shared/data/remote/HttpClientFactory.kt`

**Steps:**
1. Create `HttpClientFactory` class with Ktor configuration:
   - `ContentNegotiation` plugin with `kotlinx.serialization`
   - `Logging` plugin (configurable per environment)
   - `HttpTimeout` plugin (use `AppConfig.Api.TIMEOUT_SECONDS`)
   - `Auth` plugin with Bearer token support (load/refresh)
   - `DefaultRequest` plugin with base URL
2. Create `TokenProvider` interface for token management
3. Wire into Koin `NetworkModule.kt`

**Acceptance Criteria:**
- [ ] HTTP client creates successfully
- [ ] Auth headers attached automatically
- [ ] Token refresh works when access token expires
- [ ] Timeout configured from `AppConfig`

#### Task A1.3: Implement Identity API Client (Real)
**Priority:** CRITICAL
**File to modify:** `shared/.../data/remote/api/IdentityApiImpl.kt`

**Steps:**
1. Replace mock implementations with real Ktor HTTP calls:
   - `POST /auth/login` -> `LoginResponse`
   - `POST /auth/refresh` -> `RefreshResponse`
   - `POST /auth/logout` -> 204 No Content
   - `GET /auth/me` -> `UserDTO`
   - `GET /users?page=&size=&sort=` -> `PaginatedResponse<UserDTO>`
   - `POST /users` -> `UserDTO`
   - `PUT /users/{id}` -> `UserDTO`
   - `DELETE /users/{id}` -> 204 No Content
   - `GET /dashboard/statistics` -> `DashboardStatistics`
2. Use `Result<T>` return type for all calls
3. Map HTTP errors to `AppError` sealed class
4. Verify DTO serialization matches API contracts in `IMPLEMENTATION_PLAN.md`

**Acceptance Criteria:**
- [ ] All Identity API endpoints implemented with real Ktor calls
- [ ] Error mapping works (network, auth, validation, server errors)
- [ ] DTOs serialize/deserialize correctly
- [ ] `useRealApi` flag in `ApiConfig.kt` switches between mock and real

#### Task A1.4: Implement Biometric API Client (Real)
**Priority:** CRITICAL
**File to modify:** `shared/.../data/remote/api/BiometricApiImpl.kt`

**Steps:**
1. Replace mock/TODO implementations with real Ktor HTTP calls:
   - `POST /enrollments` -> `EnrollmentResponse` (sends Base64 image)
   - `POST /verify` -> `VerificationResponse` (sends Base64 image)
   - `POST /liveness` -> `LivenessResponse` (sends Base64 image)
   - `GET /enrollments/{userId}` -> `List<EnrollmentDTO>`
   - `DELETE /enrollments/{enrollmentId}` -> 204 No Content
2. Handle Base64 encoding of image byte arrays
3. Use separate `BIOMETRIC_BASE_URL` from `AppConfig`

**Acceptance Criteria:**
- [ ] All Biometric API endpoints implemented
- [ ] Image Base64 encoding works correctly
- [ ] Liveness, enrollment, and verification calls functional
- [ ] Error handling consistent with Identity API

#### Task A1.5: Update Repositories to Use Real APIs
**Priority:** CRITICAL
**Files to modify:**
- `shared/.../data/repository/AuthRepositoryImpl.kt`
- `shared/.../data/repository/BiometricRepositoryImpl.kt`
- `shared/.../data/repository/UserRepositoryImpl.kt`

**Steps:**
1. Update `AuthRepositoryImpl`:
   - Store tokens via `TokenStorage` on login
   - Clear tokens on logout
   - Implement `checkAuthState()` to validate stored token
2. Update `UserRepositoryImpl`:
   - Wire CRUD operations to real Identity API
   - Implement pagination support
3. Update `BiometricRepositoryImpl`:
   - Wire enrollment to real Biometric API
   - Wire verification to real Biometric API
   - Wire liveness check to real Biometric API

**Acceptance Criteria:**
- [ ] Login flow stores JWT tokens and returns User
- [ ] Logout clears tokens
- [ ] User CRUD operations call real API
- [ ] Biometric operations send images and return results

---

### A-Phase 2: Desktop Security Tab (Days 4-6)

#### Task A2.1: Implement Security Tab UI
**Priority:** CRITICAL
**File to rewrite:** `desktopApp/.../tabs/SecurityTab.kt`

**Steps:**
1. Replace `EmptyState` placeholder with full implementation
2. Build **Security Alerts Section:**
   - Card-based layout showing recent security events
   - Alert types: Failed Login, Suspicious Activity, Session Expired, Account Locked
   - Each alert shows: timestamp, user, event type, IP address, severity badge
   - Color-coded severity (Critical=Red, Warning=Orange, Info=Blue)
3. Build **Audit Log Table:**
   - Columns: Timestamp, User, Action, Resource, IP Address, Status
   - Pagination controls (page size selector, prev/next)
   - Sort by column header click
4. Build **Active Sessions Section:**
   - List of active user sessions
   - Columns: User, Device, IP, Login Time, Last Activity
   - "Terminate Session" button per row
5. Build **Security Summary Cards:**
   - Failed login attempts (24h)
   - Active sessions count
   - Locked accounts count
   - Last security scan timestamp

**Acceptance Criteria:**
- [ ] Security alerts display with severity badges
- [ ] Audit log table with pagination and sorting
- [ ] Active sessions list with terminate capability
- [ ] Summary cards showing security metrics
- [ ] Connected to ViewModel (mock data acceptable initially, real API when backend available)

#### Task A2.2: Create SecurityViewModel
**Priority:** HIGH
**New file:** `shared/.../presentation/viewmodel/SecurityViewModel.kt`

**Steps:**
1. Create `SecurityUiState` data class with:
   - `securityAlerts: List<SecurityAlert>`
   - `auditLogs: List<AuditLogEntry>`
   - `activeSessions: List<ActiveSession>`
   - `failedLoginsCount: Int`, `activeSessionsCount: Int`, `lockedAccountsCount: Int`
   - `isLoading: Boolean`, `errorMessage: String?`
   - Pagination state
2. Create SecurityViewModel with functions:
   - `loadSecurityAlerts()`
   - `loadAuditLogs(page, size, sort)`
   - `loadActiveSessions()`
   - `terminateSession(sessionId)`
   - `refreshAll()`
3. Register in Koin `ViewModelModule.kt`

**Acceptance Criteria:**
- [ ] ViewModel manages all security screen state
- [ ] Loading/error states handled
- [ ] Pagination works for audit logs
- [ ] Session termination triggers reload

---

### A-Phase 3: Desktop Settings Tab (Days 7-9)

#### Task A3.1: Implement Settings Tab UI - 6 Sections
**Priority:** CRITICAL
**File to rewrite:** `desktopApp/.../tabs/SettingsTab.kt`

**Steps:**
1. Replace `EmptyState` placeholder with scrollable settings layout
2. Build **Section 1 - Profile Settings:**
   - Display name, email (read-only), avatar placeholder
   - Edit name form with Save button
3. Build **Section 2 - Security Settings:**
   - Change password form (current + new + confirm)
   - Two-factor authentication toggle
   - Session timeout duration selector (dropdown)
4. Build **Section 3 - Biometric Settings:**
   - Confidence threshold slider (0.70 - 1.00, default 0.85)
   - Liveness threshold slider (0.70 - 1.00, default 0.80)
   - Quality threshold slider (0.50 - 1.00, default 0.75)
   - Max enrollment retries number input
5. Build **Section 4 - System Settings:**
   - API URL text fields (Identity + Biometric)
   - Environment selector (Development/Staging/Production)
   - Logging level selector (DEBUG/INFO/WARN/ERROR)
   - Cache size input + "Clear Cache" button
6. Build **Section 5 - Notification Settings:**
   - Email notifications toggle
   - Security alert notifications toggle
   - Enrollment notifications toggle
   - Verification failure notifications toggle
7. Build **Section 6 - Appearance Settings:**
   - Theme selector (Light/Dark/System)
   - Language selector (English, other supported)
   - Density selector (Compact/Normal/Comfortable)

**Acceptance Criteria:**
- [ ] All 6 sections render correctly in scrollable layout
- [ ] Form inputs validate before saving
- [ ] Settings persist (via SettingsViewModel -> local storage)
- [ ] Visual separation between sections (cards or dividers)

#### Task A3.2: Create SettingsViewModel
**Priority:** HIGH
**New file:** `shared/.../presentation/viewmodel/SettingsViewModel.kt`

**Steps:**
1. Create `SettingsUiState` data class with fields for all 6 sections
2. Implement load/save for each section
3. Use `ISecureStorage` for sensitive settings (passwords)
4. Use shared preferences / local storage for non-sensitive settings
5. Register in Koin `ViewModelModule.kt`

---

### A-Phase 4: Desktop Polish & Live Data (Days 10-12)

#### Task A4.1: Connect Analytics Tab to Real API Data
**Priority:** HIGH
**File to modify:** `desktopApp/.../tabs/AnalyticsTab.kt`

**Steps:**
1. Replace hardcoded chart data with `AdminViewModel` state
2. Call `GET /dashboard/statistics` via repository
3. Map `DashboardStatistics` DTO to chart data:
   - `verificationsByDay` -> bar chart
   - `enrollmentsByDay` -> line/area chart
   - `totalUsers`, `activeUsers`, `totalVerifications` -> stat cards
4. Add loading skeleton while data fetches
5. Add refresh button and auto-refresh interval

**Acceptance Criteria:**
- [ ] Charts display real API data
- [ ] Stats cards show live counts
- [ ] Loading state shown while fetching
- [ ] Error state with retry button

#### Task A4.2: Implement Users Tab Filter & Export
**Priority:** HIGH
**File to modify:** `desktopApp/.../tabs/UsersTab.kt`

**Steps:**
1. **Filter functionality:**
   - Replace TODO with dropdown filter options
   - Filter by: Status (Active/Inactive/Locked), Role (Admin/Operator/Viewer)
   - Apply filters to API query parameters
   - Show active filter count badge
2. **Export functionality:**
   - Replace TODO with file save dialog
   - Export formats: CSV
   - Export visible/filtered users
   - Include columns: Name, Email, ID Number, Status, Role, Created Date

**Acceptance Criteria:**
- [ ] Filter dropdown works with status and role options
- [ ] Filters apply to displayed data
- [ ] CSV export produces valid file
- [ ] Export respects current filters

#### Task A4.3: Fix AdminViewModel Backend TODOs
**Priority:** HIGH
**File to modify:** `shared/.../presentation/viewmodel/AdminViewModel.kt`

**Steps:**
1. Replace 5 TODO items with real implementations:
   - "When backend is ready, save to API" -> call UserRepository
   - "Actual database connection test" -> call health check endpoint
   - "Actual cache clearing" -> clear local cache/storage
   - "Actual log export" -> generate and save log file
   - "Actual system health check" -> call API health endpoint
2. Add proper error handling for each operation

**Acceptance Criteria:**
- [ ] All 5 TODOs replaced with working code
- [ ] Error states shown to user on failure
- [ ] Success feedback shown on completion

---

## Member B - Android Completion & Testing & Production

### B-Phase 1: Android Platform Polish (Days 1-3)

#### Task B1.1: Android Error Handling & Loading States
**Priority:** HIGH
**Files to modify:**
- `androidApp/.../ui/screen/LoginScreen.kt`
- `androidApp/.../ui/screen/RegisterScreen.kt`
- `androidApp/.../ui/screen/HomeScreen.kt`
- `androidApp/.../ui/screen/BiometricEnrollScreen.kt`
- `androidApp/.../ui/screen/BiometricVerifyScreen.kt`

**Steps:**
1. Add loading indicators during async operations (login, register, capture)
2. Add error snackbars/dialogs for:
   - Network errors (no connection, timeout)
   - Validation errors (empty fields, invalid email)
   - Auth errors (wrong password, account locked)
   - Camera errors (permission denied, not available)
3. Add success feedback (snackbar after enrollment, navigation after login)
4. Ensure all screens handle back-press correctly

**Acceptance Criteria:**
- [ ] Every screen shows loading spinner during operations
- [ ] Error messages are user-friendly (not raw exceptions)
- [ ] Success feedback shown after successful operations
- [ ] No crashes on error states

#### Task B1.2: Android Camera Permission Flow
**Priority:** HIGH
**Files to modify:**
- `androidApp/.../android/MainActivity.kt`
- `shared/.../platform/CameraPermissionHelper.kt`

**Steps:**
1. Implement runtime permission request for CAMERA
2. Handle permission denied gracefully (show rationale, settings link)
3. Handle "Don't ask again" scenario (direct to app settings)
4. Verify CameraX lifecycle binding in BiometricEnroll/Verify screens
5. Test on Android 10, 11, 12, 13, 14 permission models

**Acceptance Criteria:**
- [ ] Camera permission requested before camera screens
- [ ] Denied permission shows explanation dialog
- [ ] "Never ask again" directs to system settings
- [ ] Camera preview starts correctly after permission grant

#### Task B1.3: Android Navigation & State Preservation
**Priority:** MEDIUM
**File to modify:** `androidApp/.../ui/navigation/AppNavigation.kt`

**Steps:**
1. Verify navigation graph handles all screen transitions
2. Add auth state check on app start (redirect to login if no token)
3. Handle deep links if needed
4. Preserve state on configuration changes (rotation)
5. Handle process death (save/restore ViewModel state)

**Acceptance Criteria:**
- [ ] App starts on login screen if not authenticated
- [ ] App starts on home screen if token is valid
- [ ] Screen rotation preserves form input
- [ ] Back navigation works correctly from all screens

---

### B-Phase 2: Android Backend Connection (Days 4-6)

> **Dependency:** Requires Member A's A1.2-A1.5 (Ktor client + API implementations) to be completed.

#### Task B2.1: Connect Android Login/Register to Real API
**Priority:** CRITICAL
**Files to modify:**
- `androidApp/.../ui/screen/LoginScreen.kt`
- `androidApp/.../ui/screen/RegisterScreen.kt`
- Related ViewModels

**Steps:**
1. Wire `LoginScreen` submit button to `LoginUseCase` (which uses real API via Member A's work)
2. Handle login response: store tokens, navigate to Home
3. Handle login errors: show validation errors, network errors, auth errors
4. Wire `RegisterScreen` to register API
5. Handle registration success: auto-login or redirect to login
6. Test with real backend (or mock server if backend not ready)

**Acceptance Criteria:**
- [ ] Login calls real API and stores JWT tokens
- [ ] Failed login shows appropriate error message
- [ ] Registration creates user and provides feedback
- [ ] Token persists across app restarts

#### Task B2.2: Connect Android Biometric Screens to Real API
**Priority:** CRITICAL
**Files to modify:**
- `androidApp/.../ui/screen/BiometricEnrollScreen.kt`
- `androidApp/.../ui/screen/BiometricVerifyScreen.kt`

**Steps:**
1. Wire `BiometricEnrollScreen`:
   - Capture photo via CameraX
   - Convert image to byte array
   - Call `EnrollUserUseCase` -> liveness check -> enrollment
   - Show step progress (Capturing -> Liveness -> Enrolling -> Success/Fail)
2. Wire `BiometricVerifyScreen`:
   - Capture photo via CameraX
   - Call `VerifyUserUseCase` -> liveness check -> verification
   - Show result with confidence percentage
3. Handle all error states in both flows

**Acceptance Criteria:**
- [ ] Enrollment flow captures image and sends to API
- [ ] Liveness check runs before enrollment/verification
- [ ] Verification shows match result with confidence
- [ ] Error states display appropriate messages
- [ ] Retry available on failure

#### Task B2.3: Connect Android Home Screen to Real Data
**Priority:** HIGH
**File to modify:** `androidApp/.../ui/screen/HomeScreen.kt`

**Steps:**
1. Display logged-in user info from stored auth state
2. Show enrollment status (enrolled / not enrolled)
3. Show recent verification history
4. Add logout button that clears tokens and navigates to login
5. Add pull-to-refresh for user data

**Acceptance Criteria:**
- [ ] Home screen shows real user name/email
- [ ] Enrollment status reflects API state
- [ ] Logout clears all local data
- [ ] Pull-to-refresh updates data

---

### B-Phase 3: Test Coverage Expansion (Days 7-10)

#### Task B3.1: ViewModel Tests - Expand to 80% Coverage
**Priority:** HIGH
**Files to create/modify in:** `shared/src/commonTest/`

**Steps:**
1. **Expand `AdminViewModelTest.kt`:**
   - Test Settings save/load operations
   - Test Security alerts loading
   - Test error states for all operations
   - Test pagination in users list
2. **Expand `KioskViewModelTest.kt`:**
   - Test camera initialization failure
   - Test liveness check failure during enrollment
   - Test liveness check failure during verification
   - Test low quality score rejection
   - Test retry logic
3. **Expand `LoginViewModelTest.kt`:**
   - Test successful login flow
   - Test wrong password
   - Test network error during login
   - Test token expiry and refresh
   - Test logout
4. **Create `SecurityViewModelTest.kt`:**
   - Test alerts loading
   - Test audit log pagination
   - Test session termination
   - Test error states
5. **Create `SettingsViewModelTest.kt`:**
   - Test settings load from storage
   - Test settings save to storage
   - Test validation of threshold values
   - Test password change validation

**Acceptance Criteria:**
- [ ] Each ViewModel has 10+ test cases
- [ ] Happy path and error paths both covered
- [ ] All tests pass: `./gradlew :shared:testDebugUnitTest`
- [ ] Coverage report shows >= 80% on ViewModels

#### Task B3.2: Repository Tests - Expand Coverage
**Priority:** HIGH
**Files to create/modify:**

**Steps:**
1. **Expand `UserRepositoryImplTest.kt`:**
   - Test getUsers with pagination
   - Test createUser success/failure
   - Test updateUser success/failure
   - Test deleteUser success/failure
   - Test searchUsers with query
2. **Create `AuthRepositoryImplTest.kt`:**
   - Test login stores tokens
   - Test login with invalid credentials
   - Test logout clears tokens
   - Test checkAuthState with valid/expired token
   - Test token refresh flow
3. **Create `BiometricRepositoryImplTest.kt`:**
   - Test enrollUser with valid image
   - Test enrollUser with low quality
   - Test verifyUser match found
   - Test verifyUser no match
   - Test checkLiveness live/not live

**Acceptance Criteria:**
- [ ] Each repository has 5+ test cases
- [ ] Mock API responses verified
- [ ] Error propagation tested
- [ ] All tests pass

#### Task B3.3: Use Case Tests - Complete Coverage
**Priority:** MEDIUM

**Steps:**
1. Ensure all 10 use cases have dedicated test files
2. Add missing tests for:
   - `LoginUseCase` / `RegisterUseCase`
   - `EnrollUserUseCase`
   - `VerifyUserUseCase` / `CheckLivenessUseCase`
   - `DeleteUserUseCase` / `UpdateUserUseCase`
3. Test input validation in use cases
4. Test error propagation from repositories

**Acceptance Criteria:**
- [ ] All use cases have test files
- [ ] Input validation tested
- [ ] Error cases tested
- [ ] All tests pass

---

### B-Phase 4: Production Readiness (Days 11-12)

#### Task B4.1: Environment Configuration
**Priority:** HIGH
**File to modify:** `shared/.../config/AppConfig.kt` and related

**Steps:**
1. Create `Environment.kt` enum (DEVELOPMENT, STAGING, PRODUCTION)
2. Create `EnvironmentConfig` object that switches URLs by environment
3. Remove all hardcoded `localhost` URLs from non-config files
4. Set `useRealApi = true` as default (mock mode opt-in for development)
5. Verify `.env.example` matches actual required variables

**Acceptance Criteria:**
- [ ] Environment can be switched via single config change
- [ ] No hardcoded localhost URLs outside config
- [ ] Production URLs configured (even if placeholders)
- [ ] Mock mode is opt-in, not default

#### Task B4.2: Build Verification & Release Config
**Priority:** HIGH

**Steps:**
1. Verify Android release build:
   - `./gradlew :androidApp:assembleRelease`
   - Check APK size < 50MB
   - Verify ProGuard/R8 rules if minification enabled
2. Verify Desktop distribution build:
   - `./gradlew :desktopApp:packageDistributionForCurrentOS`
   - Check app size < 100MB
3. Verify all tests pass:
   - `./gradlew test`
4. Check for secrets/credentials in committed files
5. Verify `.gitignore` covers `local.properties`, `.env`, build outputs

**Acceptance Criteria:**
- [ ] Android release build succeeds
- [ ] Desktop distribution build succeeds
- [ ] All tests pass
- [ ] No secrets in repository
- [ ] APK < 50MB, Desktop < 100MB

#### Task B4.3: Error Handling Audit
**Priority:** MEDIUM

**Steps:**
1. Audit all `try/catch` blocks for proper error mapping
2. Ensure no raw exceptions leak to UI (all mapped to `AppError`)
3. Verify network error handling (no connection, timeout, server error)
4. Verify auth error handling (expired token, forbidden)
5. Add global error handler for uncaught exceptions

**Acceptance Criteria:**
- [ ] All errors mapped to user-friendly messages
- [ ] No stack traces visible to users
- [ ] Network errors suggest retry
- [ ] Auth errors redirect to login

---

## Shared/Collaborative Tasks

These tasks require **both members** to coordinate.

### Day 1: Kickoff & Package Cleanup (Both Members)

| Task | Owner | Notes |
|------|-------|-------|
| Review this plan together | Both | Align on approach, clarify questions |
| Package consolidation (A1.1) | Member A leads | Member B reviews and verifies Android side |
| Set up shared test environment | Member B leads | Ensure both can run backend locally |
| Verify base build works | Both | `./gradlew build` passes before any changes |

### Day 12: Integration & Final Review (Both Members)

| Task | Owner | Notes |
|------|-------|-------|
| End-to-end manual testing | Both | Test all flows on Desktop + Android |
| Cross-review code changes | Both | Member A reviews B's code and vice versa |
| Update documentation | Both | Update README, remove inaccurate 100% claim |
| Final build verification | Both | Release builds for both platforms |

---

## Timeline & Milestones

### 12-Day Sprint (Excludes iOS - deferred to v1.1)

| Day | Member A | Member B | Milestone |
|-----|----------|----------|-----------|
| **1** | A1.1: Package cleanup | B1.1: Android error handling | Clean build baseline |
| **2** | A1.2: Ktor HTTP client | B1.2: Camera permissions | HTTP client ready |
| **3** | A1.3: Identity API client | B1.3: Android navigation | Identity API ready |
| **4** | A1.4: Biometric API client | B2.1: Android login connection | **Milestone: API layer complete** |
| **5** | A1.5: Update repositories | B2.2: Android biometric connection | Repositories wired |
| **6** | A2.1: Security Tab UI | B2.3: Android home screen | **Milestone: Android connected** |
| **7** | A2.2: SecurityViewModel | B3.1: ViewModel tests (start) | Security tab functional |
| **8** | A3.1: Settings Tab UI (start) | B3.1: ViewModel tests (finish) | Settings tab started |
| **9** | A3.1: Settings Tab UI (finish) | B3.2: Repository tests | **Milestone: Desktop complete** |
| **10** | A4.1: Analytics live data | B3.3: Use case tests | Live data connected |
| **11** | A4.2: Filter/Export + A4.3: AdminVM TODOs | B4.1: Environment config + B4.2: Build verification | **Milestone: Feature complete** |
| **12** | Integration testing (both) | Code review + B4.3: Error audit | **Milestone: Release candidate** |

### Key Milestones

| Milestone | Day | Criteria |
|-----------|-----|----------|
| Clean Build Baseline | 1 | Package cleanup done, build passes |
| API Layer Complete | 4 | All Ktor API clients implemented |
| Android Connected | 6 | Android app uses real API calls |
| Desktop Complete | 9 | Security + Settings tabs implemented |
| Feature Complete | 11 | All pending features implemented |
| Release Candidate | 12 | All tests pass, builds succeed, manual testing done |

---

## Risk Register

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Backend API not ready/available | HIGH | HIGH | Keep mock mode as fallback; test with mock server |
| API contract mismatch (DTOs don't match real responses) | MEDIUM | HIGH | Validate DTOs against backend Swagger/OpenAPI early (Day 2) |
| CameraX issues on specific Android devices | MEDIUM | MEDIUM | Test on 3+ devices/emulators; add fallback UI |
| Ktor dependency conflicts with existing libs | LOW | HIGH | Resolve in Phase 1; lock dependency versions |
| Test coverage target (80%) too ambitious | MEDIUM | LOW | Prioritize critical path tests; accept 70% if needed |
| Member blocked waiting on other's deliverable | MEDIUM | MEDIUM | Phase 2B starts with non-API tasks; daily sync meetings |

---

## Success Criteria

| Metric | Target | How to Verify |
|--------|--------|---------------|
| Feature Completion | 95% (iOS deferred) | All pending items marked complete |
| Test Coverage | >= 70% (stretch: 80%) | `./gradlew koverReport` |
| Build Success | All platforms | `./gradlew build` passes |
| Android APK Size | < 50MB | Check `androidApp/build/outputs/` |
| Desktop App Size | < 100MB | Check distribution package |
| Zero Critical Bugs | 0 crashes in manual testing | End-to-end testing on Day 12 |
| API Integration | Real API calls working | Toggle `useRealApi = true` and verify |
| Documentation Accuracy | 100% | All docs reflect actual code state |

---

## What Is NOT In Scope (Deferred)

| Item | Reason | Target Release |
|------|--------|----------------|
| iOS Application | No macOS available / team skillset | v1.1 |
| Offline Support | Requires local database + sync logic | v1.1 |
| Integration/E2E Tests | Focus on unit tests first | v1.1 |
| Desktop System Tray | Low priority cosmetic | v1.1 |
| UI/Compose Tests | Low ROI for current stage | v1.2 |

---

## Appendix: Files Changed Per Task

### Member A File Ownership

| File | Task | Action |
|------|------|--------|
| All files with `com.fivucsas.mobile` | A1.1 | Modify imports |
| `shared/.../data/remote/HttpClientFactory.kt` | A1.2 | Create new |
| `shared/.../data/remote/api/IdentityApiImpl.kt` | A1.3 | Rewrite |
| `shared/.../data/remote/api/BiometricApiImpl.kt` | A1.4 | Rewrite |
| `shared/.../data/repository/AuthRepositoryImpl.kt` | A1.5 | Modify |
| `shared/.../data/repository/UserRepositoryImpl.kt` | A1.5 | Modify |
| `shared/.../data/repository/BiometricRepositoryImpl.kt` | A1.5 | Modify |
| `shared/.../di/NetworkModule.kt` | A1.2 | Modify |
| `desktopApp/.../tabs/SecurityTab.kt` | A2.1 | Rewrite |
| `shared/.../presentation/viewmodel/SecurityViewModel.kt` | A2.2 | Create new |
| `shared/.../presentation/state/SecurityUiState.kt` | A2.2 | Create new |
| `desktopApp/.../tabs/SettingsTab.kt` | A3.1 | Rewrite |
| `shared/.../presentation/viewmodel/SettingsViewModel.kt` | A3.2 | Create new |
| `desktopApp/.../tabs/AnalyticsTab.kt` | A4.1 | Modify |
| `desktopApp/.../tabs/UsersTab.kt` | A4.2 | Modify |
| `shared/.../presentation/viewmodel/AdminViewModel.kt` | A4.3 | Modify (5 TODOs) |
| `shared/.../di/ViewModelModule.kt` | A2.2, A3.2 | Modify (register new VMs) |

### Member B File Ownership

| File | Task | Action |
|------|------|--------|
| `androidApp/.../ui/screen/LoginScreen.kt` | B1.1, B2.1 | Modify |
| `androidApp/.../ui/screen/RegisterScreen.kt` | B1.1, B2.1 | Modify |
| `androidApp/.../ui/screen/HomeScreen.kt` | B1.1, B2.3 | Modify |
| `androidApp/.../ui/screen/BiometricEnrollScreen.kt` | B1.1, B2.2 | Modify |
| `androidApp/.../ui/screen/BiometricVerifyScreen.kt` | B1.1, B2.2 | Modify |
| `androidApp/.../android/MainActivity.kt` | B1.2 | Modify |
| `androidApp/.../ui/navigation/AppNavigation.kt` | B1.3 | Modify |
| `shared/.../platform/CameraPermissionHelper.kt` | B1.2 | Modify |
| `shared/src/commonTest/.../AdminViewModelTest.kt` | B3.1 | Expand |
| `shared/src/commonTest/.../KioskViewModelTest.kt` | B3.1 | Expand |
| `shared/src/commonTest/.../LoginViewModelTest.kt` | B3.1 | Expand |
| `shared/src/commonTest/.../SecurityViewModelTest.kt` | B3.1 | Create new |
| `shared/src/commonTest/.../SettingsViewModelTest.kt` | B3.1 | Create new |
| `shared/src/commonTest/.../AuthRepositoryImplTest.kt` | B3.2 | Create new |
| `shared/src/commonTest/.../BiometricRepositoryImplTest.kt` | B3.2 | Create new |
| `shared/src/commonTest/.../UserRepositoryImplTest.kt` | B3.2 | Expand |
| `shared/.../config/AppConfig.kt` | B4.1 | Modify |
| `shared/.../config/Environment.kt` | B4.1 | Create new |
| `androidApp/build.gradle.kts` | B4.2 | Verify/modify |

---

**Document Status:** Ready for execution
**Created:** January 31, 2026
**Sprint Duration:** 12 working days
**Team Size:** 2 members
**Target Completion:** ~95% (iOS deferred)
