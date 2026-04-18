# client-apps TODO

Short list of tracked follow-ups. See also `CHANGELOG.md` and `docs/plans/`.

## Authenticator (v5.1.0 — deferred work)

- [ ] QR-code scanner for `otpauth://` URIs (CameraX + ML Kit barcode scanner
      already present as a dependency; wiring and a dedicated scan screen are
      the remaining work). Bottom-sheet entry currently falls back to manual
      entry and surfaces a Toast pointing users to manual entry. Track with
      `docs/plans/CLIENT_APPS_PARITY.md` once that doc lands.
- [ ] iOS and Desktop `hmacSha1`/`hmacSha256`/`hmacSha512` actuals. Current
      `iosMain` stubs throw `TODO()`. Android + Desktop actuals use
      `javax.crypto.Mac` and are fully functional.
- [ ] Compose UI test for `AuthenticatorScreen` (account renders, code
      updates on tick, tap copies to clipboard). Unit tests for the core
      engine and otpauth parser land in this commit; Compose tests require
      a running AVD in CI which is not wired in this environment.
- [ ] Biometric gate on reveal / pull-to-refresh (deliberately omitted in
      5.1.0; will be reviewed with the authenticator in day-to-day use).
