package com.fivucsas.shared.domain.exception

/**
 * Base exception for all application exceptions
 * 
 * All custom exceptions inherit from this.
 * Makes it easy to catch all app-specific errors.
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Validation error
 * 
 * Thrown when user input fails validation.
 * Example: Invalid email format, Turkish ID checksum failed
 */
class ValidationException(message: String) : AppException(message)

/**
 * Network error
 * 
 * Thrown when network communication fails.
 * Example: No internet connection, timeout, DNS failure
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Server error
 * 
 * Thrown when server returns error response.
 * Example: 500 Internal Server Error, 503 Service Unavailable
 */
class ServerException(
    val statusCode: Int,
    message: String,
    cause: Throwable? = null
) : AppException("Server error ($statusCode): $message", cause)

/**
 * Authentication error
 * 
 * Thrown when authentication fails.
 * Example: Invalid credentials, expired token, unauthorized access
 */
class AuthException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Not found error
 * 
 * Thrown when requested resource doesn't exist.
 * Example: User not found, biometric data not found
 */
class NotFoundException(
    val resourceType: String,
    val resourceId: String
) : AppException("$resourceType with ID '$resourceId' not found")

/**
 * Biometric error
 * 
 * Thrown when biometric operations fail.
 * Example: No face detected, low quality image, liveness check failed
 */
class BiometricException(
    message: String,
    val errorCode: BiometricErrorCode,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Biometric error codes
 */
enum class BiometricErrorCode {
    NO_FACE_DETECTED,
    MULTIPLE_FACES_DETECTED,
    LOW_QUALITY_IMAGE,
    LIVENESS_CHECK_FAILED,
    FACE_TOO_SMALL,
    FACE_TOO_LARGE,
    POOR_LIGHTING,
    FACE_NOT_CENTERED,
    ENROLLMENT_FAILED,
    VERIFICATION_FAILED
}

/**
 * Business logic error
 * 
 * Thrown when business rules are violated.
 * Example: User already enrolled, cannot delete active user
 */
class BusinessException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Data conflict error
 * 
 * Thrown when data conflict occurs.
 * Example: Email already exists, duplicate national ID
 */
class ConflictException(
    message: String,
    val conflictField: String,
    cause: Throwable? = null
) : AppException(message, cause)
