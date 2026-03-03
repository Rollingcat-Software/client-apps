package com.fivucsas.shared.di

import com.fivucsas.shared.domain.usecase.admin.CreateUserUseCase
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
import com.fivucsas.shared.domain.usecase.invite.CreateInviteUseCase
import com.fivucsas.shared.domain.usecase.invite.GetInvitesUseCase
import com.fivucsas.shared.domain.usecase.invite.GetReceivedInvitesUseCase
import com.fivucsas.shared.domain.usecase.invite.RespondToInviteUseCase
import com.fivucsas.shared.domain.usecase.invite.RevokeInviteUseCase
import com.fivucsas.shared.domain.usecase.tenant.GetTenantSettingsUseCase
import com.fivucsas.shared.domain.usecase.tenant.UpdateTenantSettingsUseCase
import com.fivucsas.shared.domain.usecase.verification.CheckLivenessUseCase
import com.fivucsas.shared.domain.usecase.verification.IdentifyUserUseCase
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
    factoryOf(::CreateUserUseCase)
    factoryOf(::SearchUsersUseCase)
    factoryOf(::GetStatisticsUseCase)
    factoryOf(::UpdateUserUseCase)
    factoryOf(::DeleteUserUseCase)

    // Enrollment Use Cases
    factoryOf(::EnrollUserUseCase)

    // Verification Use Cases
    factoryOf(::VerifyUserUseCase)
    factoryOf(::CheckLivenessUseCase)
    factoryOf(::IdentifyUserUseCase)

    // Invite Use Cases
    factoryOf(::GetInvitesUseCase)
    factoryOf(::CreateInviteUseCase)
    factoryOf(::RevokeInviteUseCase)
    factoryOf(::GetReceivedInvitesUseCase)
    factoryOf(::RespondToInviteUseCase)

    // Tenant Use Cases
    factoryOf(::GetTenantSettingsUseCase)
    factoryOf(::UpdateTenantSettingsUseCase)
}
