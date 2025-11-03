package com.fivucsas.shared.domain.model

/**
 * User model - shared across all platforms
 * 
 * Represents a registered user in the system
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val idNumber: String,
    val phoneNumber: String = "",
    val status: UserStatus,
    val enrollmentDate: String = "",
    val hasBiometric: Boolean = false
)

/**
 * User status enumeration
 */
enum class UserStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    SUSPENDED
}
