package com.fivucsas.shared.domain.validation

/**
 * Centralized validation rules
 * 
 * Contains all validation logic for the application.
 * Single Responsibility: Each function validates one thing.
 * 
 * Benefits:
 * - Reusable across all platforms
 * - Easy to test
 * - Consistent validation everywhere
 * - Easy to update rules
 */
object ValidationRules {
    
    // Validation constants
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 100
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    private const val NATIONAL_ID_LENGTH = 11
    private const val MIN_PHONE_LENGTH = 10
    private const val MAX_PHONE_LENGTH = 15
    
    // Regex patterns
    private const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    private const val TURKISH_NAME_REGEX = "^[a-zA-ZğüşöçİĞÜŞÖÇ\\s]+$"
    private const val PHONE_REGEX = "^\\+?[0-9]{10,15}$"
    
    /**
     * Validate full name
     * 
     * Rules:
     * - Required (not blank)
     * - 2-100 characters
     * - Only letters and spaces
     * - Supports Turkish characters (ğ, ü, ş, ö, ç, İ)
     */
    fun validateFullName(name: String): ValidationResult {
        return when {
            name.isBlank() -> 
                ValidationResult.Error("Name is required")
            
            name.length < MIN_NAME_LENGTH -> 
                ValidationResult.Error("Name must be at least $MIN_NAME_LENGTH characters")
            
            name.length > MAX_NAME_LENGTH -> 
                ValidationResult.Error("Name must not exceed $MAX_NAME_LENGTH characters")
            
            !name.matches(Regex(TURKISH_NAME_REGEX)) -> 
                ValidationResult.Error("Name must contain only letters and spaces")
            
            name.trim().split("\\s+".toRegex()).size < 2 ->
                ValidationResult.Error("Please enter both first and last name")
            
            else -> 
                ValidationResult.Success
        }
    }
    
    /**
     * Validate email address
     * 
     * Rules:
     * - Required (not blank)
     * - Valid email format (RFC 5322 simplified)
     * - Contains @ and domain
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> 
                ValidationResult.Error("Email is required")
            
            !email.matches(Regex(EMAIL_REGEX)) -> 
                ValidationResult.Error("Invalid email format (example: user@domain.com)")
            
            email.length > 254 -> // RFC 5321
                ValidationResult.Error("Email is too long")
            
            else -> 
                ValidationResult.Success
        }
    }
    
    /**
     * Validate Turkish National ID (TC Kimlik No)
     * 
     * Rules:
     * - Must be exactly 11 digits
     * - First digit cannot be 0
     * - Must pass Turkish ID algorithm:
     *   - 10th digit = ((sum of odd positions * 7) - (sum of even positions)) % 10
     *   - 11th digit = (sum of first 10 digits) % 10
     * 
     * Reference: https://en.wikipedia.org/wiki/Turkish_Identification_Number
     */
    fun validateNationalId(id: String): ValidationResult {
        return when {
            id.isBlank() -> 
                ValidationResult.Error("National ID is required")
            
            id.length != NATIONAL_ID_LENGTH -> 
                ValidationResult.Error("National ID must be exactly $NATIONAL_ID_LENGTH digits")
            
            !id.all { it.isDigit() } -> 
                ValidationResult.Error("National ID must contain only digits")
            
            id[0] == '0' ->
                ValidationResult.Error("National ID cannot start with 0")
            
            !isValidTurkishId(id) -> 
                ValidationResult.Error("Invalid Turkish National ID number")
            
            else -> 
                ValidationResult.Success
        }
    }
    
    /**
     * Turkish National ID validation algorithm
     * 
     * Algorithm:
     * 1. First digit cannot be 0
     * 2. 10th digit = ((d1+d3+d5+d7+d9) * 7 - (d2+d4+d6+d8)) mod 10
     * 3. 11th digit = (d1+d2+d3+d4+d5+d6+d7+d8+d9+d10) mod 10
     */
    private fun isValidTurkishId(id: String): Boolean {
        if (id.length != 11 || id[0] == '0') return false
        
        val digits = id.map { it.toString().toInt() }
        
        // Validate 10th digit
        val oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8]
        val evenSum = digits[1] + digits[3] + digits[5] + digits[7]
        val tenthDigit = ((oddSum * 7) - evenSum) % 10
        
