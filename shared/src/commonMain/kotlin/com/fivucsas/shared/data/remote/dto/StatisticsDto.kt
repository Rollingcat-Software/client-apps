package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Statistics

/**
 * Data Transfer Object for Statistics
 * 
 * TODO: Add @Serializable when Ktor is added (Week 2)
 */
data class StatisticsDto(
    val totalUsers: Int,
    val activeUsers: Int,
    val pendingVerifications: Int,
    val verificationsToday: Int,
    val successRate: Double,
    val failedAttempts: Int
)

/**
 * Convert DTO to domain model
 */
fun StatisticsDto.toModel(): Statistics {
    return Statistics(
        totalUsers = totalUsers,
        activeUsers = activeUsers,
        pendingVerifications = pendingVerifications,
        verificationsToday = verificationsToday,
        successRate = successRate,
        failedAttempts = failedAttempts
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
        failedAttempts = failedAttempts
    )
}
