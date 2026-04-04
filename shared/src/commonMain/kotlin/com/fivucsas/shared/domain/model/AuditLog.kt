package com.fivucsas.shared.domain.model

/**
 * Domain model for an audit log entry (admin dashboard view).
 */
data class AuditLog(
    val id: String,
    val userId: String = "",
    val action: String = "",
    val status: String = "",
    val ipAddress: String = "",
    val details: String = "",
    val timestamp: String = ""
)
