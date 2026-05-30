plugins {
    kotlin("android")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("com.android.application")
    id("com.google.gms.google-services")
}

// ──────────────────────────────────────────────────────────────────────────────
// Release signing config (env-var / gradle-property driven, NEVER hardcoded).
//
// Resolution order for each value:
//   1. OS environment variable   (used by GitHub Actions / CI)
//   2. Gradle property           (read from local.properties for dev machines)
//   3. Sensible default          (path + alias only — never a password)
//
// If keystorePassword resolves to null OR the keystore file does not exist
// the release build falls back to the debug signing config. This keeps
// unsigned CI builds (PRs, forks) working without exposing any secret.
//
// See docs/RELEASE.md for rotation + CI secret setup.
// ──────────────────────────────────────────────────────────────────────────────
val keystorePath: String = System.getenv("ANDROID_KEYSTORE_PATH")
    ?: (findProperty("android.keystore.path") as? String)
    ?: "${rootDir}/keystore/release.jks"
val keystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
    ?: (findProperty("android.keystore.password") as? String)
val releaseKeyAlias: String = System.getenv("ANDROID_KEY_ALIAS")
    ?: (findProperty("android.key.alias") as? String)
    ?: "fivucsas"
val releaseKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")
    ?: (findProperty("android.key.password") as? String)
    ?: keystorePassword

val hasReleaseSigning: Boolean = keystorePassword != null && file(keystorePath).exists()

android {
    namespace = "com.fivucsas.mobile.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fivucsas.mobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 8
        versionName = "5.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword!!
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
            // BouncyCastle OSGI manifest conflicts
            pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // Exclude BouncyCastle bundled by Android to avoid duplicate class conflicts
    configurations.all {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                // Fall back to debug signing so PR builds on forks still produce
                // an installable APK. Production release MUST set the env vars.
                logger.lifecycle(
                    "⚠ ANDROID_KEYSTORE_PASSWORD not set or keystore missing at " +
                            "$keystorePath — release APK will be debug-signed."
                )
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Allow JVM unit tests to stub android.* methods (Log, Base64) as no-ops
    // instead of throwing "Method not mocked" — enables pure-JVM ViewModel tests
    // without Robolectric.
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":shared"))

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // CameraX (1.4.1+ required for 16KB page size alignment on Android 15+)
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Accompanist (permissions)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.5")

    // ML Kit Text Recognition (latin script) — used by MrzAnalyzer for
    // passport / ID-card MRZ OCR.
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // FIDO2 / Credential Manager for WebAuthn hardware token support
    implementation("com.google.android.gms:play-services-fido:21.1.0")

    // AndroidX Credential Manager for WebAuthn (passkeys, platform & cross-platform authenticators)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // Koin for Android
    implementation("io.insert-koin:koin-android:4.0.2")
    implementation("io.insert-koin:koin-androidx-compose:4.0.2")

    // Firebase (Cloud Messaging for push notifications)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    // QR Code rendering (Compose Multiplatform, also works on Android)
    implementation("io.github.alexzhirkevich:qrose:1.0.1")

    // Authenticator vault serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // EncryptedSharedPreferences for TOTP vault
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // BouncyCastle for NFC SOD validation (e-Passport/eID)
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // ── JVM Unit Testing (src/test) ──
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // ── E2E / Instrumented Testing ──
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

