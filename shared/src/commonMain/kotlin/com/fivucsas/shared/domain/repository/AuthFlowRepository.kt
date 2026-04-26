package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuthFlow

interface AuthFlowRepository {
    /**
     * Lists all auth flows configured for a tenant.
     *
     * Note: this endpoint requires authentication (the tenant CRUD is admin-only),
     * so callers running pre-login should be prepared for a `Result.failure` and
     * fall back to the legacy PASSWORD flow.
     */
    suspend fun getAuthFlows(tenantId: String): Result<List<AuthFlow>>

    /**
     * Returns the *active* auth flow for a given operation type, optionally
     * scoped to a tenant. Used by the LoginScreen to discover what primary
     * step (PASSWORD, EMAIL_OTP, FACE, TOTP, ...) it should render.
     *
     * Returns `Result.success(null)` when:
     *   - no flow is configured for the (tenantId, operationType) pair
     *   - the endpoint requires authentication and the user is anonymous
     *     (the implementation deliberately swallows 401/403 so the login
     *     screen always falls back to the legacy PASSWORD form)
     *
     * Returns `Result.failure` only on hard network errors.
     *
     * @param operationType e.g. "APP_LOGIN", "DOOR_ACCESS"
     * @param tenantId optional tenant scope. When null, the implementation
     *                 will skip the tenant-scoped fetch and return null
     *                 (no public cross-tenant discovery endpoint exists yet).
     */
    suspend fun getActiveFlow(
        operationType: String,
        tenantId: String?
    ): Result<AuthFlow?>
}
