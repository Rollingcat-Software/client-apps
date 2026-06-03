package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuditLogPageDto

/**
 * Audit Log API interface
 *
 * Endpoints:
 * - GET /api/v1/audit-logs?action=X&userId=Y&page=0&size=20  (admin — TENANT_ADMIN/ROOT only)
 * - GET /api/v1/my/activity?page=0&size=20                   (user-scoped — current user's own events)
 */
interface AuditLogApi {
    suspend fun getAuditLogs(
        action: String? = null,
        userId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): AuditLogPageDto

    /**
     * Current user's OWN activity events (no admin authority required).
     * Backed by GET /api/v1/my/activity.
     */
    suspend fun getMyActivity(
        page: Int = 0,
        size: Int = 20
    ): AuditLogPageDto
}
