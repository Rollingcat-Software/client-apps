# Get Started - FIVUCSAS Client Apps

Welcome! This is the **client-apps** module for the FIVUCSAS project --- a Kotlin Multiplatform (KMP) application targeting Android, iOS, and Desktop.

## What You Have

This project is a fully working Kotlin Multiplatform application with:

- **shared/** -- Common business logic, ViewModels, UI components (Compose Multiplatform)
- **androidApp/** -- Android application module
- **desktopApp/** -- Desktop (JVM) application module
- **iOS target** -- Ready for SwiftUI wrappers

## Quick Start

### Prerequisites

- **JDK 17+** (Temurin/Corretto recommended)
- **Android Studio Ladybug+** with Kotlin Multiplatform plugin
- **Android SDK** (API 34+ recommended)
- **Gradle 8.0+** (wrapper included)

### Option 1: Android Studio (Recommended)

1. Open this folder in Android Studio
2. Install the **Kotlin Multiplatform** plugin (Settings -> Plugins)
3. Sync Gradle (automatic on open)
4. Select `androidApp` run configuration
5. Click Run

### Option 2: Command Line

```bash
# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Run desktop app
./gradlew :desktopApp:run

# Run shared module tests
./gradlew :shared:testDebugUnitTest
```

## Project Structure

```
client-apps/
├── shared/                        # Shared KMP module
│   └── src/
│       ├── commonMain/            # Common Kotlin code
│       │   └── kotlin/com/fivucsas/shared/
│       │       ├── config/        # AppConfig, UIDimens, BiometricConfig
│       │       ├── domain/        # Models, use cases, repository interfaces
│       │       ├── data/          # Repository implementations, API services
│       │       ├── presentation/  # ViewModels, UI state classes
│       │       ├── platform/      # Platform abstraction interfaces
│       │       └── ui/            # Shared Compose UI (theme, components)
│       └── commonTest/            # Shared unit tests
├── androidApp/                    # Android application
│   └── src/main/
│       ├── kotlin/                # Android-specific code
│       └── AndroidManifest.xml    # Permissions (CAMERA, INTERNET, RECORD_AUDIO, etc.)
├── desktopApp/                    # Desktop (JVM) application
│   └── src/desktopMain/kotlin/    # Desktop-specific code
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Module declarations
└── gradle.properties              # Kotlin/Compose versions
```

## Architecture

This app follows **Clean Architecture + MVVM** with Kotlin Multiplatform:

```
┌──────────────────────────────────┐
│     Presentation Layer           │
│  (Compose UI + ViewModels)       │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│      Domain Layer                │
│  (Entities + Use Cases)          │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│       Data Layer                 │
│  (Repositories + API clients)    │
└──────────────────────────────────┘
```

- **ViewModels** in shared module (AdminViewModel, KioskViewModel)
- **Compose Multiplatform** for shared UI
- **Platform interfaces** (ICameraService, ILogger, ISecureStorage) for testability

## Features

### Implemented
- [x] Admin Dashboard (user management, analytics)
- [x] Kiosk Mode (enrollment, verification)
- [x] Face detection and liveness
- [x] NFC document reading
- [x] Voice verification
- [x] FIDO2/WebAuthn integration (Credential Manager)
- [x] Shared UI component library (Atomic Design)
- [x] Centralized configuration system
- [x] 50+ ViewModel unit tests

### Upcoming
- [ ] iOS SwiftUI wrappers
- [ ] Desktop NFC (javax.smartcardio)
- [ ] Windows Hello (JNA)
- [ ] Offline caching

## Development Workflow

### 1. Start Backend Services

```bash
cd ../identity-core-api
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d

cd ../biometric-processor
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

### 2. Build and Run

```bash
# Android (debug APK)
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run

# All tests
./gradlew test
```

### 3. Common Tasks

```bash
# Clean build
./gradlew clean

# Check for dependency updates
./gradlew dependencyUpdates

# Lint
./gradlew lint
```

## Troubleshooting

### "SDK location not found"
Set `ANDROID_HOME` environment variable or create `local.properties` with `sdk.dir=/path/to/android/sdk`.

### Gradle sync fails
```bash
./gradlew --stop
./gradlew clean
./gradlew :shared:compileKotlinAndroid
```

### "Unable to connect to API"
Check that backend services are running and API URLs in `AppConfig.kt` are correct.

## Documentation

1. **GET_STARTED.md** (this file) -- Start here
2. **QUICKSTART.md** -- Detailed setup steps
3. **QUICK_REFERENCE.md** -- Command reference and architecture overview
4. **README.md** -- Full project documentation

## Learning Resources

- [Kotlin Multiplatform docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [Kotlin coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android Credential Manager (FIDO2)](https://developer.android.com/identity/sign-in/credential-manager)

---

*Last updated: 2026-04-04*
