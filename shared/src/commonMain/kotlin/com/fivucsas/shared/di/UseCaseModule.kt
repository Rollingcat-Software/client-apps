package com.fivucsas.shared.di

import com.fivucsas.shared.domain.usecase.admin.*
import com.fivucsas.shared.domain.usecase.enrollment.*
import com.fivucsas.shared.domain.usecase.verification.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Use case module - Provides business logic use cases
 */
val useCaseModule = module {
    // Admin Use Cases (factories - new instance per injection)
    factoryOf(::GetUsersUseCase)
    factoryOf(::SearchUsersUseCase)
    factoryOf(::GetStatisticsUseCase)
    factoryOf(::UpdateUserUseCase)
    factoryOf(::DeleteUserUseCase)
    
    // Enrollment Use Cases
    factoryOf(::EnrollUserUseCase)
    
    // Verification Use Cases
    factoryOf(::VerifyUserUseCase)
    factoryOf(::CheckLivenessUseCase)
}
