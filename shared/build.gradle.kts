plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }

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
                implementation("io.ktor:ktor-client-core:2.3.5")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
                implementation("io.ktor:ktor-client-logging:2.3.5")

                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

                // Multiplatform Settings (SharedPreferences)
                implementation("com.russhwolf:multiplatform-settings:1.1.0")

                // Koin for Dependency Injection
                implementation("io.insert-koin:koin-core:3.5.0")
                implementation("io.insert-koin:koin-compose:1.1.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                // Coroutines Test
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

                // Turbine (for Flow testing)
                implementation("app.cash.turbine:turbine:1.0.0")

                // MockK (for mocking) - Note: Common MockK doesn't exist, we'll use expect/actual pattern
                // For now, we'll create our own test doubles
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.5")

                // ViewModel - Android only
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

                // Android specific
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
                implementation("androidx.activity:activity-compose:1.8.1")

                // Koin for Android
                implementation("io.insert-koin:koin-android:3.5.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.3.5")
                implementation(compose.desktop.currentOs)
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
                implementation("io.ktor:ktor-client-darwin:2.3.5")
            }
        }
    }
}

android {
    namespace = "com.fivucsas.mobile.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
