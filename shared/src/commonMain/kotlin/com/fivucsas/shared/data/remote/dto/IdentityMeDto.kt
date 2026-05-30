package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.IdentityEmail
import com.fivucsas.shared.domain.model.IdentityMe
import com.fivucsas.shared.domain.model.IdentityMembership
import kotlinx.serialization.Serializable

/**
 * "Person view" returned by `GET /api/v1/identity/me` (account-linking
 * Phase 2). Mirrors the web `IdentityMe` shape. All fields defaulted so a
 * sparse server response never fails deserialization.
 */
@Serializable
data class IdentityMeDto(
    val identityId: String = "",
    val emails: List<IdentityEmailDto> = emptyList(),
    val memberships: List<IdentityMembershipDto> = emptyList()
) {
    fun toDomain(): IdentityMe = IdentityMe(
        identityId = identityId,
        emails = emails.map { it.toDomain() },
        memberships = memberships.map { it.toDomain() }
    )
}

@Serializable
data class IdentityEmailDto(
    val email: String = "",
    val verified: Boolean = false
) {
    fun toDomain(): IdentityEmail = IdentityEmail(email = email, verified = verified)
}

@Serializable
data class IdentityMembershipDto(
    val userId: String = "",
    val tenantId: String? = null,
    val tenantName: String? = null,
    val role: String? = null,
    val isActive: Boolean = false
) {
    fun toDomain(): IdentityMembership = IdentityMembership(
        userId = userId,
        tenantId = tenantId,
        tenantName = tenantName,
        role = role,
        isActive = isActive
    )
}

/** Request body for `POST /identity/link/initiate`. */
@Serializable
data class LinkInitiateRequest(val email: String)

/** Request body for `POST /identity/link/confirm`. */
@Serializable
data class LinkConfirmRequest(
    val email: String,
    val otp: String,
    val password: String
)

/** Request body for `POST /identity/unlink`. */
@Serializable
data class UnlinkRequest(val membershipUserId: String)

/** Request body for `POST /auth/switch-membership`. */
@Serializable
data class SwitchMembershipRequest(val targetUserId: String)

/**
 * Response from `POST /auth/switch-membership`. By contract (Phase 5) this
 * is the SAME shape as `POST /auth/login` — an access + refresh token pair
 * (+ optional `expiresIn`) — so we reuse the canonical token-persistence
 * path after a switch.
 */
@Serializable
data class SwitchMembershipResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresIn: Long? = null
)
