package com.fivucsas.shared.di

import com.fivucsas.shared.domain.usecase.admin.DeleteUserUseCase
import com.fivucsas.shared.domain.usecase.admin.GetStatisticsUseCase
import com.fivucsas.shared.domain.usecase.admin.GetUsersUseCase
import com.fivucsas.shared.domain.usecase.admin.SearchUsersUseCase
import com.fivucsas.shared.domain.usecase.admin.UpdateUserUseCase
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.domain.usecase.auth.RegisterUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.ApproveQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.GetQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.StartQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.verification.CheckLivenessUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Use case module - Provides business logic use cases
 */
val useCaseModule = module {
    // Auth Use Cases
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::StartQrLoginSessionUseCase)
    factoryOf(::GetQrLoginSessionUseCase)
    factoryOf(::ApproveQrLoginSessionUseCase)

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
