# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.fivucsas.**$$serializer { *; }
-keepclassmembers class com.fivucsas.** { *** Companion; }
-keepclasseswithmembers class com.fivucsas.** { kotlinx.serialization.KSerializer serializer(...); }

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ML Kit
-keep class com.google.mlkit.** { *; }

# BouncyCastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Compose
-dontwarn androidx.compose.**

# FIDO2 / Credential Manager
-keep class com.google.android.gms.fido.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# CameraX
-keep class androidx.camera.** { *; }

# Coil
-dontwarn coil.**

# Firebase Cloud Messaging
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# SLF4J
-dontwarn org.slf4j.impl.StaticLoggerBinder
