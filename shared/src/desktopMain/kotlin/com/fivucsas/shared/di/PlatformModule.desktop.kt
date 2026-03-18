package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.platform.DefaultNetworkMonitor
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.DesktopCameraServiceImpl
import com.fivucsas.shared.platform.DesktopTokenStorage
import com.fivucsas.shared.platform.FingerprintAuthenticator
import com.fivucsas.shared.platform.INetworkMonitor
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.NoOpPushNotificationService
import com.fivucsas.shared.platform.providePlatformFingerprintAuthenticator
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.single

/**
 * Desktop Platform Module
 *
 * Provides Desktop-specific implementations for platform abstractions.
 * Uses Koin for dependency injection.
 *
 * Provides:
 * - DesktopCameraServiceImpl: JavaCV-based camera implementation
 * - DesktopTokenStorage: Preferences-based token storage
 *
 * Design Patterns:
 * - Dependency Injection Pattern: Uses Koin for IoC
 * - Factory Pattern: Creates platform-specific instances
 */
actual val platformModule = module {
    // Camera Service
    // Single instance using JavaCV for webcam access
    single { DesktopCameraServiceImpl() } bind ICameraService::class
    // Token Storage
    // Uses Java Preferences for persistent storage
    single { DesktopTokenStorage() } bind TokenStorage::class
    single<FingerprintAuthenticator> { providePlatformFingerprintAuthenticator() }
    // Push notifications not supported on desktop
    single<IPushNotificationService> { NoOpPushNotificationService() }
    // Secure Storage for offline cache
    single<ISecureStorage> { com.fivucsas.shared.platform.factory.DesktopSecureStorage() }
    // Network Monitor — always-online default on desktop
    single<INetworkMonitor> { DefaultNetworkMonitor() }
}
