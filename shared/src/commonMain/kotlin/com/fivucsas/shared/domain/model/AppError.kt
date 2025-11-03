package com.fivucsas.shared.domain.model

/**
 * Application error types
 * Comprehensive error handling for better UX
 */
sealed class AppError(
    val message: String,
    val userMessage: String,
    val isRetryable: Boolean = false
) {
    
    // Network Errors
    data class NetworkError(
        val errorMessage: String = "Network connection failed"
    ) : AppError(
        message = errorMessage,
        userMessage = "Please check your internet connection and try again",
        isRetryable = true
    )
    
    data class TimeoutError(
        val errorMessage: String = "Request timeout"
    ) : AppError(
        message = errorMessage,
        userMessage = "The request is taking too long. Please try again",
        isRetryable = true
    )
    
    // Server Errors
    data class ServerError(
        val code: Int,
        val errorMessage: String = "Server error: $code"
    ) : AppError(
        message = errorMessage,
        userMessage = when (code) {
            500 -> "Server is experiencing issues. Please try again later"
            503 -> "Service temporarily unavailable. Please try again"
            else -> "Something went wrong on our end. We're working on it"
        },
        isRetryable = code in 500..599
    )
    
    data class UnauthorizedError(
        val errorMessage: String = "Unauthorized"
    ) : AppError(
        message = errorMessage,
        userMessage = "Your session has expired. Please log in again",
        isRetryable = false
    )
    
    data class NotFoundError(
        val resource: String
    ) : AppError(
        message = "$resource not found",
        userMessage = "The requested $resource could not be found",
        isRetryable = false
    )
    
    // Validation Errors
    data class ValidationError(
        val field: String,
        val reason: String
    ) : AppError(
        message = "Validation failed for $field: $reason",
        userMessage = reason,
        isRetryable = false
    )
    
    data class InvalidInputError(
        val field: String,
        val errorMessage: String
    ) : AppError(
        message = "Invalid input for $field",
        userMessage = errorMessage,
        isRetryable = false
    )
    
    // Business Logic Errors
    data class BiometricEnrollmentError(
        val reason: String
    ) : AppError(
        message = "Biometric enrollment failed: $reason",
        userMessage = "Could not enroll face: $reason",
        isRetryable = true
    )
    
    data class VerificationError(
        val reason: String
    ) : AppError(
        message = "Verification failed: $reason",
        userMessage = "Verification failed: $reason",
        isRetryable = true
    )
    
    data class LivenessCheckError(
        val reason: String
    ) : AppError(
        message = "Liveness check failed: $reason",
        userMessage = "Please ensure good lighting and try again",
        isRetryable = true
    )
    
    // Generic Errors
    data class UnknownError(
        val errorMessage: String = "An unexpected error occurred"
    ) : AppError(
        message = errorMessage,
        userMessage = "Something unexpected happened. Please try again",
        isRetryable = true
    )
    
    data class PermissionError(
        val permission: String
    ) : AppError(
        message = "Permission denied: $permission",
        userMessage = "This feature requires $permission permission",
        isRetryable = false
    )
}

/**
 * Convert exceptions to AppError
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is NetworkException -> when (this) {
            is NetworkException.NetworkError -> AppError.NetworkError(message ?: "Network error")
            is NetworkException.Timeout -> AppError.TimeoutError(message ?: "Timeout")
            is NetworkException.ServerError -> AppError.ServerError(code, message ?: "Server error")
            is NetworkException.Unauthorized -> AppError.UnauthorizedError(message ?: "Unauthorized")
            is NetworkException.NotFound -> AppError.NotFoundError("Resource")
            is NetworkException.Unknown -> AppError.UnknownError(message ?: "Unknown error")
        }
        else -> AppError.UnknownError(message ?: "An unexpected error occurred")
    }
}
