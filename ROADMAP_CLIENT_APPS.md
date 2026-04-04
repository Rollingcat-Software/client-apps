# FIVUCSAS Client-Apps & Web-App Completion Roadmap

**Created:** 2026-04-04
**Last Updated:** 2026-04-04
**Current Completion:** 92%
**APK Release:** v2.0.0 (https://github.com/Rollingcat-Software/client-apps/releases/tag/v2.0.0)

---

## Phase 1: Critical Build Fixes — COMPLETE

All 8 items done on 2026-04-04.

---

## Phase 2: Core Feature Completion — 8/8 DONE

| # | Task | Status |
|---|------|--------|
| 2.1 | Multi-step auth flow UI | DONE |
| 2.2 | Verification pipeline | DONE |
| 2.3 | Voice search UI | DONE |
| 2.4 | Step-up authentication | DONE |
| 2.5 | Role/permission management | DONE |
| 2.6 | Audit log admin dashboard | DONE |
| 2.7 | iOS real implementations (AVFoundation, Keychain, LocalAuthentication, WebAuthn passkeys) | DONE |
| 2.8 | Desktop WebAuthn (software ECDSA authenticator + credential store) + Fingerprint (PKCS12 keystore) | DONE |

---

## Phase 3: Polish & Production — 3/7 DONE

| # | Task | Status |
|---|------|--------|
| 3.1 | Developer portal screen | DONE |
| 3.2 | Widget demo screen | DONE |
| 3.3 | Test coverage expansion (60%+ target) | TODO |
| 3.4 | CSV export for analytics/audit | TODO |
| 3.5 | E2E testing framework | TODO |
| 3.6 | Gradle 10 compatibility | TODO |
| 3.7 | Release signing keystore | DONE |

---

## Phase 4: Maintenance

| # | Task | Status |
|---|------|--------|
| 4.1 | Resource consolidation | TODO |
| 4.2 | Client-side ML migration (TFLite voice, on-device YOLO) | TODO |
| 4.3 | Istanbul card YOLO training data | TODO |

---

## Web-App Feedback Backlog (Chrome Mobile Testing 2026-04-04)

| # | Item | Status |
|---|------|--------|
| W1 | Face enrollment slow on mobile (MediaPipe fails, timeout fallback) | KNOWN LIMITATION |
| W2 | Face search (matches vs results field mismatch) | FIXED |
| W3 | NFC auth token (wrong localStorage key → DI TokenService) | FIXED |
| W4 | NFC "Whose Card?" for all users | FIXED |
| W5 | Card detection frozenset crash | FIXED |
| W6 | Istanbul card misclassified as ehliyet | KNOWN LIMITATION |
| W7 | Forgot password flow (code + new password) | FIXED |
| W8 | Phone number dialog for SMS OTP enrollment | FIXED |
| W9 | Non-admin personal dashboard | FIXED |
| W10 | Login page: removed broken alt auth, added MFA info text | FIXED |
| W11 | Notifications: user activity via /my/activity endpoint | FIXED |
| W12 | User activity/summary page | PARTIALLY DONE (dashboard has it) |
| W13 | OAuth widget demo tenant (Marmara Exam Portal created) | DONE |
| W14 | GitHub repos organization/cleanup | TODO |
| W15 | User audit log view (via /my/activity endpoint) | FIXED |
| W16 | Cross-device session awareness | TODO |
| W17 | Voice STT verification (speech-to-text matching) | FUTURE |
| W18 | BYOD (Bring Your Own Database) | FUTURE |
| W19 | BaaS per-feature API rental model | FUTURE |

### Additional Issues Found (Late Session 2026-04-04)

| # | Item | Priority | Status |
|---|------|----------|--------|
| W20 | Biometric Tools page width overflow on mobile Chrome | HIGH | FIXED |
| W21 | 2FA login flow crashes (SecondaryAuthFlow ErrorBoundary) | HIGH | FIXED |
| W22 | Hardcoded strings audit across entire app (centralize to i18n) | MEDIUM | FIXED |
| W23 | Terms/Privacy policy pages need real content | LOW | PLACEHOLDER DONE |
| W24 | Settings page duplicates enrollment features | LOW | TODO (remove or link) |
| W25 | Dashboard "from" text not translated | LOW | FIXED |
| W26 | Notification dates need proper formatting | LOW | FIXED |

---

## Feature Parity (Updated end of session)

| Feature | Web-App | Client-Apps | Gap |
|---------|---------|-------------|-----|
| Face biometrics | FULL | FULL | None |
| Voice biometrics | FULL | FULL | None |
| NFC (TC Kimlik) | Web NFC only | FULL native | Client WINS |
| FIDO2/WebAuthn | FULL | FULL | None |
| TOTP/OTP | FULL | FULL | None |
| QR Code | FULL | FULL | None |
| Multi-step auth | FULL | DONE | None |
| Verification pipeline | FULL | DONE | None |
| Voice search | FULL | DONE | None |
| Step-up auth | FULL | DONE | None |
| Role management | FULL | DONE | None |
| Audit log admin | FULL | DONE | None |
| Developer portal | FULL | DONE | None |
| Widget demo | FULL | DONE | None |

---

## Completion Tracking

- [x] Phase 1 complete — 2026-04-04
- [x] Phase 2 complete (8/8) — 2026-04-04
- [ ] Phase 3 complete (3/7, tests + CSV + Gradle remaining)
- [ ] Phase 4 (maintenance)
- [x] Web-app backlog W20-W26 cleared (except W24 low-priority)
- [x] 2FA login properly working
- [x] 100% feature parity achieved
