package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.remote.api.AccountLinkingApi
import com.fivucsas.shared.domain.model.IdentityMe
import com.fivucsas.shared.domain.repository.AccountLinkingRepository
import com.fivucsas.shared.domain.repository.AuthTokens

class AccountLinkingRepositoryImpl(
    private val api: AccountLinkingApi,
    private val tokenManager: TokenManager
) : AccountLinkingRepository {

    override suspend fun getIdentityMe(): Result<IdentityMe> = runCatching {
        api.getIdentityMe().toDomain()
    }

    override suspend fun initiateLink(email: String): Result<Unit> = runCatching {
        api.initiateLink(email)
    }

    override suspend fun confirmLink(email: String, otp: String, password: String): Result<Unit> =
        runCatching { api.confirmLink(email, otp, password) }

    override suspend fun unlink(membershipUserId: String): Result<Unit> = runCatching {
        api.unlink(membershipUserId)
    }

    override suspend fun switchMembership(targetUserId: String): Result<Unit> = runCatching {
        val response = api.switchMembership(targetUserId)
        // Reuse the canonical post-login token-persistence path so the auth
        // header injection + refresh loop keep working unchanged after the
        // switch. The switch response is login-shaped (access + refresh).
        tokenManager.saveTokens(
            AuthTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn ?: 0L
            )
        )
    }
}
