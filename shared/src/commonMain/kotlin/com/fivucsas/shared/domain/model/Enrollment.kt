package com.fivucsas.shared.domain.model

/**
 * Enrollment domain model
 * Represents a biometric enrollment for a user
 */
data class Enrollment(
    val id: String,
    val userId: String,
    val method: String,
    val status: EnrollmentStatus = EnrollmentStatus.PENDING,
    val enrolledAt: String = "",
    val updatedAt: String = "",
    val metadata: Map<String, String> = emptyMap()
)

enum class EnrollmentStatus {
    PENDING,
    ACTIVE,
    REVOKED,
    EXPIRED;

    companion object {
        fun fromString(value: String): EnrollmentStatus {
            return when (value.uppercase()) {
                "PENDING" -> PENDING
                "ACTIVE" -> ACTIVE
                "REVOKED" -> REVOKED
                "EXPIRED" -> EXPIRED
                else -> PENDING
            }
        }
    }
}
