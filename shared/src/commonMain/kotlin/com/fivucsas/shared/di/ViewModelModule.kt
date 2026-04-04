package com.fivucsas.shared.di

import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.viewmodel.AuthFlowViewModel
import com.fivucsas.shared.presentation.viewmodel.BiometricBackupViewModel
import com.fivucsas.shared.presentation.viewmodel.DeviceViewModel
import com.fivucsas.shared.presentation.viewmodel.EnrollmentViewModel
import com.fivucsas.shared.presentation.viewmodel.MultiStepAuthViewModel
import com.fivucsas.shared.presentation.viewmodel.IdentifyViewModel
import com.fivucsas.shared.presentation.viewmodel.InviteViewModel
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.presentation.viewmodel.SessionViewModel
import com.fivucsas.shared.presentation.viewmodel.TenantSettingsViewModel
import com.fivucsas.shared.presentation.viewmodel.UserProfileViewModel
import com.fivucsas.shared.presentation.viewmodel.AnalyticsViewModel
import com.fivucsas.shared.presentation.viewmodel.CardDetectionViewModel
import com.fivucsas.shared.presentation.viewmodel.HardwareTokenViewModel
import com.fivucsas.shared.presentation.viewmodel.LivenessViewModel
import com.fivucsas.shared.presentation.viewmodel.OtpViewModel
import com.fivucsas.shared.presentation.viewmodel.TotpViewModel
import com.fivucsas.shared.presentation.viewmodel.VoiceViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.ChangePasswordViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginViewModel
import com.fivucsas.shared.presentation.viewmodel.DeveloperPortalViewModel
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
    factoryOf(::ChangePasswordViewModel)
    factoryOf(::FingerprintViewModel)
    factoryOf(::QrLoginViewModel)
    factoryOf(::InviteViewModel)
    factoryOf(::IdentifyViewModel)
    factoryOf(::TenantSettingsViewModel)
    factoryOf(::UserProfileViewModel)
    factoryOf(::AuthFlowViewModel)
    factoryOf(::SessionViewModel)
    factoryOf(::DeviceViewModel)
    factoryOf(::EnrollmentViewModel)
    factoryOf(::VoiceViewModel)
    factoryOf(::OtpViewModel)
    factoryOf(::TotpViewModel)
    factoryOf(::AnalyticsViewModel)
    factoryOf(::LivenessViewModel)
    factoryOf(::CardDetectionViewModel)
    factoryOf(::HardwareTokenViewModel)
    factoryOf(::BiometricBackupViewModel)
    factoryOf(::MultiStepAuthViewModel)
    factoryOf(::DeveloperPortalViewModel)
}
