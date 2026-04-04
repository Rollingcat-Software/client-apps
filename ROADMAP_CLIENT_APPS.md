# FIVUCSAS Client-Apps Completion Roadmap

**Created:** 2026-04-04
**Current Completion:** 60%
**Target:** 100%
**Total Estimated Effort:** ~150-160 hours

---

## Phase 1: Critical Build Fixes (BLOCKING)

| # | Task | Effort | Status |
|---|------|--------|--------|
| 1.1 | Fix Desktop DI import error (PlatformModule.desktop.kt:18) | 5 min | IN PROGRESS |
| 1.2 | Add RECORD_AUDIO permission to AndroidManifest.xml | 15 min | IN PROGRESS |
| 1.3 | Fix 16 deprecated Compose APIs (LinearProgressIndicator, ArrowBack, Divider) | 2-3 hrs | IN PROGRESS |
| 1.4 | Archive outdated 100_PERCENT_COMPLETE.md | 5 min | IN PROGRESS |

**Phase 1 Total:** ~3 hours

---

## Phase 2: Core Feature Completion

| # | Task | Effort | Priority | Status |
|---|------|--------|----------|--------|
| 2.1 | Multi-step auth flow UI (orchestrator + step progress indicator) | 8-10 hrs | HIGH | TODO |
| 2.2 | Verification pipeline screens (8 screens: doc scan, OCR results, face match, data verify, address proof, watchlist, age verify, video interview) | 12-15 hrs | HIGH | TODO |
| 2.3 | Voice search UI (1:N speaker identification screen with results) | 8-12 hrs | HIGH | TODO |
| 2.4 | Step-up authentication screen (challenge UI, multi-factor confirmation) | 6-8 hrs | HIGH | TODO |
| 2.5 | Role/permission management UI (admin CRUD for roles + permissions) | 8-10 hrs | MEDIUM | TODO |
| 2.6 | Audit log admin dashboard (full audit view, not just user activity) | 6-8 hrs | MEDIUM | TODO |
| 2.7 | iOS real implementations (AVFoundation camera, Keychain, LocalAuthentication) | 8-10 hrs | MEDIUM | TODO |
| 2.8 | Desktop WebAuthn (javax.smartcardio + Windows Hello via JNA) | 4-6 hrs | LOW | TODO |

**Phase 2 Total:** ~70 hours

---

## Phase 3: Polish & Production Ready

| # | Task | Effort | Priority | Status |
|---|------|--------|----------|--------|
| 3.1 | Developer portal screen (SDK docs, API key management, test credentials) | 10-15 hrs | MEDIUM | TODO |
| 3.2 | Widget demo screen (interactive preview, config builder, code snippets) | 6-8 hrs | MEDIUM | TODO |
| 3.3 | Test coverage expansion (target 60%+ from current ~18%) | 15-20 hrs | HIGH | TODO |
| 3.4 | Documentation updates (remove Flutter references from 5 .md files) | 4-6 hrs | HIGH | TODO |
| 3.5 | CSV export for analytics/audit logs | 4-6 hrs | LOW | TODO |
| 3.6 | E2E testing framework (Appium or similar) | 10-15 hrs | LOW | TODO |
| 3.7 | Gradle 10 compatibility fixes | 1-2 hrs | LOW | TODO |

**Phase 3 Total:** ~70-85 hours

---

## Phase 4: Maintenance (Post-Launch)

| # | Task | Effort | Status |
|---|------|--------|--------|
| 4.1 | Resource consolidation (duplicate AndroidManifest cleanup) | 1 hr | TODO |
| 4.2 | App icon assets for all densities | 1-2 hrs | TODO |
| 4.3 | ProGuard/R8 optimization for release builds | 2-3 hrs | TODO |

**Phase 4 Total:** ~5 hours

---

## Key Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Android screens | 35 | 43 (+8 verification) |
| Shared ViewModels | 25 | 33 (+8 new features) |
| API interfaces | 16 | 18 (+2 verification, widget) |
| Test files | 15 (~50 tests) | 65+ (~200 tests) |
| iOS completion | 30% | 80% |
| Desktop completion | 60% | 85% |
| Build status | FAILING | GREEN |
| Documentation | Outdated | Current |

---

## Feature Parity with Web-App

| Feature | Web-App | Client-Apps | Gap |
|---------|---------|-------------|-----|
| Face biometrics | FULL | FULL | None |
| Voice biometrics | FULL | FULL | None |
| NFC (TC Kimlik) | Web NFC only | FULL native | Client WINS |
| FIDO2/WebAuthn | FULL | FULL (just completed) | None |
| TOTP/OTP | FULL | FULL | None |
| QR Code | FULL | FULL | None |
| Multi-step auth | FULL | MINIMAL | Phase 2.1 |
| Verification pipeline | FULL (9 steps) | MISSING | Phase 2.2 |
| Voice search | FULL | API only | Phase 2.3 |
| Step-up auth | FULL | PARTIAL | Phase 2.4 |
| Role management | FULL | MISSING | Phase 2.5 |
| Audit log admin | FULL | PARTIAL | Phase 2.6 |
| Developer portal | FULL | MISSING | Phase 3.1 |
| Widget demo | FULL | MISSING | Phase 3.2 |
| Auth widget SDK | FULL | N/A | Not needed (native) |

---

## Completion Tracking

- [ ] Phase 1 complete (build fixes)
- [ ] Phase 2 complete (core features)
- [ ] Phase 3 complete (polish)
- [ ] Phase 4 complete (maintenance)
- [ ] 100% feature parity achieved
