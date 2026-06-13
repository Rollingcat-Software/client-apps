# Mobile Local Build & Test Guide

**Audience:** a developer (or a Claude Code instance) on a laptop who wants to **build the
FIVUCSAS mobile app from source, install it on a real phone or emulator, and verify the
login flow** — without relying on the GitHub release APK or mobile data on the phone.

> Why this guide exists: the FIVUCSAS server (Hetzner CX43) **cannot run an Android
> emulator** — it has no `/dev/kvm` / CPU virtualization, no display, and no Android SDK.
> It can compile the app and run unit tests headlessly, but it cannot exercise the phone
> UI. A laptop with Android Studio (or a phone over USB) can. Use this guide there.

---

## 0. Current state (read first)

- **Latest release: `v5.2.3`** (versionCode 10). Tag: https://github.com/Rollingcat-Software/client-apps/releases/tag/v5.2.3
- Recent login fixes — verify these specifically:
  - **v5.2.2** fixed a login **flicker** (the MFA screen used to bounce straight back to
    Login: a Koin *factory* `LoginViewModel` handed the MFA screen a fresh empty copy with
    no session token). Login now reaches the MFA step.
  - **v5.2.3** fixed **MFA completion** showing a false **"Verification failed"**: after the
    server returned `200 AUTHENTICATED`, the app wrote login data to encrypted storage
    *inside* the verify `try` *before* committing success — a throw there was caught and
    overwrote the success with the generic error. Now success is committed first and caching
    is best-effort, so a server success can never render as a failure.
  - **v5.2.3** also fixed the system **navigation bar covering the bottom Cancel button**
    (window-insets padding).
  - The old **"Continue as Guest (Face Check)"** button was **removed** (it was a demo
    leftover with no web equivalent) and a **show/hide password** toggle was added.

The acceptance checklist in §5 is exactly what to confirm on a device.

---

## 1. Prerequisites

| Need | Detail |
|------|--------|
| JDK | **21** (`JavaVersion.VERSION_21`, `jvmToolchain(21)`) |
| Android SDK | `compileSdk 35`, `targetSdk 35`, `minSdk 24` (Android 7.0+) |
| `ANDROID_HOME` | point at your SDK; Android Studio installs it |
| A target | **either** an emulator (needs KVM/HAXM/Hypervisor on the laptop) **or** a physical Android phone (API 24+) with USB debugging |
| Repo | `github.com/Rollingcat-Software/client-apps`, default branch `main` |

**No signing keystore is needed for testing** — debug builds self-sign. The production
keystore is only for release builds (see `docs/SIGNING.md` / `docs/RELEASE.md`).

**Phone-over-USB uses no mobile data:** the laptop (on wifi/ethernet) downloads deps and
builds; `adb`/Gradle pushes the APK to the phone over the USB cable. The phone only spends
data on its own API calls to `api.fivucsas.com`, which are tiny (JSON), not the 100 MB APK.

---

## 2. Build & install (debug)

```bash
git clone https://github.com/Rollingcat-Software/client-apps.git   # or: git pull
cd client-apps

# Build a debug APK (no keystore required)
./gradlew :androidApp:assembleDebug
# → androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Plug in the phone (Developer Options → USB debugging ON), confirm it's seen:
adb devices

# Install + launch on the connected phone or a running emulator:
./gradlew :androidApp:installDebug
# (or: adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk)
```

The app targets **production** by default — `ApiConfig`/`AppConfig.BASE_URL =
https://api.fivucsas.com/api/v1`. No local backend is required; you're testing against the
real API.

> To reproduce the **production-signed** app exactly (e.g. to test FINGERPRINT — see §6),
> install the release APK from the GitHub Releases page instead of a debug build.

---

## 3. Fast UI/compile sanity without a phone or emulator

```bash
./gradlew :desktopApp:run     # launches the Compose Desktop app on the laptop
```

Good for a quick "does it compile and render" check. **Caveat:** the desktop app's login
uses OAuth-loopback / QR screens, **not** the Android `LoginScreen` + `MfaFlowScreen` path —
so it does **not** reproduce the Android MFA flow. Use a phone/emulator for that.

