# FIVUCSAS Mobile App - How to Run and Test

## Overview

This is a **Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP)** project that targets:

- **Android** - Native Android app
- **iOS** - Native iOS app
- **Desktop** - JVM desktop application (Windows, macOS, Linux)

## Architecture

### Technology Stack

- **Kotlin 1.9.21** - Programming language
- **Compose Multiplatform 1.5.11** - UI framework
- **Ktor 2.3.5** - Networking client
- **Kotlinx Serialization** - JSON handling
- **Kotlinx Coroutines** - Async/await
- **Kotlinx DateTime** - Date/time handling

### Project Structure

```
mobile-app/
├── shared/              # Shared business logic (KMP)
│   ├── commonMain/      # Common code for all platforms
│   ├── androidMain/     # Android-specific implementations
│   ├── iosMain/         # iOS-specific implementations
│   └── desktopMain/     # Desktop-specific implementations
├── androidApp/          # Android app entry point
├── desktopApp/          # Desktop app entry point
└── iosApp/              # iOS app (Xcode project)
```

## Prerequisites

### Required Tools

1. **JDK 11+** (for Android/Shared) and **JDK 21** (for Desktop)
    - Download: https://adoptium.net/
    - Verify: `java -version`

2. **Android Studio** (for Android development)
    - Download: https://developer.android.com/studio
    - Install Android SDK 24-34
    - Install Android Build Tools

3. **Xcode 14+** (for iOS development - macOS only)
    - Install from App Store
    - Install Command Line Tools: `xcode-select --install`

4. **Gradle 8.14+** (included in project)
    - Verify: `.\gradlew --version`

### Recommended Tools

- **IntelliJ IDEA 2023.3+** - Best IDE for KMP development
- **Kotlin Plugin** - Should be bundled with IntelliJ
- **Compose Multiplatform IDE Support Plugin**

## How to Run

### 1. Desktop Application

The desktop app is the **easiest to run** and requires no emulator/device.

```bash
# Navigate to project root
cd C:\Users\ahabg\OneDrive\Belgeler\GitHub\FIVUCSAS\mobile-app

# Run desktop app
.\gradlew :desktopApp:run

# Or build standalone JAR
.\gradlew :desktopApp:packageUberJarForCurrentOS
```

**Output**: Executable JAR in `desktopApp/build/compose/jars/`

**System Requirements**:

- Windows: Windows 10+
- macOS: macOS 10.14+
- Linux: Any modern distribution

### 2. Android Application

#### Option A: Using Android Studio (Recommended)

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select `androidApp` configuration
4. Click Run (▶️) or press `Shift+F10`

#### Option B: Using Command Line

```bash
# Build debug APK
.\gradlew :androidApp:assembleDebug

# Install on connected device/emulator
.\gradlew :androidApp:installDebug

# Build and run
.\gradlew :androidApp:installDebug && adb shell am start -n com.fivucsas.mobile/.MainActivity
```

**Output**: APK in `androidApp/build/outputs/apk/debug/`

**Requirements**:

- Android 7.0+ (API 24+)
- Minimum 2GB RAM
- Camera permission for biometric features

### 3. iOS Application (macOS only)

```bash
# Open iOS project in Xcode
open iosApp/iosApp.xcworkspace

# Or build from command line
.\gradlew :shared:linkDebugFrameworkIosArm64  # For physical device
.\gradlew :shared:linkDebugFrameworkIosX64    # For simulator

# Then open in Xcode and run
```

**Requirements**:

- macOS 12.0+
- Xcode 14.0+
- iOS 12.0+
- Apple Developer Account (for device deployment)

## How to Build

### Build All Targets

```bash
# Build everything (Desktop + Android)
.\gradlew build

# Build specific targets
.\gradlew :desktopApp:assemble      # Desktop
.\gradlew :androidApp:assembleDebug # Android debug
.\gradlew :androidApp:assembleRelease # Android release
```

### Build for Production

#### Desktop

```bash
# Create distributable packages
.\gradlew :desktopApp:packageDistributionForCurrentOS

# Creates:
# - Windows: MSI installer
# - macOS: DMG
# - Linux: DEB package
```

Output: `desktopApp/build/compose/binaries/main/`

#### Android

```bash
# Build release APK (split by ABI)
.\gradlew :androidApp:assembleRelease

# Build App Bundle (for Play Store)
.\gradlew :androidApp:bundleRelease
```

**Before release build**, configure signing in `androidApp/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("path/to/keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

#### iOS

1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Select `Product` → `Archive`
3. Follow App Store Connect upload process

## Testing

### Unit Tests

```bash
# Run all tests
.\gradlew test

# Run specific module tests
.\gradlew :shared:testDebugUnitTest
.\gradlew :androidApp:testDebugUnitTest
.\gradlew :desktopApp:test

# With coverage report
.\gradlew test jacocoTestReport
```

**Coverage reports**: `build/reports/jacoco/test/html/index.html`

### Android Instrumented Tests

```bash
# Run on connected device/emulator
.\gradlew :androidApp:connectedDebugAndroidTest
```

### Desktop Integration Tests

```bash
.\gradlew :desktopApp:test
```

## Troubleshooting

### Common Issues

#### 1. Kotlin/Compose Version Mismatch

**Error**: `Compose Multiplatform X doesn't support Kotlin Y`

