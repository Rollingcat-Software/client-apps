package com.fivucsas.shared.di

import org.koin.dsl.module

/**
 * Main application module - Combines all modules
 *
 * Architecture:
 * - networkModule: HTTP client, API services
 * - repositoryModule: Data repositories
 * - useCaseModule: Business logic use cases
 * - viewModelModule: Presentation layer ViewModels
 * - platformModule: Platform-specific implementations (Camera, Storage, etc.)
 */
val appModule = module {
    includes(
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule,
        platformModule
    )
}

/**
 * Get all application modules
 * @return List of all Koin modules for the application
 */
fun getAppModules() = listOf(appModule)
