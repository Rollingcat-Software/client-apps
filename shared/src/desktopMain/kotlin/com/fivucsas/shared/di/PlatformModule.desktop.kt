package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.BiometricStepUpLocalStore
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.domain.biometric.BiometricAuthenticator
import com.fivucsas.shared.platform.DesktopBiometricAuthenticator
import com.fivucsas.shared.platform.DesktopBiometricStepUpLocalStore
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.platform.DesktopCameraServiceImpl
import com.fivucsas.shared.platform.DesktopTokenStorage
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
    single { DesktopBiometricAuthenticator() } bind BiometricAuthenticator::class
    single { DesktopBiometricStepUpLocalStore() } bind BiometricStepUpLocalStore::class
}
