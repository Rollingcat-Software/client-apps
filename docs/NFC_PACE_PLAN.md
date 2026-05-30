# NFC PACE — implementation plan & status

**Status: EF.CardAccess parse + vector-tested key derivation landed; on-card
GM handshake + secure messaging deferred (needs a physical PACE card).**

PACE (Password Authenticated Connection Establishment, BSI TR-03110 /
ICAO 9303-11) lets the app open an encrypted channel to PACE-only documents,
broadening reading beyond BAC. This document records what shipped, what is
deferred, and exactly what unblocks the rest.

## Shipped (this PR)

- `data/nfc/pace/CardAccessParser.kt` — parses `EF.CardAccess`
  (`SET OF SecurityInfo`) and extracts the advertised `PACEInfo` entries
  (protocol OID + version + standardized-domain-parameter id). Pure
  BouncyCastle ASN.1; fully unit-tested (`CardAccessParserTest`) with no card.
- `data/nfc/pace/PaceKeyDerivation.kt` — the TR-03110 / ICAO 9303 KDF
  primitive `KDF(K, c) = H(K ‖ c)` (counters 1=enc, 2=mac, 3=password;
  SHA-1→AES-128, SHA-256→AES-192/256). **Vector-tested** (`PaceKeyDerivationTest`)
  byte-exact against the published ICAO 9303 key-derivation worked example
  (Kseed `561754EE…` → Kenc `EB0F20E3…` / Kmac `6DC37B57…`, independently
  re-verified with `sha1sum`). This is the same primitive used for the PACE
  nonce-decryption key `K_π`, the session enc/mac keys, and BAC. No card needed.
- `data/nfc/pace/PaceAuthenticator.kt` — protocol **selection** (prefers
  Generic Mapping + AES), `derivePasswordKey(mrzSeed)` (π = SHA-1(seed) →
  `K_π = KDF(π,3)`, vector-tested), and the integration **seam**: `run(...)`
  returns `PaceResult.NotImplemented` (→ BAC fallback) until the on-card
  exchange lands. The full GM + AES-SM outline is in the KDoc.

## Deferred (on-card handshake + secure messaging)

The remaining leg — MSE:Set AT + the chained GENERAL AUTHENTICATE (encrypted-
nonce fetch, GM EC point mapping, mapped-key exchange, token verify) and the
AES-CMAC secure-messaging channel — is **not** wired to the card. Key
derivation is done and proven; the GM EC point arithmetic + the live APDU
exchange still need a real PACE document to validate end to end. Shipping an
un-validated on-card handshake would be worse than a loud BAC fallback, so it
waits on:

1. **A PACE-capable physical test document** (a Turkish eID / passport that
   actually rejects BAC and requires PACE). Without one we cannot prove the
   handshake end to end.
2. **BSI TR-03110 / ICAO 9303-11 Appendix G GM worked-example vectors** (the
   standard's Annex
   gives intermediate values for the nonce, mapped points, and tokens) to
   unit-test each GM step against, card-free.

## Integration plan (when unblocked)

1. Read `EF.CardAccess` (public) before BAC; `CardAccessParser.parse` →
   `PaceAuthenticator.selectProtocol`.
2. Implement `run(...)`: MSE:Set AT → GENERAL AUTHENTICATE (encrypted nonce →
   GM mapping → mapped ephemeral key exchange → token verify) → open AES
   secure messaging.
3. Return the resulting `SecureMessaging`; the existing DG-read path
   (`readFileSecure`) is unchanged. In the readers, try PACE first and fall
   back to BAC on `NotImplemented` / `Failure`.
4. Set `PassportData.paceSuccessful = true` on the PACE path.

## Operator needs (for the user)

- PACE-capable physical test card(s).
- BSI TR-03110 reference vectors (or a known-good captured PACE trace).
