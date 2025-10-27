# 🎯 Quick Reference - FIVUCSAS Mobile App

**Your complete guide to building a Flutter cross-platform app!**

---

## 📱 YES! Android Studio Works for Cross-Platform!

✅ **Android** - Full support
✅ **iOS** - Full support (build on macOS, or just develop and let CI/CD build)
✅ **Web** - Full support
✅ **Desktop** - Windows, macOS, Linux

**One codebase, all platforms!** 🚀

---

## 🚀 Super Quick Start

### For Android Studio Users (RECOMMENDED):

```
1. Open Android Studio
2. Install Flutter plugin (Settings → Plugins → "Flutter")
3. File → Open → Select this folder
4. Terminal → flutter create .
5. Terminal → flutter pub get
6. Click Run ▶️

Done! App running! 🎉
```

**Full guide:** [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md)

---

## 📚 Documentation Map

| File                          | What It Is                    | Read When            |
|-------------------------------|-------------------------------|----------------------|
| **ANDROID_STUDIO_SETUP.md** ⭐ | Complete Android Studio guide | Using Android Studio |
| **GET_STARTED.md**            | Overview & quick start        | First time here      |
| **QUICKSTART.md**             | Detailed setup steps          | Setting up project   |
| **README.md**                 | Full documentation            | Reference            |
| **FLUTTER_APP_GUIDE.md**      | Implementation with code      | Coding features      |

---

## 🎨 Android Studio Workflow

### Setup (One Time)

1. Install Android Studio
2. Install Flutter plugin
3. Configure Flutter SDK path
4. Run `flutter doctor`
5. Create emulator

### Daily Development

1. Open project in Android Studio
2. Start emulator (Tools → Device Manager)
3. Click Run ▶️
4. Edit code → Auto hot reload
5. Debug with breakpoints
6. Commit & push (Ctrl+K)

### Key Shortcuts

- **Shift+F10** - Run app
- **Ctrl+S** - Save & hot reload
- **Alt+Enter** - Quick fix
- **Ctrl+Alt+L** - Format code
- **Shift Shift** - Search everything

---

## 📂 Your Project Structure

```
mobile-app/
├── lib/              ⭐ YOUR CODE HERE
│   ├── core/         → Utilities, config, theme
│   ├── features/     → Auth, biometric, profile
│   └── main.dart     → App entry point
├── android/          → Android config
├── ios/              → iOS config
├── assets/           → Images, icons, fonts
├── test/             → Tests
└── pubspec.yaml      → Dependencies
```

---

## ✅ Quick Checklist

### Initial Setup

- [ ] Android Studio installed
- [ ] Flutter plugin installed
- [ ] Flutter SDK configured
- [ ] `flutter doctor` ✓ green
- [ ] Emulator created

### Project Setup

- [ ] Opened mobile-app folder
- [ ] Ran `flutter create .`
- [ ] Ran `flutter pub get`
- [ ] App runs successfully
- [ ] Hot reload works

### Ready to Code

- [ ] Read FLUTTER_APP_GUIDE.md
- [ ] Understand Clean Architecture
- [ ] Know BLoC pattern basics
- [ ] Backend services running
- [ ] `.env` file configured

---

## 🎯 Implementation Phases

### Phase 1: Setup ✅

- [x] Install tools
- [x] Create project
- [x] Configure environment
- [ ] Run default app

### Phase 2: Core (Week 1-2)

- [ ] Environment configuration
- [ ] Theme & constants
- [ ] Network layer
- [ ] Error handling
- [ ] Dependency injection

### Phase 3: Auth (Week 3-4)

- [ ] Login screen
- [ ] Register screen
- [ ] JWT token management
- [ ] Secure storage
- [ ] Auth BLoC

### Phase 4: Biometric (Week 5-7)

- [ ] Camera integration
- [ ] Face detection
- [ ] Biometric enrollment
- [ ] Liveness puzzle
- [ ] Verification flow

### Phase 5: Features (Week 8-10)

- [ ] Home screen
- [ ] Profile management
- [ ] Settings
- [ ] History
- [ ] QR scanner

### Phase 6: Polish (Week 11-12)

- [ ] Testing
- [ ] UI/UX improvements
- [ ] Performance optimization
- [ ] Documentation
- [ ] Deployment

---

## 🔧 Common Commands

```bash
# Setup
flutter doctor                    # Check setup
flutter create .                  # Initialize project
flutter pub get                   # Get dependencies

# Development
flutter run                       # Run app
flutter run -d <device-id>       # Run on specific device
flutter devices                   # List devices

# Hot reload (while running)
r                                # Hot reload
R                                # Hot restart
q                                # Quit

# Build
flutter build apk --release      # Android APK
flutter build appbundle          # Play Store
flutter build ios --release      # iOS (macOS)

# Testing
flutter test                     # Run tests
flutter analyze                  # Check code
dart format lib/                 # Format code

# Clean
flutter clean                    # Clean build
flutter pub upgrade              # Update packages

# Code generation
flutter pub run build_runner build --delete-conflicting-outputs
```

