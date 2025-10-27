# 🎨 Android Studio Setup for Flutter Cross-Platform Development

Yes! **Android Studio is the BEST IDE for Flutter development.** You can build cross-platform apps (Android & iOS) from Android Studio.

## ✅ Why Android Studio is Perfect for Flutter

- ✅ **Official Support** - Google's official IDE for Flutter
- ✅ **Cross-Platform** - Build Android AND iOS from one IDE
- ✅ **Powerful Tools** - Built-in emulators, debugger, profiler
- ✅ **Flutter Plugin** - Full Flutter integration
- ✅ **Hot Reload** - See changes instantly
- ✅ **Device Manager** - Easy emulator control

## 📦 Prerequisites

1. **Android Studio** (latest version)
2. **Flutter SDK** (will install via Android Studio)
3. **Android SDK** (comes with Android Studio)
4. **Xcode** (macOS only, for iOS builds)

## 🚀 Step-by-Step Setup

### Step 1: Install Android Studio

1. **Download** Android Studio from: https://developer.android.com/studio
2. **Install** with default settings
3. **Launch** Android Studio

### Step 2: Install Flutter Plugin in Android Studio

1. Open **Android Studio**
2. Go to **File → Settings** (or **Android Studio → Preferences** on Mac)
3. Select **Plugins**
4. Search for **"Flutter"**
5. Click **Install**
6. Also install **Dart** plugin (will be suggested)
7. Click **Restart IDE**

