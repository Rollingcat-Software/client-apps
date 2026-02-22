package com.fivucsas.shared.domain.model

/**
 * User role enumeration
 */
enum class UserRole {
    SUPERADMIN,
    ORG_ADMIN,
    OPERATOR,
    ENROLLED_USER,
    USER;

    companion object {
        fun fromString(value: String): UserRole {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: USER
        }
    }
}

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
    val hasBiometric: Boolean = false,
    val role: UserRole = UserRole.USER
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
