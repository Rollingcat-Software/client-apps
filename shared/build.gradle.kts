plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(21)

    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                // Ktor (Networking)
                implementation("io.ktor:ktor-client-core:3.1.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
                implementation("io.ktor:ktor-client-logging:3.1.1")

                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

                // Multiplatform Settings (SharedPreferences)
                implementation("com.russhwolf:multiplatform-settings:1.3.0")

                // Koin for Dependency Injection
                implementation("io.insert-koin:koin-core:4.0.2")
                implementation("io.insert-koin:koin-compose:4.0.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                // Coroutines Test
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")

                // Turbine (for Flow testing)
                implementation("app.cash.turbine:turbine:1.2.0")

                // MockK (for mocking) - Note: Common MockK doesn't exist, we'll use expect/actual pattern
                // For now, we'll create our own test doubles
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.1.1")

                // ViewModel - Android only
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

                // Android specific
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
                implementation("androidx.activity:activity-compose:1.9.3")

                // CameraX (used by AndroidCameraService + preview)
                implementation("androidx.camera:camera-core:1.4.1")
                implementation("androidx.camera:camera-camera2:1.4.1")
                implementation("androidx.camera:camera-lifecycle:1.4.1")
                implementation("androidx.camera:camera-view:1.4.1")

                // Accompanist (permissions helper)
                implementation("com.google.accompanist:accompanist-permissions:0.32.0")

                // ML Kit Face Detection
                implementation("com.google.mlkit:face-detection:16.1.5")


                // Koin for Android
                implementation("io.insert-koin:koin-android:4.0.2")

                // BiometricPrompt support
                implementation("androidx.biometric:biometric:1.1.0")

                // AndroidX Credential Manager for WebAuthn/FIDO2 (passkeys)
                implementation("androidx.credentials:credentials:1.5.0")
                implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:3.1.1")
                implementation(compose.desktop.currentOs)
                // Webcam capture - JavaCV (used by DesktopCameraServiceImpl)
                implementation("org.bytedeco:javacv-platform:1.5.10")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.1")
            }
        }
    }
}

android {
    namespace = "com.fivucsas.mobile.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