![Flutter Plugin Installation](https://docs.flutter.dev/assets/images/docs/tools/android-studio/plugins.png)

### Step 3: Configure Flutter SDK Path

After restart:

1. Go to **File → Settings → Languages & Frameworks → Flutter**
2. Set **Flutter SDK path**: `C:\src\flutter` (or wherever you installed Flutter)
3. Click **OK**

### Step 4: Install Flutter SDK (if not already installed)

**Option A: Via Android Studio**

1. **File → New → New Flutter Project**
2. Click **Install SDK** if prompted
3. Choose installation path: `C:\src\flutter`
4. Wait for download and installation

**Option B: Manual Installation**

```powershell
# Download Flutter SDK
# https://docs.flutter.dev/get-started/install/windows

# Extract to C:\src\flutter

# Add to PATH
setx PATH "%PATH%;C:\src\flutter\bin"

# Verify
flutter doctor
```

### Step 5: Accept Android Licenses

Open **Terminal** in Android Studio (Alt+F12) and run:

```bash
flutter doctor --android-licenses
```

Press **Y** to accept all licenses.

### Step 6: Verify Setup

In Android Studio Terminal, run:

```bash
flutter doctor
```

You should see something like:

```
[✓] Flutter (Channel stable, 3.24.0, on Microsoft Windows)
[✓] Android toolchain - develop for Android devices
[✓] Android Studio (version 2024.1)
[✓] Connected device (1 available)
[✓] Network resources
```

## 🎯 Creating Your FIVUCSAS Flutter Project in Android Studio

### Method 1: Import Existing Project (Recommended)

Since you already have the mobile-app folder:

1. **Open Android Studio**
2. Click **File → Open**
3. Navigate to: `C:\Users\ahabg\OneDrive\Belgeler\GitHub\FIVUCSAS\mobile-app`
4. Click **OK**

**First time setup:**
```bash
# In Android Studio Terminal (Alt+F12)
flutter create --org com.fivucsas --project-name fivucsas_mobile .
flutter pub get
```

### Method 2: Create New Flutter Project

1. **File → New → New Flutter Project**
2. Select **Flutter Application**
3. Click **Next**
4. **Project name**: `fivucsas_mobile`
5. **Project location**: `C:\Users\ahabg\OneDrive\Belgeler\GitHub\FIVUCSAS\mobile-app`
6. **Organization**: `com.fivucsas`
7. Select platforms: ✅ Android ✅ iOS ✅ Web (optional)
8. Click **Finish**

## 📱 Running Your Flutter App on Android

### Setup Android Emulator

1. **Tools → Device Manager** (or AVD Manager)
2. Click **Create Virtual Device**
3. Select device: **Pixel 6** (recommended)
4. Select system image: **API 34** (latest)
5. Click **Next** → **Finish**

### Run on Emulator

1. **Start emulator** from Device Manager
2. Wait for emulator to fully boot
3. **Select device** from device dropdown (top toolbar)
4. Click **Run** button (▶️) or press **Shift+F10**
5. App will build and run automatically

### Run on Physical Android Device

1. **Enable Developer Options** on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   
2. **Enable USB Debugging**:
   - Settings → Developer Options → USB Debugging

3. **Connect** phone via USB

4. **Select device** from device dropdown

5. Click **Run** (▶️)

## 🍎 Running Your Flutter App on iOS (macOS only)

### Prerequisites

```bash
# Install Xcode from Mac App Store
# Then run:
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
sudo xcodebuild -runFirstLaunch

# Install CocoaPods
sudo gem install cocoapods
```

### Setup iOS Simulator

1. **Tools → Device Manager**
2. iOS simulators will appear automatically
3. Select **iPhone 15** or latest
4. Click **Run** (▶️)

### Run on Physical iOS Device

1. **Connect** iPhone via USB
2. **Open** `ios/Runner.xcworkspace` in Xcode
3. **Sign** the app with your Apple Developer account
4. **Select device** in Android Studio
5. Click **Run** (▶️)

## 🎨 Android Studio Interface for Flutter

```
┌─────────────────────────────────────────────────────────────┐
│  File  Edit  View  Navigate  Code  Analyze  Build  Tools    │
├─────────────────────────────────────────────────────────────┤
│  [▶️ Run] [🐛 Debug] [🔥 Hot Reload]  [Device: Pixel 6 ▼]  │
├──────────────┬──────────────────────────────────────────────┤
│              │                                               │
│  📁 Project  │  📝 Code Editor                              │
│              │                                               │
│  lib/        │  import 'package:flutter/material.dart';     │
│  ├─ core/    │                                               │
│  ├─ features/│  void main() {                               │
│  └─ main.dart│    runApp(MyApp());                          │
│              │  }                                            │
│  android/    │                                               │
│  ios/        │                                               │
│              │                                               │
├──────────────┴──────────────────────────────────────────────┤
│  Terminal: flutter run                                       │
│  Hot reload ⚡ Press "r" to hot reload                       │
└─────────────────────────────────────────────────────────────┘
```

## 🔥 Essential Android Studio Features for Flutter

### 1. Hot Reload (⚡ Lightning Fast Development)

**What it does:** Updates UI instantly without restarting app

**How to use:**
- **Save file** → Auto hot reload (if enabled)
- **Click** Hot Reload button (⚡)
- **Press** `Ctrl+S` (or `Cmd+S` on Mac)
- **Terminal:** Press `r`

```dart
// Change this:
Text('Hello World')

// To this (Save → Hot Reload):
Text('Hello FIVUCSAS')
// Changes appear INSTANTLY!
```

### 2. Hot Restart (🔄 Full App Restart)

**When to use:** State changes, dependency injection changes

**How to use:**
- **Click** Hot Restart button (🔄)
- **Terminal:** Press `R`

### 3. Flutter Inspector (🔍 UI Debugger)

**What it does:** Visual debugging of widget tree

**How to use:**
1. **Run app** in Debug mode
2. **View → Tool Windows → Flutter Inspector**
3. Explore widget tree, layout issues

### 4. Device Manager (📱 Emulator Control)

**Access:** Tools → Device Manager

**Features:**
- Create/delete emulators
- Quick boot emulators
- Wipe emulator data
- Snapshot management

### 5. Logcat (📋 Real-time Logs)

**Access:** View → Tool Windows → Logcat

**Filter logs:**
```
flutter:   # Flutter logs only
package:com.fivucsas.mobile  # Your app only
```

### 6. Dart Analysis (✅ Code Quality)

**Real-time features:**
- Syntax errors
- Type checking
- Quick fixes (Alt+Enter)
- Code completion

### 7. Debugger (🐛 Debug Your App)

**Breakpoints:**
- Click left margin to add breakpoint
- Run in Debug mode (🐛)
- Step through code (F8, F7)

## 📋 Useful Android Studio Shortcuts

### Essential Shortcuts

| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Run | Shift+F10 | Control+R |
| Debug | Shift+F9 | Control+D |
| Hot Reload | Ctrl+S | Cmd+S |
| Hot Restart | Ctrl+Shift+\ | Cmd+Shift+\ |
| Format Code | Ctrl+Alt+L | Cmd+Option+L |
| Quick Fix | Alt+Enter | Option+Enter |
| Find File | Ctrl+Shift+N | Cmd+Shift+O |
| Search Everywhere | Shift Shift | Shift Shift |
| Terminal | Alt+F12 | Option+F12 |
| Build | Ctrl+F9 | Cmd+F9 |

### Flutter-Specific Shortcuts

| Action | Shortcut |
|--------|----------|
| Extract Widget | Ctrl+Alt+W |
| Wrap with Widget | Alt+Enter → Wrap |
| Remove Widget | Alt+Enter → Remove |
| Go to Definition | Ctrl+B or Ctrl+Click |
| Show Documentation | Ctrl+Q |

## 🎯 Project Structure in Android Studio

```
mobile-app/
├── 📁 android/                    ← Android-specific code
│   ├── app/
│   │   ├── build.gradle          ← Android config
│   │   └── src/main/
│   │       └── AndroidManifest.xml
│   └── gradle/
│
├── 📁 ios/                        ← iOS-specific code (macOS)
│   ├── Runner/
│   │   └── Info.plist            ← iOS config
│   └── Podfile                   ← iOS dependencies
│
├── 📁 lib/                        ← Main Dart code ⭐
│   ├── 📁 core/                  ← Core utilities
│   │   ├── config/
│   │   ├── constants/
│   │   ├── network/
│   │   ├── theme/
│   │   └── errors/
│   │
│   ├── 📁 features/              ← Feature modules
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   ├── biometric/
│   │   └── profile/
│   │
│   ├── 📄 main.dart              ← App entry point
│   ├── 📄 app.dart
│   └── 📄 injection_container.dart
│
├── 📁 assets/                     ← Images, fonts, etc.
│   ├── images/
│   ├── icons/
│   └── fonts/
│
├── 📁 test/                       ← Unit & widget tests
│
├── 📄 pubspec.yaml               ← Dependencies ⭐
└── 📄 README.md
```

## 🔧 Configure pubspec.yaml

In Android Studio, open `pubspec.yaml` and add dependencies:

```yaml
name: fivucsas_mobile
description: FIVUCSAS Mobile Application

environment:
  sdk: '>=3.5.0 <4.0.0'

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
  
  # UI
  flutter_svg: ^2.0.9
  cached_network_image: ^3.3.0
  
  # Camera & ML
  camera: ^0.10.5+5
  google_mlkit_face_detection: ^0.10.0

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.1
  build_runner: ^2.4.6
```

**After editing, click "Pub get" in the banner** or run:
```bash
flutter pub get
```

## 🎨 Workflow in Android Studio

### 1. Daily Development Flow

```bash
# Morning
1. Open Android Studio
2. Pull latest code (VCS → Update Project)
3. Run flutter pub get
4. Start emulator
5. Run app (Shift+F10)

# Development
1. Edit code in lib/
2. Save (Ctrl+S) → Hot reload automatically
3. Check console for errors
4. Use Flutter Inspector for UI debugging

# Testing
1. Write tests in test/
2. Run tests (Right-click → Run tests)
3. Check coverage

# End of day
1. Commit changes (Ctrl+K)
2. Push to Git (Ctrl+Shift+K)
```

### 2. Creating New Features

**Right-click on `lib/features/` → New → Directory:**

```
my_feature/
├── data/
│   ├── models/
│   ├── datasources/
│   └── repositories/
├── domain/
│   ├── entities/
│   ├── repositories/
│   └── usecases/
└── presentation/
    ├── bloc/
    ├── pages/
    └── widgets/
```

### 3. Code Generation

For JSON serialization, Retrofit, etc.:

```bash
# In Android Studio Terminal
flutter pub run build_runner build --delete-conflicting-outputs
```

## 🐛 Debugging in Android Studio

### Set Breakpoints

1. **Click** left margin next to line number (red dot appears)
2. **Run in Debug mode** (🐛 or Shift+F9)
3. App will pause at breakpoint
4. **Inspect variables** in Debug window
5. **Step through code:**
   - F8: Step over
   - F7: Step into
   - Shift+F8: Step out
   - F9: Resume

### Debug Console

View real-time logs:
- **Flutter:** Tab shows print() statements
- **Logcat:** Tab shows all Android logs
- **Console:** Shows build output

### Flutter DevTools

**Access:** When running, click "Open DevTools" in Run window

**Features:**
- Widget inspector
- Performance profiler
- Memory profiler
- Network inspector

## 📱 Build & Deploy from Android Studio

### Build APK (Android)

1. **Build → Flutter → Build APK**
2. Or Terminal: `flutter build apk --release`
3. APK location: `build/app/outputs/flutter-apk/app-release.apk`

### Build App Bundle (Play Store)

1. **Build → Flutter → Build App Bundle**
2. Or Terminal: `flutter build appbundle --release`
3. AAB location: `build/app/outputs/bundle/release/app-release.aab`

### Build iOS (macOS only)

1. **Build → Flutter → Build iOS**
2. Or Terminal: `flutter build ios --release`

## 🎯 Android Studio Tips for Flutter

### 1. Live Templates (Code Snippets)

Type these shortcuts and press Tab:

- `stless` → StatelessWidget
- `stful` → StatefulWidget
- `build` → Build method
- `initS` → initState method
- `dis` → dispose method

### 2. Extract Widget Refactoring

1. **Select widget code**
2. **Right-click → Refactor → Extract Flutter Widget**
3. **Name the widget**
4. Widget is extracted automatically!

### 3. Wrap with Widget

1. **Place cursor** on widget
2. **Alt+Enter**
3. Select **"Wrap with..."**
4. Choose: Container, Padding, Center, etc.

### 4. Flutter Outline

**View → Tool Windows → Flutter Outline**

- See widget tree structure
- Quick navigation
- Wrap/remove widgets with buttons

### 5. Multi-cursor Editing

- **Alt+Shift+Click** to add cursor
- **Alt+J** to select next occurrence
- Edit multiple places at once!

## 🔄 Version Control (Git) in Android Studio

### Setup Git

1. **VCS → Enable Version Control Integration**
2. Select **Git**
3. Click **OK**

### Common Git Operations

| Action | Shortcut | Menu |
|--------|----------|------|
| Commit | Ctrl+K | VCS → Commit |
| Push | Ctrl+Shift+K | VCS → Git → Push |
| Pull | Ctrl+T | VCS → Update Project |
| Branches | - | VCS → Git → Branches |

## ✅ Quick Start Checklist

Use this when setting up:

- [ ] Install Android Studio
- [ ] Install Flutter plugin
- [ ] Install Dart plugin
- [ ] Configure Flutter SDK path
- [ ] Run `flutter doctor`
- [ ] Accept Android licenses
- [ ] Create Android emulator
- [ ] Open mobile-app folder
- [ ] Run `flutter create .`
- [ ] Run `flutter pub get`
- [ ] Run app (Shift+F10)
- [ ] Verify hot reload works

## 🎓 Learning Resources

### Android Studio
- [Android Studio User Guide](https://developer.android.com/studio/intro)
- [Keyboard Shortcuts](https://developer.android.com/studio/intro/keyboard-shortcuts)

### Flutter in Android Studio
- [Flutter Setup](https://docs.flutter.dev/get-started/editor?tab=androidstudio)
- [Debugging Flutter Apps](https://docs.flutter.dev/testing/debugging)

## 🆘 Common Issues

### Issue: "Flutter SDK not found"
**Solution:**
1. File → Settings → Languages & Frameworks → Flutter
2. Set Flutter SDK path: `C:\src\flutter`
3. Click Apply

### Issue: "Android licenses not accepted"
**Solution:**
```bash
flutter doctor --android-licenses
```

### Issue: "Emulator won't start"
**Solution:**
1. Tools → Device Manager
2. Wipe emulator data
3. Cold boot

### Issue: "Hot reload not working"
**Solution:**
1. Check "Enable hot reload on save"
2. File → Settings → Languages & Frameworks → Flutter
3. Enable hot reload checkbox

## 🚀 You're Ready!

Android Studio is **perfect** for Flutter cross-platform development! You get:

✅ **One IDE** for Android & iOS
✅ **Powerful tools** built-in
✅ **Fast development** with hot reload
✅ **Professional debugging**
✅ **Easy deployment**

**Start building your FIVUCSAS app now!** 🎉

---

**Next Steps:**
1. Open Android Studio
2. Open `mobile-app` folder
3. Run `flutter create .`
4. Follow `FLUTTER_APP_GUIDE.md` to implement features!
