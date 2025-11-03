package com.fivucsas.shared.di

import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * ViewModel module - Provides ViewModels for UI
 */
val viewModelModule = module {
    // ViewModels (factory scoped - new instance per screen)
    factoryOf(::KioskViewModel)
    factoryOf(::AdminViewModel)
}
