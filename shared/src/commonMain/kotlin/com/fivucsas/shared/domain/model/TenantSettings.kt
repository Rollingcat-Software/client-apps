package com.fivucsas.shared.domain.model

data class TenantSettings(
    val tenantName: String,
    val livenessCheckEnabled: Boolean,
    val confidenceThreshold: Float,
    val maxEnrollmentAttempts: Int,
    val sessionTimeoutMinutes: Int,
    val autoLockEnabled: Boolean,
    val nfcExamEntryEnabled: Boolean,
    val inviteExpiryDays: Int
)
