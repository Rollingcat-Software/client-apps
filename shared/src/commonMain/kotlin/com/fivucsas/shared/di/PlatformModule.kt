package com.fivucsas.shared.di

import org.koin.core.module.Module

/**
 * Platform Module Interface
 *
 * Defines the contract for platform-specific dependency injection modules.
 * Each platform (Android, Desktop, iOS) provides its own implementation.
 *
 * Design Principles:
 * - Dependency Inversion Principle: High-level code depends on abstraction
 * - Single Responsibility Principle: Only handles platform-specific DI
 *
 * This follows the expect/actual pattern in Kotlin Multiplatform.
 */
expect val platformModule: Module
