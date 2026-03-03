package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.TenantSettings
import kotlinx.serialization.Serializable

@Serializable
data class TenantSettingsDto(
    val tenantName: String,
    val livenessCheckEnabled: Boolean,
    val confidenceThreshold: Float,
    val maxEnrollmentAttempts: Int,
    val sessionTimeoutMinutes: Int,
    val autoLockEnabled: Boolean,
    val nfcExamEntryEnabled: Boolean,
    val inviteExpiryDays: Int
)

fun TenantSettingsDto.toModel(): TenantSettings {
    return TenantSettings(
        tenantName = tenantName,
        livenessCheckEnabled = livenessCheckEnabled,
        confidenceThreshold = confidenceThreshold,
        maxEnrollmentAttempts = maxEnrollmentAttempts,
        sessionTimeoutMinutes = sessionTimeoutMinutes,
        autoLockEnabled = autoLockEnabled,
        nfcExamEntryEnabled = nfcExamEntryEnabled,
        inviteExpiryDays = inviteExpiryDays
    )
}

fun TenantSettings.toDto(): TenantSettingsDto {
    return TenantSettingsDto(
        tenantName = tenantName,
        livenessCheckEnabled = livenessCheckEnabled,
        confidenceThreshold = confidenceThreshold,
        maxEnrollmentAttempts = maxEnrollmentAttempts,
        sessionTimeoutMinutes = sessionTimeoutMinutes,
        autoLockEnabled = autoLockEnabled,
        nfcExamEntryEnabled = nfcExamEntryEnabled,
        inviteExpiryDays = inviteExpiryDays
    )
}
