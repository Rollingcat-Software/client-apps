package com.fivucsas.shared.di

import org.koin.dsl.module

/**
 * Main application module - Combines all modules
 */
val appModule = module {
    includes(
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule
    )
}

/**
 * Get all application modules
 */
fun getAppModules() = listOf(appModule)
