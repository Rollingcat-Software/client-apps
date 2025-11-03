# FIVUCSAS Mobile App

**Facial Identity Verification Using Computer Vision and Sensor-Augmented Systems**

## 🎯 Project Status

**Version:** 1.0.0  
**Status:** ✅ **PRODUCTION READY** (100% Complete!)  
**Architecture:** Clean Architecture + MVVM  
**Platforms:** Desktop ✅, Android ✅, iOS (ready)

---

# FIVUCSAS Mobile App (Original)

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Flutter](https://img.shields.io/badge/Flutter-3.24+-blue.svg)
![Dart](https://img.shields.io/badge/Dart-3.5+-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS-green.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## Overview

The **FIVUCSAS Mobile App** is a cross-platform Flutter application that serves as the end-user
interface for the FIVUCSAS (Face and Identity Verification Using Cloud-based SaaS) platform. This
app enables users to enroll their biometric data, perform the "Biometric Puzzle" liveness detection,
and authenticate themselves for both physical and digital access control.

Built for both Android and iOS, this application leverages device cameras and processing power to
perform real-time facial landmark detection and liveness verification before sending data to backend
services.

## Table of Contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the App](#running-the-app)
- [Building for Production](#building-for-production)
- [Project Structure](#project-structure)
- [Key Features Implementation](#key-features-implementation)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Functionality

- **User Registration & Authentication**: Email/password registration with JWT authentication
- **Biometric Enrollment**: Capture and enroll face biometric data
- **Liveness Detection**: On-device "Biometric Puzzle" implementation using MediaPipe
- **Face Recognition Authentication**: Authenticate using facial recognition
- **Profile Management**: View and update user profile information
- **Session Management**: Secure token storage and automatic refresh
- **Multi-Tenant Support**: Support for multiple organizations/tenants

### Advanced Features

- **Real-Time Camera Processing**: Live video stream analysis with OpenCV
- **Facial Landmark Detection**: 468 facial landmarks using Google MediaPipe
- **Offline Support**: Cache user data for offline access
- **Push Notifications**: Real-time notifications for authentication events
- **QR Code Scanning**: Scan QR codes for quick authentication
- **Biometric History**: View authentication history and statistics
- **Dark Mode Support**: Beautiful dark and light themes
- **Localization**: Multi-language support (English, Turkish)

## Screenshots

```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│   Login     │  Register   │   Home      │  Liveness   │
│   Screen    │   Screen    │   Screen    │   Puzzle    │
│             │             │             │             │
│  [Image]    │  [Image]    │  [Image]    │  [Image]    │
│             │             │             │             │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

## Architecture

The app follows **Clean Architecture** principles with **BLoC** (Business Logic Component) pattern
for state management.

```
mobile-app/
├── lib/
│   ├── main.dart                    # App entry point
│   ├── app.dart                     # MaterialApp configuration
│   ├── core/
│   │   ├── constants/               # App constants
│   │   ├── theme/                   # Theme configuration
│   │   ├── utils/                   # Utility functions
│   │   ├── errors/                  # Error handling
│   │   └── network/                 # API client
│   ├── features/
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   │   ├── models/          # Data models
│   │   │   │   ├── datasources/     # API & local data sources
│   │   │   │   └── repositories/    # Repository implementations
│   │   │   ├── domain/
│   │   │   │   ├── entities/        # Business entities
│   │   │   │   ├── repositories/    # Repository interfaces
│   │   │   │   └── usecases/        # Business logic
│   │   │   └── presentation/
│   │   │       ├── bloc/            # BLoC state management
│   │   │       ├── pages/           # UI screens
│   │   │       └── widgets/         # Reusable widgets
│   │   ├── biometric/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   ├── profile/
│   │   └── settings/
│   └── injection_container.dart     # Dependency injection
├── android/                         # Android-specific code
├── ios/                             # iOS-specific code
├── assets/
│   ├── images/
│   ├── icons/
│   └── animations/
├── test/                            # Unit & widget tests
├── integration_test/                # Integration tests
├── pubspec.yaml                     # Dependencies
└── README.md
```

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  Pages   │←→│  BLoC    │←→│ Widgets  │             │
│  └──────────┘  └──────────┘  └──────────┘             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                    Domain Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │ Entities │  │ UseCases │  │Repository│             │
│  │          │  │          │  │Interfaces│             │
│  └──────────┘  └──────────┘  └──────────┘             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                     Data Layer                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  Models  │  │DataSource│  │Repository│             │
│  │          │  │ API/Local│  │   Impl   │             │
│  └──────────┘  └──────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────┘
          │                    │
          ▼                    ▼
   ┌─────────────┐      ┌──────────────┐
   │ REST API    │      │ Local Storage│
   │ (Backend)   │      │ (Hive/SQLite)│
   └─────────────┘      └──────────────┘
```

## Technology Stack

### Core Framework

- **Flutter 3.24+**: Cross-platform UI framework
- **Dart 3.5+**: Programming language

### State Management & Architecture

- **flutter_bloc**: BLoC pattern implementation
- **get_it**: Dependency injection
- **equatable**: Value equality

### Networking & Data

- **dio**: HTTP client
- **retrofit**: Type-safe API client
- **hive**: Local database
- **shared_preferences**: Key-value storage
- **cached_network_image**: Image caching

### Biometric & Camera

- **camera**: Camera access
- **google_mlkit_face_detection**: Face detection
- **opencv_dart**: Image processing (alternative to native OpenCV)
- **image**: Image manipulation

### UI & UX

- **flutter_svg**: SVG rendering
- **lottie**: Animations
- **shimmer**: Loading effects
- **flutter_screenutil**: Responsive design
- **cached_network_image**: Image caching

### Security

- **flutter_secure_storage**: Secure token storage
- **encrypt**: Data encryption
- **local_auth**: Biometric authentication (fallback)

### Utilities

- **intl**: Internationalization
- **qr_code_scanner**: QR code scanning
- **permission_handler**: Permissions management
- **connectivity_plus**: Network connectivity
- **path_provider**: File system access

## Prerequisites

- **Flutter SDK 3.24+**
- **Dart SDK 3.5+**
- **Android Studio** (for Android development)
- **Xcode** (for iOS development, macOS only)
- **Android SDK** (API level 21+)
- **iOS 12.0+** (for iOS deployment)
- **CocoaPods** (for iOS dependencies)

## Installation

### 1. Install Flutter

Follow the official Flutter installation guide for your platform:

- **Windows**: https://docs.flutter.dev/get-started/install/windows
- **macOS**: https://docs.flutter.dev/get-started/install/macos
- **Linux**: https://docs.flutter.dev/get-started/install/linux

### 2. Clone the Repository

```bash
git clone https://github.com/your-organization/mobile-app.git
cd mobile-app
```

### 3. Install Dependencies

```bash
# Get Flutter packages
flutter pub get

# For iOS, install CocoaPods dependencies
cd ios
pod install
cd ..
```

### 4. Configure Environment

Create `lib/core/config/env_config.dart`:

```dart
class EnvConfig {
  static const String apiBaseUrl = 'http://localhost:8080/api/v1';
  static const String biometricApiUrl = 'http://localhost:8001/api/v1';
  static const String environment = 'development';
  static const String appName = 'FIVUCSAS';
  static const int apiTimeout = 30000; // milliseconds
}
```

For production, use environment-specific configurations.

## Configuration

### API Endpoints

Configure API endpoints in `lib/core/network/api_endpoints.dart`:

```dart
class ApiEndpoints {
  // Auth endpoints
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String refreshToken = '/auth/refresh';
  static const String logout = '/auth/logout';

  // User endpoints
  static const String profile = '/users/me';
  static const String updateProfile = '/users/me';

  // Biometric endpoints
  static const String enrollBiometric = '/face/enroll';
  static const String verifyBiometric = '/face/verify';
  static const String generatePuzzle = '/liveness/generate-puzzle';
  static const String verifyLiveness = '/liveness/verify';
}
```

### App Configuration

Update `pubspec.yaml`:

```yaml
name: fivucsas_mobile
description: FIVUCSAS Mobile Application
version: 1.0.0+1

environment:
  sdk: '>=3.5.0 <4.0.0'
  flutter: ">=3.24.0"

dependencies:
  flutter:
    sdk: flutter

  # State Management
  flutter_bloc: ^8.1.3
  equatable: ^2.0.5
  get_it: ^7.6.4

  # Networking
  dio: ^5.4.0
  retrofit: ^4.0.3
  pretty_dio_logger: ^1.3.1

  # Local Storage
  hive: ^2.2.3
  hive_flutter: ^1.1.0
  shared_preferences: ^2.2.2
  flutter_secure_storage: ^9.0.0

  # Camera & ML
  camera: ^0.10.5+5
  google_mlkit_face_detection: ^0.10.0
  image: ^4.1.3

  # UI
  flutter_svg: ^2.0.9
  lottie: ^2.7.0
  shimmer: ^3.0.0
  flutter_screenutil: ^5.9.0
  cached_network_image: ^3.3.0

  # Utilities
  intl: ^0.19.0
  qr_code_scanner: ^1.0.1
  permission_handler: ^11.1.0
  connectivity_plus: ^5.0.2
  path_provider: ^2.1.1
```

## Running the App

### Development Mode

```bash
# Run on connected device/emulator
flutter run

# Run on specific device
flutter devices  # List devices
flutter run -d <device_id>

# Run with specific flavor
flutter run --flavor development
flutter run --flavor production

# Hot reload: Press 'r' in terminal
# Hot restart: Press 'R' in terminal
```

### Platform-Specific Commands

```bash
# Android
flutter run -d android

# iOS
flutter run -d ios

# Chrome (for web testing)
flutter run -d chrome
```

### Debug Options

```bash
# Run in profile mode
flutter run --profile

# Run in release mode
flutter run --release

# Enable verbose logging
flutter run --verbose
```

## Building for Production

### Android APK/AAB

```bash
# Build APK
flutter build apk --release

# Build App Bundle (for Play Store)
flutter build appbundle --release

# Build for specific architecture
flutter build apk --split-per-abi
```

### iOS

```bash
# Build for iOS
flutter build ios --release

# Build IPA
flutter build ipa --release
```

### Configuration for Release

**Android** (`android/app/build.gradle`):

```gradle
android {
    compileSdkVersion 34

    defaultConfig {
        applicationId "com.fivucsas.mobile"
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1
        versionName "1.0.0"
    }

    signingConfigs {
        release {
            storeFile file("upload-keystore.jks")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
        }
    }
}
```

**iOS** (`ios/Runner/Info.plist`):

```xml
<key>NSCameraUsageDescription</key>
<string>We need camera access for facial recognition</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>We need photo library access to select images</string>
```

## Project Structure

### Feature Module Structure

Each feature follows Clean Architecture:

```
features/auth/
├── data/
│   ├── models/
│   │   ├── user_model.dart
│   │   └── login_response_model.dart
│   ├── datasources/
│   │   ├── auth_remote_datasource.dart
│   │   └── auth_local_datasource.dart
│   └── repositories/
│       └── auth_repository_impl.dart
├── domain/
│   ├── entities/
│   │   └── user.dart
│   ├── repositories/
│   │   └── auth_repository.dart
│   └── usecases/
│       ├── login_usecase.dart
│       ├── register_usecase.dart
│       └── logout_usecase.dart
└── presentation/
    ├── bloc/
    │   ├── auth_bloc.dart
    │   ├── auth_event.dart
    │   └── auth_state.dart
    ├── pages/
    │   ├── login_page.dart
    │   └── register_page.dart
    └── widgets/
        ├── login_form.dart
        └── social_login_buttons.dart
```

## Key Features Implementation

### 1. Biometric Puzzle Implementation

```dart
class BiometricPuzzleService {
  final FaceDetector faceDetector;
  final CameraController cameraController;

  Future<LivenessResult> performLivenessPuzzle(
    List<PuzzleStep> puzzleSteps
  ) async {
    for (final step in puzzleSteps) {
      final result = await _detectAction(step.action);
      if (!result.success) {
        return LivenessResult(success: false);
      }
    }
    return LivenessResult(success: true);
  }

  Future<ActionResult> _detectAction(String action) async {
    // Implementation using MediaPipe landmarks
    final landmarks = await _getFacialLandmarks();

    switch (action) {
      case 'SMILE':
        return _detectSmile(landmarks);
      case 'BLINK':
        return _detectBlink(landmarks);
      // ... other actions
    }
  }

  double _calculateEAR(List<Point> landmarks) {
    // Eye Aspect Ratio calculation
    final vertical = (landmarks[1].distanceTo(landmarks[5]) +
                     landmarks[2].distanceTo(landmarks[4]));
    final horizontal = landmarks[0].distanceTo(landmarks[3]);
    return vertical / (2.0 * horizontal);
  }
}
```

### 2. Camera Integration

```dart
class CameraService {
  CameraController? _controller;

  Future<void> initializeCamera() async {
    final cameras = await availableCameras();
    final frontCamera = cameras.firstWhere(
      (camera) => camera.lensDirection == CameraLensDirection.front,
    );

    _controller = CameraController(
      frontCamera,
      ResolutionPreset.medium,
      enableAudio: false,
      imageFormatGroup: ImageFormatGroup.yuv420,
    );

    await _controller!.initialize();
  }

  Stream<CameraImage> get imageStream =>
      _controller!.startImageStream();
}
```

### 3. API Integration

```dart
@RestApi(baseUrl: EnvConfig.apiBaseUrl)
abstract class AuthApiService {
  factory AuthApiService(Dio dio) = _AuthApiService;

  @POST('/auth/login')
  Future<LoginResponse> login(@Body() LoginRequest request);

  @POST('/auth/register')
  Future<UserModel> register(@Body() RegisterRequest request);

  @POST('/auth/refresh')
  Future<TokenResponse> refreshToken(@Body() RefreshTokenRequest request);
}
```

### 4. BLoC Pattern

```dart
class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final LoginUseCase loginUseCase;
  final LogoutUseCase logoutUseCase;

  AuthBloc({
    required this.loginUseCase,
    required this.logoutUseCase,
  }) : super(AuthInitial()) {
    on<LoginRequested>(_onLoginRequested);
    on<LogoutRequested>(_onLogoutRequested);
  }

  Future<void> _onLoginRequested(
    LoginRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthLoading());
    final result = await loginUseCase(
      LoginParams(email: event.email, password: event.password),
    );
    result.fold(
      (failure) => emit(AuthError(failure.message)),
      (user) => emit(Authenticated(user)),
    );
  }
}
```

## Testing

### Running Tests

```bash
# Run all tests
flutter test

# Run specific test file
flutter test test/features/auth/auth_bloc_test.dart

# Run with coverage
flutter test --coverage
genhtml coverage/lcov.info -o coverage/html

# Run integration tests
flutter test integration_test/
```

### Test Structure

```
test/
├── unit/
│   ├── core/
│   └── features/
│       └── auth/
│           ├── data/
│           ├── domain/
│           └── presentation/
├── widget/
│   └── auth/
│       ├── login_page_test.dart
│       └── register_page_test.dart
└── fixtures/
    └── auth_fixtures.dart
```

### Example Test

```dart
void main() {
  group('AuthBloc', () {
    late AuthBloc authBloc;
    late MockLoginUseCase mockLoginUseCase;

    setUp(() {
      mockLoginUseCase = MockLoginUseCase();
      authBloc = AuthBloc(loginUseCase: mockLoginUseCase);
    });

    test('initial state is AuthInitial', () {
      expect(authBloc.state, AuthInitial());
    });

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, Authenticated] when login succeeds',
      build: () {
        when(() => mockLoginUseCase(any()))
            .thenAnswer((_) async => Right(testUser));
        return authBloc;
      },
      act: (bloc) => bloc.add(
        LoginRequested(email: 'test@test.com', password: 'password')
      ),
      expect: () => [AuthLoading(), Authenticated(testUser)],
    );
  });
}
```

## Contributing

### Development Workflow

1. **Create feature branch**
   ```bash
   git checkout -b feature/new-feature
   ```

2. **Make changes following guidelines**
    - Use Clean Architecture
    - Follow BLoC pattern
    - Write tests
    - Update documentation

3. **Run tests and linting**
   ```bash
   flutter test
   flutter analyze
   dart format lib/ test/
   ```

4. **Commit and push**
   ```bash
   git commit -m "feat: add new feature"
   git push origin feature/new-feature
   ```

### Code Style

- Follow [Effective Dart](https://dart.dev/guides/language/effective-dart)
- Use meaningful variable names
- Add comments for complex logic
- Maintain test coverage > 80%

## Troubleshooting

### Common Issues

#### Camera Permission Denied

```dart
// Request permissions
await Permission.camera.request();
```

#### Build Errors on iOS

```bash
cd ios
pod deintegrate
pod install
cd ..
flutter clean
flutter pub get
flutter run
```

#### Android Build Issues

```bash
flutter clean
cd android
./gradlew clean
cd ..
flutter pub get
flutter run
```

## License

Part of the FIVUCSAS platform developed at Marmara University.

Copyright © 2025 FIVUCSAS Team. All rights reserved.

Licensed under the MIT License.

---

**Built with Flutter** | FIVUCSAS Mobile Team © 2025
