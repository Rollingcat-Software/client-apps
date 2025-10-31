buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.5.11")
    }
}

plugins {
    kotlin("multiplatform").version("1.9.21").apply(false)
    kotlin("android").version("1.9.21").apply(false)
    id("com.android.application").version("8.2.2").apply(false)
    id("com.android.library").version("8.2.2").apply(false)
    kotlin("plugin.serialization").version("1.9.21").apply(false)
    id("org.jetbrains.compose").version("1.5.11").apply(false)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