**Solution**: Check compatibility matrix:

- Compose 1.5.11 → Kotlin 1.9.21 ✅
- See: https://github.com/JetBrains/compose-jb/blob/master/VERSIONING.md

#### 2. iOS Build Fails (Windows)

**Error**: iOS targets cannot be built on Windows

**Solution**: This is expected. iOS development requires macOS. Add to `gradle.properties`:

```properties
kotlin.native.ignoreDisabledTargets=true
```

#### 3. Gradle Daemon Issues

```bash
# Stop all daemons
.\gradlew --stop

# Clean and rebuild
.\gradlew clean build
```

#### 4. Android Build Fails

```bash
# Invalidate caches
# In Android Studio: File → Invalidate Caches → Invalidate and Restart

# Or from command line
.\gradlew clean
Remove-Item -Recurse -Force .gradle, build, */build
.\gradlew build
```

#### 5. Desktop App Doesn't Start

**Error**: `UnsupportedClassVersionError`

**Solution**: Ensure JDK 21 is installed and JAVA_HOME is set:

```bash
# Check Java version
java -version  # Should show 21.x

# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
```

#### 6. Camera/Permissions Issues on Android

Add to `androidApp/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

## Configuration

### Environment Variables

Create `.env` file in project root:

```properties
# API Configuration
API_BASE_URL=http://10.0.2.2:8080/api/v1  # Android emulator
# API_BASE_URL=http://localhost:8080/api/v1  # Desktop/iOS simulator

BIOMETRIC_API_URL=http://10.0.2.2:8001/api/v1

# App Configuration
APP_NAME=FIVUCSAS
ENVIRONMENT=development
LOG_LEVEL=DEBUG
```

**Note**: Android emulator uses `10.0.2.2` to access host machine's `localhost`.

### Gradle Properties

Edit `gradle.properties`:

```properties
# Performance
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# Kotlin
kotlin.code.style=official
kotlin.native.ignoreDisabledTargets=true

# Android
android.useAndroidX=true
android.enableJetifier=true
```

## Development Workflow

### 1. Project Setup

```bash
# Clone and setup
git clone <repo-url>
cd mobile-app

# Build shared module first
.\gradlew :shared:build

# Run desktop app for quick testing
.\gradlew :desktopApp:run
```

### 2. Making Changes

```bash
# After code changes, rebuild shared module
.\gradlew :shared:build

# Then run target platform
.\gradlew :desktopApp:run           # Desktop
.\gradlew :androidApp:installDebug  # Android
```

### 3. Code Quality

```bash
# Kotlin linting
.\gradlew ktlintCheck

# Format code
.\gradlew ktlintFormat

# Static analysis
.\gradlew detekt
```

## IDE Setup

### IntelliJ IDEA / Android Studio

1. **Import Project**
    - `File` → `Open` → Select `mobile-app` folder
    - Wait for Gradle sync

2. **Configure Run Configurations**

   **Desktop:**
    - Main class: `com.fivucsas.desktop.MainKt`
    - Module: `desktopApp.main`
    - JRE: 21

   **Android:**
    - Module: `androidApp`
    - Use existing `androidApp` configuration

3. **Enable Compose Support**
    - `Settings` → `Languages & Frameworks` → `Kotlin` → Enable "Compose"

### VS Code

Install extensions:

- Kotlin Language
- Gradle for Java

## Performance Optimization

### Desktop

```kotlin
// In desktopApp/src/desktopMain/kotlin/Main.kt
application {
    nativeDistributions {
        targetFormats(TargetFormat.Msi)
        packageName = "FIVUCSAS"
        packageVersion = "1.0.0"
        
        windows {
            // Optimize Windows build
            menuGroup = "FIVUCSAS"
            perUserInstall = true
        }
    }
}
```

### Android

```kotlin
// In androidApp/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## Next Steps

After successfully running the app:

1. **Configure Backend URLs** in
   `shared/src/commonMain/kotlin/com/fivucsas/mobile/data/remote/ApiClient.kt`
2. **Test Authentication** - Register → Login → Logout flow
3. **Test Biometric Features** - Enroll → Verify
4. **Review Architecture** - Check `shared/src/commonMain` for business logic
5. **Customize UI** - Modify Compose UI in respective platform modules

## Additional Resources

- **Kotlin Multiplatform**: https://kotlinlang.org/docs/multiplatform.html
- **Compose Multiplatform**: https://www.jetbrains.com/lp/compose-multiplatform/
- **Ktor Client**: https://ktor.io/docs/client.html
- **Kotlinx Serialization**: https://github.com/Kotlin/kotlinx.serialization

## Support

For issues specific to:

- **KMP Setup**: Check Kotlin Slack #multiplatform
- **Compose UI**: Check Kotlin Slack #compose
- **Android Issues**: Check Android Studio logs
- **iOS Issues**: Check Xcode console

---

**Last Updated**: 2025-10-31  
**Kotlin Version**: 1.9.21  
**Compose Multiplatform Version**: 1.5.11
