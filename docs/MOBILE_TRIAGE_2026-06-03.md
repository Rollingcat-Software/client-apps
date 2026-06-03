# Mobile App Triage — 2026-06-03

Investigation of user-reported issues on the **released v5.3.0 APK** (`origin/main`,
tag `v5.3.0` = commit `65d33306`). Each issue was traced through the mobile KMP code
(`shared/` + `androidApp/`), the web app (`web-app`, `main`), and the backend
(`identity-core-api`, `main`).

## ⚠️ First: confirm the installed APK build

The `v5.3.0` tag points at commit `65d33306` (PR #80), which **already contains** the
NFC-MRZ camera fix and the QR-screen scroll/nav-bar-inset fix. If the demo phone runs
a build older than that tag, issues #8 and #9 below are stale and disappear on a fresh
rebuild from the tag. **Verify the phone's APK is built from `v5.3.0` (`65d33306`) or
later.**

## Status legend
`FIXED-HERE` shipped in this branch · `FIXED-IN-5.3.0` already in the released code ·
`DEFERRED` follow-up (needs more than a mobile-only quick win) · `BY-DESIGN` working as
intended.

| # | Issue | Root cause | Status |
|---|-------|-----------|--------|
| 1 | "My Invitations" crashes with a JSON/array decode error | Mobile calls `GET /api/v1/invites/received`; **no such backend endpoint** → 404 object decoded as a list → serializer throws. Mobile is ahead of the API. | **FIXED-HERE** |
| 2 | Activity History always empty | Screen is a hardcoded `emptyList()` (`ActivityHistoryScreen.kt:61`); never calls the API. Correct endpoint `GET /api/v1/my/activity` **exists** and the web app uses it. | **FIXED** (#83) |
| 3 | Settings: dead/misleading controls | "Enable Notifications" + "Biometric Authentication" + "Analytics" switches are local no-ops (two default ON). The "Voice, Voice Search, OTP, TOTP, Liveness, Card, Token" line is a static string advertising 6 methods the app no longer ships, mislabeled under "Biometric Authentication". | **FIXED-HERE** |
| 4 | Notifications always empty | Hardcoded `emptyList()` (`NotificationsScreen.kt:40`). **No notifications-feed endpoint exists** on the backend (only notification *preferences*). | **FIXED-HERE** (bell hidden) |
| 5 | "Login requests" inaccessible from web | Backend + mobile approver + web initiator panel are all built, but the web launch button was removed in PR #141 and **never re-homed**, so nothing can start a request → mobile screen always empty. | **FIXED** (web #199 re-homed the button) |
| 6 | "Scan ID Card / Capture Front" — what is it? | The "Add Card" photo wizard (`CardScanScreen.kt`). It photographs the card, said "Card Added Successfully", then **discards the images** — no OCR, no upload, no backend. | **FIXED-HERE** (honest copy) |
| 7 | QR login "not working because of mobile" | Two unrelated QR systems. Cross-device QR login (phone scans desktop) has a complete mobile+API half but the **web/desktop screen that shows the login QR was never built**. The web's only QR is a same-device MFA step. | **PARTIAL** (web #199 built the desktop QR; multi-step handoff deferred) |
| 8 | "Scan with MRZ" disabled | Button requires the 3 MRZ fields; v5.3.0 added a "Scan MRZ with camera" button that auto-fills them (PR #80). | **FIXED-IN-5.3.0** |
| 9 | QR bottom button invisible | QR screen now scrolls + has nav-bar insets (PR #80); button is reachable (may be below the fold). | **FIXED-IN-5.3.0** |
| 10 | "Join a Tenant" / "Members: 0" | Honest read-only directory; self-service join genuinely doesn't exist. "Members: 0" is real data (tenants the user isn't in, scoped by the tenant filter), not a bug. | **BY-DESIGN** |

## What this branch changed (demo-safe, mobile-only)

1. **`InviteApiImpl.getReceivedInvites()`** → returns `emptyList()` (the endpoint
   doesn't exist; the screen has a proper empty state). Kills the crash.
2. **`SettingsScreen.kt`** → removed the three dead switches; Security card now shows
   only the wired Change Password; auth card retitled "Authentication" with subtitle
   "Authenticator app (TOTP)".
3. **`StringResources.kt`** → new `SETTINGS_AUTH_SECTION` key (EN "Authentication" / TR
   "Kimlik Doğrulama"); honest `SETTINGS_AUTH_METHODS_SUB`; "Card Added*" → "Photos
   Captured (preview only)" (EN + TR).
4. **`DashboardScreen.kt`** → notifications bell hidden (no backend feed).

> All mobile fixes require an **APK rebuild + reinstall** to reach the phone.

## Resolution (2026-06-03)

The scope-B follow-ups were built and shipped the same day:

- **#2 Activity History — DONE (client-apps #83, merged).** New `getMyActivity(page,size)`
  on the audit API/repository hitting `GET /api/v1/my/activity`, a new
  `ActivityHistoryViewModel`, action→chip mapping, loading + Retry-on-error states.
  Reaches devices on the next APK rebuild.
- **#5 Approve-login — DONE (web-app #199, merged + deployed).** `Layer1Shortcuts`
  renders the "Approve on another device" button again (both `LoginPage` and
  `HostedLoginApp` already passed `onApproveClick`; the panel self-collects the email).
  So a web sign-in can finally surface a request on the mobile "Login requests" screen.
- **#7 Cross-device QR login — PARTIAL (web-app #199, merged + deployed).** New
  `qr-login.ts` + `QrLoginPanel` on both surfaces; the QR encodes the **sessionId**
  (`fivucsas://qr-login?session=<id>` — what the mobile scanner resolves; `qrContent`
  is not a lookup key). Works end-to-end for single-step tenants (browser-verified
  live on app.fivucsas.com). **Remaining:** a multi-step tenant (e.g. Marmara) returns
  `mfaRequired` and the panel shows "continue here" rather than bridging the
  `mfaSessionToken` into the step-up flow — that handoff needs a small API change (add
  the next step's `availableMethods` to the QR poll response) and was deferred to avoid
  a prod API rebuild right before the demo.

Deploy state: **app.fivucsas.com** auto-deployed via the Hostinger workflow on merge;
**verify.fivucsas.com** rebuilt + redeployed (Docker). The **mobile** fixes (scope-A +
#83) reach the phone only after an operator **APK rebuild + reinstall**.

## Still deferred (need new backend endpoints)

- **#4 Notifications feed** / **#1 invitations-received list** — post-demo; no backend
  endpoint exists yet.
