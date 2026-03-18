package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.local.StepUpTokenManager
import com.fivucsas.shared.data.remote.api.AuthBiometricApi
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.api.AuthFlowApi
import com.fivucsas.shared.data.remote.api.RootAdminApi
import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.api.DeviceApi
import com.fivucsas.shared.data.remote.api.EnrollmentApi
import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.api.InviteApi
import com.fivucsas.shared.data.remote.api.SessionApi
import com.fivucsas.shared.data.remote.api.TenantSettingsApi
import com.fivucsas.shared.data.repository.AuthFlowRepositoryImpl
import com.fivucsas.shared.data.repository.AuthRepositoryImpl
import com.fivucsas.shared.data.repository.BiometricRepositoryImpl
import com.fivucsas.shared.data.repository.DeviceRepositoryImpl
import com.fivucsas.shared.data.repository.EnrollmentRepositoryImpl
import com.fivucsas.shared.data.repository.FingerprintRepositoryImpl
import com.fivucsas.shared.data.repository.InviteRepositoryImpl
import com.fivucsas.shared.data.repository.SessionRepositoryImpl
import com.fivucsas.shared.data.repository.TenantSettingsRepositoryImpl
import com.fivucsas.shared.data.repository.QrLoginRepositoryImpl
import com.fivucsas.shared.data.repository.RootAdminRepositoryImpl
import com.fivucsas.shared.data.repository.UserRepositoryImpl
import com.fivucsas.shared.domain.repository.AuthFlowRepository
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.DeviceRepository
import com.fivucsas.shared.domain.repository.EnrollmentRepository
import com.fivucsas.shared.domain.repository.FingerprintRepository
import com.fivucsas.shared.domain.repository.InviteRepository
import com.fivucsas.shared.domain.repository.SessionRepository
import com.fivucsas.shared.domain.repository.TenantSettingsRepository
import com.fivucsas.shared.domain.repository.QrLoginRepository
import com.fivucsas.shared.domain.repository.RootAdminRepository
import com.fivucsas.shared.domain.repository.UserRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Repository module - Provides repository implementations
 *
 * Each repository is injected with its corresponding API client
 * for real backend communication.
 */
val repositoryModule = module {
    // Auth Repository - with AuthApi and TokenManager
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get<AuthApi>(),
            tokenManager = get<TokenManager>(),
            stepUpTokenManager = get<StepUpTokenManager>()
        )
    }

    // Biometric Repository - with BiometricApi
    single<BiometricRepository> {
        BiometricRepositoryImpl(
            biometricApi = get<BiometricApi>()
        )
    }

    single<QrLoginRepository> {
        QrLoginRepositoryImpl(
            qrLoginApi = get()
        )
    }

    single<FingerprintRepository> {
        FingerprintRepositoryImpl(
            authBiometricApi = get<AuthBiometricApi>(),
            fingerprintAuthenticator = get(),
            stepUpTokenManager = get<StepUpTokenManager>()
        )
    }

    // User Repository - with IdentityApi
    single<UserRepository> {
        UserRepositoryImpl(
            identityApi = get<IdentityApi>()
        )
    }

    // Invite Repository - with InviteApi
    single<InviteRepository> {
        InviteRepositoryImpl(
            inviteApi = get<InviteApi>()
        )
    }

    // Tenant Settings Repository
    single<TenantSettingsRepository> {
        TenantSettingsRepositoryImpl(
            tenantSettingsApi = get<TenantSettingsApi>()
        )
    }

    // Root Admin Repository - with RootAdminApi (replaces MockRootAdminRepository)
    single<RootAdminRepository> {
        RootAdminRepositoryImpl(
            api = get<RootAdminApi>()
        )
    }

    // Auth Flow Repository
    single<AuthFlowRepository> {
        AuthFlowRepositoryImpl(
            authFlowApi = get<AuthFlowApi>()
        )
    }

    // Session Repository
    single<SessionRepository> {
        SessionRepositoryImpl(
            sessionApi = get<SessionApi>()
        )
    }

    // Device Repository
    single<DeviceRepository> {
        DeviceRepositoryImpl(
            deviceApi = get<DeviceApi>()
        )
    }

    // Enrollment Repository
    single<EnrollmentRepository> {
        EnrollmentRepositoryImpl(
            enrollmentApi = get<EnrollmentApi>()
        )
    }

    // Voice Repository
    single<com.fivucsas.shared.domain.repository.VoiceRepository> {
        com.fivucsas.shared.data.repository.VoiceRepositoryImpl(
            voiceApi = get<com.fivucsas.shared.data.remote.api.VoiceApi>()
        )
    }

    // OTP Repository
    single<com.fivucsas.shared.domain.repository.OtpRepository> {
        com.fivucsas.shared.data.repository.OtpRepositoryImpl(
            otpApi = get<com.fivucsas.shared.data.remote.api.OtpApi>()
        )
    }

    // TOTP Repository
    single<com.fivucsas.shared.domain.repository.TotpRepository> {
        com.fivucsas.shared.data.repository.TotpRepositoryImpl(
            totpApi = get<com.fivucsas.shared.data.remote.api.TotpApi>()
        )
    }

    // Dashboard Repository
    single<com.fivucsas.shared.domain.repository.DashboardRepository> {
        com.fivucsas.shared.data.repository.DashboardRepositoryImpl(
            dashboardApi = get<com.fivucsas.shared.data.remote.api.DashboardApi>()
        )
    }

    // Offline Cache
    single { OfflineCache(storage = get()) }
}