        if (tenthDigit < 0) {
            // Handle negative modulo
            val correctedTenthDigit = (tenthDigit + 10) % 10
            if (correctedTenthDigit != digits[9]) return false
        } else {
            if (tenthDigit != digits[9]) return false
        }
        
        // Validate 11th digit
        val sumFirst10 = digits.take(10).sum()
        val eleventhDigit = sumFirst10 % 10
        
        if (eleventhDigit != digits[10]) return false
        
        return true
    }
    
    /**
     * Validate phone number
     * 
     * Rules:
     * - Optional (can be blank)
     * - If provided: 10-15 digits
     * - Can start with + (international)
     * - Supports formats: +90 5XX XXX XXXX, 05XXXXXXXXX, etc.
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        // Phone is optional
        if (phone.isBlank()) {
            return ValidationResult.Success
        }
        
        return when {
            phone.length < MIN_PHONE_LENGTH -> 
                ValidationResult.Error("Phone number must be at least $MIN_PHONE_LENGTH digits")
            
            phone.length > MAX_PHONE_LENGTH -> 
                ValidationResult.Error("Phone number must not exceed $MAX_PHONE_LENGTH digits")
            
            !phone.matches(Regex(PHONE_REGEX)) -> 
                ValidationResult.Error("Invalid phone number format (only digits and optional +)")
            
            else -> 
                ValidationResult.Success
        }
    }
    
    /**
     * Validate password
     * 
     * Rules:
     * - Required (not blank)
     * - 8-128 characters
     * - Must contain at least one uppercase letter
     * - Must contain at least one lowercase letter
     * - Must contain at least one digit
     * - Must contain at least one special character
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> 
                ValidationResult.Error("Password is required")
            
            password.length < MIN_PASSWORD_LENGTH -> 
                ValidationResult.Error("Password must be at least $MIN_PASSWORD_LENGTH characters")
            
            password.length > MAX_PASSWORD_LENGTH -> 
                ValidationResult.Error("Password must not exceed $MAX_PASSWORD_LENGTH characters")
            
            !password.any { it.isUpperCase() } ->
                ValidationResult.Error("Password must contain at least one uppercase letter")
            
            !password.any { it.isLowerCase() } ->
                ValidationResult.Error("Password must contain at least one lowercase letter")
            
            !password.any { it.isDigit() } ->
                ValidationResult.Error("Password must contain at least one digit")
            
            !password.any { !it.isLetterOrDigit() } ->
                ValidationResult.Error("Password must contain at least one special character")
            
            else -> 
                ValidationResult.Success
        }
    }
    
    /**
     * Validate address
     * 
     * Rules:
     * - Optional (can be blank)
     * - If provided: 10-500 characters
     */
    fun validateAddress(address: String): ValidationResult {
        // Address is optional
        if (address.isBlank()) {
            return ValidationResult.Success
        }
        
        return when {
            address.length < 10 -> 
                ValidationResult.Error("Address must be at least 10 characters")
            
            address.length > 500 -> 
                ValidationResult.Error("Address must not exceed 500 characters")
            
            else -> 
                ValidationResult.Success
        }
    }
    
    /**
     * Validate multiple fields at once
     * 
     * Returns the first error found, or Success if all valid.
     * 
     * Example:
     * ```
     * val result = validateAll(
     *     validateFullName(name),
     *     validateEmail(email),
     *     validateNationalId(id)
     * )
     * ```
     */
    fun validateAll(vararg validations: ValidationResult): ValidationResult {
        validations.forEach { validation ->
            if (validation is ValidationResult.Error) {
                return validation
            }
        }
        return ValidationResult.Success
    }
}
