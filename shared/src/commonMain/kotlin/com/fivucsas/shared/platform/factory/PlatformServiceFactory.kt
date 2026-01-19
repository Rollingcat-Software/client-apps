package com.fivucsas.shared.platform.factory

import com.fivucsas.shared.platform.*

/**
 * Platform Service Factory (Factory Pattern)
 *
 * Creates platform-specific service implementations following Factory pattern.
 * Centralizes adapter creation for Hexagonal Architecture.
 *
 * Design Principles Applied:
 * - Factory Pattern: Centralized object creation
 * - Dependency Inversion Principle (DIP): Returns interface types
 * - Open/Closed Principle (OCP): Open for new platforms, closed for modification
 * - Single Responsibility Principle (SRP): Only creates platform services
 *
 * Hexagonal Architecture Role: FACTORY (Creates adapters for ports)
 */
interface PlatformServiceFactory {
    /**
     * Create camera service for current platform
     * @return Platform-specific ICameraService implementation
     */
    fun createCameraService(): ICameraService

    /**
     * Create logger for current platform
     * @return Platform-specific ILogger implementation
     */
    fun createLogger(): ILogger

    /**
     * Create secure storage for current platform
     * @return Platform-specific ISecureStorage implementation
     */
    fun createSecureStorage(): ISecureStorage

    /**
     * Create navigation service for current platform
     * @return Platform-specific INavigationService implementation
     */
    fun createNavigationService(): INavigationService

    /**
     * Create dialog service for current platform
     * @return Platform-specific IDialogService implementation
     */
    fun createDialogService(): IDialogService

    /**
     * Create notification service for current platform
     * @return Platform-specific INotificationService implementation
     */
    fun createNotificationService(): INotificationService

    /**
     * Create configuration provider for current platform
     * @return Platform-specific IConfigurationProvider implementation
     */
    fun createConfigurationProvider(): IConfigurationProvider
}

/**
 * Platform types supported by the application
 */
enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP,
    WEB
}

/**
 * Get current platform type
 * This is implemented as an expect/actual function per platform
 */
expect fun getCurrentPlatform(): PlatformType

/**
 * Create platform-specific service factory
 * This is implemented as an expect/actual function per platform
 */
expect fun createPlatformServiceFactory(): PlatformServiceFactory
