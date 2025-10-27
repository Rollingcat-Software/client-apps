# 🚀 Get Started - FIVUCSAS Mobile App

Welcome! This is your **mobile-app** folder for the FIVUCSAS project.

## 📋 What You Have

This folder is ready to become a Flutter cross-platform mobile application with:

- ✅ **README.md** - Detailed documentation about the mobile app
- ✅ **QUICKSTART.md** - Quick setup and initialization guide
- ✅ **.env.example** - Environment configuration template
- ✅ **setup.bat** - Automated setup script for Windows
- ✅ **FLUTTER_APP_GUIDE.md** - Complete implementation guide (in parent folder)

## 🎯 Quick Start (Choose Your Path)

### 🎨 Option 1: Android Studio (RECOMMENDED) ⭐

**Best for:** Full IDE experience, visual tools, debugging

1. **Install Android Studio** from https://developer.android.com/studio
2. **Install Flutter plugin** (File → Settings → Plugins → Search "Flutter")
3. **Open** this folder in Android Studio
4. **Follow** [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md) for complete guide

**Advantages:**

- ✅ Visual interface
- ✅ Built-in emulator
- ✅ Hot reload UI button
- ✅ Debugger tools
- ✅ Flutter Inspector
- ✅ Cross-platform (Android + iOS)

### ⚡ Option 2: Automated Setup (Command Line)

**Best for:** Quick setup via terminal

```bash
# Just run the setup script
setup.bat
```

This will:

- Check Flutter installation
- Initialize the Flutter project
- Create the project structure
- Set up environment files
- Get all dependencies

### 💻 Option 3: Manual Setup

**Best for:** VS Code users or manual control

```bash
# 1. Initialize Flutter project
flutter create --org com.fivucsas --project-name fivucsas_mobile .

# 2. Get dependencies
flutter pub get

# 3. Run the app
flutter run
```

## 📚 Documentation Guide

1. **GET_STARTED.md** (this file) - Start here!
2. **ANDROID_STUDIO_SETUP.md** - Android Studio setup & workflow ⭐
3. **QUICKSTART.md** - Detailed setup instructions
4. **README.md** - Full mobile app documentation
5. **../FLUTTER_APP_GUIDE.md** - Complete coding guide with examples

## 🛠️ Prerequisites

Before starting, make sure you have:

### Required

