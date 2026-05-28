# FIVUCSAS — Face and Identity Verification Using Cloud-based SaaS

## Client Apps

**Cross-Platform Applications for Face and Identity Verification Using Cloud-based SaaS**

## Authenticator

Starting in v5.1.0 the Android app ships a standalone TOTP authenticator
(`com.fivucsas.authenticator.*`) that works as a drop-in replacement for
Google Authenticator or Microsoft Authenticator:

- RFC 6238 TOTP with HMAC-SHA1/SHA256/SHA512
- Configurable digits (6/8) and period, 30-second default
- `otpauth://` URI parser compatible with major issuers
- AES256-GCM `EncryptedSharedPreferences` vault (master key held in the
  hardware Keystore)
- Compose Material 3 UI: grouped code, per-account countdown ring,
  tap-to-copy, swipe-delete confirmation, manual account entry

The core engine lives in `shared/src/commonMain/kotlin/com/fivucsas/authenticator/totp/`
via KMP `expect`/`actual`, so iOS and Desktop will reuse it once their HMAC
actuals land (tracked in `docs/TODO.md`). QR code scanning is deferred; manual
entry is the supported flow in 5.1.0.


## Feature coverage matrix (hosted-first, since 2026-04-16)

On 2026-04-16 FIVUCSAS pivoted to **hosted-first authentication**: native
clients are thin OAuth 2.0 / OIDC clients that hand off to
`verify.fivucsas.com/login` and receive an `?code=…&state=…` callback. Face,
Voice, Fingerprint, and NFC live on the hosted login page — native apps no
longer reimplement them. The parity matrix collapsed from 20 reference
columns to **13 thin-OAuth-client columns**: OAuth login, secure token
storage, token refresh, deep-link handler, account dashboard, cross-device
sessions, GDPR/KVKK export, offline display, push/WebSocket approval
handler, TOTP authenticator (companion), QR display + scanner, signed
release artifact, public distribution.

**Platform status (2026-04-18e):**

- **Android: 13 / 13** — v5.2.0-rc1 shipped 2026-04-18e.
- **Desktop (Windows + Linux): scaffolding in flight** — Agents B/C/D on OAuth loopback + secure storage + installers (e.g. `desktopApp/.../security/SecureTokenStorage.kt`).
- **iOS: 0 / 13** — Phase 2 (July 2026), blocked on Apple Developer enrollment; no `iosApp/` module yet.
- **macOS: out of scope** — no Mac available for `codesign` / `notarytool`.

**Canonical plan:** [`../docs/plans/CLIENT_APPS_PARITY.md`](../docs/plans/CLIENT_APPS_PARITY.md)
— rewritten 2026-04-18 with the 13-column matrix, per-platform gap analysis,
release criteria, test strategy, and the pre-pivot 20-row matrix archived in
its Appendix A.

Pivot context: [`../web-app/docs/AUDIT_REPORT_2026-04-16.md`](../web-app/docs/AUDIT_REPORT_2026-04-16.md)
and [`../web-app/docs/plans/HOSTED_LOGIN_INTEGRATION.md`](../web-app/docs/plans/HOSTED_LOGIN_INTEGRATION.md)
(PR-1 hosted-login spec, merged to `main` 2026-04-16).


## Project Status

**Version:** 1.1.0
**Status:** PRODUCTION READY (Phase 1 complete -- build fixes, FIDO2/WebAuthn, permissions)
**Architecture:** Clean Architecture + MVVM + Kotlin Multiplatform
**Platforms:** Android (APK releases), Desktop (JVM), iOS (CI ready)
**CI/CD:** Android Build + iOS Build workflows GREEN (GitHub Actions, self-hosted runner)

---

## 🏗️ Architecture Overview (Kotlin Multiplatform)

This project uses **Kotlin Multiplatform (KMP)** with **Compose Multiplatform** for cross-platform
development.

### Project Structure

