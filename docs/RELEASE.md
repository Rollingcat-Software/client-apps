# Android Release Signing & Keystore Management

This document describes how the FIVUCSAS Android app is signed, how CI builds
signed APKs/AABs, and how to rotate the upload keystore safely.

The signing config is defined in
[`androidApp/build.gradle.kts`](../androidApp/build.gradle.kts). It reads all
secrets from environment variables (CI) or Gradle properties (local dev) — **no
passwords are ever committed**. If no password is found and the keystore file
does not exist, release builds fall back to debug signing so that PR builds on
forks still produce an installable APK.

---

## Required GitHub Secrets

Before the next signed release can be produced, a repository maintainer must
create the following secrets in **Settings → Secrets and variables → Actions**
for the `fivucsas/client-apps` repository:

| Secret name                 | Value                                                                            | Used by                                  |
|-----------------------------|----------------------------------------------------------------------------------|------------------------------------------|
| `ANDROID_KEYSTORE_BASE64`   | Base64 encoding of `keystore/release.jks`                                        | `.github/workflows/android-build.yml`    |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore store password (the current rotated password — never the old leaked one) | `.github/workflows/android-build.yml`    |
| `ANDROID_KEY_ALIAS`         | Key alias inside the keystore (currently `fivucsas`)                             | `.github/workflows/android-build.yml`    |
| `ANDROID_KEY_PASSWORD`      | Private-key password (often same as store password, but can differ)              | `.github/workflows/android-build.yml`    |

If any of these are missing and a release build is triggered, the workflow will
fail fast with a clear error message.

### Generate the base64 blob

```bash
# Linux (what the Hetzner runner uses)
base64 -w0 keystore/release.jks > release.jks.b64

# macOS
base64 -i keystore/release.jks -o release.jks.b64
```

Open `release.jks.b64`, copy the entire single-line contents, paste it into
GitHub as `ANDROID_KEYSTORE_BASE64`. Delete `release.jks.b64` immediately
afterwards.

---

## Rotate the keystore password (single-session procedure)

Run these two commands locally against `keystore/release.jks`, enter the old
password once, the new one twice each:

```bash
keytool -storepasswd -keystore keystore/release.jks
keytool -keypasswd -alias fivucsas -keystore keystore/release.jks
```

Then, **immediately** (same session, same minute):

1. Update your `local.properties` (see [Local release build](#local-release-build)).
2. Update the `ANDROID_KEYSTORE_PASSWORD` and `ANDROID_KEY_PASSWORD` GitHub
   secrets with the new value.
3. Do **not** change `ANDROID_KEYSTORE_BASE64` — `keytool -storepasswd` does not
   alter the key material, only the envelope — but re-encode and re-upload it
   anyway so the stored blob matches the on-disk file:
   ```bash
   base64 -w0 keystore/release.jks > release.jks.b64
   # copy into ANDROID_KEYSTORE_BASE64, then:
   rm release.jks.b64
   ```
4. Record the rotation in `CHANGELOG.md` (date + "rotated Android upload
   keystore password" — **do not** paste the password).

The whole rotation is four commands + one secret paste; no code change is
required because `build.gradle.kts` resolves everything at build time.

---

## Local release build

For a local signed build on a developer machine, add the following to
`local.properties` (already `.gitignored`):

```
android.keystore.path=/absolute/path/to/keystore/release.jks
android.keystore.password=<current-store-password>
android.key.alias=fivucsas
android.key.password=<current-key-password>
```

Then:

```bash
./gradlew :androidApp:assembleRelease       # signed release APK
./gradlew :androidApp:bundleRelease         # signed AAB for Play Store upload
```

If `android.keystore.password` is omitted, the release build will be
debug-signed (a warning is logged). This is safe and intentional: it lets new
contributors build the app without needing the production keystore.

Alternatively, export env vars for a single build:

```bash
export ANDROID_KEYSTORE_PATH="$PWD/keystore/release.jks"
export ANDROID_KEYSTORE_PASSWORD="..."
export ANDROID_KEY_ALIAS="fivucsas"
export ANDROID_KEY_PASSWORD="..."
./gradlew :androidApp:assembleRelease
unset ANDROID_KEYSTORE_PASSWORD ANDROID_KEY_PASSWORD
```

---

## Rotation cadence

- **Every 6 months**, or immediately on any of the following events:
  - Known or suspected compromise of the keystore file or password.
  - A developer with historical access to the keystore leaves the team.
  - The keystore was ever pasted into chat/email/screenshot/anything cloud.
- Log every rotation in `CHANGELOG.md` (date + reason category only, never the
  password).

---

## Emergency revocation — what to do if the keystore leaks

The Android upload keystore is **not** a revocable credential in the classic
sense: Google Play Store pins the app to the signing key fingerprint forever.
However, since 2017 Play App Signing lets you rotate the **upload key**
independently of the app signing key. Procedure:

1. **Stop the bleed.** Immediately rotate the store + key password
   (`keytool -storepasswd`, `keytool -keypasswd`) so a stolen copy of the
   `.jks` file is useless without the new password. Update the GitHub secrets
   and `local.properties` in the same session.

2. **Generate a new upload key.**
   ```bash
   keytool -genkeypair -v \
     -keystore keystore/release-new.jks \
     -keyalg RSA -keysize 4096 -validity 10000 \
     -alias fivucsas
   ```
   Choose a strong, fresh password. Store it in a password manager.

3. **Request upload-key reset in Play Console.**
   Go to **Play Console → <app> → Release → Setup → App integrity → Upload
   key certificate → Request upload key reset**. Attach the new
   `upload_certificate.pem` extracted with:
   ```bash
   keytool -export -rfc \
     -keystore keystore/release-new.jks \
     -alias fivucsas \
     -file upload_certificate.pem
   ```
   Google takes 1–2 business days to approve.

4. **Publish the new upload key.** Replace `keystore/release.jks` with
   `release-new.jks`, re-encode to base64, and update all three GitHub secrets
   (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`,
   `ANDROID_KEY_PASSWORD` — `ANDROID_KEY_ALIAS` stays `fivucsas`).

5. **Audit.** Review recent signed releases; invalidate any Play Store release
   track that was built during the suspected compromise window.

6. **Document.** Add a `SECURITY.md` entry and a CHANGELOG row with date,
   reason category (do not include any password or hash), and the Play Console
   ticket ID.

Because Play App Signing holds the real signing key, end users are unaffected
by an upload key rotation — they continue receiving updates signed by Google's
app-signing key as long as Play accepts our new upload certificate.

---

## What changed on 2026-04-18

- Removed the hardcoded (now-revoked, leaked) signing password from `build.gradle.kts`.
- Switched signing config to env-var / Gradle-property resolution.
- Release builds now fall back to debug signing when no password is set
  (keeps PR + fork CI green).
- Added base64-keystore decode step to `android-build.yml`, gated on the
  manual `workflow_dispatch(build_type=release)` trigger only.
- This document.
