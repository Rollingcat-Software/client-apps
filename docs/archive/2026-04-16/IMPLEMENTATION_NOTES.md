# Implementation Notes

**Last Updated:** February 2, 2026
**Branch:** `feature/glsm/backend-integration`

---

## Recent Changes

### Biometric Enroll Screen — Full Refactor

**Date:** February 2, 2026
**Files Changed:**

| File | Action |
|------|--------|
| `androidApp/.../ui/screen/BiometricEnrollScreen.kt` | Rewritten |
| `androidApp/.../ui/screen/BiometricVerifyScreen.kt` | Rewritten |
| `androidApp/.../ui/util/ImageCaptureUtils.kt` | Created |
| `shared/.../viewmodel/auth/BiometricViewModel.kt` | Modified |
| `shared/.../usecase/enrollment/EnrollUserUseCase.kt` | Modified |

**What changed:**

1. **Enrollment form added** — Two-step flow: Form (personal info) then Camera capture. Replaces previously hardcoded `"Test User"` / `"test@example.com"` enrollment data. All fields are optional for development/testing — validation is advisory, not blocking.

2. **Photo preview step** — After capture, user sees a mirrored preview of their photo and can choose to **Submit** or **Retake**. The non-mirrored original bytes are sent to the backend. Flow: `Form → Camera → Preview → Submit → Success`.

3. **Camera capture fixed** — Removed broken custom `ImageProxy.toBitmap()` extension that only worked with JPEG format. Created `ImageCaptureUtils.kt` using CameraX 1.3+ built-in `toBitmap()` which handles all device-specific image formats (YUV_420_888, JPEG, etc.). All intermediate bitmaps are properly recycled.

4. **Error handling** — `ImageCaptureException` is now reported to the UI via `BiometricViewModel.onCaptureError()`. Both screens show error cards with "Try Again" buttons.

5. **Camera resource cleanup** — `DisposableEffect { onDispose { cameraController.unbind() } }` added to both screens to prevent camera resource leaks on navigation.

6. **Permission handling** — Detects permanent denial (`shouldShowRationale == false` after request) and shows "Open App Settings" button instead of a non-functional "Grant Permission" button.

7. **National ID made optional** — Both UI validation (`BiometricEnrollScreen`) and backend validation (`EnrollUserUseCase`) now skip national ID if left blank. Same treatment for all other fields.

**Architectural decisions:**

- Form state is local to the composable (`remember`), not in the ViewModel. Form data is a UI concern; the ViewModel only handles API calls (SRP).
- `ImageCaptureUtils.kt` is shared between enroll and verify screens to avoid duplicate bitmap logic (DRY).
- The `BiometricEnrollScreen` function signature is unchanged (`userId`, `viewModel`, `onNavigateBack`) for backward compatibility. `userId` parameter is currently unused but kept to preserve the navigation route contract.
- Step tracking uses `rememberSaveable` with string constants to survive configuration changes.

**Known limitations:**

- `BiometricViewModel` does not extend `androidx.lifecycle.ViewModel` — state is lost on configuration change. Requires KMP multiplatform lifecycle dependency upgrade.
- `LocalLifecycleOwner.current` is deprecated in newer Compose versions. Migration to Lifecycle 2.8+ API deferred.
- `Icons.Default.ArrowBack` and `Icons.Default.Send` show deprecation warnings (should use `AutoMirrored` variants). Non-functional issue.

---

## Future Plan

### FP-1: Session Manager — Auto-fill Enrollment Form from Logged-in User

**Priority:** HIGH
**Depends on:** Auth flow storing user data after login

**Problem:**
After login, the app only stores JWT tokens (`TokenManager`). The user's name, email, and ID are not persisted. The enrollment form cannot auto-fill from the current session. Dashboard and Profile screens also use hardcoded `"Test User"` / `"test@fivucsas.com"`.

**Proposed solution:**

1. Create `SessionManager` class in `shared/src/commonMain/`:
   ```
   class SessionManager(private val secureStorage: ISecureStorage) {
       fun saveUserSession(user: User)
       fun getCurrentUser(): User?
       fun getCurrentUserEmail(): String?
       fun getCurrentUserName(): String?
       fun clearSession()
   }
   ```
   Storage keys already exist in `ISecureStorage.StorageKeys` (`USER_ID`, `USER_EMAIL`) but are unused.

2. Update `AuthRepositoryImpl` — after successful login, call `SessionManager.saveUserSession()` with the user data from the JWT token claims or a `/auth/me` API call.

3. Update `BiometricEnrollScreen` — inject `SessionManager` via Koin, pre-fill form fields from `sessionManager.getCurrentUser()` on initial load.

4. Update `AppNavigation.kt` — replace hardcoded `userName = "Test User"` and `userEmail = "test@fivucsas.com"` with values from `SessionManager`.

5. Register `SessionManager` as a singleton in `AppModule.kt`.

