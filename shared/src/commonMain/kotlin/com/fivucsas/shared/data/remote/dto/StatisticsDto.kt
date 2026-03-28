package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Statistics
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Statistics
 *
 * Maps to the backend GET /api/v1/statistics response.
 * The backend returns fields like totalUsers, activeUsers, biometricEnrolledUsers,
 * totalVerifications, authSuccessRate, etc. All fields default to 0/0.0 so that
 * missing fields (ignoreUnknownKeys = true) don't cause crashes.
 */
@Serializable
data class StatisticsDto(
    // Core user counts
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val inactiveUsers: Int = 0,
    val suspendedUsers: Int = 0,
    // Biometric & enrollment
    val biometricEnrolledUsers: Int = 0,
    val pendingEnrollments: Int = 0,
    val successfulEnrollments: Int = 0,
    val failedEnrollments: Int = 0,
    // Verifications
    val totalVerifications: Int = 0,
    val averageVerificationsPerUser: Double = 0.0,
    val verificationSuccessRate: Double = 0.0,
    // Auth
    val authSuccessRate: Double = 0.0,
    // Tenants
    val totalTenants: Int = 0,
    // Legacy / dashboard-compatible fields (may be returned by future endpoints)
    val pendingVerifications: Int = 0,
    val verificationsToday: Int = 0,
    val successRate: Double = 0.0,
    val failedAttempts: Int = 0,
    // P3: Enrollment breakdown by method
    val faceEnrollments: Int = 0,
    val voiceEnrollments: Int = 0,
    val fingerprintEnrollments: Int = 0,
    val totpEnrollments: Int = 0,
    val nfcEnrollments: Int = 0,
    // P3: Auth metrics
    val totalAuthAttempts: Int = 0,
    val authSuccessCount: Int = 0,
    val authFailureCount: Int = 0,
    // P3: Recent activity counts
    val loginsToday: Int = 0,
    val registrationsToday: Int = 0,
    val enrollmentsToday: Int = 0
)

/**
 * Convert DTO to domain model.
 * Maps real backend fields to the domain Statistics model.
 * Uses the real backend fields where available, falls back to legacy fields.
 */
fun StatisticsDto.toModel(): Statistics {
    // The backend returns authSuccessRate as a percentage (e.g. 93.6 = 93.6%).
    // The domain model expects successRate as a fraction (0.0 - 1.0).
    val resolvedSuccessRate = when {
        authSuccessRate > 0.0 -> authSuccessRate / 100.0
        successRate > 0.0 -> successRate
        else -> 0.0
    }

    return Statistics(
        totalUsers = totalUsers,
        activeUsers = activeUsers,
        pendingVerifications = if (pendingVerifications > 0) pendingVerifications else pendingEnrollments,
        verificationsToday = if (verificationsToday > 0) verificationsToday else totalVerifications,
        successRate = resolvedSuccessRate,
        failedAttempts = if (failedAttempts > 0) failedAttempts else failedEnrollments,
        faceEnrollments = if (faceEnrollments > 0) faceEnrollments else biometricEnrolledUsers,
        voiceEnrollments = voiceEnrollments,
        fingerprintEnrollments = fingerprintEnrollments,
        totpEnrollments = totpEnrollments,
        nfcEnrollments = nfcEnrollments,
        totalAuthAttempts = totalAuthAttempts,
        authSuccessCount = authSuccessCount,
        authFailureCount = authFailureCount,
        loginsToday = loginsToday,
        registrationsToday = registrationsToday,
        enrollmentsToday = if (enrollmentsToday > 0) enrollmentsToday else successfulEnrollments
    )
}

/**
 * Convert domain model to DTO
 */
fun Statistics.toDto(): StatisticsDto {
    return StatisticsDto(
        totalUsers = totalUsers,
        activeUsers = activeUsers,
        pendingVerifications = pendingVerifications,
        verificationsToday = verificationsToday,
        successRate = successRate,
        failedAttempts = failedAttempts,
        faceEnrollments = faceEnrollments,
        voiceEnrollments = voiceEnrollments,
        fingerprintEnrollments = fingerprintEnrollments,
        totpEnrollments = totpEnrollments,
        nfcEnrollments = nfcEnrollments,
        totalAuthAttempts = totalAuthAttempts,
        authSuccessCount = authSuccessCount,
        authFailureCount = authFailureCount,
        loginsToday = loginsToday,
        registrationsToday = registrationsToday,
        enrollmentsToday = enrollmentsToday
    )
}
