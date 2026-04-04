package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuditLog

interface AuditLogRepository {
    suspend fun getAuditLogs(
        action: String? = null,
        userId: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<AuditLog>>
}
