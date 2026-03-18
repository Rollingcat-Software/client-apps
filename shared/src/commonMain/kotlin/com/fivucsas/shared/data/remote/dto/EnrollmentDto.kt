package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.model.EnrollmentStatus
import kotlinx.serialization.Serializable

/**
 * Enrollment DTO — matches Identity Core API (Spring Boot / Jackson) response:
 * GET /users/{userId}/enrollments
 *
 * Server returns: id, authMethodType, status, enrolledAt, expiresAt, createdAt,
 * userId, userName, userEmail, tenantId, qualityScore, livenessScore,
 * errorCode, errorMessage, completedAt
 */
@Serializable
data class EnrollmentDto(
    val id: String = "",
    val authMethodType: String = "",
    val status: String = "PENDING",
    val enrolledAt: String? = null,
    val expiresAt: String? = null,
    val createdAt: String? = null,
    val userId: String = "",
    val userName: String? = null,
    val userEmail: String? = null,
    val tenantId: String? = null,
    val qualityScore: Float? = null,
    val livenessScore: Float? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val completedAt: String? = null
)

fun EnrollmentDto.toDomain(): Enrollment = Enrollment(
    id = id,
    userId = userId,
    method = authMethodType,
    status = EnrollmentStatus.fromString(status),
    enrolledAt = enrolledAt ?: createdAt ?: "",
    completedAt = completedAt,
    userName = userName,
    userEmail = userEmail,
    tenantId = tenantId,
    qualityScore = qualityScore,
    livenessScore = livenessScore
)
