package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.IdentityMe

/**
 * Repository for account-linking (Phase 2) + in-session membership
 * switching (Phase 5), mirroring the web `useLinkedAccounts` /
 * `useAccountSwitcher` feature hooks.
 */
interface AccountLinkingRepository {
    suspend fun getIdentityMe(): Result<IdentityMe>
    suspend fun initiateLink(email: String): Result<Unit>
    suspend fun confirmLink(email: String, otp: String, password: String): Result<Unit>
    suspend fun unlink(membershipUserId: String): Result<Unit>

    /**
     * Switch the active membership. On success the returned login-shaped
     * tokens are persisted through the canonical token path so the auth
     * interceptor + refresh loop keep working — the caller then resets app
     * context (re-navigate to the post-login home).
     */
    suspend fun switchMembership(targetUserId: String): Result<Unit>
}
