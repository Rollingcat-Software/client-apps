plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
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
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.5")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

                // Android specific
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
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
}
