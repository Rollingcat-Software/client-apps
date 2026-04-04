# Quick Start Guide - FIVUCSAS Client Apps

This guide will help you set up and build the Kotlin Multiplatform client applications.

## Prerequisites

1. **JDK 17+**
   - Download from: https://adoptium.net/ (Eclipse Temurin recommended)
   - Verify: `java -version`

2. **Android SDK**
   - Install via Android Studio or command-line tools
   - Set `ANDROID_HOME` environment variable
   - Install API 34+ via SDK Manager

3. **Android Studio** (recommended)
   - Download from: https://developer.android.com/studio
   - Install Kotlin Multiplatform plugin

4. **Verify Installation**
   ```bash
   java -version          # JDK 17+
   echo $ANDROID_HOME     # Should point to Android SDK
   ./gradlew --version    # Gradle wrapper
   ```

## Step-by-Step Setup

### 1. Clone and Open

```bash
# If cloning the parent repo
git clone --recurse-submodules <fivucsas-repo-url>
cd fivucsas/client-apps

# Or if already cloned
cd client-apps
```

Open this folder in Android Studio as a Gradle project.

### 2. Sync Gradle

Android Studio will auto-sync on open. If not:
- File -> Sync Project with Gradle Files
- Or from terminal: `./gradlew :shared:compileKotlinAndroid`

### 3. Build Android APK

```bash
# Debug build
./gradlew :androidApp:assembleDebug

# APK location: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### 4. Run Desktop App

```bash
./gradlew :desktopApp:run
```

### 5. Run Tests

```bash
# Shared module unit tests
./gradlew :shared:testDebugUnitTest

# All tests
./gradlew test
```

## Project Structure

```
client-apps/
├── shared/                           # Shared KMP module
│   └── src/
│       ├── commonMain/kotlin/com/fivucsas/shared/
│       │   ├── config/              # AppConfig, UIDimens, BiometricConfig
│       │   ├── domain/              # Models, use cases, repository interfaces
│       │   ├── data/                # Repository impls, API services
│       │   ├── presentation/        # ViewModels, UI state
│       │   ├── platform/            # ICameraService, ILogger, ISecureStorage
│       │   └── ui/                  # Shared Compose components
│       │       ├── theme/           # AppColors, AppTypography, AppShapes
│       │       └── components/      # atoms/, molecules/, organisms/
│       └── commonTest/              # ViewModel tests + mocks
├── androidApp/                      # Android app module
│   └── src/main/
│       ├── kotlin/                  # Android-specific implementations
│       └── AndroidManifest.xml      # Permissions
├── desktopApp/                      # Desktop (JVM) app module
│   └── src/desktopMain/kotlin/      # Desktop-specific implementations
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Configuration

### API Endpoints

Edit `shared/src/commonMain/kotlin/com/fivucsas/shared/config/AppConfig.kt`:

```kotlin
object Api {
    const val BASE_URL = "https://auth.rollingcatsoftware.com/api/v1"
    // For local development:
    // const val BASE_URL = "http://10.0.2.2:8080/api/v1"  // Android emulator
    // const val BASE_URL = "http://localhost:8080/api/v1"  // Desktop/iOS
}
```

### Android Permissions

The `AndroidManifest.xml` includes:
- `INTERNET` -- API communication
- `CAMERA` -- Face detection/enrollment
- `RECORD_AUDIO` -- Voice verification
- `NFC` -- Document reading

## Build for Production

### Android

```bash
# Release APK (requires signing config)
./gradlew :androidApp:assembleRelease

# GitHub Actions builds APK automatically on push to main
```

### Desktop

```bash
# Run
./gradlew :desktopApp:run

# Package (platform-specific installer)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

## Useful Commands

```bash
# Clean build
./gradlew clean

# Dependency tree
./gradlew :shared:dependencies

# Run linter
./gradlew lint

# Check for outdated dependencies
./gradlew dependencyUpdates

# List all available tasks
./gradlew tasks --all
```

## Troubleshooting

### Gradle sync fails
```bash
./gradlew --stop
./gradlew clean
./gradlew :shared:compileKotlinAndroid
```

### "SDK location not found"
Create `local.properties` in the project root:
```properties
sdk.dir=/path/to/android/sdk
```

### Build errors after adding dependencies
```bash
./gradlew clean
./gradlew :androidApp:assembleDebug
```

### Desktop app crashes on launch
Ensure JDK 17+ is used:
```bash
java -version
```

## CI/CD

Both Android and iOS builds run automatically via GitHub Actions:
- **Android Build**: triggers on push to `main`, produces debug APK artifact
- **iOS Build**: triggers on push to `main`, builds framework

## Next Steps

1. Read `README.md` for full documentation
2. Explore the `shared/` module for business logic
3. Check `QUICK_REFERENCE.md` for command cheatsheet
4. Start backend services before running the app

## Support

- Architecture docs: `README.md`
- Parent project: `../CLAUDE.md`
- API docs: https://auth.rollingcatsoftware.com/swagger-ui.html

---

*Last updated: 2026-04-04*
