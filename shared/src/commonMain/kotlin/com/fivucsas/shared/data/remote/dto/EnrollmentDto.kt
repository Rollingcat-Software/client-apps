package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.model.EnrollmentStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val method: String = "",
    val status: String = "PENDING",
    @SerialName("enrolled_at") val enrolledAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
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
