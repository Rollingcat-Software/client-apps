# Quick Reference - FIVUCSAS Client Apps

**Kotlin Multiplatform (KMP) + Compose Multiplatform cross-platform application**

---

## Platforms

- Android -- Full support (APK via GitHub Releases)
- iOS -- Build ready (CI workflow GREEN)
- Desktop (JVM) -- Full support

**One shared codebase, all platforms.**

---

## Super Quick Start

```bash
# 1. Build Android APK
./gradlew :androidApp:assembleDebug

# 2. Run desktop app
./gradlew :desktopApp:run

# 3. Run tests
./gradlew :shared:testDebugUnitTest
```

---

## Documentation Map

| File                    | What It Is                     | Read When            |
|-------------------------|--------------------------------|----------------------|
| **GET_STARTED.md**      | Overview and quick start       | First time here      |
| **QUICKSTART.md**       | Detailed setup steps           | Setting up project   |
| **QUICK_REFERENCE.md**  | Command reference (this file)  | Quick lookup         |
| **README.md**           | Full documentation             | Reference            |

---

## Android Studio Workflow

### Setup (One Time)

1. Install Android Studio (Ladybug+)
2. Install Kotlin Multiplatform plugin
3. Open this folder as project
4. Sync Gradle
5. Create emulator or connect device

### Daily Development

1. Open project in Android Studio
2. Start emulator (Tools -> Device Manager)
3. Select `androidApp` configuration
4. Click Run
5. Edit shared code -> rebuild
6. Commit and push

### Key Shortcuts

- **Shift+F10** -- Run app
- **Ctrl+Alt+L** -- Format code
- **Alt+Enter** -- Quick fix
- **Shift Shift** -- Search everything
- **Ctrl+B** -- Go to definition

---

## Project Structure

```
client-apps/
├── shared/              # Shared KMP module (business logic + UI)
│   └── src/
│       ├── commonMain/  # Common Kotlin code
│       └── commonTest/  # Shared tests
├── androidApp/          # Android application
├── desktopApp/          # Desktop (JVM) application
├── build.gradle.kts     # Root build config
└── settings.gradle.kts  # Module declarations
```

---

## Common Commands

```bash
# Build
./gradlew :androidApp:assembleDebug       # Android debug APK
./gradlew :androidApp:assembleRelease     # Android release APK
./gradlew :desktopApp:run                 # Run desktop app

# Test
./gradlew test                            # All tests
./gradlew :shared:testDebugUnitTest       # Shared module tests only

# Maintenance
./gradlew clean                           # Clean build
./gradlew dependencies                    # Show dependency tree
./gradlew lint                            # Run linter

# Useful
./gradlew tasks --all                     # List all available tasks
```

---

## Architecture Quick Reference

```
┌─────────────────────────────────┐
│    Presentation (UI)            │
│    Compose screens + ViewModels │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│    Domain (Business Logic)      │
│    Entities + Use Cases         │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│    Data (API, Storage)          │
│    Repositories + API clients   │
└─────────────────────────────────┘
```

**Data Flow:**
1. User taps button (Compose UI)
2. ViewModel calls UseCase (Domain)
3. UseCase calls Repository (Domain interface)
4. Repository fetches from API (Data layer)
5. Data flows back up
6. ViewModel emits new state
7. Compose UI recomposes

---

## Platform Targets

### Primary
- Android 8.0+ (API 26+)
- Desktop JVM (Windows, macOS, Linux)

### Ready
- iOS 15.0+ (CI workflow configured)

---

## Key Configuration Files

| File | Purpose |
|------|---------|
| `shared/.../config/AppConfig.kt` | API URLs, timeouts, session settings |
| `shared/.../config/UIDimens.kt` | UI dimensions and spacing |
| `shared/.../config/BiometricConfig.kt` | Biometric thresholds |
| `shared/.../config/AnimationConfig.kt` | Animation timing |
| `gradle.properties` | Kotlin/Compose/AGP versions |
| `androidApp/src/main/AndroidManifest.xml` | Android permissions |

---

## Troubleshooting

### Gradle sync fails
```bash
./gradlew --stop && ./gradlew clean
```

### "SDK location not found"
Create `local.properties`:
```
sdk.dir=/path/to/android/sdk
```

### Build errors after dependency changes
```bash
./gradlew clean
./gradlew :shared:compileKotlinAndroid
```

---

## Features Implemented

- [x] Admin Dashboard (user CRUD, search, analytics)
- [x] Kiosk Mode (enrollment, verification flows)
- [x] Face detection and liveness check
- [x] NFC document reading
- [x] Voice verification
- [x] FIDO2/WebAuthn (Android Credential Manager)
- [x] Shared Compose UI component library (Atomic Design)
- [x] 50+ ViewModel unit tests
- [x] Android + iOS CI workflows (GitHub Actions)
- [x] APK releases via GitHub Releases

---

## Learning Resources

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [Kotlin coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Material Design 3](https://m3.material.io/)
- [Android Credential Manager](https://developer.android.com/identity/sign-in/credential-manager)

---

*Quick Reference v2.0 -- Updated 2026-04-04*
