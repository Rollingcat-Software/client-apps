package com.fivucsas.shared.domain.model

/**
 * Statistics model for admin dashboard
 * 
 * Contains system-wide statistics and metrics
 */
data class Statistics(
    val totalUsers: Int = 0,
    val verificationsToday: Int = 0,
    val successRate: Double = 0.0,
    val failedAttempts: Int = 0,
    val activeUsers: Int = 0,
    val pendingVerifications: Int = 0
)
