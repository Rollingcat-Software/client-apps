package com.fivucsas.shared.domain.model

/**
 * Statistics model for admin dashboard
 *
 * Contains system-wide statistics and metrics.
 * Extended with enrollment breakdown and recent activity for P3 analytics.
 */
data class Statistics(
    val totalUsers: Int = 0,
    val verificationsToday: Int = 0,
    val successRate: Double = 0.0,
    val failedAttempts: Int = 0,
    val activeUsers: Int = 0,
    val pendingVerifications: Int = 0,
    // P3: Enrollment breakdown by method
    val faceEnrollments: Int = 0,
    val voiceEnrollments: Int = 0,
    val fingerprintEnrollments: Int = 0,
    val totpEnrollments: Int = 0,
    val nfcEnrollments: Int = 0,
    // P3: Auth success/failure metrics
    val totalAuthAttempts: Int = 0,
    val authSuccessCount: Int = 0,
    val authFailureCount: Int = 0,
    // P3: Recent activity counts
    val loginsToday: Int = 0,
    val registrationsToday: Int = 0,
    val enrollmentsToday: Int = 0
)
