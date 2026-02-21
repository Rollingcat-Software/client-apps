package com.fivucsas.desktop.platform

import com.fivucsas.shared.platform.DesktopCameraServiceImpl
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.ILogger
import com.fivucsas.shared.platform.ISecureStorage
import org.koin.dsl.module

/**
 * Desktop Platform Module
 *
 * Koin module providing desktop-specific implementations
 * of platform abstraction interfaces.
 *
 * Usage:
 * ```
 * startKoin {
 *     modules(desktopPlatformModule)
 * }
 * ```
 */
val desktopPlatformModule = module {
    // Camera Service - Singleton
    single<ICameraService> { DesktopCameraServiceImpl() }

    // Logger - Singleton
    single<ILogger> { DesktopLoggerImpl() }

    // Secure Storage - Singleton
    single<ISecureStorage> { DesktopSecureStorageImpl() }
}
