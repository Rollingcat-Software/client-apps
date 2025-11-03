import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.fivucsas.desktop"
version = "1.0.0"

kotlin {
    jvmToolchain(21)

    sourceSets {
        val main by getting {
            kotlin.srcDirs("src/desktopMain/kotlin")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // Koin for Desktop
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-compose:1.1.0")
}

compose.desktop {
    application {
        mainClass = "com.fivucsas.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "FIVUCSAS"
            packageVersion = "1.0.0"

            description = "FIVUCSAS Face and Identity Verification System"
            vendor = "Marmara University"

            windows {
                menuGroup = "FIVUCSAS"
                upgradeUuid = "fivucsas-desktop-app"
            }

            macOS {
                bundleID = "com.fivucsas.desktop"
            }

            linux {
                packageName = "fivucsas"
            }
        }
    }
}
