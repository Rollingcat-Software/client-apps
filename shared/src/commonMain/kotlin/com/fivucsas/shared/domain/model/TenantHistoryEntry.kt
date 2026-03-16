package com.fivucsas.shared.domain.model

data class TenantHistoryEntry(
    val action: String,
    val user: String,
    val detail: String,
    val timestamp: String,
    val isSuccess: Boolean
)
