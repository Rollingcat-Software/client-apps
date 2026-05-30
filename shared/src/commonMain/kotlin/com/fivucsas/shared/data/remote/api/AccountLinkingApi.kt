package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.IdentityMeDto
import com.fivucsas.shared.data.remote.dto.SwitchMembershipResponse

/**
 * Account-linking + workspace-switcher API (identity-core-api, Bearer auth).
 *
 * Endpoints:
 * - GET  /identity/me                → person view (emails + memberships)
 * - POST /identity/link/initiate     → send OTP to a target email
 * - POST /identity/link/confirm      → verify OTP + step-up password, link
 * - POST /identity/unlink            → split a membership back to its own identity
 * - POST /auth/switch-membership     → switch active membership (login-shaped tokens)
 */
interface AccountLinkingApi {
    suspend fun getIdentityMe(): IdentityMeDto
    suspend fun initiateLink(email: String)
    suspend fun confirmLink(email: String, otp: String, password: String)
    suspend fun unlink(membershipUserId: String)
    suspend fun switchMembership(targetUserId: String): SwitchMembershipResponse
}
