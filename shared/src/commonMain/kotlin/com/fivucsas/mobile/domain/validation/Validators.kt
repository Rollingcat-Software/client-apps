package com.fivucsas.mobile.domain.validation

import com.fivucsas.mobile.domain.model.errors.AppError

/**
 * Email Validator
 * Validates email format according to RFC 5322
 */
object EmailValidator {
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )

    fun validate(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid(AppError.ValidationError.EmptyField)
            !EMAIL_REGEX.matches(email) -> ValidationResult.Invalid(AppError.ValidationError.InvalidEmail)
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Password Validator
 * Enforces strong password policy:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 */
object PasswordValidator {
    private const val MIN_LENGTH = 8
    private val UPPERCASE_REGEX = Regex(".*[A-Z].*")
    private val LOWERCASE_REGEX = Regex(".*[a-z].*")
    private val DIGIT_REGEX = Regex(".*\\d.*")

    fun validate(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid(AppError.ValidationError.EmptyField)
            password.length < MIN_LENGTH -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("Password must be at least $MIN_LENGTH characters")
            )

            !UPPERCASE_REGEX.matches(password) -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("Password must contain at least one uppercase letter")
            )

            !LOWERCASE_REGEX.matches(password) -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("Password must contain at least one lowercase letter")
            )

            !DIGIT_REGEX.matches(password) -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("Password must contain at least one number")
            )

            else -> ValidationResult.Valid
        }
    }

    fun validateMatch(password: String, confirmPassword: String): ValidationResult {
        return when {
            password != confirmPassword -> ValidationResult.Invalid(AppError.ValidationError.PasswordMismatch)
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Name Validator
 */
object NameValidator {
    private const val MIN_LENGTH = 2
    private const val MAX_LENGTH = 50
    private val NAME_REGEX = Regex("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s'-]+$")

    fun validate(name: String, fieldName: String = "Name"): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid(AppError.ValidationError.EmptyField)
            name.length < MIN_LENGTH -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("$fieldName must be at least $MIN_LENGTH characters")
            )

            name.length > MAX_LENGTH -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("$fieldName must be at most $MAX_LENGTH characters")
            )

            !NAME_REGEX.matches(name) -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("$fieldName contains invalid characters")
            )

            else -> ValidationResult.Valid
        }
    }
}

/**
 * Image Validator
 */
object ImageValidator {
    private const val MIN_SIZE_BYTES = 10 * 1024
    private const val MAX_SIZE_BYTES = 10 * 1024 * 1024
    private const val MIN_DIMENSION = 480

    fun validateSize(sizeBytes: Int): ValidationResult {
        return when {
            sizeBytes < MIN_SIZE_BYTES -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("Image too small. Please capture a clearer photo")
            )

            sizeBytes > MAX_SIZE_BYTES -> ValidationResult.Invalid(
                AppError.ValidationError.Custom("Image too large. Maximum size is 10MB")
            )

            else -> ValidationResult.Valid
        }
    }

    fun validateDimensions(width: Int, height: Int): ValidationResult {
        return when {
            width < MIN_DIMENSION || height < MIN_DIMENSION -> ValidationResult.Invalid(
                AppError.BiometricError.ImageQualityTooLow
            )

            else -> ValidationResult.Valid
        }
    }
}

/**
 * Validation Result
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val error: AppError) : ValidationResult()

    fun isValid(): Boolean = this is Valid
    fun getErrorOrNull(): AppError? = (this as? Invalid)?.error
}

fun List<ValidationResult>.combineValidations(): ValidationResult {
    val firstError = this.firstOrNull { it is ValidationResult.Invalid }
    return firstError ?: ValidationResult.Valid
}
