# client-apps TODO

> Source of truth for current sprint across Android / iOS / Desktop. Derived
> from the 2026-04-18e cross-platform deep review. See also `CHANGELOG.md`,
> `README.md` "Feature coverage matrix," and `../docs/plans/` (especially
> `PATH_TO_20_20.md`, `CLIENT_APPS_PARITY.md`, `NFC_PUSH_APPROVAL_PROTOCOL.md`).

**Last updated:** 2026-04-18e

---

## Phase A — Android 20/20 close-out (current sprint)

Five Android feature-parity gaps identified by the 2026-04-18e cross-platform
deep review. Android baseline is ~15/20 against the web-app 20/20 reference.
Canonical plan with file-level detail: `../docs/plans/PATH_TO_20_20.md`.

### A1 — Passport BAC MFA integration (~2 days)

NFC crypto stack (`PassportNfcReader` 873 LOC, `TurkishEidReader` 457,
`BacAuthentication` 502, `SecureMessaging` 470, plus Dg1/Dg2/MRZ parsers +
`CardReaderFactory`, **5,447 LOC total**) is already under
`androidApp/src/main/kotlin/com/fivucsas/mobile/android/data/nfc/`.
`NfcReadScreen.kt` (642 LOC) exists with MRZ input UI + `koinInject<INfcService>`.
The gap is **integration only**: `MfaFlowScreen.kt:324` still routes
`NFC_DOCUMENT` to `GenericMethodStepInput` placeholder.

- [ ] Port `MrzScannerScreen.kt` from `practice-and-test/UniversalNfcReader`
      (CameraX preview + ML Kit text recognition + ICAO MRZ line regex).
- [ ] Create `NfcStepScreen.kt` hosting `MrzScannerScreen` → BAC key
      derivation → `PassportNfcReader.read()` → DG1/DG2 parse → server
      `/api/v1/mfa/nfc/verify`.
- [ ] Create `NfcStepViewModel.kt` + `MrzScannerViewModel.kt`.
- [ ] Replace the `GenericMethodStepInput` dispatch at `MfaFlowScreen.kt:324`.
- [ ] MRZ capture copy in EN + TR `strings.xml`.

#### NFC card enrollment (done) + passive auth (deferred)

- [x] **Enroll wiring (2026-05-30).** `NfcReadScreen` now offers "Register
      this card" → `EnrollNfcCardUseCase` → `POST /api/v1/nfc/enroll`. The
      serial is normalized to the API-canonical UPPERHEX-no-separators form
      (`normalizeCardSerial`), aligned with identity-core-api so a
      mobile-enrolled card matches a web verify. Shared client:
      `NfcEnrollmentApi(Impl)` + `NfcEnrollmentRepository(Impl)`.
- [x] **Passive authentication → server (CODE-COMPLETE 2026-05-30).** The
      readers now surface raw `EF.SOD` + DG1 + DG2 bytes through
      `NfcIdentityDocumentData`; a "Verify authenticity" action base64-encodes
      them and POSTs to `/api/v1/nfc/verify-authenticity` (200 authentic /
      422 `reasonCode` / 400 missing-SOD), fail-closed. Shared
      `NfcAuthenticityApi/Repository` + `VerifyNfcAuthenticityUseCase`, i18n,
      tests. **Awaiting only OPERATOR:** load ICAO-PKD CSCA roots (esp. Turkey)
      into the **bio container** trust dir, else verify returns
      `NO_TRUST_STORE` (422). Needs physical eID/passport test cards to
      end-to-end validate.
- [x] **`CscaCertificateStore` populate hook (CODE-COMPLETE).** The store was
      never empty (full PEM/DER/chain loading + DS→CSCA validation already
      existed); added `loadBundledRoots(context)` auto-discovery from
      `assets/csca/<COUNTRY>/` (called at startup) + a drop-in README. Client
      DS→CSCA check stays *advisory* (server verdict authoritative). OPERATOR:
      drop Turkey CSCA root into `assets/csca/TUR/`.
- [ ] **PACE** (read `EF.CardAccess`, GM mapping, AES secure messaging) to
      broaden beyond BAC for PACE-only documents. Needs PACE-capable test
      docs + a reference-vector doc.

### A2 — GDPR/KVKK export mobile UI (~2 days)

Backend `GET /users/{id}/export` shipped 2026-04-16b. Web-app wired
2026-04-18 on `MyProfilePage`. Android has **zero UI**.

- [ ] New `GdprRepository` in `data/repository/` hitting the `/export` endpoint.
- [ ] New `GdprViewModel` in `shared/presentation/viewmodel/`.
- [ ] "Download my data" row on `ProfileScreen` with DataStore-gated rate limit.
- [ ] Android `DownloadManager` integration to persist the returned JSON to
      the Downloads folder.
- [ ] 8 i18n keys (EN + TR): button label, confirmation dialog, success toast,
      error toast, "Download started" notification, file-name template,
      rate-limit message, KVKK disclosure.
- [ ] DI wiring in `AppModule.kt`.

### A3 — FCM action buttons + `fivucsas://nfc-session` deep-link (~2 days)

Current `FivucsasFirebaseMessagingService` shows plain notifications. The
`AndroidManifest.xml` has only the `TECH_DISCOVERED` intent-filter. Protocol
spec already exists in `../docs/plans/NFC_PUSH_APPROVAL_PROTOCOL.md` (Ed25519
device registration, FCM/APNS push payload, V39 migration sketch, 13-threat
security review).

- [ ] Allow / Deny `NotificationCompat.Action` buttons on push notifications
      built in `FivucsasFirebaseMessagingService`.
- [ ] New `ApprovalActionReceiver` (BroadcastReceiver) POSTing signed
      approval to `/api/v1/nfc/approve` or `/deny`.
