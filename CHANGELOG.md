# Changelog

All notable changes to the FIVUCSAS client apps (Android, iOS, Desktop).

## [5.0.0] - 2026-04-08
- N-step MFA flow integration in mobile app
- MFA timeout handling, i18n error messages, method deduplication
- Domain migration from rollingcatsoftware.com to fivucsas.com

## [4.0.0] - 2026-04-05
- Phase 3 complete: 277 tests, CSV export, E2E framework
- Firebase Cloud Messaging push notifications for Android
- 39 ViewModel tests covering all remaining gaps
- CI fixes for Android and iOS builds

## [3.0.0] - 2026-04-04
- Phase 2 complete: iOS real implementations + Desktop WebAuthn (~1,337 lines)
- Multi-step authentication flow + voice search UI
- FIDO2/WebAuthn integration via Credential Manager
- Widget demo, release keystore, Dev Portal
- FIVUCSAS launcher icons (all densities + adaptive)

## [2.0.0] - 2026-04-04
- APK v2.0.0 production config with ProGuard and shrinkResources
- Phase 1 complete: build fixes, deprecated API updates, permissions
- Flutter-to-KMP documentation rewrite

## [1.0.0] - 2026-03-30
- Kotlin 2.1.20, AGP 8.7.3, Compose 1.7.3, Gradle 9.4.1
- Non-2xx response handling to prevent deserialization crashes
- KMP shared module with Android + Desktop targets

## [0.1.0] - 2025-10-17
- Initial commit: project scaffolding and environment setup
