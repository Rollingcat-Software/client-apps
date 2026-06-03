package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.AuditLogApi
import com.fivucsas.shared.data.remote.dto.AuditLogDto
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
        auditLogApi.getAuditLogs(
            action = action,
            userId = userId,
            page = page,
            size = size
        ).content.map { it.toDomain() }
    }

    override suspend fun getMyActivity(
        page: Int,
        size: Int
    ): Result<List<AuditLog>> = runCatching {
        auditLogApi.getMyActivity(
            page = page,
            size = size
        ).content.map { it.toDomain() }
    }

    private fun AuditLogDto.toDomain(): AuditLog = AuditLog(
        id = id,
        userId = userId ?: "",
        action = action,
        status = if (success) "SUCCESS" else "FAILURE",
        ipAddress = ipAddress ?: "",
        details = errorMessage
            ?: entityType?.let { "$it/${entityId ?: ""}" }
            ?: "",
        timestamp = timestamp ?: ""
    )
}
