import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

group = "com.fivucsas.desktop"
// Tracks the client-apps release line (Android versionName) so the produced
// .deb / .msi installer version matches the published release, not a stale 1.0.0.
version = "5.3.0"

kotlin {
    jvmToolchain(21)

    sourceSets {
        val main by getting {
            kotlin.srcDirs("src/desktopMain/kotlin")
        }
        val test by getting {
            kotlin.srcDirs("src/desktopTest/kotlin")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.uiTooling)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.1")

    // Ktor client (hosted-first OAuth loopback — RFC 8252). The CIO engine + content
    // negotiation are already resolved transitively via :shared, but we list them
    // explicitly here so the symbols are visible from the desktopApp source set.
    implementation("io.ktor:ktor-client-core:3.1.1")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Koin for Desktop
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("io.insert-koin:koin-compose:4.0.2")

    // Webcam capture - JavaCV
    implementation("org.bytedeco:javacv-platform:1.5.10")
    implementation("com.google.zxing:core:3.5.3")

    // JNA for Windows DPAPI (Crypt32) secure token storage.
    // jna-platform transitively depends on jna, so both classes load.
    implementation("net.java.dev.jna:jna-platform:5.14.0")

    // Test dependencies for SecureTokenStorage suite and future desktop-only tests.
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "com.fivucsas.desktop.MainKt"

        nativeDistributions {
            // NOTE: .dmg is intentionally omitted — no macOS signer available.
            // Windows .msi builds on Linux via jpackage+WiX only if wine is installed;
            // the CI workflow builds it on a Windows runner instead.
            targetFormats(TargetFormat.Deb, TargetFormat.Msi)

            packageName = "FIVUCSAS"
            packageVersion = project.version.toString()
            vendor = "Rollingcat Software"
            description = "FIVUCSAS identity verification desktop client"
            copyright = "© 2026 Rollingcat Software"

            // Shared LICENSE at parent repo root
            val licenseCandidate = rootProject.file("LICENSE")
            if (licenseCandidate.exists()) {
                licenseFile.set(licenseCandidate)
            } else {
                // fallback to the FIVUCSAS umbrella repo LICENSE if we're checked out as a submodule
                val umbrellaLicense = rootProject.file("../LICENSE")
                if (umbrellaLicense.exists()) licenseFile.set(umbrellaLicense)
            }

            // JVM modules required for JDBC, JNDI (Koin), and EC crypto (JWT/TLS).
            // Add more here as jpackage reports missing modules.
            modules("java.sql", "java.naming", "jdk.crypto.ec")

            linux {
                iconFile.set(project.file("icons/fivucsas.png"))
                packageName = "fivucsas"
                menuGroup = "Internet"
                appCategory = "Network"
                debMaintainer = "support@fivucsas.com"
            }

            windows {
                iconFile.set(project.file("icons/fivucsas.ico"))
                // MUST be stable across versions — regenerating this breaks upgrade paths on existing installs.
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                menuGroup = "FIVUCSAS"
                shortcut = true
                dirChooser = true
            }

            macOS {
                bundleID = "com.fivucsas.desktop"
                iconFile.set(project.file("icons/fivucsas.png"))
            }
        }
    }
}
