package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.UserStatus
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for User
 *
 * Matches the Identity Core API (Spring Boot / Jackson) user response format.
 * The server returns camelCase JSON with firstName/lastName (not a single "name" field).
 * All fields are nullable with defaults to avoid deserialization crashes.
 */
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val idNumber: String? = null,
    val status: String = "ACTIVE",
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val role: String? = null,
    val roles: List<String> = emptyList(),
    val tenantId: String? = null,
    val enrolledAt: String? = null,
    val lastVerifiedAt: String? = null,
    val verificationCount: Int = 0,
    val lastLoginAt: String? = null,
    val lastLoginIp: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val biometricEnrolled: Boolean = false
)

/**
 * Paginated response wrapper matching Spring Boot Page<T> format.
 * GET /users returns { content: [...], page, size, totalPages }.
 */
@Serializable
data class PagedUserResponse(
    val content: List<UserDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalPages: Int = 0
)

/**
 * Convert DTO to domain model
 */
fun UserDto.toModel(): User {
    val fullName = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { email }
    val resolvedRole = role ?: roles.firstOrNull() ?: "USER"
    val resolvedStatus = try {
        UserStatus.valueOf(status)
    } catch (_: Exception) {
        UserStatus.ACTIVE
    }

    return User(
        id = id,
        name = fullName,
        email = email,
        idNumber = idNumber ?: "",
        phoneNumber = phoneNumber ?: "",
        status = resolvedStatus,
        enrollmentDate = enrolledAt ?: createdAt ?: "",
        hasBiometric = biometricEnrolled,
        role = UserRole.fromString(resolvedRole)
    )
}

/**
 * Convert domain model to DTO (for create/update requests)
 */
fun User.toDto(): UserDto {
    val nameParts = name.split(" ", limit = 2)
    return UserDto(
        id = id,
        email = email,
        firstName = nameParts.getOrNull(0) ?: name,
        lastName = nameParts.getOrNull(1),
        idNumber = idNumber.takeIf { it.isNotBlank() },
        phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
        status = status.name,
        biometricEnrolled = hasBiometric,
        role = role.name
    )
}

/**
 * Convert list of DTOs to domain models
 */
fun List<UserDto>.toModels(): List<User> {
    return map { it.toModel() }
}

/**
 * Convert list of domain models to DTOs
 */
fun List<User>.toDtos(): List<UserDto> {
    return map { it.toDto() }
}
