package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.platform.DefaultNetworkMonitor
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.INetworkMonitor
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.platform.ILogger
import com.fivucsas.shared.platform.FingerprintAuthenticator
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.IosCameraService
import com.fivucsas.shared.platform.IosSecureStorage
import com.fivucsas.shared.platform.IosLogger
import com.fivucsas.shared.platform.IosTokenStorage
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.NoOpNfcService
import com.fivucsas.shared.platform.NoOpPushNotificationService
import com.fivucsas.shared.platform.providePlatformFingerprintAuthenticator
import org.koin.dsl.module

/**
 * iOS Platform Module
 *
 * Provides iOS-specific implementations for platform abstractions.
 * Uses Koin for dependency injection.
 *
 * Provides:
 * - IosCameraService: AVFoundation-based camera implementation
 * - IosSecureStorage: iOS Keychain-based secure storage
 * - IosLogger: NSLog-based logging implementation
 *
 * Design Patterns:
 * - Dependency Injection Pattern: Uses Koin for IoC
 * - Factory Pattern: Creates platform-specific instances
 * - Singleton Pattern: Single instances for stateful services
 *
 * Architecture:
 * - Hexagonal Architecture: Platform implementations as adapters
 * - SOLID Principles: Dependency inversion through interfaces
 */
actual val platformModule = module {
    // Camera Service - Singleton for state management
    single<ICameraService> { IosCameraService() }

    // Secure Storage - Singleton for consistent data access
    single<ISecureStorage> { IosSecureStorage() }

    // Token Storage - Keychain-backed via IosSecureStorage adapter
    single<TokenStorage> { IosTokenStorage(get()) }

    // Logger - Singleton for centralized logging
    single<ILogger> { IosLogger() }

    // Fingerprint Authenticator - iOS stub for now
    single<FingerprintAuthenticator> { providePlatformFingerprintAuthenticator() }

    // Push Notification Service - stub until APNs is configured
    single<IPushNotificationService> { NoOpPushNotificationService() }

    // Network Monitor — default always-online on iOS for now
    single<INetworkMonitor> { DefaultNetworkMonitor() }

    // NFC Service — no-op on iOS until Core NFC is integrated
    single<INfcService> { NoOpNfcService() }
}
