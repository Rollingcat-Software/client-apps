package com.fivucsas.shared.domain.model

/**
 * User role enumeration
 *
 * Roles:
 * - GUEST: Pre-auth user, can only do face check (not stored as a role in DB)
 * - USER: Registered account not yet assigned to any tenant
 * - TENANT_MEMBER: User who is a member of a tenant (enrolled/active)
 * - TENANT_ADMIN: Full tenant management + all member capabilities
 * - ROOT: Platform-level admin, has all permissions
 */
enum class UserRole {
    GUEST,
    USER,
    TENANT_MEMBER,
    TENANT_ADMIN,
    ROOT;

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.uppercase().trim()) {
                // New role names
                "ROOT" -> ROOT
                "TENANT_ADMIN" -> TENANT_ADMIN
                "TENANT_MEMBER" -> TENANT_MEMBER
                "USER" -> USER
                "GUEST" -> GUEST
                // Backward compatibility with old role names
                "SUPERADMIN", "SUPER_ADMIN" -> ROOT
                "ORG_ADMIN", "ADMIN" -> TENANT_ADMIN
                "OPERATOR" -> TENANT_ADMIN
                "ENROLLED_USER" -> TENANT_MEMBER
                else -> USER
            }
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
