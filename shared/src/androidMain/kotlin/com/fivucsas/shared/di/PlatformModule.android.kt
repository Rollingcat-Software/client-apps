package com.fivucsas.shared.di

import com.fivucsas.mobile.android.data.nfc.AndroidNfcService
import com.fivucsas.shared.platform.AndroidCameraService
import com.fivucsas.shared.platform.FingerprintAuthenticator
import com.fivucsas.shared.platform.AndroidTokenStorage
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.providePlatformFingerprintAuthenticator
import com.fivucsas.shared.data.local.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
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

    // NFC Service
    single<INfcService> {
        AndroidNfcService(androidContext())
    }
}