**Files to create/modify:**
- `shared/.../data/local/SessionManager.kt` — Create
- `shared/.../data/repository/AuthRepositoryImpl.kt` — Modify (save user on login)
- `shared/.../di/AppModule.kt` — Modify (register SessionManager)
- `androidApp/.../ui/screen/BiometricEnrollScreen.kt` — Modify (pre-fill form)
- `androidApp/.../ui/navigation/AppNavigation.kt` — Modify (use real user data)

---

### FP-2: Liveness Detection in Enrollment Flow

**Priority:** HIGH
**Depends on:** Backend biometric API liveness endpoint

**Problem:**
The infrastructure for liveness detection exists (`CheckLivenessUseCase`, `BiometricRepository.checkLiveness()`, `BiometricConfig.LIVENESS_THRESHOLD`) but is not wired into the enrollment or verification flows. A static photo can pass enrollment.

**Proposed solution:**
Add a liveness check step between photo capture and enrollment submission. Options:
- **Server-side liveness:** Send the captured image to the `/biometric/liveness` endpoint before enrollment. Simplest approach, requires backend support.
- **Client-side liveness:** Use ML Kit Face Detection to require actions (blink, smile, turn head) before capturing. More secure but requires adding the ML Kit dependency.

**Recommended approach:** Start with server-side liveness (minimal client changes), add client-side as a second layer later.

---

### FP-3: Client-side Image Quality Checks

**Priority:** MEDIUM

**Problem:**
`BiometricConfig` defines quality thresholds (brightness, sharpness, blur) but no client-side checks are performed. Poor quality images are sent to the backend, wasting API calls and causing failed enrollments.

**Proposed solution:**
After capture (in the preview step), analyze the bitmap before allowing submission:
- **Brightness check:** Calculate average pixel luminance, reject if below `MIN_BRIGHTNESS` or above `MAX_BRIGHTNESS`.
- **Blur detection:** Apply Laplacian variance calculation on the bitmap. Reject if below `MIN_SHARPNESS`.
- Show user-friendly feedback: "Photo is too dark — try again with better lighting."

This fits naturally in the `PhotoPreviewContent` composable, between capture and submit.

---

### FP-4: ML Kit Face Detection for Guided Capture

**Priority:** LOW
**Dependency:** `com.google.mlkit:face-detection`

**Problem:**
The circular face guide is purely visual. There is no validation that a face is actually present or properly positioned in the frame.

**Proposed solution:**
Integrate ML Kit Face Detection to:
- Detect face presence in real-time during camera preview
- Show green/red frame indicator based on face position
- Require face centered within the guide before enabling the capture button
- Optionally detect face landmarks for liveness (eye open/closed, smile)

---

### FP-5: ViewModel Lifecycle Awareness

**Priority:** MEDIUM

**Problem:**
`BiometricViewModel` is a plain Kotlin class, not `androidx.lifecycle.ViewModel`. Combined with Koin `factoryOf`, a new instance is created on every injection. State is lost on configuration changes (screen rotation).

**Proposed solution:**
- Add `androidx.lifecycle:lifecycle-viewmodel` multiplatform dependency (available since Lifecycle 2.8+)
- Extend `BiometricViewModel` from `ViewModel`
- Change Koin registration from `factoryOf` to `viewModelOf`
- Use `koinViewModel()` instead of `koinInject()` in composables

This affects all ViewModels in the app (`LoginViewModel`, `RegisterViewModel`, `BiometricViewModel`, etc.) and should be done as a single coordinated change.

---

## Reference

### Enrollment Screen Flow Diagram

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────┐
│          │     │              │     │              │     │          │
│   Form   │────>│    Camera    │────>│   Preview    │────>│ Success  │
│          │     │   Capture    │     │   (Review)   │     │          │
└──────────┘     └──────────────┘     └──────────────┘     └──────────┘
     ^                  ^                    │                    │
     │                  │                    │                    │
     │                  └── Retake ──────────┘                   │
     │                                                           │
     └──────────────── Back ─────────────────────────────────────┘
```

### Files Overview

```
androidApp/src/main/kotlin/com/fivucsas/mobile/android/
├── ui/
│   ├── screen/
│   │   ├── BiometricEnrollScreen.kt   ← Multi-step enrollment (form + capture + preview)
│   │   └── BiometricVerifyScreen.kt   ← Camera capture + verification result
│   ├── util/
│   │   └── ImageCaptureUtils.kt       ← Shared camera bitmap utilities
│   └── navigation/
│       └── AppNavigation.kt           ← Route definitions + screen wiring
│
shared/src/commonMain/kotlin/com/fivucsas/shared/
├── presentation/viewmodel/auth/
│   └── BiometricViewModel.kt          ← enrollFace, verifyFace, onCaptureError
├── domain/
│   ├── usecase/enrollment/
│   │   └── EnrollUserUseCase.kt       ← 5-step enrollment with rollback
│   ├── model/
│   │   └── EnrollmentData.kt          ← Form data model
│   └── validation/
│       └── ValidationRules.kt         ← Shared validation (name, email, TC ID, phone)
└── config/
    └── BiometricConfig.kt             ← Thresholds, timeouts, quality settings
```
