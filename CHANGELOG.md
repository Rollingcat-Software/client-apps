# Changelog

All notable changes to the FIVUCSAS client apps (Android, iOS, Desktop).

## [Unreleased] — v5.2.0 planning

Five Android feature-parity gaps identified by the 2026-04-18e cross-platform
deep review. Expected ship order `20A` → `20B` → `20C` → `20D` → `20E`
(ordering is for merge-conflict minimisation; all five are parallelizable in
their own feature branches). Canonical plan: `../docs/plans/PATH_TO_20_20.md`.

- **[20A] Passport BAC MFA integration.** Wire the already-ported NFC crypto
  stack (`androidApp/data/nfc/`, 5,447 LOC) into the multi-step MFA
  dispatcher. Port `MrzScannerScreen.kt` from
  `practice-and-test/UniversalNfcReader` for camera MRZ capture. Create
  `NfcStepScreen.kt`. Replace the `GenericMethodStepInput` dispatch at
  `MfaFlowScreen.kt:324`. (~2 engineer-days)
- **[20B] GDPR/KVKK export mobile UI.** New `GdprRepository` + `GdprViewModel`
  hitting `GET /users/{id}/export`; "Download my data" row on
  `ProfileScreen`; Android `DownloadManager` integration; 8 i18n keys
  (EN + TR). Backend shipped 2026-04-16b; web-app wired 2026-04-18. (~2 days)
- **[20C] FCM action buttons + `fivucsas://nfc-session` deep link.** Allow /
  Deny `NotificationCompat.Action` buttons, new `ApprovalActionReceiver`
  POSTing Ed25519-signed approvals, custom-scheme intent-filter in
  `AndroidManifest.xml`, `MainActivity.onNewIntent` handler. Protocol spec:
  `../docs/plans/NFC_PUSH_APPROVAL_PROTOCOL.md`. (~2 days)
- **[20D] Dark mode toggle in Settings.** `ThemeMode { SYSTEM, LIGHT, DARK }`
  enum + `ThemePreferences` (DataStore) + `CompositionLocal` + 3-radio row
  on `SettingsScreen`. Palettes already present in `AppColors.kt`. (~1 day)
- **[20E] Authenticator QR scanner.** New `OtpQrScannerScreen.kt` reusing the
  existing `QrScannerScreen` CameraX + ML Kit barcode pipeline; pipes scan
  results through `OtpauthUri.parse()` into `AuthenticatorViewModel`.
  Replaces the Toast fallback in the v5.1.0 "Scan QR" bottom-sheet branch.
  (~1 day)

Nothing below is done — tracking only. Items move under `## [5.2.0]` once
merged and smoke-tested per the Wave 4 verification checklist in
`PATH_TO_20_20.md`.

## [5.1.0] - 2026-04-18
- TOTP authenticator engine (RFC 6238) in shared `commonMain`: HMAC-SHA1/256/512, configurable digits and period
- `otpauth://` URI parser with Google/Microsoft-authenticator-compatible examples covered by tests
- `EncryptedSharedPreferences`-backed `TotpVault` for account storage (AES256-GCM, hardware-keystore-backed master key)
- Compose Material 3 `AuthenticatorScreen` (Android): grouped code display, per-account countdown ring, tap-to-copy, swipe/delete confirm, manual entry bottom sheet
- Full i18n coverage (EN + TR) for the new screen; no hardcoded strings
- QR code scanning deferred to a follow-up (manual entry only in 5.1.0); tracked in `docs/TODO.md`
- All RFC 6238 Appendix B test vectors pass for SHA1, SHA256, and SHA512

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
