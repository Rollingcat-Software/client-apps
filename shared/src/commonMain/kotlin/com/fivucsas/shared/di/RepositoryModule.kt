package com.fivucsas.shared.di

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.repository.AuthRepositoryImpl
import com.fivucsas.shared.data.repository.BiometricRepositoryImpl
import com.fivucsas.shared.data.repository.UserRepositoryImpl
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.BiometricRepository
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
            tokenManager = get<TokenManager>()
        )
    }

    // Biometric Repository - with BiometricApi
    single<BiometricRepository> {
        BiometricRepositoryImpl(
            biometricApi = get<BiometricApi>()
        )
    }

    // User Repository - with IdentityApi
    single<UserRepository> {
        UserRepositoryImpl(
            identityApi = get<IdentityApi>()
        )
    }
}