---

## 🐛 Troubleshooting

### "Flutter not found"

```bash
# Add to PATH
setx PATH "%PATH%;C:\src\flutter\bin"
```

### "Android licenses"

```bash
flutter doctor --android-licenses
```

### "Can't connect to API"

Check `.env` file:

- Android Emulator: `http://10.0.2.2:8080`
- iOS Simulator: `http://localhost:8080`
- Physical Device: `http://YOUR_PC_IP:8080`

### "Build errors"

```bash
flutter clean
flutter pub get
flutter run
```

### "Hot reload not working"

- Check "Enable hot reload on save" in settings
- Try hot restart (R)
- Restart app

---

## 🎨 Architecture Quick Reference

```
┌─────────────────────────────────┐
│    Presentation (UI)            │
│    - Pages, Widgets, BLoC       │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│    Domain (Business Logic)      │
│    - Entities, Use Cases        │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│    Data (API, DB, Cache)        │
│    - Models, Repositories       │
└─────────────────────────────────┘
```

**Example Flow:**

1. User clicks Login button (Presentation)
2. BLoC calls LoginUseCase (Domain)
3. UseCase calls AuthRepository (Domain)
4. Repository fetches from API (Data)
5. Data flows back up ⬆️
6. BLoC emits new state
7. UI updates automatically

---

## 📱 Supported Platforms

### Primary Targets

- ✅ **Android** 5.0+ (API 21+)
- ✅ **iOS** 12.0+

### Also Supported

- ✅ **Web** (Chrome, Safari, Firefox)
- ✅ **Windows** 10+
- ✅ **macOS** 10.14+
- ✅ **Linux** (Ubuntu, Debian)

**Write once, run everywhere!**

---

## 🎓 Learning Path

### Day 1: Setup

- [ ] Install Android Studio
- [ ] Setup Flutter
- [ ] Run first app
- [ ] Explore Android Studio

### Week 1: Flutter Basics

- [ ] Dart language basics
- [ ] Widgets (Stateless/Stateful)
- [ ] Layouts (Column, Row, Stack)
- [ ] Material Design

### Week 2: State Management

- [ ] BLoC pattern
- [ ] Events & States
- [ ] Dependency injection
- [ ] Navigation

### Week 3: Networking

- [ ] HTTP requests (Dio)
- [ ] JSON parsing
- [ ] Error handling
- [ ] Authentication

### Week 4: Advanced

- [ ] Camera usage
- [ ] ML Kit integration
- [ ] Local storage
- [ ] Testing

---

## 🔗 Important Links

### Official Docs

- **Flutter**: https://docs.flutter.dev/
- **Dart**: https://dart.dev/guides
- **BLoC**: https://bloclibrary.dev/
- **Material Design**: https://m3.material.io/

### Packages

- **pub.dev**: https://pub.dev/ (Flutter packages)
- **ML Kit**: https://developers.google.com/ml-kit

### Tools

- **Android Studio**: https://developer.android.com/studio
- **Flutter SDK**: https://docs.flutter.dev/get-started/install

---

## 💡 Pro Tips

1. **Use Android Studio** - Best IDE for Flutter
2. **Hot Reload** - Save time, reload instantly
3. **Flutter Inspector** - Debug UI visually
4. **Code Generation** - Use build_runner for models
5. **Shortcuts** - Learn them, work faster
6. **DevTools** - Performance profiling
7. **Clean Architecture** - Organized, testable code
8. **Git Often** - Commit frequently
9. **Test** - Write tests as you code
10. **Ask** - Community is helpful!

---

## 🆘 Need Help?

### Documentation

1. ANDROID_STUDIO_SETUP.md - IDE setup
2. FLUTTER_APP_GUIDE.md - Coding guide
3. README.md - Feature docs

### Commands

```bash
flutter doctor -v      # Detailed diagnostics
flutter analyze        # Find issues
```

### Community

- Stack Overflow: [flutter] tag
- Flutter Discord
- r/FlutterDev on Reddit

---

## ✨ Next Actions

**Right now:**

1. ✅ Install Android Studio
2. ✅ Install Flutter plugin
3. ✅ Open this folder
4. ✅ Run `flutter create .`
5. ✅ Click Run ▶️

**This week:**

1. 📚 Read FLUTTER_APP_GUIDE.md
2. 💻 Setup project structure
3. 🎨 Create core configuration
4. 🔐 Implement authentication

**This month:**

1. 📱 Complete all features
2. 🧪 Write tests
3. 🎨 Polish UI
4. 🚀 Deploy to stores

---

## 🎉 You Got This!

**Remember:**

- Android Studio = ✅ Perfect for Flutter
- Cross-platform = ✅ iOS + Android from one code
- Hot reload = ⚡ Super fast development
- Community = 💪 Always here to help

**Start coding now!** 🚀

---

*Quick Reference v1.0 - Updated 2025-01-23*
