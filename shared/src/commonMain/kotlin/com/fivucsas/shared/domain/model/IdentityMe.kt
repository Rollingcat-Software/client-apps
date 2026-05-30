package com.fivucsas.shared.domain.model

/**
 * Account-linking (Phase 2) "person view": one identity owning N verified
 * emails and N tenant memberships. Mirrors the web `IdentityMe` model so
 * mobile and web present the same linked-accounts + workspace-switcher UX.
 */
data class IdentityMe(
    val identityId: String,
    val emails: List<IdentityEmail>,
    val memberships: List<IdentityMembership>
)

/** One verified email controlled by the person. */
data class IdentityEmail(
    val email: String,
    val verified: Boolean
)

/** One tenant membership (a `users` row) the person holds. */
data class IdentityMembership(
    val userId: String,
    val tenantId: String?,
    val tenantName: String?,
    val role: String?,
    val isActive: Boolean
)
