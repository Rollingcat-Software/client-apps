package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.UserStatus
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for User
 *
 * Used for API communication (JSON serialization).
 * Separate from domain model to:
 * - Allow API changes without affecting domain
 * - Handle nullable/optional fields from API
 * - Add serialization annotations
 */
@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val idNumber: String,
    val phoneNumber: String? = null,
    val status: String,
    val enrollmentDate: String,
    val hasBiometric: Boolean = false,
    val role: String? = null
)

/**
 * Convert DTO to domain model
 */
fun UserDto.toModel(): User {
    return User(
        id = id,
        name = name,
        email = email,
        idNumber = idNumber,
        phoneNumber = phoneNumber ?: "",
        status = UserStatus.valueOf(status),
        enrollmentDate = enrollmentDate,
        hasBiometric = hasBiometric,
        role = UserRole.fromString(role ?: "USER")
    )
}

/**
 * Convert domain model to DTO
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        idNumber = idNumber,
        phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
        status = status.name,
        enrollmentDate = enrollmentDate,
        hasBiometric = hasBiometric,
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
