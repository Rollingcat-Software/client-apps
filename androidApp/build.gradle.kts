plugins {
    kotlin("android")
    kotlin("plugin.compose")
    id("com.android.application")
}

android {
    namespace = "com.fivucsas.mobile.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fivucsas.mobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/release.jks")
            storePassword = "fivucsas2026"
            keyAlias = "fivucsas"
            keyPassword = "fivucsas2026"
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
            signingConfig = signingConfigs.getByName("release")
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

    // FIDO2 / Credential Manager for WebAuthn hardware token support
    implementation("com.google.android.gms:play-services-fido:21.1.0")

    // AndroidX Credential Manager for WebAuthn (passkeys, platform & cross-platform authenticators)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // Koin for Android
    implementation("io.insert-koin:koin-android:4.0.2")
    implementation("io.insert-koin:koin-androidx-compose:4.0.2")

    // BouncyCastle for NFC SOD validation (e-Passport/eID)
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // ── E2E / Instrumented Testing ──
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

