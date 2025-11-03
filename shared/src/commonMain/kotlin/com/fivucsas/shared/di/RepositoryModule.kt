package com.fivucsas.shared.di

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
 */
val repositoryModule = module {
    // Auth Repository
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    
    // Biometric Repository
    singleOf(::BiometricRepositoryImpl) { bind<BiometricRepository>() }
    
    // User Repository
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
}
