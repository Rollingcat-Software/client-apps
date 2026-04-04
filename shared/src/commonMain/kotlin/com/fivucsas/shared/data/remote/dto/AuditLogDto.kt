package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AuditLogDto(
    val id: String = "",
    val userId: String? = null,
    val tenantId: String? = null,
    val action: String = "",
    val entityType: String? = null,
    val entityId: String? = null,
    val success: Boolean = true,
    val errorMessage: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val details: JsonObject? = null,
    val timestamp: String? = null
)

@Serializable
data class AuditLogPageDto(
    val content: List<AuditLogDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalPages: Int = 0
)
