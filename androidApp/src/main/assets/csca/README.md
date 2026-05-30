# Bundled CSCA roots (ICAO-PKD) — operator drop-in

This directory is the **populate hook** for client-side passive authentication.
The code is complete; only the certificate files are missing (operator action).

## Layout

```
assets/csca/<COUNTRY>/<anything>.{cer,crt,der,pem,p7c,p7b}
```

- `<COUNTRY>` is an ISO 3166-1 **alpha-3** code, e.g. `TUR` for Turkey.
- Each file may hold one certificate or a PKCS#7 chain (chains are expanded).
- `CscaCertificateStore.loadBundledRoots(context)` (called at app startup in
  `FIVUCSASApplication.onCreate`) auto-discovers and loads everything here.

## To enable Turkish eID passive auth

Drop the official Turkey CSCA root(s) from the ICAO PKD into:

```
assets/csca/TUR/csca-turkey.cer
```

## Important

- The client-side DS→CSCA chain check is **advisory only**. The authoritative,
  fail-closed verdict comes from the server (`POST /api/v1/nfc/verify-authenticity`),
  which has its own trust store and returns `reasonCode=NO_TRUST_STORE` until
  the operator loads CSCA roots into the **bio container** as well.
- Obtain CSCA certificates only from official sources (ICAO PKD). Some countries
  do not publish their CSCA certificates publicly.
- Do **not** commit private keys here — these are public root certificates only.
