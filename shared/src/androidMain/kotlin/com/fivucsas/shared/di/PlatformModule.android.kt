package com.fivucsas.shared.di

import com.fivucsas.shared.platform.AndroidCameraService
import com.fivucsas.shared.platform.FingerprintAuthenticator
import com.fivucsas.shared.platform.AndroidTokenStorage
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.NoOpNfcService
import com.fivucsas.shared.platform.NoOpPushNotificationService
import com.fivucsas.shared.platform.providePlatformFingerprintAuthenticator
import com.fivucsas.shared.data.local.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android Platform Module
 *
 * Provides Android-specific implementations for platform abstractions.
 * Uses Koin for dependency injection with Android-specific bindings.
 *
 * Provides:
 * - AndroidCameraService: CameraX-based camera implementation
 * - AndroidTokenStorage: Encrypted SharedPreferences for secure token storage
 *
 * Note: NFC service is registered as a no-op default here.
 * The androidApp module overrides this with AndroidNfcService
 * (which lives in the androidApp module and can't be referenced from shared).
 *
 * Design Patterns:
 * - Dependency Injection Pattern: Uses Koin for IoC
 * - Factory Pattern: Creates platform-specific instances
 */
actual val platformModule = module {
    // Camera Service
    // Single instance that requires Android Context and LifecycleOwner
    single<ICameraService> {
        AndroidCameraService(
            context = androidContext(),
            lifecycleOwner = get() // LifecycleOwner must be provided by the app
        )
    }

    // Token Storage
    // Secure storage using EncryptedSharedPreferences
    single<TokenStorage> {
        AndroidTokenStorage(androidContext())
    }

    single<FingerprintAuthenticator> { providePlatformFingerprintAuthenticator() }

    // NFC Service — default no-op; overridden in androidApp with AndroidNfcService
    single<INfcService> {
        NoOpNfcService()
    }

    // Push Notification Service — default no-op; overridden in androidApp when Firebase is configured
    single<IPushNotificationService> {
        NoOpPushNotificationService()
    }
}
