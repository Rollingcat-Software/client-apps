package com.fivucsas.shared.domain.model

/**
 * Enrollment data model - shared across all platforms
 *
 * Contains user information for enrollment process
 */
data class EnrollmentData(
    val fullName: String = "",
    val email: String = "",
    val idNumber: String = "",
    val phoneNumber: String = "",
    val address: String = ""
)
