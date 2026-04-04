package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.AuditLogApi
import com.fivucsas.shared.domain.model.AuditLog
import com.fivucsas.shared.domain.repository.AuditLogRepository

class AuditLogRepositoryImpl(
    private val auditLogApi: AuditLogApi
) : AuditLogRepository {

    override suspend fun getAuditLogs(
        action: String?,
        userId: String?,
        page: Int,
        size: Int
    ): Result<List<AuditLog>> = runCatching {
        val response = auditLogApi.getAuditLogs(
            action = action,
            userId = userId,
            page = page,
            size = size
        )
        response.content.map { dto ->
            AuditLog(
                id = dto.id,
                userId = dto.userId ?: "",
                action = dto.action,
                status = if (dto.success) "SUCCESS" else "FAILURE",
                ipAddress = dto.ipAddress ?: "",
                details = dto.errorMessage
                    ?: dto.entityType?.let { "$it/${dto.entityId ?: ""}" }
                    ?: "",
                timestamp = dto.timestamp ?: ""
            )
        }
    }
}
