package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuditLogPageDto

/**
 * Audit Log API interface
 *
 * Endpoints:
 * - GET /api/v1/audit-logs?action=X&userId=Y&page=0&size=20
 */
interface AuditLogApi {
    suspend fun getAuditLogs(
        action: String? = null,
        userId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): AuditLogPageDto
}