- [ ] **Flutter SDK 3.24+** - [Install Guide](https://docs.flutter.dev/get-started/install/windows)
- [ ] **Dart SDK 3.5+** (comes with Flutter)
- [ ] **Android Studio** with Android SDK
- [ ] **Git**

### Optional (but recommended)

- [ ] **Android Studio** (Recommended! Best for Flutter) ⭐
- [ ] **Visual Studio Code** with Flutter extension (Alternative)
- [ ] **Xcode** (macOS only, for iOS development)
- [ ] **Android Emulator** or physical device

## ✅ Verify Installation

Run this command to check your setup:

```bash
flutter doctor
```

Fix any issues before proceeding.

## 🏗️ Project Structure (After Setup)

```
mobile-app/
├── android/                    # Android native code
├── ios/                        # iOS native code
├── lib/                        # Dart source code
│   ├── core/                   # Core utilities
│   │   ├── config/            # App configuration
│   │   ├── constants/         # Constants
│   │   ├── network/           # API client
│   │   ├── theme/             # App theme
│   │   ├── errors/            # Error handling
│   │   └── utils/             # Utilities
│   ├── features/              # Feature modules
│   │   ├── auth/              # Authentication
│   │   ├── biometric/         # Biometric features
│   │   ├── home/              # Home screen
│   │   └── profile/           # User profile
│   ├── shared/                # Shared widgets
│   ├── app.dart               # App widget
│   ├── main.dart              # Entry point
│   └── injection_container.dart  # Dependency injection
├── assets/                    # Images, fonts, etc.
├── test/                      # Tests
├── pubspec.yaml              # Dependencies
├── .env                       # Environment variables
└── README.md                  # Documentation
```

## 🎨 Architecture

This app follows **Clean Architecture** with:

- **BLoC Pattern** - State management
- **Repository Pattern** - Data abstraction
- **Use Cases** - Business logic
- **Dependency Injection** - GetIt

```
┌──────────────────────────────────┐
│     Presentation Layer           │
│  (BLoC + UI + Widgets)           │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│      Domain Layer                │
│  (Entities + Use Cases)          │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│       Data Layer                 │
│  (Models + Repositories + API)   │
└──────────────────────────────────┘
```

## 📱 Features to Implement

### Phase 1: Core Features

- [ ] User authentication (login/register)
- [ ] JWT token management
- [ ] Profile management
- [ ] App navigation

### Phase 2: Biometric Features

- [ ] Camera integration
- [ ] Face detection with ML Kit
- [ ] Biometric enrollment
- [ ] Liveness detection (Biometric Puzzle)

### Phase 3: Advanced Features

- [ ] Offline support
- [ ] Push notifications
- [ ] QR code scanning
- [ ] Biometric history

## 🔧 Development Workflow

### 1. Start Backend Services

Before running the mobile app, start your backend:

```bash
cd ../identity-core-api
./gradlew bootRun

cd ../biometric-processor
python -m uvicorn app.main:app --reload
```

### 2. Configure API Endpoints

Edit `.env` file:

```env
# For Android Emulator
API_BASE_URL=http://10.0.2.2:8080/api/v1

# For iOS Simulator
API_BASE_URL=http://localhost:8080/api/v1

# For Physical Device (replace with your PC's IP)
API_BASE_URL=http://192.168.1.100:8080/api/v1
```

### 3. Run the App

```bash
# List available devices
flutter devices

# Run on specific device
flutter run -d <device-id>

# Or just run (will prompt for device)
flutter run
```

### 4. Hot Reload

While app is running:

- Press `r` for hot reload
- Press `R` for hot restart
- Press `q` to quit

## 🧪 Testing

```bash
# Run all tests
flutter test

# Run with coverage
flutter test --coverage

# Run specific test
flutter test test/features/auth/auth_bloc_test.dart
```

## 📦 Building for Production

### Android

```bash
# Build APK
flutter build apk --release

# Build App Bundle (for Play Store)
flutter build appbundle --release
```

### iOS

```bash
# Build for iOS
flutter build ios --release

# Build IPA
flutter build ipa --release
```

## 🐛 Common Issues & Solutions

### Issue: "Flutter command not found"

**Solution:** Add Flutter to PATH

```bash
# Windows
set PATH=%PATH%;C:\src\flutter\bin

# Permanent: Add to System Environment Variables
```

### Issue: "Android licenses not accepted"

**Solution:**

```bash
flutter doctor --android-licenses
```

### Issue: "Unable to connect to API"

**Solutions:**

1. Check backend is running
2. Check firewall settings
3. Use correct IP address:
    - Android Emulator: `10.0.2.2`
    - iOS Simulator: `localhost`
    - Physical device: Your computer's IP

### Issue: Build errors after adding dependencies

**Solution:**

```bash
flutter clean
flutter pub get
flutter run
```

## 📖 Learning Resources

### Flutter Basics

- [Flutter Documentation](https://docs.flutter.dev/)
- [Dart Language Tour](https://dart.dev/guides/language/language-tour)
- [Flutter Widget Catalog](https://docs.flutter.dev/development/ui/widgets)

### Architecture & Patterns

- [BLoC Pattern](https://bloclibrary.dev/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Flutter Best Practices](https://docs.flutter.dev/development/ui/layout/best-practices)

### ML & Camera

- [ML Kit Face Detection](https://developers.google.com/ml-kit/vision/face-detection)
- [Camera Plugin](https://pub.dev/packages/camera)

## 🆘 Need Help?

1. **Check documentation:**
    - QUICKSTART.md - Setup help
    - README.md - Feature documentation
    - FLUTTER_APP_GUIDE.md - Implementation guide

2. **Common commands:**
   ```bash
   flutter doctor          # Check setup
   flutter clean           # Clean build
   flutter pub get         # Get dependencies
   flutter analyze         # Check code
   ```

3. **Project documentation:**
    - Main README: `../README.md`
    - Implementation guide: `../IMPLEMENTATION_GUIDE.md`

## 🎯 Next Steps

1. ✅ **Read this file** (you're here!)
2. 📦 **Run setup:** Execute `setup.bat` or follow QUICKSTART.md
3. 🏃 **Test it:** Run `flutter run` to see the default app
4. 📚 **Learn:** Read FLUTTER_APP_GUIDE.md for implementation
5. 💻 **Code:** Start implementing features!

## 🎨 Visual Studio Code Setup (Optional)

Install these extensions for better development experience:

1. **Flutter** - Dart Code
2. **Dart** - Dart Code
3. **Flutter Widget Snippets**
4. **Awesome Flutter Snippets**
5. **Error Lens**
6. **Better Comments**

### Recommended VS Code Settings

Create `.vscode/settings.json`:

```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll": true
  },
  "dart.lineLength": 80,
  "dart.debugExternalPackageLibraries": true,
  "dart.debugSdkLibraries": false
}
```

## 🚀 Ready to Start?

Run the setup script and start building your mobile app:

```bash
# Windows
setup.bat

# Then start coding!
code .
```

**Good luck with your FIVUCSAS mobile app development!** 🎉

---

*Last updated: 2025-01-23*
