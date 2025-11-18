# FIVUCSAS Mobile App

**Facial Identity Verification Using Computer Vision and Sensor-Augmented Systems**

## 🎯 Project Status

**Version:** 1.0.0
**Status:** ✅ **PRODUCTION READY** (Architecture Refactored!)
**Architecture:** Clean Architecture + MVVM + Kotlin Multiplatform
**Platforms:** Desktop ✅, Android ✅, iOS (ready)

---

## 🏗️ Architecture Overview (Kotlin Multiplatform)

This project uses **Kotlin Multiplatform (KMP)** with **Compose Multiplatform** for cross-platform development.

### Project Structure

```
mobile-app/
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

Following **Atomic Design** principles, the shared UI library provides consistent, reusable components:

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

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Kotlin 1.9+
- Gradle 8.0+

### Build & Run

```bash
# Desktop application
./gradlew :desktopApp:run

# Android application
./gradlew :androidApp:assembleDebug

# Run tests
./gradlew test
```

---

## 📋 Recent Refactoring (Phase 0)

The codebase underwent a comprehensive 14-day architectural refactoring:

| Phase | Achievement |
|-------|-------------|
| 0.1 Package Consolidation | Eliminated duplicate packages (-727 lines) |
| 0.2 Extract Configuration | Centralized magic numbers (4 config files) |
| 0.3 Shared UI Components | Created component library (+1,870 lines) |
| 0.4 Refactor AdminDashboard | 2,326 → 150 lines (93% reduction) |
| 0.5 Refactor KioskMode | 1,745 → 100 lines (94% reduction) |
| 0.6 Platform Abstractions | Created testable interfaces (7 files) |
| 0.7 ViewModel Tests | Added 50+ unit tests |
| 0.8 Documentation | Updated architecture docs |

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

Copyright © 2025 FIVUCSAS Team. All rights reserved.

---

**Built with Kotlin Multiplatform** | FIVUCSAS Team © 2025
