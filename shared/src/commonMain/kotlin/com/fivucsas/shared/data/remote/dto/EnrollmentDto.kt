package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.model.EnrollmentStatus
import kotlinx.serialization.Serializable

/**
 * Enrollment DTO — server returns camelCase JSON (Spring Boot / Jackson)
 */
@Serializable
data class EnrollmentDto(
    val id: String = "",
    val userId: String = "",
    val method: String = "",
    val status: String = "PENDING",
    val enrolledAt: String = "",
    val updatedAt: String = "",
    val metadata: Map<String, String> = emptyMap()
)

fun EnrollmentDto.toDomain(): Enrollment = Enrollment(
    id = id,
    userId = userId,
    method = method,
    status = EnrollmentStatus.fromString(status),
    enrolledAt = enrolledAt,
    updatedAt = updatedAt,
    metadata = metadata
)