---

## 4. Unit tests (headless, fast — run before every push)

```bash
./gradlew :shared:test :androidApp:testDebugUnitTest
```

Key regression tests that must stay green:
- `MfaFlowAuthenticatedRegressionTest` (shared) — proves a server `AUTHENTICATED` response
  becomes `Authenticated` state even if encrypted-storage caching throws (the v5.2.3 fix).
- `MfaHandoffTest` / `LoginViewModelTest` — the v5.2.2 session-handoff contract.
- `LoginScreenTest` — guest button absent, password toggle present.

CI (`.github/workflows/android-build.yml`) runs these before assembling, so a red unit test
blocks the build.

---

## 5. Acceptance checklist — what to verify on a device

**Login screen**
- [ ] There is **no** "Continue as Guest (Face Check)" button.
- [ ] The password field has a **show/hide (eye) toggle** that works.

**Direct login (no MFA)** — use a no-MFA test account (e.g. the seeded
`e2e-sweep@fivucsas.local` ROOT account; **get its password from the operator / your
team's secret store — do not hard-code credentials in the repo**), or register a fresh
account from the app's Register screen:
- [ ] email + password → lands on the **Dashboard**, no flicker, no bounce-back.

**MFA login** — use a real MFA account (e.g. `ahabgu@gmail.com`, a 3-step flow: password +
two of {Email OTP, SMS OTP, TOTP, …}):
- [ ] password → **"Select Verification Method" (Step 2 of 3)**.
- [ ] pick a method (e.g. **Email OTP** → receive the code → enter it) → advances to Step 3.
- [ ] complete Step 3 → lands on the **Dashboard**.
- [ ] **It must NOT show "Verification failed" after a step the server accepted.** (v5.2.3.)
- [ ] the bottom **Cancel** button is fully visible, not half-hidden by the system nav bar.

**NFC** (NFC-capable phone)
- [ ] The NFC read screen offers a **"Register this card"** action (calls `/nfc/enroll`).

If any login box above is unchecked on **v5.2.3 or a fresh build of `main`**, capture the
screen + the method, and have the server side read the prod API logs for that timestamp to
confirm whether the failure is client or server (the v5.2.3 bug was a client mishandling of a
server **200** — always check the server verdict before assuming the API failed).

---

## 6. Known caveats — do NOT chase these as bugs

- **FINGERPRINT / passkey (WebAuthn) will fail on a debug build and on most emulators.**
  WebAuthn ties the assertion to the app's **signing-certificate SHA-256**, which must be
  registered server-side (Digital Asset Links / allowed origins). A **debug** APK is signed
  with the debug keystore (different cert than the release), so its passkey assertions are
  rejected ("incoming request cannot be validated"). Test **password / email OTP / SMS OTP /
  TOTP / face** on debug builds; validate FINGERPRINT only with the **production-signed
  release** on a real device that has a screen lock + Google Play services.
- **Email/SMS OTP** require the account's real email/phone to receive the code.
- **Emulator + camera/NFC:** FACE needs a (virtual) camera; NFC needs real NFC hardware —
  use a physical phone for those.

---

## 7. Iterating with Claude Code on the laptop

1. Edit `shared/` (cross-platform logic/UI) or `androidApp/` (Android host).
2. `./gradlew :androidApp:installDebug` to push to the connected phone; re-test.
3. `./gradlew :shared:test :androidApp:testDebugUnitTest` must be green before pushing.
4. Branch off `main`, open a PR to `Rollingcat-Software/client-apps`.
5. The biometric/auth backend is shared with the web app; the API contract lives in
   `identity-core-api` (Spring) and `biometric-processor` (FastAPI). Login/MFA endpoints:
   `POST /api/v1/auth/login`, `POST /api/v1/auth/mfa/step`, `DELETE /api/v1/auth/mfa/session/{token}`.

## 8. Reference
- `README.md`, `CLAUDE.md` — architecture & conventions.
- `docs/RELEASE.md`, `docs/SIGNING.md` — signing & release.
- Open GitHub issues — current backlog / roadmap.
