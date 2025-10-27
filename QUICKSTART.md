# Quick Start Guide - FIVUCSAS Mobile App

This guide will help you initialize and set up the Flutter mobile app in this folder.

## Prerequisites

1. **Install Flutter SDK**
    - Download from: https://docs.flutter.dev/get-started/install/windows
    - Extract to `C:\src\flutter`
    - Add to PATH: `C:\src\flutter\bin`

2. **Verify Installation**
   ```bash
   flutter doctor
   ```

## Step-by-Step Initialization

### 1. Initialize Flutter Project (in this folder)

```bash
# Navigate to mobile-app directory
cd C:\Users\ahabg\OneDrive\Belgeler\GitHub\FIVUCSAS\mobile-app

# Initialize Flutter project (this will create files in current directory)
flutter create --org com.fivucsas --project-name fivucsas_mobile .

# Get dependencies
flutter pub get
```

### 2. Update pubspec.yaml

Replace the contents of `pubspec.yaml` with:

```yaml
name: fivucsas_mobile
description: FIVUCSAS Mobile Application for Biometric Authentication
publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: '>=3.5.0 <4.0.0'
  flutter: ">=3.24.0"

dependencies:
  flutter:
    sdk: flutter
  flutter_localizations:
    sdk: flutter

  # State Management
  flutter_bloc: ^8.1.3
  equatable: ^2.0.5
  get_it: ^7.6.4

  # Networking
  dio: ^5.4.0
  retrofit: ^4.0.3
  retrofit_generator: ^8.0.6
  pretty_dio_logger: ^1.3.1
  json_annotation: ^4.8.1

  # Local Storage
  hive: ^2.2.3
  hive_flutter: ^1.1.0
  shared_preferences: ^2.2.2
  flutter_secure_storage: ^9.0.0

  # Camera & ML
  camera: ^0.10.5+5
  google_mlkit_face_detection: ^0.10.0
  image: ^4.1.3

  # UI Components
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
  logger: ^2.0.2+1
  dartz: ^0.10.1
  encrypt: ^5.0.3

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.1
  
  # Code Generation
  build_runner: ^2.4.6
  json_serializable: ^6.7.1
  hive_generator: ^2.0.1
  
  # Testing
  mockito: ^5.4.3
  bloc_test: ^9.1.5

flutter:
  uses-material-design: true
  
  assets:
    - assets/images/
    - assets/icons/
    - assets/animations/
```

Then run:

```bash
flutter pub get
```

### 3. Create Project Structure

**Windows PowerShell:**

```powershell
# Navigate to lib directory
cd lib

# Create core directories
New-Item -ItemType Directory -Force -Path "core\constants"
New-Item -ItemType Directory -Force -Path "core\theme"
New-Item -ItemType Directory -Force -Path "core\utils"
New-Item -ItemType Directory -Force -Path "core\errors"
New-Item -ItemType Directory -Force -Path "core\network"
New-Item -ItemType Directory -Force -Path "core\config"

# Create feature directories
New-Item -ItemType Directory -Force -Path "features\auth\data\models"
New-Item -ItemType Directory -Force -Path "features\auth\data\datasources"
New-Item -ItemType Directory -Force -Path "features\auth\data\repositories"
New-Item -ItemType Directory -Force -Path "features\auth\domain\entities"
New-Item -ItemType Directory -Force -Path "features\auth\domain\repositories"
New-Item -ItemType Directory -Force -Path "features\auth\domain\usecases"
New-Item -ItemType Directory -Force -Path "features\auth\presentation\bloc"
New-Item -ItemType Directory -Force -Path "features\auth\presentation\pages"
New-Item -ItemType Directory -Force -Path "features\auth\presentation\widgets"

New-Item -ItemType Directory -Force -Path "features\biometric\data\models"
New-Item -ItemType Directory -Force -Path "features\biometric\data\datasources"
New-Item -ItemType Directory -Force -Path "features\biometric\data\repositories"
New-Item -ItemType Directory -Force -Path "features\biometric\data\services"
New-Item -ItemType Directory -Force -Path "features\biometric\domain\entities"
New-Item -ItemType Directory -Force -Path "features\biometric\domain\repositories"
New-Item -ItemType Directory -Force -Path "features\biometric\domain\usecases"
New-Item -ItemType Directory -Force -Path "features\biometric\presentation\bloc"
New-Item -ItemType Directory -Force -Path "features\biometric\presentation\pages"
New-Item -ItemType Directory -Force -Path "features\biometric\presentation\widgets"

New-Item -ItemType Directory -Force -Path "features\home\presentation\pages"
New-Item -ItemType Directory -Force -Path "features\home\presentation\widgets"

New-Item -ItemType Directory -Force -Path "features\profile\data\models"
New-Item -ItemType Directory -Force -Path "features\profile\data\datasources"
New-Item -ItemType Directory -Force -Path "features\profile\data\repositories"
New-Item -ItemType Directory -Force -Path "features\profile\domain\entities"
New-Item -ItemType Directory -Force -Path "features\profile\domain\repositories"
New-Item -ItemType Directory -Force -Path "features\profile\domain\usecases"
New-Item -ItemType Directory -Force -Path "features\profile\presentation\bloc"
New-Item -ItemType Directory -Force -Path "features\profile\presentation\pages"
New-Item -ItemType Directory -Force -Path "features\profile\presentation\widgets"

# Create shared directory
New-Item -ItemType Directory -Force -Path "shared\widgets"

# Go back to mobile-app root
cd ..
```

