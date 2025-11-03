package com.fivucsas.shared.domain.validation

/**
 * Validation result sealed class
 * 
 * Represents the outcome of a validation check.
 * Use pattern matching to handle success/error cases.
 */
sealed class ValidationResult {
    /**
     * Validation passed
     */
    object Success : ValidationResult()
    
    /**
     * Validation failed with error message
     */
    data class Error(val message: String) : ValidationResult()
    
    /**
     * Check if validation is valid
     */
    val isValid: Boolean get() = this is Success
    
    /**
     * Get error message if validation failed
     */
    val errorMessage: String? get() = (this as? Error)?.message
}
