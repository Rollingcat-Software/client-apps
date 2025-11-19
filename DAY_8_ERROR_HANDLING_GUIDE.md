# Day 8: Error Handling & Polish 🎨

**Date:** November 3, 2025  
**Status:** In Progress  
**Estimated Time:** 60 minutes  
**Difficulty:** MEDIUM  
**Impact:** HIGH - Professional UX!

---

## 📊 What We'll Achieve

### Before Day 8 ⚠️

```kotlin
// Basic error handling
try {
    val users = getUsers()
} catch (e: Exception) {
    // Generic error - not helpful!
    showError("Something went wrong")
}
```

### After Day 8 ✅

```kotlin
// Professional error handling
when (val result = getUsers()) {
    is Result.Success -> {
        showUsers(result.data)
    }
    is Result.Error -> {
        when (result.error) {
            is NetworkError -> showRetryableError("No connection", ::retry)
            is ValidationError -> showFieldError(result.error.field)
            is ServerError -> showContactSupport(result.error.code)
        }
    }
    is Result.Loading -> {
        showLoadingWithProgress()
    }
}
```

---

## 🎯 Goals

1. ✅ Enhanced error types and messages
2. ✅ Retry mechanisms
3. ✅ Loading state management
4. ✅ Input validation with feedback
5. ✅ Success confirmations
6. ✅ Better user experience

---

## 📦 Step 1: Enhanced Error Types (15 minutes)

### 1.1 Create Comprehensive Error Types

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/domain/model/AppError.kt`

```kotlin
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
```

---

## 🔄 Step 2: Loading State Management (15 minutes)

### 2.1 Create Loading State Model

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/domain/model/LoadingState.kt`

```kotlin
package com.fivucsas.shared.domain.model

/**
 * Loading state with progress
 * Better UX with detailed loading information
 */
sealed class LoadingState {
    data object Idle : LoadingState()
    
    data class Loading(
        val progress: Float? = null,
        val message: String? = null
    ) : LoadingState()
    
    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : LoadingState()
    
    data class Error(
        val error: AppError,
        val canRetry: Boolean = error.isRetryable
    ) : LoadingState()
}

/**
 * Extension functions for LoadingState
 */
val LoadingState.isLoading: Boolean
    get() = this is LoadingState.Loading

val LoadingState.isSuccess: Boolean
    get() = this is LoadingState.Success<*>

val LoadingState.isError: Boolean
    get() = this is LoadingState.Error

val LoadingState.isIdle: Boolean
    get() = this is LoadingState.Idle

/**
 * Get data or null
 */
fun <T> LoadingState.getDataOrNull(): T? {
    return when (this) {
        is LoadingState.Success<*> -> data as? T
        else -> null
    }
}

/**
 * Get error or null
 */
fun LoadingState.getErrorOrNull(): AppError? {
    return when (this) {
        is LoadingState.Error -> error
        else -> null
    }
}
```

---

## ✅ Step 3: Input Validation (15 minutes)

### 3.1 Create Validation Rules

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/domain/validation/ValidationRules.kt`

Update existing file with enhanced rules:

```kotlin
package com.fivucsas.shared.domain.validation

/**
 * Validation rules with detailed error messages
 */
object ValidationRules {
    
