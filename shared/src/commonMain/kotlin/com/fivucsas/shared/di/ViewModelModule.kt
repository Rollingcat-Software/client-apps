package com.fivucsas.shared.di

import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * ViewModel module - Provides ViewModels for UI
 */
val viewModelModule = module {
    // ViewModels (factory scoped - new instance per screen)
    factoryOf(::KioskViewModel)
    factoryOf(::AdminViewModel)
    factoryOf(::LoginViewModel)
    factoryOf(::RegisterViewModel)
    factoryOf(::BiometricViewModel)
    factoryOf(::FingerprintViewModel)
    factoryOf(::QrLoginViewModel)
}
