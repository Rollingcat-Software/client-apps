# FIVUCSAS Client-Apps Completion Roadmap

**Created:** 2026-04-04
**Last Updated:** 2026-04-04
**Current Completion:** 80%
**Target:** 100%
**APK Release:** v2.0.0 (https://github.com/Rollingcat-Software/client-apps/releases/tag/v2.0.0)

---

## Phase 1: Critical Build Fixes — COMPLETE

| # | Task | Effort | Status |
|---|------|--------|--------|
| 1.1 | Fix Desktop DI import error (PlatformModule.desktop.kt:18) | 5 min | DONE |
| 1.2 | Add RECORD_AUDIO permission to AndroidManifest.xml | 15 min | DONE |
| 1.3 | Fix 16 deprecated Compose APIs (LinearProgressIndicator, ArrowBack, Divider) | 2-3 hrs | DONE |
| 1.4 | Archive outdated 100_PERCENT_COMPLETE.md | 5 min | DONE |
| 1.5 | FIDO2/WebAuthn integration (Credential Manager, 19 files, 1,058 lines) | 6-8 hrs | DONE |
| 1.6 | APK v2.0.0 config (version, ProGuard, shrinkResources, strings.xml) | 1 hr | DONE |
| 1.7 | Launcher icons (all densities + adaptive) | 1 hr | DONE |
| 1.8 | Documentation rewrite (Flutter → KMP, 3 .md files) | 2 hrs | DONE |

---

## Phase 2: Core Feature Completion — 6/8 DONE

| # | Task | Effort | Priority | Status |
|---|------|--------|----------|--------|
| 2.1 | Multi-step auth flow UI (orchestrator + step progress + ViewModel) | 8-10 hrs | HIGH | DONE |
| 2.2 | Verification pipeline (dashboard + session detail + API + models) | 12-15 hrs | HIGH | DONE |
| 2.3 | Voice search UI (1:N speaker identification + waveform + results) | 8-12 hrs | HIGH | DONE |
| 2.4 | Step-up authentication screen (challenge UI, multi-factor) | 6-8 hrs | HIGH | DONE |
| 2.5 | Role/permission management UI (admin CRUD) | 8-10 hrs | MEDIUM | DONE |
| 2.6 | Audit log admin dashboard (full audit view + filtering) | 6-8 hrs | MEDIUM | DONE |
| 2.7 | iOS real implementations (AVFoundation, Keychain, LocalAuthentication) | 8-10 hrs | MEDIUM | TODO |
| 2.8 | Desktop WebAuthn (javax.smartcardio + Windows Hello via JNA) | 4-6 hrs | LOW | TODO |

---

## Phase 3: Polish & Production Ready

| # | Task | Effort | Priority | Status |
|---|------|--------|----------|--------|
| 3.1 | Developer portal screen (SDK docs, API key management) | 10-15 hrs | MEDIUM | TODO |
| 3.2 | Widget demo screen (preview, config builder, code snippets) | 6-8 hrs | MEDIUM | TODO |
| 3.3 | Test coverage expansion (target 60%+ from ~18%) | 15-20 hrs | HIGH | TODO |
| 3.4 | CSV export for analytics/audit logs | 4-6 hrs | LOW | TODO |
| 3.5 | E2E testing framework (Appium or similar) | 10-15 hrs | LOW | TODO |
| 3.6 | Gradle 10 compatibility fixes | 1-2 hrs | LOW | TODO |
| 3.7 | Release signing keystore creation | 1 hr | HIGH | TODO |

---

## Phase 4: Maintenance (Post-Launch)

| # | Task | Effort | Status |
|---|------|--------|--------|
| 4.1 | Resource consolidation (duplicate AndroidManifest cleanup) | 1 hr | TODO |
| 4.2 | Client-side ML migration (TFLite voice embeddings, on-device YOLO) | 10-15 hrs | TODO |
| 4.3 | Istanbul card YOLO training data (fix misclassification as ehliyet) | 4-6 hrs | TODO |

---

## Web-App Feedback Backlog (from Chrome Mobile Testing 2026-04-04)

These items were identified during manual testing and need implementation:

| # | Item | Priority | Status |
|---|------|----------|--------|
| W1 | Face enrollment slow on mobile (MediaPipe fails, uses timeout fallback) | LOW | KNOWN LIMITATION |
| W2 | Face search fixed (matches vs results field mismatch) | HIGH | FIXED |
| W3 | NFC auth error in Biometric Tools (missing Bearer token) | HIGH | FIXED |
| W4 | NFC "Whose Card?" button for all users (was admin-only) | MEDIUM | FIXED |
| W5 | Card detection crash (frozenset.pop) | HIGH | FIXED |
| W6 | Istanbul card misclassified as ehliyet (YOLO training data) | LOW | KNOWN LIMITATION |
| W7 | Forgot password sends code only, no proper reset link flow | MEDIUM | TODO |
| W8 | Phone number not saved from SMS OTP enrollment | MEDIUM | TODO |
| W9 | Non-admin dashboard shows "admin only" message — needs personal summary | HIGH | TODO |
| W10 | Login page lacks multi-method auth (only email+password visible) | HIGH | TODO |
| W11 | Notifications placeholder — needs real WebSocket/FCM push | MEDIUM | TODO |
| W12 | User activity/summary page (enrolled methods, login history, sessions) | HIGH | TODO |
| W13 | OAuth widget testing (widget-demo + developer-portal exist, need demo tenant flow) | MEDIUM | TODO |
| W14 | GitHub repos organization/cleanup | LOW | TODO |
| W15 | User audit log view (login/logout/enroll/delete history) | HIGH | TODO |
| W16 | Cross-device session awareness (web/mobile/desktop connections) | MEDIUM | TODO |
| W17 | Voice enrollment speech verification (STT matching passphrase) | LOW | FUTURE |
| W18 | BYOD (Bring Your Own Database) tenant feature | LOW | FUTURE |
| W19 | BaaS (Biometrics-as-a-Service) per-feature API rental model | LOW | FUTURE |

---

## Feature Parity (Updated)

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
| Developer portal | FULL | MISSING | Phase 3.1 |
| Widget demo | FULL | MISSING | Phase 3.2 |

---

## Key Metrics (Updated)

| Metric | Start | Current | Target |
|--------|-------|---------|--------|
| Android screens | 35 | 40+ | 43 |
| Shared ViewModels | 25 | 33+ | 35 |
| API interfaces | 16 | 21+ | 22 |
| Test files | 15 (~50 tests) | 15 (~50 tests) | 65+ (~200 tests) |
| iOS completion | 30% | 30% | 80% |
| Desktop completion | 60% | 65% | 85% |
| Build status | FAILING | GREEN | GREEN |
| CI/CD | Billing blocked | Self-hosted runner | All GREEN |
| Documentation | Outdated (Flutter) | Current (KMP) | Current |
| APK version | 1.0.0-MVP | 2.0.0 | 2.x |

---

## Completion Tracking

- [x] Phase 1 complete (build fixes + FIDO2 + APK config + docs) — 2026-04-04
- [ ] Phase 2 complete (6/8 done, iOS + Desktop WebAuthn remaining)
- [ ] Phase 3 complete (polish, tests, dev portal)
- [ ] Phase 4 complete (maintenance, client-side ML)
- [ ] Web-app feedback backlog cleared
- [ ] 100% feature parity achieved