    // Email validation
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Email is required")
            !email.contains("@") -> ValidationResult.Invalid("Invalid email format")
            !email.contains(".") -> ValidationResult.Invalid("Invalid email format")
            email.length < 5 -> ValidationResult.Invalid("Email is too short")
            email.length > 254 -> ValidationResult.Invalid("Email is too long")
            else -> ValidationResult.Valid
        }
    }
    
    // Name validation
    fun validateName(name: String, fieldName: String = "Name"): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("$fieldName is required")
            name.length < 2 -> ValidationResult.Invalid("$fieldName must be at least 2 characters")
            name.length > 50 -> ValidationResult.Invalid("$fieldName is too long (max 50 characters)")
            !name.all { it.isLetter() || it.isWhitespace() } -> 
                ValidationResult.Invalid("$fieldName can only contain letters")
            else -> ValidationResult.Valid
        }
    }
    
    // Phone number validation
    fun validatePhoneNumber(phone: String): ValidationResult {
        val digitsOnly = phone.filter { it.isDigit() }
        return when {
            phone.isBlank() -> ValidationResult.Invalid("Phone number is required")
            digitsOnly.length < 10 -> ValidationResult.Invalid("Phone number must be at least 10 digits")
            digitsOnly.length > 15 -> ValidationResult.Invalid("Phone number is too long")
            else -> ValidationResult.Valid
        }
    }
    
    // ID number validation
    fun validateIdNumber(idNumber: String): ValidationResult {
        return when {
            idNumber.isBlank() -> ValidationResult.Invalid("ID number is required")
            idNumber.length < 5 -> ValidationResult.Invalid("ID number must be at least 5 characters")
            idNumber.length > 20 -> ValidationResult.Invalid("ID number is too long")
            else -> ValidationResult.Valid
        }
    }
    
    // Password validation
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Password is required")
            password.length < 8 -> ValidationResult.Invalid("Password must be at least 8 characters")
            !password.any { it.isUpperCase() } -> 
                ValidationResult.Invalid("Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } -> 
                ValidationResult.Invalid("Password must contain at least one lowercase letter")
            !password.any { it.isDigit() } -> 
                ValidationResult.Invalid("Password must contain at least one number")
            else -> ValidationResult.Valid
        }
    }
    
    // Generic required field
    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Invalid("$fieldName is required")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Validation result with helpful messages
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

val ValidationResult.isValid: Boolean
    get() = this is ValidationResult.Valid

val ValidationResult.errorMessage: String?
    get() = (this as? ValidationResult.Invalid)?.message
```

---

## 🎨 Step 4: UX Enhancements (15 minutes)

### 4.1 Success Messages

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/domain/model/SuccessMessage.kt`

```kotlin
package com.fivucsas.shared.domain.model

/**
 * Success messages for user feedback
 */
sealed class SuccessMessage(val message: String) {
    data object UserEnrolled : SuccessMessage("User enrolled successfully! ✓")
    data object UserVerified : SuccessMessage("Verification successful! ✓")
    data object UserUpdated : SuccessMessage("User updated successfully")
    data object UserDeleted : SuccessMessage("User deleted successfully")
    data object LivenessCheckPassed : SuccessMessage("Liveness check passed ✓")
    
    data class Custom(val customMessage: String) : SuccessMessage(customMessage)
}
```

### 4.2 Confirmation Dialogs

**File:** `shared/src/commonMain/kotlin/com/fivucsas/shared/presentation/state/DialogState.kt`

```kotlin
package com.fivucsas.shared.presentation.state

/**
 * Dialog state for confirmations and alerts
 */
sealed class DialogState {
    data object None : DialogState()
    
    data class Confirmation(
        val title: String,
        val message: String,
        val confirmText: String = "Confirm",
        val cancelText: String = "Cancel",
        val onConfirm: () -> Unit,
        val onCancel: () -> Unit = {}
    ) : DialogState()
    
    data class Alert(
        val title: String,
        val message: String,
        val buttonText: String = "OK",
        val onDismiss: () -> Unit = {}
    ) : DialogState()
    
    data class Error(
        val title: String = "Error",
        val message: String,
        val canRetry: Boolean = false,
        val onRetry: (() -> Unit)? = null,
        val onDismiss: () -> Unit = {}
    ) : DialogState()
}
```

---

## 🎯 Success Criteria

- [ ] Enhanced error types created
- [ ] Loading state management implemented
- [ ] Input validation enhanced
- [ ] Success messages added
- [ ] Dialog state management created
- [ ] All files compile
- [ ] Better UX implemented

---

## 📊 Progress Tracking

```
Day 1: Shared Module Structure    ✅ (10%)
Day 2: Data Layer                  ✅ (20%)
Day 3: Use Cases & Validation      ✅ (30%)
Day 4: ViewModels to Shared        ✅ (50%)
Day 5: Dependency Injection        ✅ (60%)
Day 6: API Integration             ✅ (70%)
Day 7: Testing Infrastructure      ✅ (80%)
Day 8: Error Handling & Polish     ⏳ (90%) ⭐ IN PROGRESS!
----------------------------------------------
Day 9: Performance Optimization    ⬜ (95%)
Day 10: Final Integration          ⬜ (100%)
```

---

**Ready to implement Day 8? Let's create professional error handling!** 🚀