**Or use CMD:**

```cmd
cd lib
mkdir core\constants core\theme core\utils core\errors core\network core\config
mkdir features\auth\data\models features\auth\data\datasources features\auth\data\repositories
mkdir features\auth\domain\entities features\auth\domain\repositories features\auth\domain\usecases
mkdir features\auth\presentation\bloc features\auth\presentation\pages features\auth\presentation\widgets
mkdir features\biometric\data\models features\biometric\data\datasources features\biometric\data\repositories features\biometric\data\services
mkdir features\biometric\domain\entities features\biometric\domain\repositories features\biometric\domain\usecases
mkdir features\biometric\presentation\bloc features\biometric\presentation\pages features\biometric\presentation\widgets
mkdir features\home\presentation\pages features\home\presentation\widgets
mkdir features\profile\data\models features\profile\data\datasources features\profile\data\repositories
mkdir features\profile\domain\entities features\profile\domain\repositories features\profile\domain\usecases
mkdir features\profile\presentation\bloc features\profile\presentation\pages features\profile\presentation\widgets
mkdir shared\widgets
cd ..
```

### 4. Create Assets Directories

```powershell
New-Item -ItemType Directory -Force -Path "assets\images"
New-Item -ItemType Directory -Force -Path "assets\icons"
New-Item -ItemType Directory -Force -Path "assets\animations"
New-Item -ItemType Directory -Force -Path "assets\fonts"
```

### 5. Verify Everything is Working

```bash
# Check for issues
flutter doctor

# Run the app (connect a device or start emulator first)
flutter run
```

## Your Project Structure Should Look Like This:

```
mobile-app/
├── android/                    # Android-specific code
├── ios/                        # iOS-specific code
├── lib/
│   ├── core/
│   │   ├── config/
│   │   ├── constants/
│   │   ├── errors/
│   │   ├── network/
│   │   ├── theme/
│   │   └── utils/
│   ├── features/
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   ├── biometric/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   ├── home/
│   │   │   └── presentation/
│   │   └── profile/
│   │       ├── data/
│   │       ├── domain/
│   │       └── presentation/
│   ├── shared/
│   │   └── widgets/
│   ├── app.dart
│   ├── injection_container.dart
│   └── main.dart
├── assets/
│   ├── images/
│   ├── icons/
│   ├── animations/
│   └── fonts/
├── test/
├── pubspec.yaml
├── README.md
└── .env.example
```

## Next Steps

1. Follow the detailed implementation guide in `../FLUTTER_APP_GUIDE.md`
2. Start implementing features one by one:
    - Core configuration
    - Authentication feature
    - Biometric feature
    - UI components

## Useful Commands

```bash
# Clean build
flutter clean

# Get dependencies
flutter pub get

# Run code generation
flutter pub run build_runner build --delete-conflicting-outputs

# Run on specific device
flutter devices
flutter run -d <device-id>

# Build APK
flutter build apk --release

# Check for outdated packages
flutter pub outdated

# Analyze code
flutter analyze

# Format code
dart format lib/
```

## Testing Your Setup

After initialization, test that everything works:

```bash
# Run the default Flutter app
flutter run

# You should see the default Flutter counter app
# This confirms your setup is working correctly
```

## Environment Configuration

Create `.env` file for development (copy from `.env.example`):

```env
API_BASE_URL=http://10.0.2.2:8080/api/v1
BIOMETRIC_API_URL=http://10.0.2.2:8001/api/v1
ENVIRONMENT=development
```

## Android Emulator Setup

1. Open Android Studio
2. Go to Tools > Device Manager
3. Create a new Virtual Device
4. Choose a device (Pixel 6 recommended)
5. Choose Android API 34
6. Click Finish

Then run:

```bash
flutter emulators
flutter emulators --launch <emulator-id>
flutter run
```

## iOS Simulator Setup (macOS only)

```bash
# List simulators
xcrun simctl list devices

# Boot a simulator
open -a Simulator

# Run app
flutter run
```

## Troubleshooting

### Issue: Flutter command not found

**Solution:** Add Flutter to PATH

```powershell
$env:Path += ";C:\src\flutter\bin"
```

### Issue: Android licenses not accepted

**Solution:**

```bash
flutter doctor --android-licenses
```

### Issue: Gradle build errors

**Solution:**

```bash
cd android
.\gradlew clean
cd ..
flutter clean
flutter pub get
```

### Issue: iOS build errors

**Solution:**

```bash
cd ios
pod deintegrate
pod install
cd ..
flutter clean
flutter pub get
```

## Support

- Check the main guide: `../FLUTTER_APP_GUIDE.md`
- Flutter documentation: https://docs.flutter.dev
- FIVUCSAS README: `../README.md`

---

**Ready to start coding? Follow the FLUTTER_APP_GUIDE.md for detailed implementation!** 🚀
