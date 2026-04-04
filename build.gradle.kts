plugins {
    kotlin("multiplatform").version("2.1.20").apply(false)
    kotlin("android").version("2.1.20").apply(false)
    kotlin("plugin.compose").version("2.1.20").apply(false)
    id("com.android.application").version("8.7.3").apply(false)
    id("com.android.library").version("8.7.3").apply(false)
    kotlin("plugin.serialization").version("2.1.20").apply(false)
    id("org.jetbrains.compose").version("1.7.3").apply(false)
    // Firebase / Google Services plugin (applied in androidApp)
    id("com.google.gms.google-services").version("4.4.2").apply(false)
}

