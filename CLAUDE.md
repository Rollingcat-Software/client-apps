# CLAUDE.md - FIVUCSAS Client Apps

## Project Overview

Kotlin Multiplatform (Compose Multiplatform) clients for the FIVUCSAS biometric identity
platform. Shared `commonMain` business logic with `expect/actual` per platform; Koin DI.
Repo: **`Rollingcat-Software/client-apps`** (the `fivucsas/…` org 404s — `git remote -v` is
authoritative). Submodule path `/opt/projects/fivucsas/client-apps`.

Modules (`settings.gradle.kts`): `:shared` (commonMain logic), `:androidApp`, `:desktopApp`.
iOS is Phase 2 (blocked on Apple Developer enrollment).

## Build & Run

```bash
# Specs: JDK 21, compileSdk/targetSdk 35, minSdk 24. Debug build needs NO keystore.
./gradlew :androidApp:installDebug        # to a USB device
./gradlew :androidApp:assembleDebug       # APK only
./gradlew :desktopApp:run                  # no-emulator UI sanity (NOT the Android MFA flow)
./gradlew :shared:test :androidApp:testDebugUnitTest   # unit gate
```

App points at PROD `api.fivucsas.com/api/v1` (`AppConfig.BASE_URL`). **WebAuthn / FINGERPRINT
fail on a DEBUG build** (debug signing-cert SHA-256 not registered server-side / Digital Asset
Links) — don't chase it; validate fingerprint only on the production-signed release. Test
password/email/sms/totp/face on debug. Full guide: `docs/MOBILE_TESTING_GUIDE.md`.

**This Hetzner server CANNOT run an Android emulator** (no `/dev/kvm`, headless). Diagnose
mobile bugs by: (a) reading prod api logs (`docker logs identity-core-api`) for the SERVER
verdict, (b) replaying API calls with curl against `api.fivucsas.com`, (c) running shared
unit tests headlessly via Gradle JVM. The developer's laptop (KVM / phone-over-USB) runs the
real flow.

## Releases

Signed APK published per `docs/RELEASE.md` + `docs/SIGNING.md`. Keystore in `keystore/`.
**Latest: v5.2.3** (cert SHA-256 `5e403eca…`, versionCode 10). v5.2.x installs in place over
prior signed releases (same cert).

## Login fixes (2026-05-30, v5.2.2 / v5.2.3)

The freshly-downloaded production-signed v5.2.1 could not log in. Three real bugs fixed:

1. **Login flicker / can't pass MFA (v5.2.2, PR #44).** `MfaFlow` in `AppNavigation.kt` was
   reading `mfaSessionToken` from a fresh `LoginViewModel` factory instance (null token →
   instant bounce to Login). Fix: carry the MFA session as a `@Serializable` **`MfaHandoff`**
   JSON encoded into the MFA nav route
   (`shared/.../presentation/state/MfaHandoff.kt`); `LoginViewModel` stays a factory.
2. **MFA completion flips server 200 into false "Verification failed" (v5.2.3, PR #46).**
   After the server returned `200 AUTHENTICATED`, `MfaFlowViewModel.verifyStep` ran
   `offlineCache.cacheLoginData()` (EncryptedSharedPreferences write) INSIDE the verify `try`
   before publishing the success; a throw there was swallowed by the outer catch and
   overwrote success with `MFA_GENERIC_ERROR`. Fix: publish `_authResult` + Authenticated
   FIRST, then `cacheLoginData` / `registerPushToken` best-effort (`runCatching`); the outer
   catch returns early if an auth result already exists. Regression test
   `MfaFlowAuthenticatedRegressionTest`. **Pattern: never run best-effort side-effects inside
   the same try that maps exceptions to a user-facing "failed" — a server success must never
   be flippable to a failure.**
3. **UX (v5.2.2/v5.2.3):** removed the divergent "Continue as Guest (Face Check)" button (web
   has no guest-login); added a show/hide password toggle; fixed the system nav bar covering
   the bottom Cancel button (navigationBars/systemBars window insets).

**OPEN (Phase 0):** a SEPARATE on-device login bug persists even on v5.2.3 — the server
returns `200 AUTHENTICATED` (prod logs confirm) but the app still shows "Verification
failed". Every server-side throw is ruled out; needs the developer's **debug-build
`adb logcat`** to name the on-device error. Mobile config-driven login + the in-app
authenticator/approve screens are gated on this fix.

## Approve-login approver — shared KMP stack (2026-05-30, PR #53)

The no-Firebase, number-matching cross-device approve-login (backend
`POST /auth/approve-login/session` + poll + `/decide`, see `identity-core-api/CLAUDE.md`): the
APPROVER side is a shared `commonMain` stack reusing the `NfcApproval*` convention —
`data/remote/api/ApproveLoginApi{,Impl}.kt`, `data/remote/dto/ApproveLoginDto.kt`,
`data/repository/ApproveLoginRepositoryImpl.kt`, `domain/repository/ApproveLoginRepository.kt`,
`domain/model/PendingApproveLogin.kt`, `presentation/state/ApproveLoginState.kt`,
`presentation/viewmodel/ApproveLoginViewModel.kt`. The approver lists pending requests
(`GET /auth/approve-login/pending`) and approves by entering the matching number. **The
`matchNumber` is a zero-padded STRING** — keep it a String end-to-end (never an Int, or
leading zeros drop). The approver SCREEN is deferred (gated on Phase 0); the stack is merged.

## Architectural direction

**Hosted-first auth** (OAuth 2.0 / OIDC, mirrors the platform direction in the parent
CLAUDE.md): Android uses Chrome Custom Tabs + AppAuth; Desktop uses an RFC 8252 loopback
listener + OS token storage. Parity matrix: `docs/plans/CLIENT_APPS_PARITY.md`.

## Known open incident

- **GitGuardian #29836028** — Android keystore password `fivucsas2026` leaked in public git
  history (commit `db18fa7`, tag `v3.0.0`). Env/Gradle-property scaffolding shipped
  (`cb6eab9`, 2026-04-18). Rotation is operator-gated. Playbook: parent
  `docs/SECURITY_INCIDENTS.md`.

See `docs/TODO.md` for the integration backlog and `ROADMAP_AUTH_2026-05-30.md` (parent) for
the auth program tracker.
