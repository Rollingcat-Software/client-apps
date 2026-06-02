# Changelog

All notable changes to the FIVUCSAS client apps (Android, iOS, Desktop).

## [Unreleased]

## [5.3.0] - 2026-06-02

> First release since 5.2.3 (production-signed APK). Bundles the full post-5.2.3
> work: **hosted-first login** (AppAuth + Chrome Custom Tabs, #76), full **NFC
> e-document** support incl. MRZ camera scan (#50/#51/#79/#80), the slim-down to a
> **focused authenticator** (redundant server-biometric screens + dead
> web-dashboard routes removed, #79/#80), critical auth/data fixes (#56–#67),
> wave-3 i18n + a11y (EN+TR) + NavigationPolicy fail-closed (#69–#71), web-parity
> fonts (#72), the ViewModel scope-leak refactor (#73), and stale-endpoint +
> OAuth-refresh wiring (#78). versionCode 12 / versionName 5.3.0.

### Added

- **Hosted-first login (AppAuth + Chrome Custom Tabs, PR #76).** The credential +
  MFA ceremony now runs on `verify.fivucsas.com` in a Custom Tab; the app is a
  thin OAuth 2.0 / OIDC client that exchanges the returned code and routes by the
  role from `/auth/me`. The shell is restyled to match the web app (brand mark,
  "Secured by FIVUCSAS" pill, gradient action, `verify.fivucsas.com` footer).

- **NFC MRZ scan via the camera (PR #80).** `NfcReadScreen` now offers "Scan MRZ
  with camera" — ML Kit OCR (ICAO 9303 TD1/TD3) reads the passport/ID
  machine-readable zone and auto-fills the document-number / DOB / expiry fields,
  enabling the chip read without hand-typing the MRZ (manual entry kept as a
  fallback). Re-wires the existing camera scanner that had been stranded on a
  screen orphaned by the hosted-login switch.

- **Bundled Inter + Poppins fonts (web-app parity, PR #72).** `androidApp`'s
  `AppTypography` now renders the real web typefaces instead of the system
  sans-serif fallback: **Poppins** for headings (`display*`/`headline*`/
  `titleLarge`), **Inter** for body/title/label. Fonts ship under
  `androidApp/src/main/res/font/` (Inter variable + Poppins 4 static weights).
  Matches `web-app/src/theme.ts`.

- **NFC PACE: EF.CardAccess parse + vector-tested key derivation.** New
  `CardAccessParser` parses the chip's advertised `PACEInfo` entries (protocol
  OID + version + domain-parameter id; BouncyCastle ASN.1, no card needed). New
  `PaceKeyDerivation` implements the TR-03110 / ICAO 9303 KDF
  (`KDF(K,c)=H(K‖c)`, counters 1=enc/2=mac/3=password) and is **vector-tested
  byte-exact** against the published ICAO 9303 key-derivation worked example
  (Kseed `561754EE…` → Kenc `EB0F20E3…` / Kmac `6DC37B57…`). `PaceAuthenticator`
  selects a protocol (prefers Generic Mapping + AES), derives `K_π` from the
  MRZ seed, and provides the BAC-fallback seam (`run()` returns
  `NotImplemented`). Deferred (needs a physical PACE card): the on-card GM
  handshake (MSE:Set AT + GENERAL AUTHENTICATE) + AES secure messaging. See
  `docs/NFC_PACE_PLAN.md`.

- **NFC passive authentication (server-authoritative).** The NFC readers now
  surface the RAW `EF.SOD` + `DG1` + `DG2` bytes through the domain model
  (`NfcIdentityDocumentData.sodBytes/dg1Bytes/dg2Bytes`). A new "Verify
  authenticity" action on the reader result base64-encodes them and submits to
  `POST /api/v1/nfc/verify-authenticity` for the authoritative, fail-closed
  verdict (200 authentic / 422 not-authentic with `reasonCode` / 400 missing
  SOD). New shared `NfcAuthenticityApi(Impl)` +
  `NfcAuthenticityRepository(Impl)` + `VerifyNfcAuthenticityUseCase`; i18n
  EN + TR; unit tests. The client-side DS→CSCA chain check stays advisory.
  `CscaCertificateStore.loadBundledRoots(context)` (called at startup)
  auto-loads bundled ICAO-PKD CSCA roots from `assets/csca/<COUNTRY>/`.
  **Operator action to activate validation:** drop CSCA roots (esp. Turkey)
  into `assets/csca/TUR/` AND into the bio container's trust dir — until then
  the server returns `reasonCode=NO_TRUST_STORE`. The code path is complete.

- **Linked accounts + workspace (account) switcher** — mobile parity with
  the web `linkedAccounts` / `accountSwitcher` features. A new
  `LinkedAccountsScreen` (reached from Profile) lists the person's verified
  emails and tenant memberships, links another account
  (`/identity/link/initiate` → OTP + step-up password →
  `/identity/link/confirm`), unlinks a membership (`/identity/unlink`), and
  switches the active membership in-session
  (`/auth/switch-membership` → login-shaped tokens persisted via the
  canonical `TokenManager` path, then re-navigates to the post-login home).
  The switcher shows only when the person has >1 membership and is distinct
  from any SUPER_ADMIN data-scoping switch (it changes WHO you are). New
  shared client `AccountLinkingApi(Impl)` + `AccountLinkingRepository(Impl)`
  + `AccountLinkingViewModel`; `IdentityMe` domain model; i18n EN + TR; unit
  tests.

- **NFC card enrollment.** `NfcReadScreen` now offers a "Register this card"
  action on a successful read → `EnrollNfcCardUseCase` →
  `POST /api/v1/nfc/enroll`. The card serial is normalized to the
  API-canonical form (upper-case hex, no separators — e.g. `04A2245B6F7180`)
  via `normalizeCardSerial`, aligned with identity-core-api so a
  mobile-enrolled card matches a web verify and vice-versa. New shared
  client: `NfcEnrollmentApi(Impl)`, `NfcEnrollmentRepository(Impl)`,
  `NfcEnrollRequest/Response` DTOs. i18n EN + TR. (Android `Tag.getId()`
  already yields canonical UPPERHEX; the normalizer guards opaque UIDs and
  guarantees we never emit separators.)

### Changed

- **ViewModel CoroutineScope leak fixed (PR #73).** Shared ViewModels each
  created their own `Dispatchers.Main` scope but, as Koin `factory` instances,
  were never cancelled — every navigation leaked a scope and the polling VMs
  (Approve-login / QR-login / Kiosk) leaked live `while (isActive)` loops. New
  `BaseViewModel` (owns `viewModelScope` + `dispose()`) + a `disposeOnLeave()`
  Compose helper that cancels on composition-leave. 30 ViewModels converted; 58
  call sites wired across android/shared/desktop. The no-scope auth VMs
  (Login/Register/Biometric/Fingerprint/Mfa) are unchanged. See
  `CLAUDE.md` › "ViewModel lifecycle".

- **Desktop installers track the release line.** `desktopApp` version 1.0.0 →
  5.2.3, so the produced `.deb` / `.msi` match the published app version
  instead of a stale `1.0.0`. README desktop status corrected from
  "scaffolding in flight" to shipped (OAuth loopback RFC 8252 + PKCE wired as
  the default entry; real DPAPI/libsecret/AES-GCM token storage via
  `TokenStorageFactory`; `.deb` + `.msi` via `desktop-installers.yml`). Fixed
  a stale "stub storage" comment in desktop `Main.kt`.
- **CI now runs unit tests.** `android-build.yml` runs
  `./gradlew :shared:test :androidApp:testDebugUnitTest` before assembling —
  previously CI only assembled and the Kotlin unit tests never ran. (The
  instrumented `androidTest` set still needs an emulator and is not run in
  CI.)
- **Docs reconciled.** README "thin OAuth client" framing corrected — the
  FIVUCSAS Android app is a full native client (native login + adaptive MFA,
  NFC read + enroll, on-device biometric), with hosted-first OAuth as the
  primary *third-party* integration mode. `docs/TODO.md` records the enroll
  wiring as done and the passive-auth / CSCA / PACE work as deferred
  (operator-blocked: needs CSCA roots + test cards).
- **Stale REST endpoint paths + OAuth refresh-token wiring (PR #78).** Corrected
  out-of-date API paths and wired the OAuth refresh-token flow for the
  hosted-login session.
- **QR scan screens made responsive (PR #80).** `QrLoginScanScreen` +
  `OtpQrScannerScreen` now scroll, apply keyboard (`imePadding`) + system-nav-bar
  insets, and use a width-scaled square camera box/reticle instead of a fixed
  420/400dp height — the Submit/Cancel controls stay reachable on short screens
  and in landscape, and the keyboard no longer hides the payload field.
- **Server-side logout (PR #80).** Settings logout now calls
  `AuthRepository.logout()` (`POST /auth/logout` + step-up-token clear) instead
  of only clearing the local access token, so the session/refresh token is
  revoked server-side.
- **Localized the hosted-login screen + bottom navigation, EN + TR (PR #80).**
  The hosted-login screen (the first screen every user sees) was English-only;
  its labels + error messages are now localized, and the bottom-nav
  Home/Invites/QR/History labels are localized.

### Removed

- **Slimmed the Android app to a focused authenticator (PR #79 + #80).** Removed
  server-pipeline biometric screens (face/voice enroll + verify, liveness, TOTP
  enroll, card detection, biometric backup) and dead web-dashboard routes
  (auth-flows, sessions, devices, enrollments, invite-management) plus an
  unreachable fingerprint step-up flow and orphan screens — those surfaces live
  on the web dashboard / hosted page. The mobile app is now login + TOTP
  authenticator + approve-login + QR login + NFC/card capture + personal
  self-service. Shared screens/ViewModels remain for desktopApp.

## [5.2.3] — 2026-05-30 — MFA completion false-failure fix (P0)

v5.2.2 fixed the login flicker so password login reaches the MFA step — but
MFA *completion* then failed on the client: the server returned a clean
`200 AUTHENTICATED` (tokens minted, no 500) yet the app showed
"Verification failed" and stranded the user.

### Fixed

- **MFA completion no longer flips a server 200 into a false failure.** In
  `MfaFlowViewModel.verifyStep`, the `AUTHENTICATED` branch ran
  `offlineCache.cacheLoginData(...)` (an encrypted-prefs write) INSIDE the
  verify `try` BEFORE `_authResult` was published. A throw there — e.g. a
  keystore / `EncryptedSharedPreferences` write failure — was swallowed by
  the outer `catch` and overwrote the committed success with
  `MFA_GENERIC_ERROR`. Now the auth result + `Authenticated` state are
  published FIRST; the offline-cache write and FCM push registration are
  best-effort (`runCatching`); and the outer `catch` returns early if an
  auth result already exists, so a late throw can never strand an
  authenticated user. Regression test: `MfaFlowAuthenticatedRegressionTest`
  (AUTHENTICATED survives a throwing secure-storage + throwing push
  service).
- **System navigation bar no longer occludes the MFA bottom buttons.** The
  `MfaFlowScreen` has no `Scaffold`, so it drew under the system bars — the
  bottom "Cancel" (and Retry / Back / Enroll) actions were ~half-covered by
  the Android navigation bar on both gesture-nav and 3-button-nav devices.
  Added `windowInsetsPadding(WindowInsets.systemBars)` to the screen's root
  container so all content stays clear of the status and navigation bars.

### Changed

- `androidApp/build.gradle.kts`: `versionCode` 9 → 11, `versionName`
  5.2.2 → 5.2.3. (versionCode 11 re-issues the v5.2.3 build with the MFA fix
  + the navigation-bar inset fix; the original v5.2.3 APK asset is replaced.)

## [5.2.2] — 2026-05-30 — Login fix (P0)

A user who freshly installed v5.2.1 could not log in. Three login-screen
defects fixed.

### Fixed

- **Could not pass MFA / instant bounce back to Login.** The MfaFlow
  destination read its session token off `koinInject<LoginViewModel>()`, but
  `LoginViewModel` is a Koin **factory** — that injection returned a
  brand-new instance with a `null` token, so the flow immediately reset and
  navigated back to Login. A password login that escalated to MFA could never
  reach the MFA step. The MFA session state (session token + available
  methods + step counters) is now carried forward as an explicit
  `MfaHandoff` payload encoded into the navigation route; `LoginViewModel`
  stays a factory (no global-singleton auto-bounce footgun). A malformed /
  missing payload (process death) bails cleanly back to Login.

### Added

- **Show/hide password toggle** on the login form — a trailing
  Visibility / VisibilityOff `IconButton` flips
  `PasswordVisualTransformation` ↔ `VisualTransformation.None`. Accessibility
  `contentDescription` is localized (EN: "Show password" / "Hide password",
  TR: "Şifreyi göster" / "Şifreyi gizle") via new `SHOW_PASSWORD` /
  `HIDE_PASSWORD` string keys.

### Removed

- **"Continue as Guest (Face Check)" button** from all three login variants
  (legacy password, passwordless primary, unsupported primary). The web app
  has no guest-login button (web "guests" are tenant invitations, a different
  concept), and the mobile route was a dead end. The `GuestFaceCheck`
  composables/routes remain in the tree but are no longer reachable from
  login.

### Changed

- `androidApp/build.gradle.kts`: `versionCode` 8 → 9, `versionName`
  5.2.1 → 5.2.2.

## [5.2.1] — 2026-05-30 — First production-signed Android release

First Android release built and signed with the **rotated production upload
keystore** (delivered via the `ANDROID_KEYSTORE_BASE64` / `ANDROID_KEYSTORE_PASSWORD`
/ `ANDROID_KEY_ALIAS` / `ANDROID_KEY_PASSWORD` GitHub Actions secrets). Every
prior release APK was debug-signed.

### Changed

- `androidApp/build.gradle.kts`: `versionCode` 7 → 8, `versionName` 5.2.0 → 5.2.1.

### Includes (already on `main` since the v5.2.0 tag)

- **Biometric login repointed to a reachable host (#41).** `bio.fivucsas.com`
  has no public DNS; all FACE enroll/verify/liveness/search now go through
  `api.fivucsas.com/api/v1` (Identity Core API → internal processor). Passive
  liveness folded into `/verify`; `checkLiveness` returns a non-blocking result
  so it can never gate login. `ApiConfig` defaults to PRODUCTION.
- i18n + UX sweep, dynamic primary-step login screen, MFA cancel/switch-method
  wiring, SECURITY.md + LICENSE, doc-freshness fixes (#27–#42).

### Signing

- Release variant is now signed with the production keystore (not the Android
  debug key). Uploadable to Google Play; updates over a debug-signed v5.2.0
  install require uninstall-first (signing-key mismatch — expected).

## [2026-04-19] Audit remediation (MO-H1/H3/H4/H6/C3)

Addresses the four mobile findings from
`/opt/projects/fivucsas/docs/audits/AUDIT_2026-04-19.md` (Audit 4).

### Security

- **MO-H1 — iOS Keychain accessibility class.**
  `shared/src/iosMain/.../IosSecureStorage.kt`: replaced
  `kSecAttrAccessibleWhenUnlocked` with
  `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`. Prevents token
  exfiltration via iCloud Keychain sync / encrypted iCloud backup restore.
- **MO-H3 — Desktop fallback refuses headless.**
  `desktopApp/.../security/FallbackTokenStorage.kt`: when DPAPI / libsecret
  are unavailable AND `/etc/machine-id` is not readable, we no longer derive
  a key from `hostname+user+os.name`. Instead we throw a new
  `SecureStorageUnavailableException`. CI / headless test rigs can opt in
  via `FIVUCSAS_ALLOW_INSECURE_FALLBACK=1` (env) or
  `-Dfivucsas.allowInsecureFallback=true` (system property), which prints a
  loud stderr warning. Production builds must never set this.
- **MO-H4 — Android hardening.**
  `androidApp/src/main/AndroidManifest.xml`:
  `android:allowBackup="false"`,
  `android:extractNativeLibs="false"`,
  `android:dataExtractionRules="@xml/data_extraction_rules"`,
  `android:networkSecurityConfig="@xml/network_security_config"`.
  New `res/xml/data_extraction_rules.xml` denies cloud-backup +
  device-transfer across root/file/database/sharedpref/external domains
  (Android 12+). New `res/xml/network_security_config.xml` forbids
  cleartext globally and pins `api.fivucsas.com` + `verify.fivucsas.com`
  to the system trust store only (user-installed CAs not trusted in prod).
  True SPKI pinning deliberately deferred until we land a backup-pin /
  rotation plan.
- **MO-H6 — Android POST_NOTIFICATIONS permission.**
  Added `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>`
  so FCM push notifications are not silently no-op on Android 13+.

### Changed

- **MO-C3 — Desktop SecureTokenStorage dedupe.**
  Two parallel `SecureTokenStorage` interfaces used to coexist:
  `desktopApp/.../auth/SecureTokenStorage.kt` (bundle-level) and
  `desktopApp/.../security/SecureTokenStorage.kt` (key/value,
  DPAPI/libsecret/fallback). Kept the key/value primitive and folded
  `AuthStateManager` on top of it directly: the full token bundle is
  serialized to JSON via `kotlinx.serialization` and stored under the key
  `oauth_tokens`. Deleted `auth/SecureTokenStorage.kt` interface and the
  `auth/FileBackedTokenStorage.kt` adapter. `Main.kt` updated to
  `AuthStateManager()` (defaults to `TokenStorageFactory.create()`).
  `OAuthLoopbackClientTest` and `FallbackTokenStorageTest` untouched —
  neither references the deleted interface. No other production call sites
  were affected.

### Files touched

- `shared/src/iosMain/kotlin/com/fivucsas/shared/platform/IosSecureStorage.kt`
- `androidApp/src/main/AndroidManifest.xml`
- `androidApp/src/main/res/xml/data_extraction_rules.xml` (new)
- `androidApp/src/main/res/xml/network_security_config.xml` (new)
- `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/auth/AuthStateManager.kt`
- `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/auth/SecureTokenStorage.kt` (deleted)
- `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/auth/FileBackedTokenStorage.kt` (deleted)
- `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/security/SecureTokenStorage.kt`
  (added `SecureStorageUnavailableException`)
- `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/security/FallbackTokenStorage.kt`
- `desktopApp/src/desktopMain/kotlin/com/fivucsas/desktop/Main.kt`

## [Unreleased] — 2026-04-18f

### Fixed

- **Android — MFA flow no longer hangs on reload.** On process death or
  configuration change after `mfaRequired=true`, `LoginViewModel` state was
  reset, `loginState.mfaSessionToken` came back `null`, `LaunchedEffect(Unit)`
  skipped `initialize()`, and the screen sat on `MfaFlowUiState.Idle`
  rendering a bare `CircularProgressIndicator()` with no escape. Now:
  `AppNavigation.kt` re-keys the init `LaunchedEffect` on the session
  token, and if the token is missing while the VM is still `Idle` the
  route pops back to Login (clears the trap instead of stranding the
  user). Defense-in-depth: the `Idle` branch in `MfaFlowScreen.kt` now
  renders `MFA_PREPARING` copy + a visible Cancel button.
- **Turkish localisation — diacritics restored across `StringResources.kt`.**
  ~600 `trStrings` entries had been flattened to ASCII (`Giris` →
  `Giriş`, `Sifre` → `Şifre`, `Dogrulama` → `Doğrulama`, `Kullanici` →
  `Kullanıcı`, etc.). Restored by hand, verified `compileDebugKotlinAndroid`
  green. English map + `StringKey` enum untouched.

### Added

- `StringKey.MFA_PREPARING` (EN: "Preparing verification...", TR:
  "Doğrulama hazırlanıyor...") for the Idle-state fallback.

## [Unreleased] — architecture note 2026-04-18e

The client-apps feature-parity matrix shrank from **20 columns to 13**
following the 2026-04-16 hosted-first pivot (see
`../web-app/docs/AUDIT_REPORT_2026-04-16.md` and
`../web-app/docs/plans/HOSTED_LOGIN_INTEGRATION.md`, PR-1 merged to `main`
on both `web-app` and `identity-core-api`).

**What changed:**

- Face, Voice, Fingerprint (WebAuthn platform authenticator), Hardware Key,
  Passport NFC, TCKN NFC, Istanbulkart NFC, Student card NFC, Password
  login, Email OTP entry, SMS OTP entry, and the biometric enrollment flow
  are **no longer native client responsibilities**. Users authenticate on
  the hosted login page (`verify.fivucsas.com/login`) via a system-trusted
  browser surface — Chrome Custom Tabs on Android, `ASWebAuthenticationSession`
  on iOS, RFC 8252 loopback on Desktop.
- Native code now owns only a **thin OAuth 2.0 / OIDC client** (13 columns):
  OAuth login, secure token storage, token refresh, deep-link handler,
  account dashboard, cross-device sessions, GDPR/KVKK export, offline
  display, push / WebSocket approval handler, TOTP authenticator
  (companion), QR display + scanner, signed release artifact, public
  distribution.
- **macOS desktop** is explicitly **out of scope for v6** — no Mac hardware
  available for `codesign` + `notarytool`. Revisit with Mac procurement.
- **Platform status (2026-04-18e):**
  - Android: **13 / 13** (v5.2.0-rc1, Phase I complete).
  - Desktop (Windows + Linux): **2 / 13** — scaffolding work in flight
    (`SecureTokenStorage.kt` interface + DPAPI / libsecret impls; OAuth
    loopback client; Compose dashboard skeleton).
  - iOS: **0 / 13** — Phase 2 (July 2026), blocked on Apple Developer
    enrollment. No `iosApp/` module exists yet.

**Why the matrix shrank:** native biometric / NFC crypto reimplementation
tripled the maintenance surface with near-zero cross-platform reuse (Apple
CoreNFC ≠ Android NfcAdapter ≠ PC/SC), iframe-embedded widgets cannot
drive Web NFC / WebAuthn / autofill in top-level context, and every modern
IdP (Auth0, Okta, Entra, Google, Apple, Keycloak, AWS Cognito, Stripe,
Turkish banks, e-Devlet) is hosted-first. One hosted surface, one audit,
one threat model.

Canonical plan: [`../docs/plans/CLIENT_APPS_PARITY.md`](../docs/plans/CLIENT_APPS_PARITY.md)
— rewritten 2026-04-18 with the 13-column matrix. Pre-pivot 20-row matrix
preserved in Appendix A.

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
