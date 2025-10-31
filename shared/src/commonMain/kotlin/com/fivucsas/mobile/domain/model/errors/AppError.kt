package com.fivucsas.mobile.domain.model.errors

sealed class AppError(open val message: String, open val cause: Throwable? = null) {

    sealed class NetworkError(override val message: String, override val cause: Throwable? = null) :
        AppError(message, cause) {
        data class NoConnection(override val cause: Throwable? = null) :
            NetworkError("No internet connection", cause)

        data class Timeout(override val cause: Throwable? = null) :
            NetworkError("Request timed out", cause)

        data class ServerError(
            val code: Int,
            override val message: String,
            override val cause: Throwable? = null
        ) :
            NetworkError("Server error ($code): $message", cause)

        data class Unknown(override val cause: Throwable) :
            NetworkError("Network error: ${cause.message}", cause)
    }

    sealed class AuthError(override val message: String, override val cause: Throwable? = null) :
        AppError(message, cause) {
        data object InvalidCredentials : AuthError("Invalid email or password")
        data object UserAlreadyExists : AuthError("User with this email already exists")
        data object Unauthorized : AuthError("You are not authorized to perform this action")
        data object TokenExpired : AuthError("Your session has expired. Please login again")
        data class Unknown(override val cause: Throwable) :
            AuthError("Authentication error: ${cause.message}", cause)
    }

    sealed class BiometricError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        data object NoFaceDetected : BiometricError("No face detected in image")
        data object MultipleFacesDetected :
            BiometricError("Multiple faces detected. Please ensure only one face is visible")

        data object LivenessCheckFailed : BiometricError("Liveness check failed. Please try again")
        data object ImageQualityTooLow :
            BiometricError("Image quality too low. Please use better lighting")

        data object FaceNotEnrolled : BiometricError("Face not enrolled. Please enroll first")
        data object VerificationFailed : BiometricError("Face verification failed")
        data class Unknown(override val cause: Throwable) :
            BiometricError("Biometric error: ${cause.message}", cause)
    }

    sealed class ValidationError(override val message: String) : AppError(message) {
        data object InvalidEmail : ValidationError("Please enter a valid email address")
        data object WeakPassword :
            ValidationError("Password must be at least 8 characters with uppercase, lowercase, and numbers")

        data object PasswordMismatch : ValidationError("Passwords do not match")
        data object EmptyField : ValidationError("This field cannot be empty")
        data class Custom(override val message: String) : ValidationError(message)
    }

    sealed class StorageError(override val message: String, override val cause: Throwable? = null) :
        AppError(message, cause) {
        data class ReadError(override val cause: Throwable) :
            StorageError("Failed to read data", cause)

        data class WriteError(override val cause: Throwable) :
            StorageError("Failed to write data", cause)

        data object NotFound : StorageError("Data not found")
    }

    data class Unknown(override val message: String, override val cause: Throwable? = null) :
        AppError(message, cause)
}

fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        else -> AppError.Unknown(this.message ?: "Unknown error", this)
    }
}
