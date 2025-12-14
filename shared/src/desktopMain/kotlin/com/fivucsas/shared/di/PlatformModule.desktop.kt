package com.fivucsas.shared.di

import com.fivucsas.desktop.platform.DesktopCameraServiceImpl
import com.fivucsas.shared.platform.ICameraService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Desktop Platform Module
 *
 * Provides Desktop-specific implementations for platform abstractions.
 * Uses Koin for dependency injection.
 *
 * Provides:
 * - DesktopCameraServiceImpl: JavaCV-based camera implementation
 *
 * Design Patterns:
 * - Dependency Injection Pattern: Uses Koin for IoC
 * - Factory Pattern: Creates platform-specific instances
 */
actual val platformModule = module {
    // Camera Service
    // Single instance using JavaCV for webcam access
    singleOf(::DesktopCameraServiceImpl) bind ICameraService::class
}
