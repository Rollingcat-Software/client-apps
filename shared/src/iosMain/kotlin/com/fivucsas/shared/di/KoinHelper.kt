package com.fivucsas.shared.di

import org.koin.core.context.startKoin

/**
 * iOS Koin initialization helper
 * Call this from Swift/Objective-C code
 */
fun initKoin() {
    startKoin {
        modules(getAppModules())
    }
}
