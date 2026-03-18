package com.fivucsas.shared.domain.model

/**
 * Enrollment domain model
 * Represents a biometric/auth method enrollment for a user
 */
data class Enrollment(
    val id: String,
    val userId: String,
    val method: String,
    val status: EnrollmentStatus = EnrollmentStatus.PENDING,
    val enrolledAt: String = "",
    val completedAt: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val tenantId: String? = null,
    val qualityScore: Float? = null,
    val livenessScore: Float? = null
)

enum class EnrollmentStatus {
    PENDING,
    ENROLLED,
    ACTIVE,
    REVOKED,
    EXPIRED;

    companion object {
        fun fromString(value: String): EnrollmentStatus {
            return when (value.uppercase()) {
                "PENDING" -> PENDING
                "ENROLLED" -> ENROLLED
                "ACTIVE" -> ACTIVE
                "REVOKED" -> REVOKED
                "EXPIRED" -> EXPIRED
                else -> PENDING
            }
        }
    }
}
