package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuditLog

interface AuditLogRepository {
    suspend fun getAuditLogs(
        action: String? = null,
        userId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<AuditLog>>

    /**
     * Current user's OWN activity events (backed by GET /api/v1/my/activity).
     * Does not require admin authority, unlike [getAuditLogs].
     */
    suspend fun getMyActivity(
        page: Int = 0,
        size: Int = 20
    ): Result<List<AuditLog>>
}
