package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.TenantSettings
import kotlinx.serialization.Serializable

@Serializable
data class TenantSettingsDto(
    val tenantName: String = "",
    val livenessCheckEnabled: Boolean = false,
    val confidenceThreshold: Float = 0f,
    val maxEnrollmentAttempts: Int = 0,
    val sessionTimeoutMinutes: Int = 0,
    val autoLockEnabled: Boolean = false,
    val nfcExamEntryEnabled: Boolean = false,
    val inviteExpiryDays: Int = 0
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