- [ ] Add `fivucsas://nfc-session` intent-filter to `AndroidManifest.xml`
      (`android:autoVerify="false"`).
- [ ] `MainActivity.onNewIntent` handler parsing the session-id path segment
      and navigating to `NfcStepScreen`.
- [ ] Ed25519 signature helper per protocol spec (shared `commonMain`).

### A4 — Dark mode toggle in Settings (~1 day)

`AppColors.kt` already exposes both palettes. Theme is driven by
`isSystemInDarkTheme()` with no user override; Settings has no theme row.

- [ ] Add `ThemeMode { SYSTEM, LIGHT, DARK }` enum in
      `shared/presentation/state/`.
- [ ] New `ThemePreferences` backed by DataStore.
- [ ] Expose via `CompositionLocalOf<ThemeMode>` so `FivucsasTheme` can
      resolve the effective palette.
- [ ] 3-radio row on `SettingsScreen` ("Follow system / Light / Dark") with
      live preview.
- [ ] 4 i18n keys (EN + TR): section label, follow-system, light, dark.

### A5 — Authenticator QR scanner (~1 day)

v5.1.0 shipped manual entry only. The bottom-sheet "Scan QR" entry is
currently a `Toast`. Existing `QrScannerScreen` (CameraX + ML Kit barcode)
is already in the codebase for the QR-code auth method. `OtpauthUri.parse()`
is already implemented in `shared/commonMain/.../authenticator/totp/`.

- [ ] Create `OtpQrScannerScreen.kt` reusing `QrScannerScreen` CameraX + ML
      Kit pipeline, filtering `BARCODE_FORMAT_QR_CODE`.
- [ ] Pipe raw text through `OtpauthUri.parse()` and dispatch a
      `ScannedAccount` event up to `AuthenticatorViewModel.addAccount()`.
- [ ] Replace the `Toast` fallback in the "Scan QR" bottom-sheet branch of
      `AuthenticatorScreen.kt` with a navigation call.
- [ ] 3 i18n keys (EN + TR): scanner hint, permission-denied, invalid-uri.

---

## Phase B — iOS Xcode project scaffold + Apple Developer enrollment

iOS HMAC actuals are the first blocker between v5.1.0 and an iOS-shippable
TOTP authenticator. Full iOS parity tracked under Phase 2 of
`../docs/plans/CLIENT_APPS_PARITY.md`.

- [ ] iOS and Desktop `hmacSha1` / `hmacSha256` / `hmacSha512` actuals.
      Current `iosMain` stubs throw `TODO()`. Android + Desktop actuals use
      `javax.crypto.Mac` and are fully functional.
- [ ] Xcode project scaffold reusing the KMP `shared` framework (xcframework
      export already wired in `build.gradle.kts`).
- [ ] Apple Developer Program enrollment (organisation account,
      $99 USD / year).
- [ ] iOS WebAuthn via `ASAuthorizationPlatformPublicKeyCredentialProvider`
      (drop-in for the Android Credential Manager wrapper).
- [ ] iOS NFC reader via `CoreNFC` (parity with Android
      `PassportNfcReader`); ICAO MRTD chip read (DG1 MRZ, DG2 face).
- [ ] iOS hosted-login handoff via `ASWebAuthenticationSession` + AppAuth.
- [ ] Settings + Profile + GDPR export UI parity (reuse shared ViewModels).

---

## Phase C — Desktop installer signing

Desktop JVM builds are unsigned today. Full Desktop parity tracked under
Phase 3 of `../docs/plans/CLIENT_APPS_PARITY.md`.

- [ ] Desktop `hmacSha1` / `hmacSha256` / `hmacSha512` actuals (shared with
      Phase B item 1).
- [ ] Desktop NFC reader via PC/SC (`javax.smartcardio`).
- [ ] Windows Authenticode signing with EV code-signing certificate
      (SmartScreen reputation requires EV).
- [ ] macOS notarization via `notarytool` (hardened runtime + stapled
      ticket).
- [ ] Linux AppImage + `.deb` build via Compose Multiplatform's `packageDeb`
      / `packageAppImage`.
- [ ] Desktop GDPR export row + Settings dark-mode row parity (shared
      ViewModels land with Phase A2 + A4).

---

## Phase D — Test burn-down

- [ ] Fix pre-existing `BiometricViewModelTest.enrollFace` failure on
      `client-apps`. Known red test; does not block 20/20 or v5.2.0 tag.
      Root cause investigation: is it fake-service drift or an actual
      ViewModel regression?
- [ ] Compose UI test for `AuthenticatorScreen` (account renders, code
      updates on tick, tap copies to clipboard). Requires a running AVD in
      CI which is not wired in this environment.
- [ ] Compose UI test for `NfcStepScreen` + `MrzScannerScreen` (delivered
      under Phase A1) — same AVD-in-CI blocker.
- [ ] Biometric gate on Authenticator reveal / pull-to-refresh
      (deliberately omitted in v5.1.0; revisit after day-to-day use).

---

## Phase E — BYOD + localisation follow-ups

- [ ] BYOD architecture (tenants host their own biometric store) — 8-week
      lift per `../docs/plans/BYOD_ARCHITECTURE.md`. Book after parent
      `ROADMAP.md` Phase A–F are green.
- [ ] Third-language localisation stubs (Arabic RTL, German, French) — no
      hardcoded strings today, but `en.json` + `tr.json` key parity needs a
      CI linter before new locales land.
- [ ] Accessibility sweep: `aria-describedby` equivalents (`semantics
      contentDescription =`), minimum touch-target audit on Compose
      components (48.dp floor), TalkBack / VoiceOver test pass.
