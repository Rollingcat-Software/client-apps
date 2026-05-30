# NFC PACE — implementation plan & status

**Status: scaffold landed; cryptographic core deferred (operator-blocked).**

PACE (Password Authenticated Connection Establishment, BSI TR-03110 /
ICAO 9303-11) lets the app open an encrypted channel to PACE-only documents,
broadening reading beyond BAC. This document records what shipped, what is
deferred, and exactly what unblocks the rest.

## Shipped (this PR)

- `data/nfc/pace/CardAccessParser.kt` — parses `EF.CardAccess`
  (`SET OF SecurityInfo`) and extracts the advertised `PACEInfo` entries
  (protocol OID + version + standardized-domain-parameter id). Pure
  BouncyCastle ASN.1; fully unit-tested (`CardAccessParserTest`) with no card.
- `data/nfc/pace/PaceAuthenticator.kt` — protocol **selection** (prefers
  Generic Mapping + AES) and the integration **seam**: `run(...)` returns
  `PaceResult.NotImplemented` (or `NoPaceAdvertised`) so the readers cleanly
  fall back to BAC. The full GM + AES-SM protocol outline is in the KDoc.

## Deferred (cryptographic core)

The GM ECDH key agreement + AES-CMAC secure-messaging derivation are **not**
implemented. Shipping un-vetted ECC/CMAC crypto that silently derives a wrong
session key would be worse than a loud BAC fallback, so it waits on:

1. **A PACE-capable physical test document** (a Turkish eID / passport that
   actually rejects BAC and requires PACE). Without one we cannot prove the
   handshake end to end.
2. **BSI TR-03110 worked-example reference vectors** (the standard's Annex
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