```
client-apps/
├── shared/                           # Shared KMP module
│   └── src/
│       ├── commonMain/kotlin/com/fivucsas/shared/
│       │   ├── config/              # Centralized configuration
│       │   │   ├── AppConfig.kt     # API, cache, session settings
│       │   │   ├── UIDimens.kt      # UI dimensions and spacing
│       │   │   ├── AnimationConfig.kt # Animation timing
│       │   │   └── BiometricConfig.kt # Biometric thresholds
│       │   ├── domain/              # Business logic
│       │   │   ├── model/           # Domain entities
│       │   │   ├── usecase/         # Use cases
│       │   │   └── repository/      # Repository interfaces
│       │   ├── data/                # Data layer
│       │   │   ├── repository/      # Repository implementations
│       │   │   └── remote/          # API services
│       │   ├── presentation/        # Presentation layer
│       │   │   ├── viewmodel/       # ViewModels (AdminVM, KioskVM)
│       │   │   └── state/           # UI state classes
│       │   ├── platform/            # Platform abstractions
│       │   │   ├── ICameraService.kt
│       │   │   ├── ILogger.kt
│       │   │   └── ISecureStorage.kt
│       │   └── ui/                  # Shared UI components
│       │       ├── theme/           # AppColors, AppTypography, AppShapes
│       │       └── components/      # Atomic Design components
│       │           ├── atoms/       # Buttons, Text, TextFields
│       │           ├── molecules/   # Cards, Dialogs, Messages
│       │           └── organisms/   # AppBars, Layouts, EmptyState
│       └── commonTest/              # Shared tests
│           └── kotlin/com/fivucsas/shared/
│               ├── presentation/viewmodel/ # ViewModel tests
│               └── test/mocks/      # Mock implementations
│
├── desktopApp/                      # Desktop application
│   └── src/desktopMain/kotlin/com/fivucsas/desktop/
│       ├── ui/
│       │   ├── admin/              # Admin Dashboard (refactored)
│       │   │   ├── AdminDashboard.kt (150 lines)
│       │   │   ├── tabs/           # UsersTab, AnalyticsTab, etc.
│       │   │   ├── dialogs/        # AddUser, EditUser, DeleteUser
│       │   │   └── components/     # NavigationRail, Constants
│       │   └── kiosk/              # Kiosk Mode (refactored)
│       │       ├── KioskMode.kt (100 lines)
│       │       └── screens/        # Welcome, Enroll, Verify
│       └── platform/               # Desktop implementations
│           ├── DesktopCameraServiceImpl.kt
│           ├── DesktopLoggerImpl.kt
│           └── DesktopSecureStorageImpl.kt
│
└── androidApp/                      # Android application
```

---

## 🎨 Shared UI Component Library

Following **Atomic Design** principles, the shared UI library provides consistent, reusable
components:

### Theme System

- **AppColors** - Complete color palette (Primary, Secondary, Semantic, Gradients)
- **AppTypography** - Material Design 3 typography scale
- **AppShapes** - Consistent shape system

### Atoms

- `PrimaryButton`, `SecondaryButton`, `AppTextButton`, `KioskButton`
- `AppTextField`, `SearchTextField`
- `BodyText`, `TitleText`, `HeadlineText`, etc.
- `AppLoadingIndicator`, `LoadingBox`
- `AppSpacer`, `VerticalSpacer*`, `HorizontalSpacer*`

### Molecules

- `AppCard`, `InfoCard`, `StatCard`, `ClickableCard`
- `ConfirmationDialog`, `InfoDialog`, `FormDialog`
- `MessageBanner`, `SuccessMessage`, `ErrorMessage`

### Organisms

- `TopAppBar`, `SimpleTopAppBar`
- `ScreenLayout`, `CardContainerLayout`
- `EmptyState`

---

## 🔧 Configuration System

All magic numbers and constants are centralized in the `config` package:

```kotlin
// API Configuration
AppConfig.Api.BASE_URL
AppConfig.Api.TIMEOUT_SECONDS
AppConfig.Session.TIMEOUT_MINUTES

// UI Dimensions
UIDimens.SpacingSmall    // 8.dp
UIDimens.SpacingMedium   // 16.dp
UIDimens.ButtonHeight    // 48.dp
UIDimens.IconSmall       // 24.dp

// Animation Timing
AnimationConfig.DURATION_NORMAL      // 300ms
AnimationConfig.DELAY_VERIFICATION   // 2000ms

// Biometric Thresholds
BiometricConfig.CONFIDENCE_THRESHOLD // 0.85
BiometricConfig.LIVENESS_THRESHOLD   // 0.80
```

---

## 🧪 Testing

### Unit Tests

The project includes comprehensive ViewModel tests with mock implementations:

```bash
# Run shared module tests
./gradlew :shared:testDebugUnitTest
```

### Test Coverage

