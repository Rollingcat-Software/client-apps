# FIVUCSAS Desktop Installer Signing

This document covers how release builds of the desktop client are signed and
how users can verify downloads.

## TL;DR

| Platform | Signing | User verifies with |
|----------|---------|--------------------|
| Linux (`.deb`) | **GPG via `dpkg-sig`** (manual, pre-upload) | `dpkg-sig --verify` or `gpg --verify` |
| Windows (`.msi`) | **Unsigned** (Authenticode deferred, see below) | SHA256 checksum |

The public verification key for `.deb` signatures is published at:

    https://fivucsas.com/pgp.asc

## Release identity

- **Name:** `FIVUCSAS Release`
- **Email:** `release@fivucsas.com`
- **Key type:** RSA 4096-bit, signing-only, `[SC]` with `[S]` subkey
- **Fingerprint:** `3ED5 3CCD F292 AF22 E71F AB85 CCDB BE54 7D74 CA7A`
- **Generated:** 2026-04-18 on the Hetzner build server (`/home/deploy/.gnupg`)
- **Expires:** 3 years (rotation reminder: early 2029)
- **Passphrase:** none, so it can be used from CI without an interactive agent

The secret material is stored **only** on the deploy server's GPG keyring.
It is not mirrored to CI secrets yet — signing is still a manual step.

## Signing a .deb locally (current workflow)

Signing is **not yet enabled in CI**. After the Linux installer is built
(either locally or by the `Desktop Installers` GitHub Actions workflow and
downloaded as an artifact), sign it manually on the deploy server before
uploading to `/releases/`:

```bash
# One-time: install dpkg-sig
sudo apt install -y dpkg-sig

# Point dpkg-sig at our release key
dpkg-sig --sign builder \
  -k release@fivucsas.com \
  fivucsas_1.0.0_amd64.deb

# Verify locally before publishing
dpkg-sig --verify fivucsas_1.0.0_amd64.deb
```

`dpkg-sig --sign builder` embeds a detached GPG signature inside the `.deb`
control archive under the role name `builder`. The signature covers the
package contents — users who re-pack the archive break the signature.

### Upload path

```bash
# from the deploy server
scp -P 65002 fivucsas_1.0.0_amd64.deb \
    u349700627@46.202.158.52:~/domains/fivucsas.com/public_html/releases/
scp -P 65002 fivucsas_1.0.0_amd64.deb.sha256 \
    u349700627@46.202.158.52:~/domains/fivucsas.com/public_html/releases/
```

## How a user verifies a .deb

```bash
# 1. Fetch the pubkey once
curl -sL https://fivucsas.com/pgp.asc | gpg --import

# 2. Verify the signature baked into the .deb
dpkg-sig --verify fivucsas_1.0.0_amd64.deb

# 3. (Optional) Check the SHA256 we publish next to the file
sha256sum -c fivucsas_1.0.0_amd64.deb.sha256
```

If the signature is valid, `dpkg-sig` prints:

    Processing fivucsas_1.0.0_amd64.deb...
    GOODSIG _gpgbuilder <key-id>

and the `sha256sum -c` call prints `OK`.

## Windows (.msi) — unsigned, for now

The current build does **not** apply an Authenticode signature to the MSI.
A code-signing certificate from a Microsoft-trusted CA costs roughly
USD 200–480/year (DigiCert / Sectigo / SSL.com), and an EV cert is needed to
avoid the initial SmartScreen reputation warning entirely.

Until that's provisioned, users will see a Microsoft Defender SmartScreen
prompt the first time they run the installer. The download page documents the
"More info → Run anyway" workaround and exposes the MSI's SHA256 so users can
verify file integrity out of band.

When Authenticode signing is later enabled, sign on the Windows CI runner
with `signtool.exe` immediately after `packageMsi`:

```powershell
signtool sign /fd SHA256 /tr http://timestamp.digicert.com /td SHA256 `
  /f codesign.pfx /p $env:CODESIGN_PW `
  desktopApp/build/compose/binaries/main/msi/*.msi
```

## Enabling GPG signing in CI (future)

When we're ready to automate this:

1. Export the private key into a CI secret:

   ```bash
   gpg --armor --export-secret-keys release@fivucsas.com | base64 -w0
   ```

   Store the output as `GPG_PRIVATE_KEY_B64` in the `client-apps` repo secrets.

2. Add a step between "Package .deb" and "Compute SHA256" in
   `.github/workflows/desktop-installers.yml`:

   ```yaml
   - name: Import release key
     run: |
       echo "$GPG_KEY" | base64 -d | gpg --batch --import
     env:
       GPG_KEY: ${{ secrets.GPG_PRIVATE_KEY_B64 }}

   - name: Sign .deb
     run: |
       sudo apt-get update && sudo apt-get install -y dpkg-sig
       DEB=$(find desktopApp/build/compose/binaries/main/deb -name "*.deb" | head -1)
       dpkg-sig --sign builder -k release@fivucsas.com "$DEB"
       dpkg-sig --verify "$DEB"
   ```

3. Update this doc to say "signing is automatic" and drop the manual steps.

Until then, the key lives only on the deploy server and every signed `.deb`
is signed by a human running `dpkg-sig --sign builder` before `scp`.
