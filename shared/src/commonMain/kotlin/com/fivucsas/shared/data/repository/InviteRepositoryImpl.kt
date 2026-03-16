package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.InviteApi
import com.fivucsas.shared.data.remote.dto.CreateInviteRequestDto
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.data.remote.dto.toModels
import com.fivucsas.shared.data.remote.dto.toReceivedModels
import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.model.ReceivedInvite
import com.fivucsas.shared.domain.repository.InviteRepository

/**
 * Real implementation of InviteRepository.
 *
 * Connects to Identity Core API via [InviteApi].
 * Each operation wraps the API call in a [Result] for safe error propagation.
 */
class InviteRepositoryImpl(
    private val inviteApi: InviteApi
) : InviteRepository {

    // ── Admin operations ────────────────────────────────────────────────────

    override suspend fun getInvites(): Result<List<Invite>> {
        return try {
            val response = inviteApi.getInvites()
            Result.success(response.toModels())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createInvite(
        email: String,
        role: String,
        tenantId: String?
    ): Result<Invite> {
        return try {
            val request = CreateInviteRequestDto(
                email = email,
                role = role,
                tenantId = tenantId
            )
            val response = inviteApi.createInvite(request)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeInvite(inviteId: String): Result<Invite> {
        return try {
            val response = inviteApi.revokeInvite(inviteId)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendInvite(inviteId: String): Result<Invite> {
        return try {
            val response = inviteApi.resendInvite(inviteId)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Member operations ───────────────────────────────────────────────────

    override suspend fun getReceivedInvites(): Result<List<ReceivedInvite>> {
        return try {
            val response = inviteApi.getReceivedInvites()
            Result.success(response.toReceivedModels())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptInvite(inviteId: String): Result<ReceivedInvite> {
        return try {
            val response = inviteApi.acceptInvite(inviteId)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineInvite(inviteId: String): Result<ReceivedInvite> {
        return try {
            val response = inviteApi.declineInvite(inviteId)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