- **Total Kotlin tests:** 505 (static count: 447 commonTest + 33 androidTest + 25 desktopTest `@Test` annotations; as of 2026-05-28)
- **AdminViewModel**: 25+ tests (navigation, CRUD, search, dialogs)
- **KioskViewModel**: 25+ tests (navigation, enrollment, verification)

### Mock System

Mock implementations in `shared/src/commonTest/kotlin/.../test/mocks/`:

- `MockGetUsersUseCase`, `MockDeleteUserUseCase`, etc.
- `MockEnrollUserUseCase`, `MockVerifyUserUseCase`, etc.

---

## 🔌 Platform Abstractions

Interfaces enable testability and cross-platform support:

```kotlin
// Camera operations
interface ICameraService {
    suspend fun initialize(): Result<Unit>
    suspend fun captureFrame(): Result<ByteArray>
    fun isAvailable(): Boolean
    suspend fun release()
}

// Logging
interface ILogger {
    fun debug(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable?)
}

// Secure storage
interface ISecureStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
}
```

Desktop implementations are in `desktopApp/.../platform/`.

---

## Recent Changes (Phase 1 -- 2026-04-04)

- Build fixes: deprecated API migrations, Gradle compatibility
- FIDO2/WebAuthn integration via Android Credential Manager (1,058 lines, 19 files)
- RECORD_AUDIO permission added for voice verification
- INTERNET permission fix (app was crashing without it)
- Android + iOS CI workflows both GREEN

## Getting Started

### Prerequisites

- JDK 17+
- Kotlin 2.0+
- Gradle 8.0+ (wrapper included)
- Android SDK (API 34+)

### Build & Run

```bash
# Android debug APK (no keystore or secrets needed)
./gradlew :androidApp:assembleDebug

# Desktop application
./gradlew :desktopApp:run

# Run all tests
./gradlew test

# Run shared module tests only
./gradlew :shared:testDebugUnitTest
```

### Signed release builds

Signed Android release builds read the keystore path + passwords from
environment variables (CI) or Gradle properties (local dev). **No passwords
are committed.**

For local signed builds add to `local.properties` (already `.gitignored`):

```
android.keystore.path=/absolute/path/to/keystore/release.jks
android.keystore.password=<current-store-password>
android.key.alias=fivucsas
android.key.password=<current-key-password>
```

See [`docs/RELEASE.md`](docs/RELEASE.md) for the keystore rotation procedure,
required GitHub secrets, and emergency revocation playbook.

If no password is configured, `assembleRelease` falls back to debug signing
with a warning — this is intentional so that new contributors and forks can
still produce an installable APK.

---

## FIDO2/WebAuthn Integration

The Android app integrates FIDO2/WebAuthn via the Credential Manager API:

- Passkey registration and authentication
- Platform authenticator (fingerprint/face unlock) support
- Server-side challenge generation and verification
- Integrated with the identity-core-api WebAuthn endpoints

Key files:
- `androidApp/.../fido2/` -- Credential Manager wrapper
- `shared/.../domain/usecase/` -- WebAuthn use cases
- `shared/.../data/remote/` -- API client for WebAuthn endpoints

## Architecture Refactoring History (Phase 0)

The codebase underwent a comprehensive 14-day architectural refactoring:

| Phase                       | Achievement                                |
|-----------------------------|--------------------------------------------|
| 0.1 Package Consolidation   | Eliminated duplicate packages (-727 lines) |
| 0.2 Extract Configuration   | Centralized magic numbers (4 config files) |
| 0.3 Shared UI Components    | Created component library (+1,870 lines)   |
| 0.4 Refactor AdminDashboard | 2,326 → 150 lines (93% reduction)          |
| 0.5 Refactor KioskMode      | 1,745 → 100 lines (94% reduction)          |
| 0.6 Platform Abstractions   | Created testable interfaces (7 files)      |
| 0.7 ViewModel Tests         | Added 50+ unit tests                       |
| 0.8 Documentation           | Updated architecture docs                  |

**Result**: Architecture grade improved from B+ to A+

---

## 🤝 Contributing

1. Follow Clean Architecture principles
2. Use shared UI components from the component library
3. Add tests for new ViewModels
4. Use configuration constants instead of magic numbers
5. Implement platform interfaces for testability

---

## 📄 License

Part of the FIVUCSAS platform developed at Marmara University.

Copyright 2025-2026 FIVUCSAS Team. All rights reserved.

---

**Built with Kotlin Multiplatform + Compose Multiplatform** | FIVUCSAS Team 2025-2026
